package techguns.world;
import java.util.*;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import techguns.tileentities.NpcSpawnerEntity;

/** Original scanned block coordinates and weighted palettes, adapted to flattened block states. */
public final class ScannedStructure {
    private static final Map<String,ScannedStructure> CACHE=new HashMap<>();
    public final int sizeX,sizeY,sizeZ,heightOffset,foundation,cleanup;
    private final boolean netherFoundation;
    private int indexCount;
    public final String id;
    private final JsonArray palette,blocks,indexWeights,entities,entityPalette;
    private ScannedStructure(String id,JsonObject data){this.id=id;var size=data.getAsJsonArray("size");sizeX=size.get(0).getAsInt();sizeY=size.get(1).getAsInt();sizeZ=size.get(2).getAsInt();heightOffset=data.get("heightOffset").getAsInt();foundation=data.get("foundation").getAsInt();cleanup=data.has("cleanup")?data.get("cleanup").getAsInt():0;netherFoundation=!data.has("foundationMode")||data.get("foundationMode").getAsString().equals("nether");palette=data.getAsJsonArray("palette");blocks=data.getAsJsonArray("blocks");indexWeights=data.getAsJsonArray("indexWeights");indexCount=data.has("indexCount")?data.get("indexCount").getAsInt():0;entities=data.getAsJsonArray("entities");entityPalette=data.getAsJsonArray("entityPalette");}
    public static synchronized ScannedStructure load(String id){return CACHE.computeIfAbsent(id,key->{try(var input=ScannedStructure.class.getResourceAsStream("/data/techguns/legacy_structures/"+key+".json");var reader=new java.io.InputStreamReader(Objects.requireNonNull(input,"Missing original scan "+key),java.nio.charset.StandardCharsets.UTF_8)){return new ScannedStructure(key,JsonParser.parseReader(reader).getAsJsonObject());}catch(java.io.IOException error){throw new IllegalStateException("Cannot load original scan "+key,error);}});}
    public int blockCount(){return blocks.size();}
    public static BlockPos rotate(BlockPos point,int centerX,int centerZ,int turns){int x=point.getX()-centerX,z=point.getZ()-centerZ;for(int i=0;i<turns;i++){int old=x;x=z;z=-old;}return new BlockPos(x+centerX,point.getY(),z+centerZ);}
    private static Rotation rotation(int turns){return switch(Math.floorMod(turns,4)){case 1->Rotation.COUNTERCLOCKWISE_90;case 2->Rotation.CLOCKWISE_180;case 3->Rotation.CLOCKWISE_90;default->Rotation.NONE;};}
    private static <T extends Comparable<T>> BlockState property(BlockState state,Property<T> property,String value){return state.setValue(property,property.getValue(value).orElseThrow(()->new IllegalStateException("Invalid structure property "+property.getName()+"="+value)));}
    private static BlockState state(JsonObject data,int turns){
        String id=data.get("name").getAsString();var location=ResourceLocation.parse(id);if(!ForgeRegistries.BLOCKS.containsKey(location))throw new IllegalStateException("Unknown original structure block "+id);
        var state=ForgeRegistries.BLOCKS.getValue(location).defaultBlockState();
        if(data.has("properties"))for(var entry:data.getAsJsonObject("properties").entrySet()){
            var property=state.getBlock().getStateDefinition().getProperty(entry.getKey());if(property==null)throw new IllegalStateException("Missing structure property "+id+"/"+entry.getKey());state=property(state,property,entry.getValue().getAsString());
        }
        // Original BlockRotator rotates skull FACING but leaves a standing skull's tile rotation alone.
        // Community edition skulls turn their tile rotation with the structure (rotation + turns*4).
        if(state.getBlock() instanceof SkullBlock&&data.has("turnRotation")&&state.hasProperty(SkullBlock.ROTATION))return state.setValue(SkullBlock.ROTATION,Math.floorMod(state.getValue(SkullBlock.ROTATION)+4*turns,16));
        return state.getBlock() instanceof SkullBlock?state:state.rotate(rotation(turns));
    }
    public static BlockState sourceState(JsonObject data){return state(data,0);}
    private JsonObject choose(JsonObject entry,RandomSource random,RandomSource structureRandom,int biome,int index){
        if(entry.has("externalOil")){String mode=entry.get("externalOil").getAsString();var fluid=techguns.TGOreClusters.worldOil();String block;
            if(fluid==null)block="minecraft:"+switch(mode){case "liquid"->"lava";case "sand"->"magma_block";default->"sandstone";};
            else block=mode.equals("sand")&&random.nextInt(3)>1?"minecraft:sand":Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(fluid.defaultFluidState().createLegacyBlock().getBlock())).toString();
            var state=new JsonObject();state.addProperty("name",block);var result=new JsonObject();result.add("state",state);return result;
        }
        if(entry.has("biomes")){var options=entry.getAsJsonArray("biomes");return choose(options.get(biome<options.size()?biome:0).getAsJsonObject(),random,structureRandom,biome,index);}
        if(entry.has("indexed")){
            if(entry.has("stoneChance")&&structureRandom.nextFloat()<entry.get("stoneChance").getAsFloat())return choose(entry.getAsJsonObject("stone"),random,structureRandom,biome,index);
            if(entry.has("ores")&&entry.get("oreChance").getAsFloat()>0&&structureRandom.nextFloat()<entry.get("oreChance").getAsFloat())return choose(entry.getAsJsonArray("ores").get(index).getAsJsonObject(),random,structureRandom,biome,index);
            return choose(entry.getAsJsonArray("indexed").get(index).getAsJsonObject(),random,structureRandom,biome,index);
        }
        if(!entry.has("choices"))return entry;var choices=entry.getAsJsonArray("choices");int total=0;for(var item:choices)total+=item.getAsJsonObject().get("weight").getAsInt();int roll=random.nextInt(total+1),sum=0;
        // Original MultiMBlock uses inclusive endpoints and totalWeight+1.
        for(var item:choices){sum+=item.getAsJsonObject().get("weight").getAsInt();if(roll<=sum)return choose(item.getAsJsonObject(),random,structureRandom,biome,index);}throw new IllegalStateException("Invalid original palette weights");
    }
    /** Where a scan is written: a live generation region, or a plan that is later split into saved chunk pieces. */
    private interface Target {
        boolean outside(BlockPos pos);
        BlockState get(BlockPos pos);
        void set(BlockPos pos,BlockState state);
        /** The live block entity, or a detached one that is stored back into the plan by saveBlockEntity. */
        BlockEntity blockEntity(BlockPos pos);
        void saveBlockEntity(BlockPos pos,BlockEntity entity);
        void neighbourShapes(List<BlockPos> placed);
        /** Spawns an entity saved as NBT (with id and Pos), or keeps it in the plan for the chunk that holds it. */
        void entity(net.minecraft.nbt.CompoundTag tag);
    }
    private static Target worldTarget(WorldGenLevel world){return new Target(){
        @Override public boolean outside(BlockPos pos){return world.isOutsideBuildHeight(pos);}
        @Override public BlockState get(BlockPos pos){return world.getBlockState(pos);}
        @Override public void set(BlockPos pos,BlockState state){world.setBlock(pos,state,2);}
        @Override public BlockEntity blockEntity(BlockPos pos){return world.getBlockEntity(pos);}
        @Override public void saveBlockEntity(BlockPos pos,BlockEntity entity){}
        // Old fences/panes derived connections at render time; modern states store them explicitly.
        @Override public void neighbourShapes(List<BlockPos> placed){for(var pos:placed){var state=world.getBlockState(pos);var updated=Block.updateFromNeighbourShapes(state,world,pos);if(updated!=state)world.setBlock(pos,updated,2);}}
        @Override public void entity(net.minecraft.nbt.CompoundTag tag){spawnEntity(world,tag);}
    };}
    private static Target planTarget(techguns.world.structures.LegacyStructurePlan plan,net.minecraft.world.level.LevelHeightAccessor height){return new Target(){
        @Override public boolean outside(BlockPos pos){return height.isOutsideBuildHeight(pos);}
        @Override public BlockState get(BlockPos pos){return plan.getBlockState(pos);}
        @Override public void set(BlockPos pos,BlockState state){plan.setBlockState(pos,state,2);}
        @Override public BlockEntity blockEntity(BlockPos pos){var state=plan.getBlockState(pos);return state.getBlock() instanceof EntityBlock block?block.newBlockEntity(pos,state):null;}
        @Override public void saveBlockEntity(BlockPos pos,BlockEntity entity){plan.setBlockData(pos,entity.saveWithoutMetadata());}
        // Connections are resolved when each chunk is finished (PlannedStructurePiece marks every block for post-processing).
        @Override public void neighbourShapes(List<BlockPos> placed){}
        @Override public void entity(net.minecraft.nbt.CompoundTag tag){plan.addEntity(tag);}
    };}
    private void place(Target world,BlockPos pos,JsonObject original,int turns,RandomSource random,RandomSource structureRandom,int biome,int index){
        if(pos.getY()<1||world.outside(pos))return;
        var entry=choose(original,random,structureRandom,biome,index);var state=state(entry.getAsJsonObject("state"),turns);world.set(pos,state);
        if(entry.has("itemFrame"))world.entity(itemFrame(entry.getAsJsonObject("itemFrame"),pos,turns));
        if(!entry.has("spawner")&&!entry.has("lootTable")&&!entry.has("vanillaSpawner")&&!entry.has("nbt"))return;
        var be=world.blockEntity(pos);boolean changed=false;
        if(entry.has("nbt")&&be!=null){try{var tag=be.saveWithoutMetadata();tag.merge(net.minecraft.nbt.TagParser.parseTag(entry.get("nbt").getAsString()));be.load(tag);changed=true;}catch(com.mojang.brigadier.exceptions.CommandSyntaxException e){throw new IllegalStateException("Bad block data in "+id,e);}}
        if(entry.has("spawner")&&be instanceof NpcSpawnerEntity spawner){
            var data=entry.getAsJsonObject("spawner");spawner.setParams(data.get("remaining").getAsInt(),data.get("maxActive").getAsInt(),data.get("delay").getAsInt(),data.get("range").getAsInt());
            for(var type:data.getAsJsonArray("mobs")){var mob=type.getAsJsonObject();var key=ResourceLocation.parse(mob.get("entity").getAsString());if(!ForgeRegistries.ENTITY_TYPES.containsKey(key))throw new IllegalStateException("Missing structure guard "+key);spawner.addMobType(ForgeRegistries.ENTITY_TYPES.getValue(key),mob.get("weight").getAsInt());}
            if(data.has("weapon"))spawner.setWeaponOverride(new ItemStack(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(data.get("weapon").getAsString()))));changed=true;
        }
        if(entry.has("lootTable")&&be instanceof RandomizableContainerBlockEntity chest){chest.setLootTable(ResourceLocation.parse(entry.get("lootTable").getAsString()),random.nextLong());changed=true;}
        if(entry.has("vanillaSpawner")&&be instanceof SpawnerBlockEntity spawner){var type=ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(entry.get("vanillaSpawner").getAsString()));spawner.setEntityId(type,random);changed=true;}
        if(changed)world.saveBlockEntity(pos,be);
    }
    public List<BlockPos> place(WorldGenLevel world,BlockPos origin,int turns,RandomSource random){
        return place(world,origin,turns,random,random,0);
    }
    private static int placementPass(JsonObject entry){if(entry.has("externalOil")){String mode=entry.get("externalOil").getAsString();return mode.equals("liquid")||mode.equals("sand")&&techguns.TGOreClusters.worldOil()!=null?1:0;}return entry.has("pass")?entry.get("pass").getAsInt():0;}
    /** Carrier has no cleanup/foundation. Resolve its original random palette once before native chunk splitting. */
    public void plan(techguns.world.structures.LegacyStructurePlan plan,BlockPos origin,int turns,RandomSource random,RandomSource structureRandom,int biome){
        if(foundation!=0||cleanup!=0||indexWeights!=null||indexCount!=0)throw new IllegalStateException("Source scan requires its dedicated terrain/index planning adapter: "+id);
        int cx=sizeX/2,cz=sizeZ/2;
        for(int pass=0;pass<2;pass++)for(var element:blocks){var block=element.getAsJsonArray();var original=palette.get(block.get(3).getAsInt()).getAsJsonObject();if((original.has("pass")?original.get("pass").getAsInt():0)!=pass)continue;
            var pos=origin.above(heightOffset).offset(rotate(new BlockPos(block.get(0).getAsInt(),block.get(1).getAsInt(),block.get(2).getAsInt()),cx,cz,turns));var entry=choose(original,random,structureRandom,biome,0);plan.setBlockState(pos,state(entry.getAsJsonObject("state"),turns),2);
            if(entry.has("spawner")){var data=entry.getAsJsonObject("spawner");var types=new LinkedHashMap<String,Integer>();for(var raw:data.getAsJsonArray("mobs")){var mob=raw.getAsJsonObject();String name=mob.get("entity").getAsString();if(!ForgeRegistries.ENTITY_TYPES.containsKey(ResourceLocation.parse(name)))throw new IllegalStateException("Unknown source guard "+name);types.put(name,mob.get("weight").getAsInt());}var tag=techguns.world.structures.LegacyStructurePlan.guardData(data.get("remaining").getAsInt(),data.get("maxActive").getAsInt(),data.get("delay").getAsInt(),data.get("range").getAsInt(),types);
                if(data.has("weapon"))tag.put("weapon",new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(data.get("weapon").getAsString())))).save(new net.minecraft.nbt.CompoundTag()));plan.setBlockData(pos,tag);
            }
            if(entry.has("lootTable")){var data=new net.minecraft.nbt.CompoundTag();data.putString("LootTable",entry.get("lootTable").getAsString());data.putLong("LootTableSeed",random.nextLong());plan.setBlockData(pos,data);}
            if(entry.has("vanillaSpawner"))throw new IllegalStateException("Unadapted native vanilla spawner plan: "+id);
        }
        mergeChests(plan);
    }
    /** Old adjacent chests formed double inventories dynamically; modern chest type is stored in block state. */
    private static void mergeChests(techguns.world.structures.LegacyStructurePlan plan){
        for(var pos:new ArrayList<>(plan.blocks().keySet())){var left=plan.getBlockState(pos);if(!(left.getBlock() instanceof ChestBlock)||left.getValue(ChestBlock.TYPE)!=net.minecraft.world.level.block.state.properties.ChestType.SINGLE)continue;var facing=left.getValue(ChestBlock.FACING);var other=pos.relative(facing.getClockWise());var right=plan.getBlockState(other);
            if(right.is(left.getBlock())&&right.getValue(ChestBlock.FACING)==facing&&right.getValue(ChestBlock.TYPE)==net.minecraft.world.level.block.state.properties.ChestType.SINGLE){var firstData=plan.blockData().get(pos);var secondData=plan.blockData().get(other);plan.setBlockState(pos,left.setValue(ChestBlock.TYPE,net.minecraft.world.level.block.state.properties.ChestType.LEFT),2);plan.setBlockState(other,right.setValue(ChestBlock.TYPE,net.minecraft.world.level.block.state.properties.ChestType.RIGHT),2);if(firstData!=null)plan.setBlockData(pos,firstData);if(secondData!=null)plan.setBlockData(other,secondData);}
        }
    }
    public List<BlockPos> place(WorldGenLevel world,BlockPos origin,int turns,RandomSource random,RandomSource structureRandom,int biome){return place(worldTarget(world),origin,turns,random,structureRandom,biome);}
    /** The same source placement evaluated on terrain known before generation, for a native structure start (/locate). */
    public List<BlockPos> place(techguns.world.structures.LegacyStructurePlan plan,net.minecraft.world.level.LevelHeightAccessor height,BlockPos origin,int turns,RandomSource random,RandomSource structureRandom,int biome){plan.resolveAllShapes();var placed=place(planTarget(plan,height),origin,turns,random,structureRandom,biome);mergeChests(plan);return placed;}
    private List<BlockPos> place(Target world,BlockPos origin,int turns,RandomSource random,RandomSource structureRandom,int biome){
        var base=origin.above(heightOffset);int cx=sizeX/2,cz=sizeZ/2;var placed=new ArrayList<BlockPos>();
        int index=indexCount>0?structureRandom.nextInt(indexCount):0;if(indexWeights!=null){int total=0;for(var weight:indexWeights)total+=weight.getAsInt();int roll=structureRandom.nextInt(total+1),sum=0;for(int i=0;i<indexWeights.size();i++){sum+=indexWeights.get(i).getAsInt();if(roll<=sum){index=i;break;}}}
        for(var element:blocks){var block=element.getAsJsonArray();if(block.get(1).getAsInt()!=0)continue;for(int dy=1;dy<=cleanup;dy++){var pos=origin.offset(rotate(new BlockPos(block.get(0).getAsInt(),dy,block.get(2).getAsInt()),cx,cz,turns));if(pos.getY()>=1&&!world.outside(pos))world.set(pos,Blocks.AIR.defaultBlockState());}}
        for(var element:blocks){var block=element.getAsJsonArray();if(block.get(1).getAsInt()!=0)continue;
            var entry=palette.get(block.get(3).getAsInt()).getAsJsonObject();if(placementPass(entry)!=0)continue;int solid=0;
            for(int depth=1;depth<=foundation;depth++){
                var pos=base.offset(rotate(new BlockPos(block.get(0).getAsInt(),-depth,block.get(2).getAsInt()),cx,cz,turns));
                if(world.get(pos).canBeReplaced()){place(world,pos,entry,turns,random,structureRandom,biome,index);solid=0;}else if(netherFoundation&&++solid>=2)break;
            }
        }
        for(int pass=0;pass<2;pass++)for(var element:blocks){var block=element.getAsJsonArray();var entry=palette.get(block.get(3).getAsInt()).getAsJsonObject();if(placementPass(entry)!=pass)continue;var pos=base.offset(rotate(new BlockPos(block.get(0).getAsInt(),block.get(1).getAsInt(),block.get(2).getAsInt()),cx,cz,turns));place(world,pos,entry,turns,random,structureRandom,biome,index);placed.add(pos);}
        world.neighbourShapes(placed);
        if(entities!=null)for(var element:entities){var row=element.getAsJsonArray();var kind=entityPalette.get(row.get(3).getAsInt()).getAsJsonObject();
            var pos=base.offset(rotate(new BlockPos(row.get(0).getAsInt(),row.get(1).getAsInt(),row.get(2).getAsInt()),cx,cz,turns));if(pos.getY()<1||world.outside(pos))continue;
            var offset=kind.getAsJsonArray("offset");var tag=new net.minecraft.nbt.CompoundTag();tag.putString("id",kind.get("type").getAsString());
            tag.put("Pos",doubles(pos.getX()+offset.get(0).getAsDouble(),pos.getY()+offset.get(1).getAsDouble(),pos.getZ()+offset.get(2).getAsDouble()));tag.putBoolean("PersistenceRequired",true);
            if(kind.has("attach")){var face=net.minecraft.core.Direction.byName(kind.get("attach").getAsString());if(face.getAxis().isHorizontal())face=rotation(turns).rotate(face);tag.putByte("AttachFace",(byte)face.get3DDataValue());}
            if(kind.has("showBottom"))tag.putBoolean("ShowBottom",kind.get("showBottom").getAsBoolean());
            world.entity(tag);
        }
        return placed;
    }
    private static net.minecraft.nbt.ListTag doubles(double... values){var list=new net.minecraft.nbt.ListTag();for(double v:values)list.add(net.minecraft.nbt.DoubleTag.valueOf(v));return list;}
    /** Item frame of a community edition scan: facing turns with the structure; the displayed item keeps its data. */
    private net.minecraft.nbt.CompoundTag itemFrame(JsonObject data,BlockPos pos,int turns){
        var facing=net.minecraft.core.Direction.byName(data.get("facing").getAsString());if(facing.getAxis().isHorizontal())facing=rotation(turns).rotate(facing);
        var tag=new net.minecraft.nbt.CompoundTag();tag.putString("id","minecraft:item_frame");tag.put("Pos",doubles(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5));
        tag.putInt("TileX",pos.getX());tag.putInt("TileY",pos.getY());tag.putInt("TileZ",pos.getZ());tag.putByte("Facing",(byte)facing.get3DDataValue());tag.putBoolean("PersistenceRequired",true);
        if(data.has("item")&&!data.get("item").isJsonNull()){var item=new net.minecraft.nbt.CompoundTag();item.putString("id",data.get("item").getAsString());item.putByte("Count",(byte)Math.max(1,data.get("count").getAsInt()));
            if(data.has("nbt")&&!data.get("nbt").isJsonNull())try{item.put("tag",net.minecraft.nbt.TagParser.parseTag(data.get("nbt").getAsString()));}catch(com.mojang.brigadier.exceptions.CommandSyntaxException e){throw new IllegalStateException("Bad item frame data in "+id,e);}
            tag.put("Item",item);tag.putByte("ItemRotation",(byte)data.get("rotation").getAsInt());}
        return tag;
    }
    /** Spawns a saved entity into the generating world (structure pieces and the live feature use the same path). */
    public static void spawnEntity(net.minecraft.world.level.ServerLevelAccessor world,net.minecraft.nbt.CompoundTag tag){
        EntityType.create(tag.copy(),world.getLevel()).ifPresent(entity->{if(entity instanceof net.minecraft.world.entity.Mob mob)mob.setPersistenceRequired();world.addFreshEntityWithPassengers(entity);});
    }
}
