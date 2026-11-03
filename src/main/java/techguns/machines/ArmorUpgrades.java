package techguns.machines;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import techguns.*;
import techguns.items.armors.TGSuitArmor;
public final class ArmorUpgrades {
    public record Upgrade(Item ingredient,net.minecraft.resources.ResourceKey<Enchantment> enchantment,int level){}
    public record Result(ItemStack stack,int xp){}
    private static List<Upgrade> recipes;
    public static List<Upgrade> all(){if(recipes==null)recipes=List.of(
        new Upgrade(TGItems.UPGRADE_PROTECTION_1.get(),Enchantments.PROTECTION,1),new Upgrade(TGItems.UPGRADE_PROTECTION_2.get(),Enchantments.PROTECTION,2),new Upgrade(TGItems.UPGRADE_PROTECTION_3.get(),Enchantments.PROTECTION,3),
        new Upgrade(TGItems.UPGRADE_PROJECTILE_PROTECTION_1.get(),Enchantments.PROJECTILE_PROTECTION,1),new Upgrade(TGItems.UPGRADE_PROJECTILE_PROTECTION_2.get(),Enchantments.PROJECTILE_PROTECTION,2),new Upgrade(TGItems.UPGRADE_PROJECTILE_PROTECTION_3.get(),Enchantments.PROJECTILE_PROTECTION,3),
        new Upgrade(TGItems.UPGRADE_BLAST_PROTECTION_1.get(),Enchantments.BLAST_PROTECTION,1),new Upgrade(TGItems.UPGRADE_BLAST_PROTECTION_2.get(),Enchantments.BLAST_PROTECTION,2),new Upgrade(TGItems.UPGRADE_BLAST_PROTECTION_3.get(),Enchantments.BLAST_PROTECTION,3));return recipes;}
    public static Upgrade recipe(ItemStack ingredient){return all().stream().filter(r->ingredient.is(r.ingredient)).findFirst().orElse(null);}
    /** 1.21.1: enchantments are data; the holder comes from the registry (old code: Enchantment#canEnchant + EnchantmentHelper map). */
    public static Result result(ItemStack armor,ItemStack ingredient){var recipe=recipe(ingredient);if(!(armor.getItem() instanceof TGSuitArmor)||recipe==null)return new Result(ItemStack.EMPTY,0);
        var holder=techguns.util.TGItemNbt.registries().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).get(recipe.enchantment).orElse(null);
        if(holder==null||!holder.value().canEnchant(armor))return new Result(ItemStack.EMPTY,0);
        int old=EnchantmentHelper.getItemEnchantmentLevel(holder,armor);if(old>=recipe.level)return new Result(ItemStack.EMPTY,0);var output=armor.copyWithCount(1);
        EnchantmentHelper.updateEnchantments(output,m->m.set(holder,recipe.level));return new Result(output,TGConfig.upgrade_xp_cost*(recipe.level-old));}
    private ArmorUpgrades(){}
}
