package techguns.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import techguns.TGItems;
import techguns.Techguns;
import techguns.capabilities.TGExtendedPlayer;
import techguns.events.TGAutoFoodHandler;
import techguns.gui.player.TGPlayerInventoryContainer;
import techguns.util.InventoryUtil;

@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public final class InventoryGameTests {
    @GameTest(template = "empty")
    public static void invalidatedPlayerDataCanBeRevivedAndClonedWithoutLosingInventory(GameTestHelper helper) {
        var oldPlayer=helper.makeMockSurvivalPlayer(); var newPlayer=helper.makeMockSurvivalPlayer();
        var original=TGExtendedPlayer.get(oldPlayer);
        original.radlevel=421; original.tg_inventory.setItem(7,new ItemStack(TGItems.PISTOL_ROUNDS.get(),23));
        var oldHandle=oldPlayer.getCapability(techguns.capabilities.TGExtendedPlayerCapProvider.CAPABILITY);
        oldPlayer.invalidateCaps();
        helper.assertTrue(TGExtendedPlayer.find(oldPlayer).isEmpty(),"HUD lookup must safely skip an invalidated player");
        oldPlayer.reviveCaps();
        helper.assertTrue(TGExtendedPlayer.get(oldPlayer)==original,"Revive must retain the original saved data");
        helper.assertFalse(oldHandle.isPresent(),"Old invalidated handles must stay invalid after revival");
        techguns.capabilities.CapabilityEventHandler.clonePlayer(new net.minecraftforge.event.entity.player.PlayerEvent.Clone(newPlayer,oldPlayer,true));
        var copy=TGExtendedPlayer.get(newPlayer);
        helper.assertTrue(copy.entity==newPlayer && copy.radlevel==421 && copy.tg_inventory.getItem(7).getCount()==23,
                "Respawn cloning must transfer data to the new owner");
        copy.tg_inventory.getItem(7).shrink(1);
        helper.assertTrue(original.tg_inventory.getItem(7).getCount()==23,"Respawn inventories cannot share mutable stacks");
        helper.assertTrue(TGExtendedPlayer.find(oldPlayer).isEmpty(),"Cloned old player must be invalidated again");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void magazineUnloadsExactlyOne(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TGItems.PISTOL_MAGAZINE.get(), 2));
        TGItems.PISTOL_MAGAZINE.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        var inventory = TGExtendedPlayer.get(player).tg_inventory;
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "Unloading must consume exactly one magazine");
        helper.assertTrue(InventoryUtil.countItemInInv(inventory.inventory, TGItems.PISTOL_ROUNDS.get().getDefaultInstance(), 7, 15) == 3,
                "Original pistol magazine must return exactly 3 bullet items");
        helper.assertTrue(InventoryUtil.countItemInInv(inventory.inventory, TGItems.PISTOL_MAGAZINE_EMPTY.get().getDefaultInstance(), 7, 15) == 1,
                "Unloading must return one empty magazine");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void partialAmmoTransferDoesNotDuplicate(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        var inventory = TGExtendedPlayer.get(player).tg_inventory;
        for (int i = 7; i < 15; i++) inventory.setItem(i, new ItemStack(Items.COBBLESTONE, 64));
        inventory.setItem(7, new ItemStack(TGItems.PISTOL_ROUNDS.get(), 63));
        int remaining = InventoryUtil.addAmmoToPlayerInventory(player, new ItemStack(TGItems.PISTOL_ROUNDS.get(), 10));
        helper.assertTrue(remaining == 0, "All ten incoming rounds must fit");
        helper.assertTrue(inventory.getItem(7).getCount() == 64, "The ammo slot has room for one round");
        helper.assertTrue(InventoryUtil.countItemInInv(player.getInventory().items, TGItems.PISTOL_ROUNDS.get().getDefaultInstance(), 0, 36) == 9,
                "Only the remaining nine rounds may enter the ordinary inventory");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void rightClickMovesAmmoOutOfTheHand(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TGItems.RIFLE_ROUNDS.get(), 12));
        TGItems.RIFLE_ROUNDS.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "Transferred rounds must leave the hand");
        helper.assertTrue(InventoryUtil.countItemInInv(TGExtendedPlayer.get(player).tg_inventory.inventory,
                TGItems.RIFLE_ROUNDS.get().getDefaultInstance(), 7, 15) == 12, "Exactly twelve rounds must reach the ammo slots");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void playerDataRoundTripKeepsOwnerAndCopiesStacks(GameTestHelper helper) {
        var first = helper.makeMockSurvivalPlayer();
        var second = helper.makeMockSurvivalPlayer();
        var original = TGExtendedPlayer.get(first);
        original.enableJetpack = false; original.enableNightVision = true; original.enableSafemode = true;
        original.enableStepAssist = false; original.showTGHudElements = false; original.enableHovermode = true;
        original.foodleft = 5; original.lastSaturation = 0.8F; original.radlevel = 450;
        original.tg_inventory.setItem(0, TGItems.OXYGEN_MASK.get().getDefaultInstance());
        original.tg_inventory.setItem(7, new ItemStack(TGItems.PISTOL_ROUNDS.get(), 21));
        var tag = original.saveToNBT();
        helper.assertTrue(tag.getByte("states") == 38, "Original flag bit order must be preserved in saved NBT");
        var copy = TGExtendedPlayer.get(second);
        copy.loadFromNBT(tag);
        helper.assertTrue(copy.entity == second, "A respawned player's capability must keep the new entity as owner");
        helper.assertTrue(copy.foodleft == 5 && copy.lastSaturation == 0.8F && copy.radlevel == 450, "Food buffer and radiation must survive a save/load round trip");
        helper.assertTrue(copy.tg_inventory.getItem(0).is(TGItems.OXYGEN_MASK.get()), "Equipment must survive save/load");
        copy.tg_inventory.getItem(7).shrink(1);
        helper.assertTrue(original.tg_inventory.getItem(7).getCount() == 21, "Cloned inventories must not share mutable stacks");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void armorShiftClickTargetsTheHeadSlot(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        player.getInventory().setItem(9, Items.IRON_HELMET.getDefaultInstance());
        var menu = new TGPlayerInventoryContainer(1, player.getInventory());
        helper.assertTrue(!menu.quickMoveStack(player, 19).isEmpty(), "Shift-click must transfer the helmet");
        helper.assertTrue(player.getInventory().getArmor(3).is(Items.IRON_HELMET), "Helmet must equip to the head, not the boots slot");
        helper.assertTrue(player.getInventory().getArmor(0).isEmpty(), "Boots slot must remain empty");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void multiComponentReloadDoesNotConsumeOnFailure(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        var inventory = TGExtendedPlayer.get(player).tg_inventory;
        inventory.setItem(7, new ItemStack(TGItems.PISTOL_ROUNDS.get(), 3));
        boolean consumed = InventoryUtil.consumeAmmoPlayer(player, new ItemStack[]{
                new ItemStack(TGItems.PISTOL_ROUNDS.get(), 2), new ItemStack(TGItems.PISTOL_ROUNDS.get(), 2)});
        helper.assertFalse(consumed, "Two requirements of two rounds cannot be satisfied by three rounds");
        helper.assertTrue(inventory.getItem(7).getCount() == 3, "A failed reload must leave all ammunition intact");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void autofeederStoresExcessNutrition(GameTestHelper helper) {
        var player = new net.minecraftforge.common.util.FakePlayer(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "autofeeder-test"));
        var data = TGExtendedPlayer.get(player);
        player.getFoodData().setFoodLevel(19);
        player.getFoodData().setSaturation(0);
        data.tg_inventory.setItem(3, new ItemStack(Items.COOKED_BEEF, 2));
        TGAutoFoodHandler.tick(player, data);
        helper.assertTrue(player.getFoodData().getFoodLevel() == 20 && data.foodleft == 7,
                "Eating an eight-point steak at hunger 19 must store seven food points");
        helper.assertTrue(data.tg_inventory.getItem(3).getCount() == 1, "Only one steak may be consumed");
        player.getFoodData().setFoodLevel(18);
        TGAutoFoodHandler.tick(player, data);
        helper.assertTrue(player.getFoodData().getFoodLevel() == 20 && data.foodleft == 5,
                "The next hunger loss must use the saved food buffer");
        helper.assertTrue(data.tg_inventory.getItem(3).getCount() == 1, "Buffered feeding must not consume a second steak");
        helper.succeed();
    }
}
