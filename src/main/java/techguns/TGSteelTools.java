package techguns;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.*;
import net.minecraftforge.registries.RegistryObject;
import techguns.items.guns.*;
import techguns.damagesystem.TGDamageSource;
import techguns.api.damagesystem.DamageType;
import techguns.deatheffects.EntityDeathUtils.DeathType;
/** Original Steel tier: harvest 2, durability 1000, speed 7.5, damage bonus 2.5, enchantability 12. */
public final class TGSteelTools {
    private static final Tier STEEL=TierSortingRegistry.registerTier(new ForgeTier(2,1000,7.5F,2.5F,12,BlockTags.NEEDS_IRON_TOOL,()->Ingredient.of(TGItems.INGOT_STEEL.get())),new ResourceLocation("techguns","steel"),java.util.List.of(Tiers.IRON),java.util.List.of(Tiers.DIAMOND));
    public static final RegistryObject<Item> COMBAT_KNIFE=TGItems.ITEMS.register("combatknife",Knife::new);
    public static final RegistryObject<Item> CROWBAR=TGItems.ITEMS.register("crowbar",Crowbar::new);
    private static TGDamageSource source(Player player){var source=new TGDamageSource(player.level,"tg_melee",player,player,DamageType.PHYSICAL,DeathType.GORE);source.goreChance=0.25F;return source;}
    private static final class Knife extends SwordItem implements LegacyMeleeWeapon {
        Knife(){super(STEEL,3,-2.4F,new Properties());}
        @Override public TGDamageSource getMeleeDamageSource(Player player,ItemStack stack){return source(player);}
        @Override public boolean onLeftClickEntity(ItemStack stack,Player player,Entity target){return LegacyMeleeAttack.attack(this,stack,player,target);}
    }
    private static final class Crowbar extends PickaxeItem implements LegacyMeleeWeapon {
        Crowbar(){super(STEEL,1,-2.8F,new Properties());}
        @Override public TGDamageSource getMeleeDamageSource(Player player,ItemStack stack){return source(player);}
        @Override public boolean hasSwordSweep(){return false;}
        @Override public boolean onLeftClickEntity(ItemStack stack,Player player,Entity target){return LegacyMeleeAttack.attack(this,stack,player,target);}
        @Override public boolean doesSneakBypassUse(ItemStack stack,net.minecraft.world.level.LevelReader level,net.minecraft.core.BlockPos pos,Player player){return true;}
        @Override public boolean canPerformAction(ItemStack stack,ToolAction action){return action.name().equals("wrench")||super.canPerformAction(stack,action);}
    }
    public static void init(){}
    private TGSteelTools(){}
}
