package techguns;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import techguns.api.radiation.TGRadiation;
import techguns.capabilities.TGExtendedPlayer;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.radiation.*;

@Mod.EventBusSubscriber(modid = Techguns.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TGRadiationSystem {
    public static final int MINOR_POISONING = 500, SEVERE_POISONING = 750, LETHAL_POISONING = 1000;
    public static final int RADLOST_ON_DEATH = 200, INVENTORY_RADIATION_INVERVALL = 60;
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Techguns.MODID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, Techguns.MODID);
    public static final RegistryObject<MobEffect> RADIATION = EFFECTS.register("radiation", RadiationPotion::new);
    public static final RegistryObject<MobEffect> REGENERATION = EFFECTS.register("radregeneration", RadRegenerationPotion::new);
    public static final RegistryObject<MobEffect> RESISTANCE = EFFECTS.register("radresistance", RadResistancePotion::new);
    public static final RegistryObject<Potion> RAD_POTION = POTIONS.register("radpotion", () -> new Potion(new MobEffectInstance(RADIATION.get(), 300, 4)));
    public static final RegistryObject<Potion> RAD_POTION_SEVERE = POTIONS.register("radpotion_severe", () -> new Potion(new MobEffectInstance(RADIATION.get(), 300, 24)));
    public static final RegistryObject<Potion> RAD_REGENERATION_POTION = POTIONS.register("radregenerationpotion", () -> new Potion(new MobEffectInstance(REGENERATION.get(), 400, 9)));
    public static final RegistryObject<Potion> RAD_RESISTANCE_POTION = POTIONS.register("radresistancepotion", () -> new Potion(new MobEffectInstance(RESISTANCE.get(), 900, 1)));

    public static boolean isEnabled() { return !TGConfig.WIP_disableRadiationSystem; }
    @SubscribeEvent public static void attributes(EntityAttributeModificationEvent event) {
        for (var type : event.getTypes()) if (!event.has(type, TGRadiation.RADIATION_RESISTANCE.get())) event.add(type, TGRadiation.RADIATION_RESISTANCE.get());
    }
    public static void setup() {
        RESISTANCE.get().addAttributeModifier(TGRadiation.RADIATION_RESISTANCE.get(), "515AD21C-3FB2-4E36-BBCE-88C2ED738DE2", 1, AttributeModifier.Operation.ADDITION);
        if (isEnabled()) {
            ItemRadiationRegistry.addRadiationData(TGItems.ENRICHED_URANIUM.get().getDefaultInstance(), 3, 100);
            ItemRadiationRegistry.addRadiationData(TGItems.YELLOWCAKE.get().getDefaultInstance(), 1, 100);
            ItemRadiationRegistry.addRadiationData(TGItems.ANTI_GRAV_CORE.get().getDefaultInstance(), 4, 100);
            ItemRadiationRegistry.addRadiationData(TGItems.PLASMA_GENERATOR.get().getDefaultInstance(), 4, 100);
            ItemRadiationRegistry.addRadiationData(TGItems.TACTICAL_NUKE_WARHEAD.get().getDefaultInstance(), 1, 100);
            ItemRadiationRegistry.addRadiationData(TGItems.ROCKET_NUKE.get().getDefaultInstance(), 1, 100);
        }
        BrewingRecipeRegistry.addRecipe(new RadiationBrewing(Potions.AWKWARD, tag("dusts/uranium"), RAD_POTION.get()));
        BrewingRecipeRegistry.addRecipe(new RadiationBrewing(Potions.AWKWARD, stack -> stack.is(TGItems.ENRICHED_URANIUM.get()), RAD_POTION_SEVERE.get()));
        BrewingRecipeRegistry.addRecipe(new RadiationBrewing(Potions.AWKWARD, tag("ingots/lead"), RAD_RESISTANCE_POTION.get()));
        BrewingRecipeRegistry.addRecipe(new RadiationBrewing(RAD_POTION.get(), stack -> stack.is(Items.GOLDEN_CARROT), RAD_REGENERATION_POTION.get()));
        BrewingRecipeRegistry.addRecipe(new RadiationBrewing(RAD_POTION.get(), stack -> stack.is(Items.GLISTERING_MELON_SLICE), RAD_REGENERATION_POTION.get()));
    }
    private static java.util.function.Predicate<ItemStack> tag(String path) {
        TagKey<Item> key = TagKey.create(net.minecraft.core.Registry.ITEM_REGISTRY, new ResourceLocation("forge", path));
        return stack -> stack.is(key);
    }
    private record RadiationBrewing(Potion input, java.util.function.Predicate<ItemStack> ingredient, Potion output) implements IBrewingRecipe {
        @Override public boolean isInput(ItemStack stack) { return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == input; }
        @Override public boolean isIngredient(ItemStack stack) { return ingredient.test(stack); }
        @Override public ItemStack getOutput(ItemStack inputStack, ItemStack ingredientStack) {
            return isInput(inputStack) && isIngredient(ingredientStack) ? PotionUtils.setPotion(new ItemStack(Items.POTION), output) : ItemStack.EMPTY;
        }
    }
    public static void applyRadToEntities(BlockEntity block, double radius, int duration, int strength, double innerRadius, int outerStrength) {
        if (block.getLevel() != null) applyRadToEntities(block.getLevel(), block.getBlockPos().getX() + 0.5,
                block.getBlockPos().getY() + 0.5, block.getBlockPos().getZ() + 0.5, radius, duration, strength, innerRadius, outerStrength);
    }
    public static void applyRadToEntities(Level level, double x, double y, double z, double radius, int duration, int strength, double innerRadius, int outerStrength) {
        Vec3 center = new Vec3(x, y, z);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius), entity -> true)) {
            double distance = center.distanceTo(entity.position());
            if (distance < radius) {
                int applied = strength;
                if (distance > innerRadius) {
                    double factor = (distance - innerRadius) / (radius - innerRadius);
                    // Preserve the original formula, including its unusual direction of interpolation.
                    applied = (int) Math.round(outerStrength + (strength - outerStrength) * factor);
                }
                entity.addEffect(new MobEffectInstance(RADIATION.get(), duration, applied, true, true));
            }
        }
    }
    public static void tickPlayer(ServerPlayer player, TGExtendedPlayer data) {
        long time = player.level.getGameTime();
        if (isEnabled() && time % INVENTORY_RADIATION_INVERVALL == 0) {
            int max = 0;
            for (ItemStack stack : player.getInventory().items) {
                ItemRadiationData radiation = ItemRadiationRegistry.getRadiationDataFor(stack);
                if (radiation != null) max = Math.max(max, radiation.radamount);
            }
            if (max > 0) player.addEffect(new MobEffectInstance(RADIATION.get(), INVENTORY_RADIATION_INVERVALL, max - 1, false, false));
        }
        if (time % 20 != 0) return;
        if (data.radlevel >= MINOR_POISONING && data.radlevel < SEVERE_POISONING) player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 30, 0, false, false));
        else if (data.radlevel >= SEVERE_POISONING) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 30, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 30, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 1, false, false));
        }
        if (data.radlevel >= LETHAL_POISONING) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 30, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, false));
            player.hurt(TGDamageSource.causeLethalRadPoisoningDamage(player.level, null, null, DeathType.LASER), 2.0F);
        }
    }
    private TGRadiationSystem() {}
}
