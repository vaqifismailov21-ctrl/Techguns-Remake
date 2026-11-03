package techguns;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import techguns.items.guns.*;
import techguns.damagesystem.TGDamageSource;
import techguns.api.damagesystem.DamageType;
import techguns.deatheffects.EntityDeathUtils.DeathType;
/** Original Steel tier: harvest 2, durability 1000, speed 7.5, damage bonus 2.5, enchantability 12. */
public final class TGSteelTools {
    /** 1.21.1: tiers are defined by the blocks they cannot mine; steel mines like iron (harvest level 2). */
    private static final Tier STEEL=new net.neoforged.neoforge.common.SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL,1000,7.5F,2.5F,12,()->Ingredient.of(TGItems.INGOT_STEEL.get()));
    public static final DeferredHolder<net.minecraft.world.item.Item, Item> COMBAT_KNIFE=TGItems.ITEMS.register("combatknife",Knife::new);
    public static final DeferredHolder<net.minecraft.world.item.Item, Item> CROWBAR=TGItems.ITEMS.register("crowbar",Crowbar::new);
    private static TGDamageSource source(Player player){var source=new TGDamageSource(player.level(),"tg_melee",player,player,DamageType.PHYSICAL,DeathType.GORE);source.goreChance=0.25F;return source;}
    private static final class Knife extends SwordItem implements LegacyMeleeWeapon {
        Knife(){super(STEEL,new Properties().attributes(SwordItem.createAttributes(STEEL,3,-2.4F)));}
        @Override public TGDamageSource getMeleeDamageSource(Player player,ItemStack stack){return source(player);}
        @Override public boolean onLeftClickEntity(ItemStack stack,Player player,Entity target){return LegacyMeleeAttack.attack(this,stack,player,target);}
    }
    private static final class Crowbar extends PickaxeItem implements LegacyMeleeWeapon {
        Crowbar(){super(STEEL,new Properties().attributes(DiggerItem.createAttributes(STEEL,1,-2.8F)));}
        @Override public TGDamageSource getMeleeDamageSource(Player player,ItemStack stack){return source(player);}
        @Override public boolean hasSwordSweep(){return false;}
        @Override public boolean onLeftClickEntity(ItemStack stack,Player player,Entity target){return LegacyMeleeAttack.attack(this,stack,player,target);}
        @Override public boolean doesSneakBypassUse(ItemStack stack,net.minecraft.world.level.LevelReader level,net.minecraft.core.BlockPos pos,Player player){return true;}
        @Override public boolean canPerformAction(ItemStack stack,ItemAbility action){return action.name().equals("wrench")||super.canPerformAction(stack,action);}
    }
    public static void init(){}
    private TGSteelTools(){}
}
