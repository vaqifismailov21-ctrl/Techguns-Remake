package techguns.gametest;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import techguns.TGItems;
import techguns.TGRadiationSystem;
import techguns.Techguns;
import techguns.api.radiation.TGRadiation;

@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public final class RadiationGameTests {
    @GameTest(template = "empty")
    public static void resistanceReducesRadiationBeforeMobDamage(GameTestHelper helper) {
        var cow = helper.spawn(EntityType.COW, 2, 1, 2);
        cow.setNoAi(true);
        cow.getAttribute(TGRadiation.RADIATION_RESISTANCE.get()).setBaseValue(2);
        TGRadiationSystem.RADIATION.get().applyEffectTick(cow, 4);
        helper.assertTrue(Math.abs(cow.getHealth() - 8.5F) < 0.0001F, "Five radiation minus two resistance must deal 1.5 mob damage");
        helper.assertTrue(TGRadiationSystem.RADIATION.get().getCurativeItems().isEmpty(), "Milk must not cure radiation exposure");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void radawayConsumesOneReturnsBagAndAppliesOriginalEffect(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "radaway-test"));
        var stack = new net.minecraft.world.item.ItemStack(TGItems.RAD_AWAY.get(), 2);
        TGItems.RAD_AWAY.get().finishUsingItem(stack, helper.getLevel(), player);
        var effect = player.getEffect(TGRadiationSystem.REGENERATION.get());
        helper.assertTrue(stack.getCount() == 1, "RadAway must consume one item");
        helper.assertTrue(effect != null && effect.getDuration() == 400 && effect.getAmplifier() == 14,
                "RadAway must grant the original twenty-second regeneration at amplifier 14");
        helper.assertTrue(player.getInventory().countItem(TGItems.INFUSION_BAG.get()) == 1, "Using RadAway must return exactly one infusion bag");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void medkitAndBandagesHealAndRegenerateLikeTechguns17(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "medkit-test"));
        player.setHealth(4F);
        var kit = new net.minecraft.world.item.ItemStack(TGItems.MEDKIT.get(), 2);
        TGItems.MEDKIT.get().finishUsingItem(kit, helper.getLevel(), player);
        var regen = player.getEffect(net.minecraft.world.effect.MobEffects.REGENERATION);
        helper.assertTrue(kit.getCount() == 1, "Medkit must consume one item");
        helper.assertTrue(Math.abs(player.getHealth() - 10F) < 0.001F, "Medkit must heal three hearts");
        helper.assertTrue(regen != null && regen.getDuration() == 400 && regen.getAmplifier() == 1, "Medkit must give Regeneration II for 20 s");
        player.removeAllEffects(); player.setHealth(4F);
        var bandage = new net.minecraft.world.item.ItemStack(TGItems.BANDAGES.get(), 1);
        TGItems.BANDAGES.get().finishUsingItem(bandage, helper.getLevel(), player);
        regen = player.getEffect(net.minecraft.world.effect.MobEffects.REGENERATION);
        helper.assertTrue(bandage.isEmpty() && Math.abs(player.getHealth() - 6F) < 0.001F, "Bandages must heal one heart");
        helper.assertTrue(regen != null && regen.getDuration() == 300 && regen.getAmplifier() == 0, "Bandages must give Regeneration I for 15 s");
        helper.assertTrue(TGItems.MEDKIT.get().getMaxStackSize(kit) == 16, "Medical items stack to 16");
        helper.succeed();
    }
}
