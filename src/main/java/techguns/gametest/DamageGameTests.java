package techguns.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import techguns.TGArmors;
import techguns.Techguns;
import techguns.api.damagesystem.DamageType;
import techguns.damagesystem.DamageSystem;
import techguns.damagesystem.ShieldStats;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;

@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public final class DamageGameTests {
    @GameTest(template = "empty")
    public static void armorFormulaRetainsCapAndToughness(GameTestHelper helper) {
        near(helper, (float) DamageSystem.getDamageAfterAbsorb_TGFormula(10, 30, 0, 0), 0.4F, "Armor must cap at 24/25 reduction");
        near(helper, (float) DamageSystem.getDamageAfterAbsorb_TGFormula(10, 20, 4, 10), 4.4F, "Toughness must reduce penetration before subtracting armor");
        near(helper, (float) DamageSystem.getDamageAfterAbsorb_TGFormula(10, 5, 0, 10), 10, "Penetration cannot produce negative armor");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void successiveBulletsIgnoreHurtCooldown(GameTestHelper helper) {
        var cow = helper.spawn(EntityType.COW, 2, 1, 2);
        cow.setNoAi(true);
        var first = TGDamageSource.causeBulletDamage(cow, null, DeathType.DEFAULT).setNoKnockback();
        var second = TGDamageSource.causeBulletDamage(cow, null, DeathType.DEFAULT).setNoKnockback();
        DamageSystem.attackEntityFrom(cow, first, 2);
        DamageSystem.attackEntityFrom(cow, second, 2);
        helper.assertTrue(first.wasSuccessful() && second.wasSuccessful(), "Both bullets must report a successful attack");
        near(helper, cow.getHealth(), 6, "Two two-point hits in the same tick must deal four total damage");
        helper.assertTrue(cow.invulnerableTime == 0 && cow.hurtTime == 0,
                "Fast TG hits must leave the original invulnerability and server animation timers unchanged");
        cow.hurt(cow.damageSources().generic(), 1);
        near(helper, cow.getHealth(), 5, "An ordinary attack immediately after a TG bullet must not be blocked by a newly created cooldown");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void typedNpcArmorDoesNotLeakIntoAnotherTarget(GameTestHelper helper) {
        var armored = new TestArmoredCow(helper.getLevel());
        armored.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(4);
        var ordinary = helper.spawn(EntityType.COW, 2, 1, 2);
        ordinary.setNoAi(true);
        ordinary.getAttribute(Attributes.ARMOR).setBaseValue(20);
        ordinary.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(4);
        var source = TGDamageSource.causeBulletDamage(ordinary, null, DeathType.DEFAULT).setNoKnockback();
        source.armorPenetration = 2;
        DamageSystem.attackEntityFrom(armored, source, 10);
        near(helper, armored.getHealth(), 6.4F, "NPC armor must use four times the per-part penetration and raw toughness");
        DamageSystem.attackEntityFrom(ordinary, source, 10);
        near(helper, ordinary.getHealth(), 6.6666665F, "Reusing a source must preserve vanilla armor on an ordinary target");
        helper.succeed();
    }

    private static final class TestArmoredCow extends net.minecraft.world.entity.animal.Cow implements techguns.api.npc.INpcTGDamageSystem {
        private TestArmoredCow(net.minecraft.server.level.ServerLevel level) { super(EntityType.COW, level); }
        @Override public float getTotalArmorAgainstType(TGDamageSource source) { return 20; }
        @Override public float getPenetrationResistance(TGDamageSource source) { return 0; }
    }

    @GameTest(template = "empty")
    public static void lethalRadiationBypassesArmorAndResistance(GameTestHelper helper) {
        var cow = helper.spawn(EntityType.COW, 2, 1, 2);
        cow.setNoAi(true);
        cow.getAttribute(Attributes.ARMOR).setBaseValue(20);
        cow.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4));
        DamageSystem.attackEntityFrom(cow, TGDamageSource.causeLethalRadPoisoningDamage(helper.getLevel(), null, null, DeathType.LASER), 2);
        near(helper, cow.getHealth(), 8, "Lethal poisoning must ignore armor and Resistance V");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shieldEventUsesTypedReductionAndPenetration(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.OFF_HAND, TGArmors.ballistic_shield.get().getDefaultInstance());
        player.startUsingItem(InteractionHand.OFF_HAND);
        var source = TGDamageSource.causeBulletDamage(player, null, DeathType.DEFAULT);
        var block = new ShieldBlockEvent(player, source, 10);
        MinecraftForge.EVENT_BUS.post(block);
        near(helper, block.getBlockedDamage(), 9.5F, "Ballistic shield must reduce ten bullet damage to 0.5");
        source.armorPenetration = 1;
        var penetrated = new ShieldBlockEvent(player, source, 10);
        MinecraftForge.EVENT_BUS.post(penetrated);
        near(helper, penetrated.getBlockedDamage(), 8, "Penetration must cancel the 1.5-point shield threshold");
        helper.assertTrue(penetrated.shieldTakesDamage(), "Blocking must still damage the shield");
        player.stopUsingItem();
        player.setItemInHand(InteractionHand.OFF_HAND, Items.SHIELD.getDefaultInstance());
        player.startUsingItem(InteractionHand.OFF_HAND);
        var wooden = new ShieldBlockEvent(player, source, 10);
        MinecraftForge.EVENT_BUS.post(wooden);
        near(helper, wooden.getBlockedDamage(), 3.5F, "Vanilla wooden shield must block 35 percent of a TG bullet");
        near(helper, (float) ShieldStats.ADVANCED_SHIELD_STATS.getReductionPercentAgainstType(DamageType.RADIATION), 75,
                "Advanced shield must preserve original radiation protection");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shieldCamoWrapsBothWaysAndSurvivesSave(GameTestHelper helper) {
        var item = TGArmors.riot_shield.get();
        var stack = item.getDefaultInstance();
        helper.assertTrue(item.switchCamo(stack, true) == 3, "Reverse camo switch must wrap to the last variant");
        var copy = net.minecraft.world.item.ItemStack.of(stack.save(new net.minecraft.nbt.CompoundTag()));
        helper.assertTrue(item.getCurrentCamoIndex(copy) == 3, "Shield camouflage must survive item NBT serialization");
        helper.assertTrue(item.switchCamo(copy) == 0, "Forward camo switch must wrap to the default variant");
        helper.assertTrue(item.getCurrentCamoIndex(stack) == 3, "Copied shields must not share camouflage NBT");
        helper.succeed();
    }

    private static void near(GameTestHelper helper, float actual, float expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 0.0001F, message + ": expected " + expected + ", got " + actual);
    }
}
