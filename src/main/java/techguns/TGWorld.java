package techguns;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
import techguns.world.*;

/** World blocks are independent of generation switches, so existing saves retain their blocks. */
public final class TGWorld {
    public static final Map<OreKind,RegistryObject<Block>> ORES=new EnumMap<>(OreKind.class);
    public static final Map<ClusterKind,RegistryObject<Block>> CLUSTERS=new EnumMap<>(ClusterKind.class);
    public static final Map<MilitaryCrateKind,RegistryObject<Block>> CRATES=new EnumMap<>(MilitaryCrateKind.class);
    public static final Map<String,RegistryObject<Item>> ITEMS=new LinkedHashMap<>();
    public static final DeferredRegister<Feature<?>> FEATURES=DeferredRegister.create(Registries.FEATURE,Techguns.MODID);
    public static final RegistryObject<Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration>> ORE_FEATURE=FEATURES.register("legacy_ores",LegacyOreFeature::new);
    public static final RegistryObject<Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration>> NETHER_STRUCTURES=FEATURES.register("nether_structures",NetherStructuresFeature::new);
    public static final RegistryObject<Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration>> SURFACE_STRUCTURES=FEATURES.register("surface_structures",SurfaceStructuresFeature::new);
    public static final DeferredRegister<net.minecraft.world.level.levelgen.structure.StructureType<?>> STRUCTURE_TYPES=DeferredRegister.create(Registries.STRUCTURE_TYPE,Techguns.MODID);
    public static final RegistryObject<net.minecraft.world.level.levelgen.structure.StructureType<techguns.world.structures.LegacySurfaceStructure>> SURFACE_STRUCTURE_TYPE=STRUCTURE_TYPES.register("legacy_surface",()->()->techguns.world.structures.LegacySurfaceStructure.CODEC);
    public static final DeferredRegister<net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType<?>> STRUCTURE_PLACEMENTS=DeferredRegister.create(Registries.STRUCTURE_PLACEMENT,Techguns.MODID);
    public static final RegistryObject<net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType<techguns.world.structures.LegacyGridPlacement>> GRID_PLACEMENT=STRUCTURE_PLACEMENTS.register("legacy_grid",()->()->techguns.world.structures.LegacyGridPlacement.CODEC);
    public static final DeferredRegister<net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType> STRUCTURE_PIECES=DeferredRegister.create(Registries.STRUCTURE_PIECE,Techguns.MODID);
    public static final RegistryObject<net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType> PLANNED_PIECE=STRUCTURE_PIECES.register("source_plan",()->techguns.world.structures.PlannedStructurePiece::new);
    public static final DeferredRegister<com.mojang.serialization.Codec<? extends net.minecraftforge.common.world.BiomeModifier>> BIOME_MODIFIERS=DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS,Techguns.MODID);
    public static final DeferredRegister<com.mojang.serialization.Codec<? extends net.minecraftforge.common.world.StructureModifier>> STRUCTURE_MODIFIERS=DeferredRegister.create(ForgeRegistries.Keys.STRUCTURE_MODIFIER_SERIALIZERS,Techguns.MODID);
    public static final RegistryObject<com.mojang.serialization.Codec<techguns.world.structures.ConfigStructureModifier>> CONFIG_STRUCTURE_MODIFIER=STRUCTURE_MODIFIERS.register("config",()->techguns.world.structures.ConfigStructureModifier.CODEC);
    public static final RegistryObject<com.mojang.serialization.Codec<NpcSpawnBiomeModifier>> NPC_SPAWN_MODIFIER=BIOME_MODIFIERS.register("npc_spawns",()->NpcSpawnBiomeModifier.CODEC);
    public static final RegistryObject<net.minecraft.world.entity.EntityType<techguns.entities.spawn.TGDummySpawn>> DUMMY=TGEntities.ENTITIES.register("tgdummyspawn",()->net.minecraft.world.entity.EntityType.Builder.<techguns.entities.spawn.TGDummySpawn>of(techguns.entities.spawn.TGDummySpawn::new,net.minecraft.world.entity.MobCategory.MONSTER).sized(.6f,1.8f).clientTrackingRange(5).updateInterval(3).build("techguns:tgdummyspawn"));
    public static final RegistryObject<Block> SPAWNER_HOLE=TGWeaponBlocks.BLOCKS.register("tg_spawner_hole",()->new techguns.blocks.NpcSpawnerBlock(false));
    public static final RegistryObject<Block> SPAWNER_SOLDIER=TGWeaponBlocks.BLOCKS.register("tg_spawner_soldier",()->new techguns.blocks.NpcSpawnerBlock(true));
    public static final RegistryObject<Block> HARD_SAND=TGWeaponBlocks.BLOCKS.register("sand_hard",()->new Block(BlockBehaviour.Properties.copy(Blocks.SAND).strength(3,3)));
    public static final RegistryObject<Block> SLIMY_EGGS=TGWeaponBlocks.BLOCKS.register("slimy",()->new Block(BlockBehaviour.Properties.copy(Blocks.DIRT).strength(4,4).sound(SoundType.SLIME_BLOCK).mapColor(net.minecraft.world.level.material.MapColor.COLOR_GREEN)));
    public static final RegistryObject<Block> SLIMY_TRAIL=TGWeaponBlocks.BLOCKS.register("slimyladder",techguns.blocks.SlimyTrailBlock::new);
    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<techguns.tileentities.NpcSpawnerEntity>> NPC_SPAWNER_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("tg_spawner",()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(techguns.tileentities.NpcSpawnerEntity::new,SPAWNER_HOLE.get(),SPAWNER_SOLDIER.get()).build(null));
    static {
        for(var kind:OreKind.values()){
            var block=TGWeaponBlocks.BLOCKS.register(kind.id(),()->new Block(BlockBehaviour.Properties.copy(Blocks.IRON_ORE).strength(kind.hardness,2).lightLevel(s->kind.light)));
            ORES.put(kind,block);
            ITEMS.put(kind.id(),TGItems.ITEMS.register(kind.id(),()->new BlockItem(block.get(),new Item.Properties())));
        }
        for(var kind:ClusterKind.values()){
            RegistryObject<Block> block=TGWeaponBlocks.BLOCKS.register(kind.id(),()->new techguns.blocks.OreClusterBlock(kind));
            CLUSTERS.put(kind,block);ITEMS.put(kind.id(),TGItems.ITEMS.register(kind.id(),()->new BlockItem(block.get(),new Item.Properties())));
        }
        for(var kind:MilitaryCrateKind.values()){
            RegistryObject<Block> block=TGWeaponBlocks.BLOCKS.register(kind.id(),()->new techguns.blocks.MilitaryCrateBlock(kind));
            CRATES.put(kind,block);ITEMS.put(kind.id(),TGItems.ITEMS.register(kind.id(),()->new BlockItem(block.get(),new Item.Properties())));
        }
        ITEMS.put("tg_spawner_hole",TGItems.ITEMS.register("tg_spawner_hole",()->new BlockItem(SPAWNER_HOLE.get(),new Item.Properties())));
        ITEMS.put("tg_spawner_soldier",TGItems.ITEMS.register("tg_spawner_soldier",()->new BlockItem(SPAWNER_SOLDIER.get(),new Item.Properties())));
        for(var block:List.of(HARD_SAND,SLIMY_EGGS,SLIMY_TRAIL))ITEMS.put(block.getId().getPath(),TGItems.ITEMS.register(block.getId().getPath(),()->new BlockItem(block.get(),new Item.Properties())));
    }
    public static void init(IEventBus bus){FEATURES.register(bus);STRUCTURE_TYPES.register(bus);STRUCTURE_PLACEMENTS.register(bus);STRUCTURE_PIECES.register(bus);BIOME_MODIFIERS.register(bus);STRUCTURE_MODIFIERS.register(bus);bus.addListener(TGWorld::attributes);bus.addListener(TGWorld::spawnPlacements);}
    private static void attributes(net.minecraftforge.event.entity.EntityAttributeCreationEvent event){event.put(DUMMY.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().build());}
    private static void spawnPlacements(net.minecraftforge.event.entity.SpawnPlacementRegisterEvent event){
        event.register(DUMMY.get(),net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND,net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,level,reason,pos,random)->{
            if(level.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL)return false;
            if(level.getBrightness(net.minecraft.world.level.LightLayer.SKY,pos)>random.nextInt(32))return false;
            int light=level.getMaxLocalRawBrightness(pos,level.getLevel().isThundering()?10:level.getSkyDarken());
            return light<=random.nextInt(8)&&net.minecraft.world.entity.Mob.checkMobSpawnRules(type,level,reason,pos,random);
        },net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
    private TGWorld(){}
}
