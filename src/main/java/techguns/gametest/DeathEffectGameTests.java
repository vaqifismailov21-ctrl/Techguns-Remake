package techguns.gametest;

import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import techguns.deatheffects.EntityDeathUtils;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.deatheffects.DeathEffectEvents;
import techguns.damagesystem.TGDamageSource;

@GameTestHolder("techguns") @PrefixGameTestTemplate(false)
public final class DeathEffectGameTests {
    @GameTest(template="empty") public static void sourceDeathChanceIsAppliedBeforeAnimation(GameTestHelper h){
        var cow=h.spawn(EntityType.COW,2,1,2);var damage=TGDamageSource.causeEnergyDamage(cow,null,DeathType.LASER);damage.goreChance=.5F;
        h.assertTrue(DeathEffectEvents.eligible(cow,damage,.49F),"Laser death below source chance must be eligible");
        h.assertTrue(!DeathEffectEvents.eligible(cow,damage,.5F),"The original strict chance boundary must be preserved");
        damage.deathType=DeathType.DEFAULT;damage.goreChance=1;
        h.assertTrue(!DeathEffectEvents.eligible(cow,damage,0),"Default death must retain the vanilla animation");h.succeed();
    }
    @GameTest(template="empty") public static void biologicalAndLaserWorkWithoutGoreModel(GameTestHelper h){
        var golem=h.spawn(EntityType.IRON_GOLEM,2,1,2);
        h.assertTrue(EntityDeathUtils.hasSpecialDeathAnim(golem,DeathType.LASER)&&EntityDeathUtils.hasSpecialDeathAnim(golem,DeathType.BIO),"Source allows ash and bio on every living model");
        h.assertTrue(!EntityDeathUtils.hasSpecialDeathAnim(golem,DeathType.GORE),"Unsupported gore models must retain vanilla death");
        var cow=h.spawn(EntityType.COW,4,1,2);h.assertTrue(EntityDeathUtils.hasSpecialDeathAnim(cow,DeathType.GORE),"Original quadruped gore must be supported");h.succeed();
    }
    @GameTest(template="empty") public static void lethalEnergyHitRetainsDropsAndVanillaDeathLifecycle(GameTestHelper h){
        var cow=h.spawn(EntityType.COW,2,1,2);cow.setHealth(1);var damage=TGDamageSource.causeEnergyDamage(cow,null,DeathType.LASER);damage.goreChance=1;
        techguns.damagesystem.DamageSystem.attackEntityFrom(cow,damage,20);
        h.assertTrue(cow.isDeadOrDying()&&!cow.isRemoved(),"Death effects must leave the normal death/loot lifecycle intact");h.succeed();
    }
}
