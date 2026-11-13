package techguns.items.armors;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import techguns.api.damagesystem.DamageType;
import techguns.damagesystem.ShieldStats;

public class GenericShield extends ShieldItem implements ICamoChangeable {
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    private final String id;
    private final int camocount, repairMatCount;
    private final TagKey<Item> repairMat;
    private final ShieldStats stats;

    public GenericShield(String id, int durability, int camos, ResourceLocation repairTag, int repairCount, ShieldStats stats) {
        super(new Item.Properties().durability(durability));
        this.id = id; camocount = Math.max(camos, 1); repairMatCount = repairCount; this.stats = stats;
        repairMat = TagKey.create(net.minecraft.core.registries.Registries.ITEM, repairTag);
    }
    public ShieldStats getShieldStats() { return stats; }
    @Override public int getCamoCount() { return camocount; }
    @Override public Component getCurrentCamoName(ItemStack stack) {
        int index = getCurrentCamoIndex(stack);
        return Component.translatable(index > 0 ? "item.techguns." + id + ".camoname." + index : "techguns.item.defaultcamo");
    }
    @Override public Component getName(ItemStack stack) { return Component.translatable("item.techguns." + id); }
    @Override public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return stack.getDamageValue() < stack.getMaxDamage() && ToolActions.DEFAULT_SHIELD_ACTIONS.contains(action);
    }
    @Override public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return ingredient.is(repairMat) || super.isValidRepairItem(stack, ingredient);
    }
    public boolean canRepairOnRepairBench(ItemStack stack) { return repairMatCount > 0; }
    public ArrayList<ItemStack> getRepairMats(ItemStack stack) {
        var result = new ArrayList<ItemStack>();
        if (stack.getDamageValue() <= 0) return result;
        float fraction = stack.getDamageValue() / (float) (stack.getMaxDamage() - 1);
        int count = (int) Math.ceil(repairMatCount * fraction);
        // Tag contents are only resolved after datapacks load, never during item registration.
        BuiltInRegistries.ITEM.getTag(repairMat).ifPresent(entries -> entries.stream().findFirst()
                .ifPresent(holder -> result.add(new ItemStack(holder, count))));
        return result;
    }
    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flags) {
        // Techguns shields do not use vanilla banner-pattern names/tooltips.
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("techguns.armorTooltip.durability").append(": " + (stack.getMaxDamage() - stack.getDamageValue()) + "/" + stack.getMaxDamage()));
            tooltip.add(Component.translatable("techguns.armorTooltip.resistances").withStyle(ChatFormatting.BLUE));
            // Techguns CE: reduction in armour points (rate x 24) and their share of 25 points.
            var percent = new java.text.DecimalFormat("#.##", java.text.DecimalFormatSymbols.getInstance(java.util.Locale.ENGLISH));
            for (var type : new DamageType[]{DamageType.PHYSICAL, DamageType.PROJECTILE, DamageType.EXPLOSION, DamageType.ENERGY,
                    DamageType.FIRE, DamageType.LIGHTNING, DamageType.POISON, DamageType.RADIATION}) {
                float value = (float) (stats.getReductionPercentAgainstType(type) / 100d * 24);
                tooltip.add(type.displayName().copy().append(": " + FORMAT.format(value) + " (" + percent.format(value / 25 * 100) + "%)"));
            }
        } else {
            tooltip.add(Component.translatable("techguns.gun.tooltip.shift1").append(" ")
                    .append(Component.translatable("techguns.gun.tooltip.shift2").withStyle(ChatFormatting.GREEN)).append(" ")
                    .append(Component.translatable("techguns.gun.tooltip.shift3").withStyle(ChatFormatting.GRAY)));
        }
    }
    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return techguns.client.render.item.ShieldItemRenderer.get();
            }
        });
    }
}
