package techguns.world;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import techguns.TGConfig;
import techguns.TGOreClusters;
import techguns.Techguns;

/**
 * Natural world generation check on a real dedicated server (system property techguns.verifyNaturalGeneration).
 * "generate": creates a fresh world region through every chunk status, records every grid decision of the original
 * structure generators, the blocks each structure wrote, ore veins and structure starts, then hashes all Techguns
 * content per chunk and stops the server (saving the world). "reload": reopens the same saved world and checks that
 * nothing generates again and every hash is unchanged after save/load.
 */
@Mod.EventBusSubscriber(modid=Techguns.MODID)
public final class NaturalGenerationVerifier {
    private static final String MODE=System.getProperty("techguns.verifyNaturalGeneration","");
    private static final int OVERWORLD_FROM=-2,OVERWORLD_TO=66,NETHER_FROM=-2,NETHER_TO=34,CHUNKS_PER_TICK=6;
    /** Runtime counters of spawners in always-loaded spawn chunks, not generated content. */
    private static final List<String> VOLATILE_BLOCK_ENTITY_KEYS=List.of("delay","mobsLeft","Delay");
    private record Decision(String dim,int cx,int cz,String id){}
    private static final Queue<Decision> DECISIONS=new ConcurrentLinkedQueue<>(),SKIPS=new ConcurrentLinkedQueue<>();
    private static final List<Map.Entry<ResourceKey<Level>,ChunkPos>> pending=new ArrayList<>();
    private static int cursor=-1,total;
    private static long started;

    public static boolean enabled(){return !MODE.isEmpty();}
    private static boolean gridSite(int cx,int cz){return cx%TGConfig.spawnWeightTGStructureSmall==0&&cz%TGConfig.spawnWeightTGStructureSmall==0;}
    public static void considered(String dim,int cx,int cz,String id){if(enabled()&&gridSite(cx,cz))DECISIONS.add(new Decision(dim,cx,cz,id));}
    public static void skipped(String dim,String id,int cx,int cz){if(enabled())SKIPS.add(new Decision(dim,cx,cz,id));}
    /** Techguns structure starts of a chunk, by structure id. */
    private static Map<String,net.minecraft.world.level.levelgen.structure.StructureStart> starts(MinecraftServer server,LevelChunk chunk){
        var result=new TreeMap<String,net.minecraft.world.level.levelgen.structure.StructureStart>();
        for(var entry:server.registryAccess().registryOrThrow(Registries.STRUCTURE).entrySet()){var key=entry.getKey().location();if(!key.getNamespace().equals(Techguns.MODID))continue;var start=chunk.getStartForStructure(entry.getValue());if(start!=null&&start.isValid())result.put(key.getPath(),start);}
        return result;
    }

    @SubscribeEvent public static void serverStarted(ServerStartedEvent event){
        if(!enabled())return;var server=event.getServer();
        // Nothing is cleared: spawn-area chunks generated before this event belong to the region too, and on reload any record means regeneration.
        pending.clear();
        for(int x=OVERWORLD_FROM;x<=OVERWORLD_TO;x++)for(int z=OVERWORLD_FROM;z<=OVERWORLD_TO;z++)pending.add(Map.entry(Level.OVERWORLD,new ChunkPos(x,z)));
        for(int x=NETHER_FROM;x<=NETHER_TO;x++)for(int z=NETHER_FROM;z<=NETHER_TO;z++)pending.add(Map.entry(Level.NETHER,new ChunkPos(x,z)));
        total=pending.size();cursor=0;started=System.currentTimeMillis();
        Techguns.LOGGER.info("TECHGUNS_NATURAL_GENERATION: mode {} seed {}, {} chunks to bring to FULL status",MODE,server.overworld().getSeed(),total);
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){
        if(cursor<0||event.phase!=TickEvent.Phase.END)return;var server=event.getServer();
        for(int i=0;i<CHUNKS_PER_TICK&&cursor<total;i++,cursor++){var entry=pending.get(cursor);server.getLevel(entry.getKey()).getChunk(entry.getValue().x,entry.getValue().z);}
        if(cursor%600<CHUNKS_PER_TICK)Techguns.LOGGER.info("TECHGUNS_NATURAL_GENERATION: {}/{} chunks ({} s)",cursor,total,(System.currentTimeMillis()-started)/1000);
        if(cursor<total)return;
        cursor=-1;
        try{finish(server);}catch(Exception error){Techguns.LOGGER.error("TECHGUNS_NATURAL_GENERATION_FAILED",error);}
        server.halt(false);
    }

