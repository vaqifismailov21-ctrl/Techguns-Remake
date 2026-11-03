package techguns;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.*;
import net.minecraftforge.registries.*;
/** Fluids used in recipes; no generation, deposits or NPC systems. */
public final class TGFluids {
    public static final DeferredRegister<FluidType> TYPES=DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES,Techguns.MODID);
    public static final DeferredRegister<Fluid> FLUIDS=DeferredRegister.create(ForgeRegistries.FLUIDS,Techguns.MODID);
    public static final RegistryObject<FluidType> ACID_TYPE=TYPES.register("creeper_acid",()->new FluidType(FluidType.Properties.create().density(100).viscosity(1000).descriptionId("techguns.fluid.creeperacid")){
        @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer){consumer.accept(new net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions(){public ResourceLocation getStillTexture(){return new ResourceLocation("techguns","blocks/acid_still");}public ResourceLocation getFlowingTexture(){return new ResourceLocation("techguns","blocks/acid_flow");}});}
    });
    public static final RegistryObject<FlowingFluid> ACID=FLUIDS.register("creeper_acid",()->new ForgeFlowingFluid.Source(properties()));
    public static final RegistryObject<FlowingFluid> ACID_FLOWING=FLUIDS.register("flowing_creeper_acid",()->new ForgeFlowingFluid.Flowing(properties()));
    public static final RegistryObject<LiquidBlock> ACID_BLOCK=TGWeaponBlocks.BLOCKS.register("block_creeper_acid",()->new LiquidBlock(ACID,BlockBehaviour.Properties.copy(Blocks.WATER)){
        @Override public void entityInside(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,net.minecraft.world.entity.Entity entity){super.entityInside(state,level,pos,entity);if(!level.isClientSide&&entity instanceof net.minecraft.world.entity.LivingEntity living){var source=new techguns.damagesystem.TGDamageSource(level,"tg_poison",null,null,techguns.api.damagesystem.DamageType.POISON,techguns.deatheffects.EntityDeathUtils.DeathType.BIO);source.goreChance=1;source.knockbackMultiplier=0;living.hurt(source,2);}}
    });
    public static final RegistryObject<Item> ACID_BUCKET=TGItems.ITEMS.register("creeper_acid_bucket",()->new BucketItem(ACID,new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    /**
     * Crude oil (added at the user's request; the original only used oil fluids of other mods). Its source is named "oil",
     * so the default FluidListOilWorldspawn ("oil", "crude_oil") finds it: desert oil clusters spawn with oil pools and
     * ore drills pump it from oil clusters. Uses the water textures, tinted black.
     */
    public static final RegistryObject<FluidType> OIL_TYPE=TYPES.register("oil",()->new FluidType(FluidType.Properties.create().density(900).viscosity(6000).motionScale(.007).canExtinguish(false).supportsBoating(true).fallDistanceModifier(0f).descriptionId("block.techguns.oil")
            .sound(net.minecraftforge.common.SoundActions.BUCKET_FILL,net.minecraft.sounds.SoundEvents.BUCKET_FILL).sound(net.minecraftforge.common.SoundActions.BUCKET_EMPTY,net.minecraft.sounds.SoundEvents.BUCKET_EMPTY)){
        @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer){consumer.accept(new net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions(){
            public ResourceLocation getStillTexture(){return ResourceLocation.withDefaultNamespace("block/water_still");}
            public ResourceLocation getFlowingTexture(){return ResourceLocation.withDefaultNamespace("block/water_flow");}
            public ResourceLocation getOverlayTexture(){return ResourceLocation.withDefaultNamespace("block/water_overlay");}
            public int getTintColor(){return 0xFF16120E;}
            public com.mojang.math.Vector3f modifyFogColor(net.minecraft.client.Camera camera,float partialTick,net.minecraft.client.multiplayer.ClientLevel level,int renderDistance,float darkenWorldAmount,com.mojang.math.Vector3f fluidFogColor){return new com.mojang.math.Vector3f(.06f,.05f,.04f);}
            public void modifyFogRender(net.minecraft.client.Camera camera,net.minecraft.client.renderer.FogRenderer.FogMode mode,float renderDistance,float partialTick,float nearDistance,float farDistance,com.mojang.blaze3d.shaders.FogShape shape){com.mojang.blaze3d.systems.RenderSystem.setShaderFogStart(0f);com.mojang.blaze3d.systems.RenderSystem.setShaderFogEnd(3f);}
        });}
    });
    public static final RegistryObject<FlowingFluid> OIL=FLUIDS.register("oil",()->new ForgeFlowingFluid.Source(oilProperties()));
    public static final RegistryObject<FlowingFluid> OIL_FLOWING=FLUIDS.register("flowing_oil",()->new ForgeFlowingFluid.Flowing(oilProperties()));
    public static final RegistryObject<LiquidBlock> OIL_BLOCK=TGWeaponBlocks.BLOCKS.register("oil",()->new LiquidBlock(OIL,BlockBehaviour.Properties.copy(Blocks.WATER).color(net.minecraft.world.level.material.MaterialColor.COLOR_BLACK)));
    /** Burns in furnaces and the fuel generator (lava bucket: 20000 ticks). */
    public static final RegistryObject<Item> OIL_BUCKET=TGItems.ITEMS.register("oil_bucket",()->new BucketItem(OIL,new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)){
        @Override public int getBurnTime(ItemStack stack,@org.jetbrains.annotations.Nullable net.minecraft.world.item.crafting.RecipeType<?> type){return 16000;}
        /** BucketItem only attaches the bucket fluid handler to its own class, not to subclasses. */
        @Override public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack,@org.jetbrains.annotations.Nullable net.minecraft.nbt.CompoundTag tag){return new net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper(stack);}
    });
    private static ForgeFlowingFluid.Properties oilProperties(){return new ForgeFlowingFluid.Properties(OIL_TYPE,OIL,OIL_FLOWING).bucket(OIL_BUCKET).block(OIL_BLOCK).slopeFindDistance(2).levelDecreasePerBlock(2).tickRate(20).explosionResistance(100f);}
    private static ForgeFlowingFluid.Properties properties(){return new ForgeFlowingFluid.Properties(ACID_TYPE,ACID,ACID_FLOWING).bucket(ACID_BUCKET).block(ACID_BLOCK);}
    public static void init(IEventBus bus){net.minecraftforge.common.ForgeMod.enableMilkFluid();TYPES.register(bus);FLUIDS.register(bus);}
    private TGFluids(){}
}
