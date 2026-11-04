package techguns.gametest;

import java.util.*;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.capabilities.TGExtendedPlayer;
import techguns.entities.projectiles.*;
import techguns.items.guns.*;

@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class WeaponParityGameTests {
    @GameTest(template="empty",batch="weapon-parity") public static void everyWeaponMatchesIndependentOriginalParameters(GameTestHelper h){
        int checked=0;
        try(var input=WeaponParityGameTests.class.getResourceAsStream("/data/techguns/verification/weapon-source-contract.json");var reader=new java.io.InputStreamReader(Objects.requireNonNull(input),java.nio.charset.StandardCharsets.UTF_8)){
            var all=JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("weapons");
            for(var element:all){var expected=element.getAsJsonObject();String id=expected.get("id").getAsString();var gun=TGuns.guns().stream().filter(g->g.getId().getPath().equals(id)).findFirst().orElseThrow().get();
                float[] actual={gun.minFiretime,gun.clipsize,gun.reloadtime,gun.maxRange,gun.recoiltime,gun.damage,gun.speed,gun.accuracy,gun.getAmmoCount(),gun.getCamoCount(),gun.getPenetration(),gun.getPelletCount(),gun.getMuzzleFlashTicks()};
                String[] names={"minFiretime","clipsize","reloadtime","maxRange","recoiltime","damage","speed","accuracy","ammoCount","camoCount","penetration","pellets","muzzleFlash"};
                for(int i=0;i<names.length;i++)near(h,actual[i],expected.get(names[i]).getAsFloat(),id+"/"+names[i]);
                var drop=expected.getAsJsonArray("damageDrop");near(h,gun.damageDropStart,drop.get(0).getAsFloat(),id+"/dropStart");near(h,gun.damageDropEnd,drop.get(1).getAsFloat(),id+"/dropEnd");near(h,gun.damageMin,drop.get(2).getAsFloat(),id+"/minimumDamage");
                h.assertTrue(gun.isSemiAuto()==expected.get("semiAuto").getAsBoolean(),id+" semiautomatic mode differs from original");
                h.assertTrue(gun.getGunHandType().name().equals(expected.get("hand").getAsString()),id+" hand type differs from original");
                h.assertTrue(gun.getCrossHairStyle().name().equals(expected.get("crosshair").getAsString()),id+" crosshair differs from original");
                h.assertTrue(gun.shootsWithLeftClick()==expected.get("leftClick").getAsBoolean(),id+" attack button differs from original");
                h.assertTrue(gun.checksRecoil()==expected.get("recoilCheck").getAsBoolean()&&gun.checksMuzzleFlash()==expected.get("flashCheck").getAsBoolean(),id+" animation checks differ from original");
                h.assertTrue(gun.canZoom()==expected.has("zoom"),id+" scope presence differs from original");
                if(expected.has("zoom")){var zoom=expected.getAsJsonObject("zoom");near(h,gun.getZoomMult(),zoom.get("mult").getAsFloat(),id+"/scope");near(h,gun.getZoomBonus(),zoom.get("bonus").getAsFloat(),id+"/scopeAccuracy");h.assertTrue(gun.isHoldZoom()==zoom.get("hold").getAsBoolean(),id+" scope control differs from original");}
                if(expected.has("spread"))near(h,gun.getSpread(),expected.get("spread").getAsFloat(),id+"/pelletSpread");checked++;
            }
        }catch(java.io.IOException e){throw new IllegalStateException(e);}
        h.assertTrue(checked==42,"All 42 gun/tool registrations must be compared");Techguns.LOGGER.info("TECHGUNS_ORIGINAL_WEAPON_PARAMETERS_PASSED: {} weapons compared with original source contracts",checked);h.succeed();
    }
    @GameTest(template="weapon_arena",batch="weapon-parity",timeoutTicks=300) public static void bfgImpactsRespectSafeModeAndChargeAgainstTerrain(GameTestHelper h){
        for(boolean safe:new boolean[]{true,false})for(int duration:new int[]{0,30,60})for(Block material:new Block[]{Blocks.DIRT,Blocks.STONE}){
            var player=player(h);var gun=(GenericGunCharge)TGuns.tfg.get();var stack=gun.getDefaultInstance();player.setItemInHand(InteractionHand.MAIN_HAND,stack);TGExtendedPlayer.get(player).enableSafemode=safe;
            int before=0;for(int x=22;x<=26;x++)for(int y=10;y<=14;y++){h.setBlock(x,y,24,material);before++;}
            h.setBlock(24,12,25,Blocks.BEDROCK);
            if(duration==0)h.assertTrue(gun.shootGunPrimary(stack,h.getLevel(),player,InteractionHand.MAIN_HAND),"Uncharged BFG must fire");
            else gun.releaseUsing(stack,h.getLevel(),player,gun.getUseDuration(stack)-duration);
            var shots=h.getLevel().getEntitiesOfClass(EnergyProjectile.class,player.getBoundingBox().inflate(20),e->e.getShooter()==player&&!e.isRemoved());h.assertTrue(shots.size()==1,"One BFG trigger must create one projectile");var shot=shots.get(0);
            for(int tick=0;tick<15&&!shot.isRemoved();tick++)shot.tick();
            h.assertTrue(shot.isRemoved(),"BFG must detonate on terrain");int left=0;for(int x=22;x<=26;x++)for(int y=10;y<=14;y++)if(!h.getLevel().isEmptyBlock(h.absolutePos(new BlockPos(x,y,24))))left++;
            h.assertTrue(safe?left==before:left<before,"BFG terrain mismatch: safe="+safe+", chargeTicks="+duration+", material="+material+", remaining="+left);
            h.assertTrue(h.getLevel().getBlockState(h.absolutePos(new BlockPos(24,12,25))).is(Blocks.BEDROCK),"BFG must respect indestructible terrain");
            for(var entity:h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,player.getBoundingBox().inflate(20)))entity.discard();
        }
        Techguns.LOGGER.info("TECHGUNS_BFG_TERRAIN_PASSED: 12 real impacts, safe/unsafe, primary/half/full charge, dirt/stone; bedrock preserved");h.succeed();
    }
    @GameTest(template="empty",batch="weapon-parity") public static void teslaDoesNotBounceBackIntoImmediatelyPreviousTarget(GameTestHelper h){
        var player=player(h);player.setPos(h.absoluteVec(new Vec3(2.5,2,1)));
        var first=h.spawn(net.minecraft.world.entity.EntityType.IRON_GOLEM,new Vec3(2.5,2,3.5));first.setNoAi(true);
        var second=h.spawn(net.minecraft.world.entity.EntityType.IRON_GOLEM,new Vec3(4.5,2,3.5));second.setNoAi(true);
        var gun=TGuns.teslagun.get();var stack=gun.getDefaultInstance();player.setItemInHand(InteractionHand.MAIN_HAND,stack);gun.shootGunPrimary(stack,h.getLevel(),player,InteractionHand.MAIN_HAND);
        var arcs=h.getLevel().getEntitiesOfClass(EnergyProjectile.class,first.getBoundingBox().inflate(10),e->e.getShooter()==player&&!e.isRemoved());
        h.assertTrue(arcs.size()==2,"Original Tesla excludes the immediately previous target: two targets create two arcs, got "+arcs.size());
        h.assertTrue(first.getHealth()<100&&second.getHealth()<100,"Both real targets must take damage");arcs.forEach(Entity::discard);h.succeed();
    }
    @GameTest(template="weapon_arena",batch="weapon-parity",timeoutTicks=400) public static void everyWeaponAndAmmoVariantActuallyHitsALivingTarget(GameTestHelper h){
        // Empty templates contain no block entries. Load the remote arena chunks before spawning targets.
        for(int x=22;x<=26;x++)for(int z=19;z<=26;z++)h.setBlock(x,10,z,Blocks.STONE);
        var report=new ArrayList<Map<String,Object>>();int guns=0;
        for(var registered:TGuns.guns()){
            var gun=registered.get();guns++;
            for(var variant:gun.getAmmoType().getVariants()){
                var player=player(h);player.setPos(h.absoluteVec(new Vec3(24.5,11,21)));TGExtendedPlayer.get(player).enableSafemode=true;
                var stack=gun.getDefaultInstance();stack.getOrCreateTag().putString("ammovariant",variant.getKey());player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                var target=h.spawn(net.minecraft.world.entity.EntityType.IRON_GOLEM,new Vec3(24.5,11,24));target.setNoAi(true);target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(20000);target.setHealth(20000);float before=target.getHealth();
                int projectiles=0;
                if(gun==TGuns.shishkebap.get())((MiningGun)gun).onLeftClickEntity(stack,player,target);
                else{
                    h.assertTrue(gun.shootGunPrimary(stack,h.getLevel(),player,InteractionHand.MAIN_HAND),"Cannot fire "+registered.getId()+"/"+variant.getKey());
                    var shots=h.getLevel().getEntitiesOfClass(GenericProjectile.class,player.getBoundingBox().inflate(20),p->p.getShooter()==player&&!p.isRemoved());projectiles=shots.size();
                    h.assertTrue(projectiles>0,"No projectile for "+registered.getId());
                    if(gun==TGuns.handcannon.get())h.assertTrue(shots.get(0) instanceof StoneBulletProjectile,"Hand cannon must use its original stone projectile and renderer");
                    if(gun==TGuns.pdw.get()||gun==TGuns.pulserifle.get())h.assertTrue(shots.stream().allMatch(p->p instanceof AdvancedBulletProjectile),"Advanced magazines must use blue advanced projectiles");
                    for(var shot:shots){
                        if(shot instanceof EnergyProjectile beam&&beam.isBeam()){if(beam.mode==EnergyProjectile.Mode.NDR)beam.tick();}
                        else {shot.setPos(h.absoluteVec(new Vec3(24.5,12.3,22)));shot.setDeltaMovement(0,0,3);shot.tick();}
                    }
                    shots.forEach(Entity::discard);
                }
                float dealt=before-target.getHealth();h.assertTrue(dealt>0,registered.getId()+"/"+variant.getKey()+" failed to damage a real living target");
                h.assertTrue(gun.getCurrentAmmo(stack)==gun.clipsize-1,"One attack must consume one ammunition unit: "+registered.getId());
                if(variant.getKey().equals("incendiary")||gun==TGuns.flamethrower.get()||gun==TGuns.alienblaster.get()||gun==TGuns.shishkebap.get())h.assertTrue(target.isOnFire(),registered.getId()+" must ignite its target");
                if(gun==TGuns.biogun.get())h.assertTrue(target.hasEffect(net.minecraft.world.effect.MobEffects.POISON),"Biogun must poison a living target");
                if(gun==TGuns.nucleardeathray.get()&&TGRadiationSystem.isEnabled())h.assertTrue(target.hasEffect(TGRadiationSystem.RADIATION.get()),"Nuclear beam must irradiate its target");
                report.add(Map.of("weapon",registered.getId().toString(),"ammo",variant.getKey(),"projectiles",projectiles,"damageRecorded",dealt,"ignites",target.isOnFire()));
                target.discard();for(var radiation:h.getLevel().getEntitiesOfClass(techguns.entities.special.EntityRadiation.class,player.getBoundingBox().inflate(25)))radiation.discard();
            }
        }
        h.assertTrue(guns==42,"Every gun/tool must be included");
        try{var output=java.nio.file.Path.of("verification");java.nio.file.Files.createDirectories(output);java.nio.file.Files.writeString(output.resolve("weapon-hit-matrix.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(java.io.IOException error){throw new IllegalStateException(error);}
        Techguns.LOGGER.info("TECHGUNS_WEAPON_HIT_MATRIX_PASSED: {} weapons/tools, {} ammunition cases, actual living-target collisions and status effects",guns,report.size());h.succeed();
    }
    @GameTest(template="empty",batch="weapon-parity") public static void chargedShotsKeepOriginalOriginAndBioGravity(GameTestHelper h){
        for(var gun:new GenericGunCharge[]{(GenericGunCharge)TGuns.tfg.get(),(GenericGunCharge)TGuns.biogun.get()}){
            var player=player(h);player.setPos(h.absoluteVec(new Vec3(2.5,2,1)));var stack=gun.getDefaultInstance();player.setItemInHand(InteractionHand.MAIN_HAND,stack);gun.releaseUsing(stack,h.getLevel(),player,gun.getUseDuration(stack)-30);
            var shots=h.getLevel().getEntitiesOfClass(GenericProjectile.class,player.getBoundingBox().inflate(8),p->p.getShooter()==player&&!p.isRemoved());h.assertTrue(shots.size()==1,"Charge release must create one projectile");var shot=shots.get(0);
            near(h,(float)Math.hypot(shot.getX()-player.getX(),shot.getZ()-player.getZ()),.16F,"Charged source hand offset without forward shift");
            if(gun==TGuns.biogun.get())near(h,shot.saveWithoutId(new net.minecraft.nbt.CompoundTag()).getFloat("gravity"),.01F,"Charged biogun gravity");shot.discard();
        }h.succeed();
    }
    private static net.minecraftforge.common.util.FakePlayer player(GameTestHelper h){var player=new net.minecraftforge.common.util.FakePlayer(h.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"weapon-parity"));player.setPos(h.absoluteVec(new Vec3(24.5,11,16.5)));player.setYRot(0);player.setYHeadRot(0);player.setXRot(0);return player;}
    private static void near(GameTestHelper h,float actual,float expected,String name){h.assertTrue(Math.abs(actual-expected)<.00001F,name+": original="+expected+", port="+actual);}
    private WeaponParityGameTests(){}
}
