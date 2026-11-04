package techguns.gametest;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.entities.npcs.*;
import techguns.entities.projectiles.*;

@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class SpecialNpcGameTests {
    @GameTest(template="weapon_arena",batch="special-npc") public static void originalNpcDeathEffectEligibility(GameTestHelper h){
        int eligible=0;for(var entry:TGNpcs.TYPES.entrySet()){
            var entity=entry.getValue().get().create(h.getLevel());boolean gore=techguns.deatheffects.EntityDeathUtils.hasSpecialDeathAnim(entity,techguns.deatheffects.EntityDeathUtils.DeathType.GORE);
            h.assertTrue(gore!=entry.getKey().equals("zombiepoliceman"),"Original exact-class gore eligibility: "+entry.getKey());if(gore)eligible++;
        }
        h.assertTrue(eligible==16,"Original humanoid gore eligibility count");
        h.assertTrue(techguns.deatheffects.EntityDeathUtils.hasSpecialDeathAnim(TGSpecialNpcs.ALIEN_BUG.get().create(h.getLevel()),techguns.deatheffects.EntityDeathUtils.DeathType.GORE),"Original alien bug gore");
        h.assertTrue(techguns.deatheffects.EntityDeathUtils.hasSpecialDeathAnim(TGSpecialNpcs.GHASTLING.get().create(h.getLevel()),techguns.deatheffects.EntityDeathUtils.DeathType.GORE),"Original ghastling gore");
        h.assertTrue(!techguns.deatheffects.EntityDeathUtils.hasSpecialDeathAnim(TGSpecialNpcs.HELICOPTER.get().create(h.getLevel()),techguns.deatheffects.EntityDeathUtils.DeathType.GORE),"Original helicopter retains its crash rather than gore");h.succeed();
    }
    @GameTest(template="weapon_arena",batch="special-npc") public static void exoticAttributesAndAlienBiteRetainSourceValues(GameTestHelper h){
        var bug=TGSpecialNpcs.ALIEN_BUG.get().create(h.getLevel());var ghast=TGSpecialNpcs.GHASTLING.get().create(h.getLevel());var heli=TGSpecialNpcs.HELICOPTER.get().create(h.getLevel());
        h.assertTrue(bug.getMaxHealth()==20&&bug.getAttributeBaseValue(Attributes.MOVEMENT_SPEED)==1&&bug.getAttributeBaseValue(Attributes.ATTACK_DAMAGE)==4,"Original alien bug attributes");
        h.assertTrue(bug.getMobType()==MobType.ARTHROPOD&&!bug.fireImmune(),"Original bug creature type/fire behavior");
        h.assertTrue(ghast.getMaxHealth()==20&&ghast.getAttributeBaseValue(Attributes.FOLLOW_RANGE)==64&&ghast.fireImmune(),"Original ghastling attributes");
        h.assertTrue(Math.abs(ghast.getEyeHeight()-1.5f)<.001,"Original ghastling eye height");
        h.assertTrue(heli.getMaxHealth()==100&&heli.getAttributeBaseValue(Attributes.FOLLOW_RANGE)==128&&heli.getAttributeBaseValue(Attributes.ARMOR_TOUGHNESS)==5&&heli.fireImmune(),"Original helicopter attributes");
        h.assertTrue(Math.abs(heli.getEyeHeight()-.5f)<.001&&heli.getBulletOffsetSide()==2,"Original helicopter weapon offsets");
        var target=h.spawn(EntityType.IRON_GOLEM,new Vec3(24,11,24));target.setNoAi(true);bug.setPos(h.absoluteVec(new Vec3(24,11,22)));h.getLevel().addFreshEntity(bug);float before=target.getHealth();
        h.assertTrue(bug.doHurtTarget(target)&&target.getHealth()<before,"Bite must damage an actual living target");h.assertTrue(bug.getAttackTimer()==10,"Bite starts ten-tick jaw animation");
        bug.handleEntityEvent((byte)4);bug.setNoAi(true);for(int tick=0;tick<10;tick++)bug.aiStep();h.assertTrue(bug.getAttackTimer()==0,"Bite animation must finish");
        bug.discard();target.discard();h.succeed();
    }
    @GameTest(template="weapon_arena",batch="special-npc") public static void helicopterAndGhastlingUseOriginalAttackSchedules(GameTestHelper h){
        var target=h.spawn(EntityType.IRON_GOLEM,new Vec3(24,11,24));target.setNoAi(true);
        var heli=TGSpecialNpcs.HELICOPTER.get().create(h.getLevel());heli.setPos(h.absoluteVec(new Vec3(24,12,8)));heli.setTarget(target);heli.setNoAi(true);h.getLevel().addFreshEntity(heli);
        var attack=new AttackHelicopter.HelicopterAttack(heli);attack.start();for(int tick=0;tick<36;tick++)attack.tick();
        var shots=h.getLevel().getEntitiesOfClass(GenericProjectile.class,heli.getBoundingBox().inflate(25),p->p.getShooter()==heli);
        h.assertTrue(shots.size()==6,"Original helicopter fires five bullets and one rocket per sequence");h.assertTrue(shots.stream().filter(p->p instanceof RocketProjectile).count()==1,"Helicopter rocket is a real rocket projectile");
        for(var shot:shots){var tag=shot.saveWithoutId(new CompoundTag());h.assertTrue(tag.getFloat("damage")==12&&!tag.getBoolean("blockdamage"),"Helicopter source damage and terrain-safe projectile flag");shot.discard();}attack.stop();h.assertTrue(!heli.isAttacking(),"Helicopter attack state resets");heli.discard();
        var ghast=TGSpecialNpcs.GHASTLING.get().create(h.getLevel());ghast.setPos(h.absoluteVec(new Vec3(24,11,16)));ghast.setTarget(target);ghast.setNoAi(true);h.getLevel().addFreshEntity(ghast);
        var fireball=new Ghastling.FireballAttack(ghast);fireball.start();for(int tick=0;tick<49;tick++)fireball.tick();
        var bolts=h.getLevel().getEntitiesOfClass(EnergyProjectile.class,ghast.getBoundingBox().inflate(12),p->p.getShooter()==ghast);
        h.assertTrue(bolts.size()==3,"Active original ghastling AI fires a three-shot burst");
        for(var bolt:bolts){var tag=bolt.saveWithoutId(new CompoundTag());h.assertTrue(bolt.mode==EnergyProjectile.Mode.ALIEN&&tag.getFloat("damage")==6&&!tag.getBoolean("blockdamage"),"Ghastling uses six-damage alien projectiles, not the unused ten-damage AI");bolt.discard();}
        h.assertTrue(!ghast.isAttacking(),"Ghastling leaves charged pose for fifty-tick recovery");ghast.discard();target.discard();
        Techguns.LOGGER.info("TECHGUNS_SPECIAL_NPC_ATTACKS_PASSED: helicopter five-bullet/one-rocket schedule, ghastling active three-shot AI, source damage and terrain-safe flags");h.succeed();
    }
    @GameTest(template="weapon_arena",batch="special-npc") public static void helicopterDeathLastsOneHundredTicks(GameTestHelper h){
        var heli=TGSpecialNpcs.HELICOPTER.get().create(h.getLevel());heli.setPos(h.absoluteVec(new Vec3(24,20,24)));heli.setNoAi(true);h.getLevel().addFreshEntity(heli);heli.setHealth(0);
        for(int tick=0;tick<99;tick++)heli.tick();h.assertTrue(!heli.isRemoved()&&heli.deathTime==99,"Helicopter must retain the original hundred-tick crash, not vanilla twenty-tick death");
        heli.tick();h.assertTrue(heli.isRemoved()&&heli.deathTime==100,"Helicopter explosion/removal at original tick 100");h.succeed();
    }
    private SpecialNpcGameTests(){}
}
