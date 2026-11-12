package techguns.items;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import techguns.TGItems;
import techguns.api.tginventory.ITGSpecialSlot;
import techguns.api.tginventory.TGSlotType;
import techguns.items.armors.TGArmorBonus;
import techguns.items.guns.ammo.AmmoType;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import techguns.util.InventoryUtil;

/** One original itemshared metadata variant, flattened into its own modern registry entry. */
public final class SharedMaterialItem extends Item implements ITGSpecialSlot {
    private final int legacyMetadata;
    private final TGSlotType slot;
    private ItemStack bullet = ItemStack.EMPTY;
    private ItemStack emptyMagazine = ItemStack.EMPTY;
    private int bulletsPerMagazine;

    public SharedMaterialItem(int legacyMetadata, TGSlotType slot, Properties properties) {
        super(properties);
        this.legacyMetadata = legacyMetadata;
        this.slot = slot;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return techguns.client.render.item.SharedMaterialRenderer.get();
            }
        });
    }

    public int legacyMetadata() {
        return legacyMetadata;
    }

    public void setAmmoType(AmmoType ammo, String variant) {
        bullet = ammo.getBullet(ammo.getIDforVariantKey(variant))[0].copy();
        emptyMagazine = ammo.getEmptyMag()[0].copy();
        bulletsPerMagazine = ammo.getBulletsPerMag();
    }

    public ItemStack unloadedBullets() {
        ItemStack result = bullet.copy();
        result.setCount(bulletsPerMagazine);
        return result;
    }

    public ItemStack unloadedMagazine() {
        return emptyMagazine.copy();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (this == TGItems.BLUEPRINTS.get()) {
            if (!level.isClientSide && level.getServer() != null) {
                boolean fresh = techguns.items.blueprint.BlueprintKnowledge.get(level.getServer()).learn(player.getUUID());
                player.displayClientMessage(Component.translatable(fresh ? "techguns.blueprints.learned" : "techguns.blueprints.known")
                        .withStyle(fresh ? ChatFormatting.GREEN : ChatFormatting.GRAY), true);
                if (fresh) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    if (!player.getAbilities().instabuild) held.shrink(1);
                }
            }
            return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
        }
        if (player.isShiftKeyDown() && !bullet.isEmpty() && bulletsPerMagazine > 0) {
            if (!level.isClientSide) {
                // Only the logical server creates the returned items or consumes the magazine.
                held.shrink(1);
                giveOrDrop(player, unloadedBullets());
                giveOrDrop(player, unloadedMagazine());
            }
            return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
        }
        if (!player.isShiftKeyDown() && slot == TGSlotType.AMMOSLOT) {
            if (!level.isClientSide) held.setCount(InventoryUtil.addAmmoToAmmoInventory(player, held));
            return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
        }
        return super.use(level, player, hand);
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
        int remaining = InventoryUtil.addAmmoToPlayerInventory(player, stack);
        if (remaining > 0) player.drop(stack.copyWithCount(remaining), false);
    }

    @Override
    public TGSlotType getSlot(ItemStack stack) {
        return slot;
    }

    @Override
    public float getBonus(TGArmorBonus type, ItemStack stack, boolean consume, Player player) {
        if (this == TGItems.OXYGEN_MASK.get() && type == TGArmorBonus.OXYGEN_GEAR) return 1.0F;
        if (this == TGItems.WORKING_GLOVES.get() && type == TGArmorBonus.BREAKSPEED) return 0.10F;
        return 0.0F;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        super.appendHoverText(stack, level, tooltip, flags);
        if (this == TGItems.MACHINE_UPGRADE_STEAM.get()) tooltip.add(Component.translatable("techguns.tooltip.steamupgrade").withStyle(ChatFormatting.GRAY));
        if (this == TGItems.BLUEPRINTS.get()) tooltip.add(Component.translatable("techguns.tooltip.blueprints").withStyle(ChatFormatting.GRAY));
        if (slot != TGSlotType.NORMAL) {
            tooltip.add(Component.translatable("techguns.tooltip.slot")
                    .append(": ").append(slot.displayName()).withStyle(ChatFormatting.GRAY));
        }
        if (!bullet.isEmpty()) {
            tooltip.add(Component.translatable("techguns.tooltip.sneakrightclickunload").withStyle(ChatFormatting.GRAY));
        }
        if (slot == TGSlotType.AMMOSLOT) {
            tooltip.add(Component.translatable("techguns.tooltip.rightclickammoslot").withStyle(ChatFormatting.GRAY));
        }
        if (this == TGItems.OXYGEN_MASK.get()) {
            tooltip.add(Component.translatable("techguns.armorTooltip.oxygengear"));
        }
        if (this == TGItems.WORKING_GLOVES.get()) {
            tooltip.add(Component.translatable("techguns.armorTooltip.miningspeed").append(": +10.0%"));
        }
        if (slot == TGSlotType.DRILL_SMALL || slot == TGSlotType.DRILL_MEDIUM || slot == TGSlotType.DRILL_LARGE) {
            int offset = legacyMetadata - TGItems.OREDRILLHEAD_STEEL.get().legacyMetadata();
            tooltip.add(Component.translatable("techguns.oredrill.mininglevel").append(" +" + (offset % 3 + 1)));
        }
    }
}
