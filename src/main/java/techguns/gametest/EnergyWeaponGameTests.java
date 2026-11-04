package techguns.gametest;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.items.guns.*;
import techguns.entities.projectiles.*;
@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class EnergyWeaponGameTests {
    @GameTest(template="empty") public static void rightButtonScopeSurvivesTickAndResetsOnReloadAndSlotChange(GameTestHelper h){
        var state=new techguns.api.guns.WeaponAimState();var gun=TGuns.as50.get();
        state.press(gun,0,false);state.tick(gun,0,true,false,false);h.assertTrue(state.active(gun,0),"First scope press must survive the following tick without requiring sneak");
        state.press(gun,0,false);h.assertFalse(state.active(gun,0),"Second button edge switches scope off");
        state.press(gun,0,false);state.tick(gun,0,true,true,false);h.assertFalse(state.active(gun,0),"Reload exits scope");
        state.press(gun,0,true);h.assertFalse(state.active(gun,0),"Reload must block scope activation");
        state.press(gun,0,false);state.tick(gun,1,true,false,false);h.assertFalse(state.active(gun,1),"A different selected slot resets scope even for the same gun item");
        state.press(gun,1,false);state.tick(TGuns.pistol.get(),1,true,false,false);h.assertFalse(state.active(gun,1),"Changing weapons must clear the old scope");h.succeed();
    }
    @GameTest(template="empty") public static void twelveEnergyGunsHaveTheirOwnProjectileTypesAndConsumption(GameTestHelper h){
        for(int x=0;x<6;x++)for(int y=1;y<6;y++)h.setBlock(x,y,5,Blocks.STONE);
        int count=0;
        for(var entry:TGuns.guns()){
            var gun=entry.get();if(gun.getEnergyMode()==null)continue;
            var player=player(h);var stack=gun.getDefaultInstance();player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            h.assertTrue(gun.shootGunPrimary(stack,h.getLevel(),player,InteractionHand.MAIN_HAND),"Energy gun must fire: "+entry.getId());
            var shots=shots(h,player);int expected=gun.getEnergyMode()==EnergyProjectile.Mode.SONIC?16:gun.getPelletCount();
            h.assertTrue(shots.size()==expected,"Original energy projectile pattern: "+entry.getId());
            h.assertTrue(shots.stream().allMatch(p->p.mode==gun.getEnergyMode()),"An energy gun must select its original specialization");
            h.assertTrue(gun.getCurrentAmmo(stack)==gun.clipsize-1,"One primary trigger consumes one ammo unit");shots.forEach(Entity::discard);count++;
        }h.assertTrue(count==12,"All twelve previously deferred energy guns must be registered");h.succeed();
    }
    @GameTest(template="empty") public static void laserHitsImmediatelyAndPersistsForSevenTicks(GameTestHelper h){
        var p=player(h);var target=h.spawn(EntityType.IRON_GOLEM,new Vec3(2.5,2,4));target.setNoAi(true);var gun=TGuns.lasergun.get();var stack=gun.getDefaultInstance();p.setItemInHand(InteractionHand.MAIN_HAND,stack);
        gun.shootGunPrimary(stack,h.getLevel(),p,InteractionHand.MAIN_HAND);var beam=shots(h,p).get(0);
        h.assertTrue(target.getHealth()<target.getMaxHealth(),"Ray trace must damage the target immediately at creation");h.assertTrue(beam.beamDistance>0&&beam.beamDistance<5,"Beam endpoint must stop at the actual target");
        float health=target.getHealth();for(int i=0;i<6;i++)beam.tick();h.assertFalse(beam.isRemoved(),"Laser visual remains for six ticks");beam.tick();h.assertTrue(beam.isRemoved()&&target.getHealth()==health,"Seven-tick visual must not apply repeated laser damage");h.succeed();
    }
    @GameTest(template="empty") public static void teslaChainsAtMostFourTimesThroughVisibleTargets(GameTestHelper h){
        var p=player(h);for(int i=0;i<3;i++){var mob=h.spawn(EntityType.IRON_GOLEM,new Vec3(2.5+i*2,2,4));mob.setNoAi(true);}
        var gun=TGuns.teslagun.get();p.setItemInHand(InteractionHand.MAIN_HAND,gun.getDefaultInstance());gun.shootGunPrimary(p.getMainHandItem(),h.getLevel(),p,InteractionHand.MAIN_HAND);
        var arcs=shots(h,p);h.assertTrue(arcs.size()>1&&arcs.size()<=5,"Tesla creates a primary beam and at most four chained beams");
        var damage=arcs.stream().map(a->a.saveWithoutId(new net.minecraft.nbt.CompoundTag()).getFloat("damage")).sorted().toList();h.assertTrue(damage.get(0)<damage.get(damage.size()-1),"Chain hits retain the original 0.75 damage multiplier");arcs.forEach(Entity::discard);h.succeed();
    }
    @GameTest(template="empty") public static void sonicRingsShareHitsSoSixteenProjectilesDoNotMultiplyDamage(GameTestHelper h){
        var p=player(h);var target=h.spawn(EntityType.IRON_GOLEM,new Vec3(2.5,2,4));target.setNoAi(true);var gun=TGuns.sonicshotgun.get();p.setItemInHand(InteractionHand.MAIN_HAND,gun.getDefaultInstance());gun.shootGunPrimary(p.getMainHandItem(),h.getLevel(),p,InteractionHand.MAIN_HAND);
        var wave=shots(h,p);h.assertTrue(wave.size()==16&&wave.stream().filter(a->a.mainProjectile).count()==1,"Sonic uses a center and two rings of five and ten projectiles");
        float before=target.getHealth();for(var a:wave){a.setPos(h.absoluteVec(new Vec3(2.5,3,2)));a.setDeltaMovement(0,0,3);a.tick();}
        h.assertTrue(target.getHealth()<before&&before-target.getHealth()<=25.1F,"Shared wave hits damage one target once");wave.forEach(Entity::discard);h.succeed();
    }
    @GameTest(template="empty") public static void tfgChargeRetainsSizeAndEnergyConsumption(GameTestHelper h){
        for(int duration:new int[]{30,60}){
            var p=player(h);var gun=(GenericGunCharge)TGuns.tfg.get();var stack=gun.getDefaultInstance();p.setItemInHand(InteractionHand.MAIN_HAND,stack);gun.releaseUsing(stack,h.getLevel(),p,gun.getUseDuration(stack)-duration);
            var shot=shots(h,p).get(0);h.assertTrue(shot.size==(duration==60?5:2.5F),"Full TFG charge adds the source size bonus");h.assertTrue(gun.getCurrentAmmo(stack)==(duration==60?10:15),"TFG charge consumes five or ten nuclear energy units");shot.discard();
        }h.succeed();
    }
    @GameTest(template="empty") public static void energyProjectileSpawnPreservesModeChargeAndBeamEndpoint(GameTestHelper h){
        var p=player(h);var original=new EnergyProjectile(h.getLevel(),p,EnergyProjectile.Mode.TFG,50,2,50,0,8,15,15,0.5F,false,EnumBulletFirePos.RIGHT).charge(1);
        original.beamDistance=12.5;var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());original.writeSpawnData(buffer);var copy=TGEntities.ENERGY_PROJECTILE.get().create(h.getLevel());copy.readSpawnData(buffer);buffer.release();
        h.assertTrue(copy.mode==EnergyProjectile.Mode.TFG&&copy.size==5&&copy.beamDistance==12.5&&copy.getDeltaMovement().equals(original.getDeltaMovement()),"Spawn data must preserve the correct energy projectile visuals");h.succeed();
    }
    @GameTest(template="empty") public static void allEnergyParticleDefinitionsParseAndSample(GameTestHelper h){
        try(var input=EnergyWeaponGameTests.class.getResourceAsStream("/assets/techguns/particles/nonenergyfx.txt");var reader=new java.io.InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8)){
            var types=techguns.client.particle.BulletImpactDefinition.read(reader,true);h.assertTrue(types.size()>=104,"Every selected energy and conventional weapon effect must parse");
            for(var type:types.values()){h.assertTrue(type.texture!=null,"Original effect needs a texture");type.sample(new Random(0),30,75);}h.succeed();
        }catch(java.io.IOException ex){throw new IllegalStateException(ex);}
    }
    private static net.minecraftforge.common.util.FakePlayer player(GameTestHelper h){var p=new net.minecraftforge.common.util.FakePlayer(h.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"energy-test"));p.setPos(h.absoluteVec(new Vec3(2.5,2,1)));p.setYRot(0);p.setYHeadRot(0);p.setXRot(0);return p;}
    private static List<EnergyProjectile> shots(GameTestHelper h,net.minecraft.world.entity.player.Player p){return h.getLevel().getEntitiesOfClass(EnergyProjectile.class,p.getBoundingBox().inflate(160),e->e.getShooter()==p&&!e.isRemoved());}
}
