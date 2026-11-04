package techguns.gametest;

import java.util.List;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;
import techguns.*;
import techguns.entities.projectiles.GenericProjectile;

/** Optional integration test against the installed, unmodified Target Dummy mod. */
@GameTestHolder(Techguns.MODID)
public final class TestToolsGameTests {
    @GameTestGenerator public static List<TestFunction> tests(){
        if(!ModList.get().isLoaded("dummmmmmy"))return List.of();
        return List.of(new TestFunction("test-tools","techguns.target_dummy_records_bullet","techguns:empty",100,0,true,TestToolsGameTests::bullet));
    }
    private static void bullet(GameTestHelper helper){
        var type=ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.fromNamespaceAndPath("dummmmmmy","target_dummy"));
        if(type==null)throw new IllegalStateException("Target Dummy entity is missing");
        var dummy=(LivingEntity)type.create(helper.getLevel());
        if(dummy==null)throw new IllegalStateException("Target Dummy could not be created");
        var target=helper.absoluteVec(new Vec3(2.5,2,1.5));dummy.moveTo(target.x,target.y,target.z);helper.getLevel().addFreshEntity(dummy);
        try{
            // The dummy keeps infinite health; its own damage accumulator drives its numbers and DPS.
            var tally=dummy.getClass().getDeclaredField("totalDamageTakenInCombat");tally.setAccessible(true);float before=tally.getFloat(dummy);
            var projectile=new GenericProjectile(TGEntities.GENERIC_PROJECTILE.get(),helper.getLevel());
            var start=helper.absoluteVec(new Vec3(.5,2.5,1.5));projectile.setPos(start);projectile.configure(8,1,20,0,0,8,0,false);projectile.setDeltaMovement(4,0,0);helper.getLevel().addFreshEntity(projectile);projectile.tick();
            float recorded=tally.getFloat(dummy)-before;
            helper.assertTrue(projectile.isRemoved(),"The real projectile must collide with the third-party dummy");
            helper.assertTrue(Math.abs(recorded-8.01F)<.002F,"The dummy must record the original 8 damage and 0.01 knockback hit; recorded "+recorded);
            helper.assertTrue(dummy.isAlive(),"The test target must remain available after a shot");
            Techguns.LOGGER.info("TECHGUNS_TARGET_DUMMY_DAMAGE_PASSED: unmodified Target Dummy recorded {} from a real generic projectile",recorded);
            helper.succeed();
        }catch(ReflectiveOperationException error){throw new IllegalStateException("Target Dummy integration fixture no longer matches the pinned release",error);}
        finally{dummy.discard();}
    }
    private TestToolsGameTests(){}
}