    private static void finish(MinecraftServer server) throws Exception {
        var report=new JsonObject();var errors=new JsonArray();report.addProperty("mode",MODE);report.addProperty("seed",server.overworld().getSeed());
        report.addProperty("generationSeconds",(System.currentTimeMillis()-started)/1000.0);report.addProperty("chunks",total);
        var hashes=new JsonObject();var counts=new TreeMap<String,Integer>();var oreRanges=new TreeMap<String,int[]>();
        // Every block planned by a Techguns structure start in the region (scans include ore cluster blocks).
        var planned=new LinkedHashMap<String,Map<BlockPos,BlockState>>();var placedPositions=new HashSet<BlockPos>();
        for(var entry:pending){var pos=entry.getValue();if(!gridSite(pos.x,pos.z))continue;var level=server.getLevel(entry.getKey());String dim=entry.getKey().location().getPath();
            for(var start:starts(server,level.getChunk(pos.x,pos.z)).entrySet()){var blocks=new LinkedHashMap<BlockPos,BlockState>();
                for(var piece:start.getValue().getPieces())if(piece instanceof techguns.world.structures.PlannedStructurePiece plan)plan.forEachCell(blocks::put);
                planned.put(dim+" "+start.getKey()+" "+pos.x+","+pos.z,blocks);if(dim.equals("overworld"))placedPositions.addAll(blocks.keySet());}}
        var ores=new HashMap<net.minecraft.world.level.block.Block,OreKind>();for(var kind:OreKind.values())ores.put(techguns.TGWorld.ORES.get(kind).get(),kind);
        var oreOutsideRange=new TreeMap<String,Integer>();var dump=new StringBuilder();
        for(var entry:pending){
            var level=server.getLevel(entry.getKey());var pos=entry.getValue();var chunk=level.getChunk(pos.x,pos.z);String dim=entry.getKey().location().getPath();
            var digest=MessageDigest.getInstance("SHA-256");int content=0;
            for(int s=0;s<chunk.getSections().length;s++){
                var section=chunk.getSections()[s];if(section.hasOnlyAir()||!section.maybeHas(st->ForgeRegistries.BLOCKS.getKey(st.getBlock()).getNamespace().equals(Techguns.MODID)))continue;
                int baseY=chunk.getMinBuildHeight()+s*16;
                for(int y=0;y<16;y++)for(int z=0;z<16;z++)for(int x=0;x<16;x++){
                    var state=section.getBlockState(x,y,z);var key=ForgeRegistries.BLOCKS.getKey(state.getBlock());if(!key.getNamespace().equals(Techguns.MODID))continue;
                    int wx=pos.getMinBlockX()+x,wy=baseY+y,wz=pos.getMinBlockZ()+z;content++;
                    digest.update((wx+","+wy+","+wz+"="+state).getBytes(StandardCharsets.UTF_8));dump.append(dim).append(' ').append(wx).append(',').append(wy).append(',').append(wz).append('=').append(state).append('\n');
                    counts.merge(dim+" "+key.getPath(),1,Integer::sum);
                    var kind=ores.get(state.getBlock());
                    if(kind!=null&&dim.equals("overworld")&&!placedPositions.contains(new BlockPos(wx,wy,wz))){
                        var range=oreRanges.computeIfAbsent(kind.id(),k->new int[]{Integer.MAX_VALUE,Integer.MIN_VALUE,0});range[0]=Math.min(range[0],wy);range[1]=Math.max(range[1],wy);range[2]++;
                        if(wy<kind.minY-4||wy>kind.maxY+2)oreOutsideRange.merge(kind.id(),1,Integer::sum);
                    }
                }
            }
            var blockEntities=new TreeMap<String,String>();
            // Pending block entities (stored tags not yet turned into objects) are created first, so both runs see the same set.
            for(var bePos:chunk.getBlockEntitiesPos()){var be=chunk.getBlockEntity(bePos,LevelChunk.EntityCreationType.IMMEDIATE);if(be==null)continue;var key=ForgeRegistries.BLOCKS.getKey(be.getBlockState().getBlock());var type=ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(be.getType());
                if(!key.getNamespace().equals(Techguns.MODID)&&!type.getNamespace().equals(Techguns.MODID)&&!be.saveWithoutMetadata().contains("LootTable"))continue;
                var saved=be.saveWithoutMetadata();for(String field:VOLATILE_BLOCK_ENTITY_KEYS)saved.remove(field);blockEntities.put(be.getBlockPos().toShortString(),saved.toString());}
            for(var be:blockEntities.entrySet()){digest.update((be.getKey()+"#"+be.getValue()).getBytes(StandardCharsets.UTF_8));content++;dump.append(dim).append(' ').append(be.getKey()).append('#').append(be.getValue()).append('\n');}
            // Border chunks can still receive decorations from neighbours outside the region, so only interior chunks are hashed.
            boolean border=entry.getKey()==Level.OVERWORLD?pos.x==OVERWORLD_FROM||pos.x==OVERWORLD_TO||pos.z==OVERWORLD_FROM||pos.z==OVERWORLD_TO:pos.x==NETHER_FROM||pos.x==NETHER_TO||pos.z==NETHER_FROM||pos.z==NETHER_TO;
            if(content>0&&!border)hashes.addProperty(dim+" "+pos.x+","+pos.z,HexFormat.of().formatHex(digest.digest()).substring(0,24));
        }
        // Full per-block listing, so a changed hash can be traced to the exact block or block entity.
        Files.writeString(server.getServerDirectory().toPath().resolve("natural-generation-content-"+MODE+".txt"),dump);
        var countJson=new JsonObject();counts.forEach(countJson::addProperty);report.add("techgunsBlocks",countJson);
        report.addProperty("chunksWithTechgunsContent",hashes.size());
        var reportPath=server.getServerDirectory().toPath().resolve("natural-generation-report.json");
        if(MODE.equals("reload")){
            var saved=JsonParser.parseString(Files.readString(reportPath)).getAsJsonObject();var savedHashes=saved.getAsJsonObject("hashes");int same=0,changed=0;
            for(var key:savedHashes.keySet()){if(savedHashes.get(key).equals(hashes.get(key)))same++;else{changed++;if(changed<=20)errors.add("Content changed after save/load: "+key);}}
            for(var key:hashes.keySet())if(!savedHashes.has(key)){changed++;if(changed<=20)errors.add("New content after save/load: "+key);}
            if(!DECISIONS.isEmpty()||!SKIPS.isEmpty())errors.add("Already generated chunks generated again: "+DECISIONS.size()+" decisions, "+SKIPS.size()+" terrain checks");
            report.addProperty("unchangedChunks",same);report.addProperty("changedChunks",changed);report.add("errors",errors);
            Files.writeString(server.getServerDirectory().toPath().resolve("natural-generation-reload.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
            if(errors.isEmpty())Techguns.LOGGER.info("TECHGUNS_NATURAL_GENERATION_RELOAD_PASSED: {} chunks with Techguns content unchanged after save/load, nothing regenerated",same);
            else Techguns.LOGGER.error("TECHGUNS_NATURAL_GENERATION_FAILED: {}",errors);
            return;
        }
        report.add("hashes",hashes);
        // Every grid site: the original table decides once (the structure of that choice is the only one asked to generate there),
        // and at most that one Techguns structure starts in the chunk.
        var decisions=new HashMap<String,List<Decision>>();for(var d:DECISIONS)decisions.computeIfAbsent(d.dim+" "+d.cx+","+d.cz,k->new ArrayList<>()).add(d);
        var sites=new JsonArray();var outcomes=new TreeMap<String,Integer>();var biomes=new TreeMap<String,Integer>();
        for(var entry:pending){
            var pos=entry.getValue();if(!gridSite(pos.x,pos.z))continue;String dim=entry.getKey().location().getPath();var level=server.getLevel(entry.getKey());
            var random=SurfaceStructuresFeature.selectionRandom(level.getSeed(),pos.x,pos.z);String expected;var site=new JsonObject();site.addProperty("dim",dim);site.addProperty("chunk",pos.x+","+pos.z);
            if(dim.equals("overworld")){
                var source=level.getChunkSource().getGenerator().getBiomeSource();var biome=source.getNoiseBiome(net.minecraft.core.QuartPos.fromBlock(pos.x*16),net.minecraft.core.QuartPos.fromBlock(64),net.minecraft.core.QuartPos.fromBlock(pos.z*16),level.getChunkSource().randomState().sampler());
                String biomeName=biome.unwrapKey().map(k->k.location().getPath()).orElse("?");site.addProperty("biome",biomeName);biomes.merge(biomeName,1,Integer::sum);
                expected=SurfaceStructuresFeature.choose(pos.x,pos.z,biome.is(BiomeTags.IS_OCEAN),SurfaceStructuresFeature.desert(biome),TGOreClusters.worldOil()!=null,random);
            }else expected=NetherStructuresFeature.choose(pos.x,pos.z,random);
            site.addProperty("selected",String.valueOf(expected));
            var list=decisions.getOrDefault(dim+" "+pos.x+","+pos.z,List.of());
            if(list.size()!=(expected==null?0:1)||expected!=null&&!expected.equals(list.get(0).id)){errors.add("Grid site "+dim+" "+pos.x+","+pos.z+" expected "+expected+" decided "+list.stream().map(Decision::id).toList());continue;}
            var found=starts(server,level.getChunk(pos.x,pos.z));String outcome;
            if(found.size()>1||found.size()==1&&!found.containsKey(expected))errors.add("Grid site "+dim+" "+pos.x+","+pos.z+" ("+expected+") has starts "+found.keySet());
            if(expected==null)outcome="none";
            else if(found.containsKey(expected)){
                outcome="placed";var start=found.get(expected);var box=start.getBoundingBox();site.addProperty("pieces",start.getPieces().size());site.addProperty("box",box.minX()+","+box.minY()+","+box.minZ()+" .. "+box.maxX()+","+box.maxY()+","+box.maxZ());
                boolean inside=true;int from=entry.getKey()==Level.OVERWORLD?OVERWORLD_FROM:NETHER_FROM,to=entry.getKey()==Level.OVERWORLD?OVERWORLD_TO:NETHER_TO;
                for(int bx=box.minX()>>4;bx<=box.maxX()>>4;bx++)for(int bz=box.minZ()>>4;bz<=box.maxZ()>>4;bz++)if(bx<from||bx>to||bz<from||bz>to)inside=false;
                site.addProperty("boxInsideGeneratedRegion",inside);
            }else outcome="terrain-rejected";
            site.addProperty("outcome",outcome);outcomes.merge(dim+" "+(expected==null?"-":expected)+" "+outcome,1,Integer::sum);sites.add(site);
        }
        var outcomeJson=new JsonObject();outcomes.forEach(outcomeJson::addProperty);report.add("siteOutcomes",outcomeJson);
        var biomeJson=new JsonObject();biomes.forEach(biomeJson::addProperty);report.add("overworldGridBiomes",biomeJson);report.add("sites",sites);
        // Planned blocks of every start compared with the finished world (neighbour shapes and later decoration may change some).
        var placements=new JsonArray();int written=0,kept=0;
        for(var p:planned.entrySet()){
            String[] key=p.getKey().split(" ");var level=server.getLevel(key[0].equals("overworld")?Level.OVERWORLD:Level.NETHER);int same=0;var replaced=new TreeMap<String,Integer>();
            for(var b:p.getValue().entrySet()){var now=level.getBlockState(b.getKey());if(now.equals(b.getValue()))same++;else replaced.merge(ForgeRegistries.BLOCKS.getKey(b.getValue().getBlock()).getPath()+"->"+ForgeRegistries.BLOCKS.getKey(now.getBlock()).getPath(),1,Integer::sum);}
            var json=new JsonObject();json.addProperty("dim",key[0]);json.addProperty("id",key[1]);json.addProperty("chunk",key[2]);json.addProperty("blocks",p.getValue().size());json.addProperty("unchanged",same);
            var top=new JsonObject();replaced.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).limit(6).forEach(e->top.addProperty(e.getKey(),e.getValue()));json.add("changedBy",top);placements.add(json);
            written+=p.getValue().size();kept+=same;
        }
        report.addProperty("structureBlocksWritten",written);report.addProperty("structureBlocksUnchanged",kept);report.add("placements",placements);
        // Ore veins: absolute original Y ranges and per-chunk yield against the same vein algorithm in solid stone.
        var oreJson=new JsonObject();int overworldChunks=(OVERWORLD_TO-OVERWORLD_FROM+1)*(OVERWORLD_TO-OVERWORLD_FROM+1);
        for(var kind:OreKind.values()){var range=oreRanges.get(kind.id());var json=new JsonObject();double ideal=idealVeinBlocks(kind);
            json.addProperty("originalY",kind.minY+".."+kind.maxY);json.addProperty("blocks",range==null?0:range[2]);if(range!=null)json.addProperty("foundY",range[0]+".."+range[1]);
            json.addProperty("perChunk",range==null?0:range[2]*1.0/overworldChunks);json.addProperty("perChunkInSolidStone",ideal);json.addProperty("ratioToSolidStone",range==null?0:range[2]/(ideal*overworldChunks));
            int bad=oreOutsideRange.getOrDefault(kind.id(),0);json.addProperty("outsideOriginalRange",bad);if(bad>0)errors.add(bad+" "+kind.id()+" blocks outside the original Y range");
            if(kind.enabled()&&(range==null||range[2]==0))errors.add("No naturally generated "+kind.id());oreJson.add(kind.id(),json);}
        report.add("ores",oreJson);report.add("errors",errors);
        Files.writeString(reportPath,new GsonBuilder().setPrettyPrinting().create().toJson(report));
        if(errors.isEmpty())Techguns.LOGGER.info("TECHGUNS_NATURAL_GENERATION_PASSED: {} chunks generated in {} s; {} grid sites decided once with original choices; {} structure blocks written, {} unchanged; {} chunks with Techguns content hashed",total,(System.currentTimeMillis()-started)/1000,sites.size(),written,kept,hashes.size());
        else Techguns.LOGGER.error("TECHGUNS_NATURAL_GENERATION_FAILED: {}",errors);
    }
    /** Mean ore blocks per chunk the original vein routine produces when every target block is stone. */
    private static double idealVeinBlocks(OreKind kind){
        var random=net.minecraft.util.RandomSource.create(42);long sum=0;int runs=4000;
        for(int r=0;r<runs;r++){int size=kind.veinBase+random.nextInt(kind.veinRandom);for(int i=0;i<kind.attempts;i++)sum+=LegacyOreFeature.veinShape(random,size).size();}
        return sum*1.0/runs;
    }
}
