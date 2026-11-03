package techguns.damagesystem;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import techguns.TGConfig;
import techguns.api.damagesystem.DamageMath;
import techguns.api.damagesystem.DamageType;
import techguns.api.npc.INpcTGDamageSystem;
import techguns.api.npc.ITGTurretDamage;

/** Original rules over the current Forge hurt pipeline, retaining totems, attribution and other mods' hooks. */
public final class DamageSystem {
    private DamageSystem() {}

    public static float getDamageFactor(LivingEntity attacker, LivingEntity target) {
        if (attacker instanceof Player && target instanceof Player) {
            var server = target.getServer();
            return server != null && server.isPvpAllowed() ? TGConfig.damagePvP : 0;
        }
        if (target instanceof Player) return attacker instanceof ITGTurretDamage
                ? TGConfig.damageTurretToPlayer : TGConfig.damageFactorNPC;
        return attacker instanceof Player ? 1 : TGConfig.damageFactorNPC;
    }

    public static float getArmorAgainstDamageTypeDefault(LivingEntity entity, float armor, DamageType type) {
        return switch (type) {
            case PHYSICAL, PROJECTILE -> armor;
            case EXPLOSION, ENERGY, ICE, LIGHTNING, DARK -> armor * 0.5F;
            case FIRE -> armor * (entity.fireImmune() ? 2 : 0.5F);
            case POISON, RADIATION, UNRESISTABLE -> 0;
        };
    }

    public static double getDamageAfterAbsorb_TGFormula(float damage, float totalArmor, float toughness, float penetration) {
        return DamageMath.afterArmor(damage, totalArmor, toughness, penetration);
    }

    public static float calculateShieldDamage(LivingEntity entity, float amount, TGDamageSource source) {
        var stats = ShieldStats.getStats(entity.getUseItem(), entity);
        return stats == null ? amount : stats.getAmount(amount, source);
    }

    public static float applyNpcArmor(LivingEntity entity, TGDamageSource source, float amount, boolean bypassArmor) {
        if (bypassArmor || !(entity instanceof INpcTGDamageSystem stats)) return amount;
        // The original NPC path uses raw toughness and four times the per-part penetration.
        // Its deprecated penetration-resistance helper is not called here in the original either.
        return (float) getDamageAfterAbsorb_TGFormula(amount, stats.getTotalArmorAgainstType(source),
                (float) entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS), source.armorPenetration * 4);
    }

    public static boolean attackEntityFrom(LivingEntity entity, DamageSource source, float amount) {
        var typed = TGDamageSource.getFromGenericDamageSource(source);
        entity.hurt(typed, amount);
        return typed.wasSuccessful();
    }

    /** Vanilla equipment contribution; TG armor support is added with its item implementation. */
    public static float getTotalArmorAgainstType(Player player, DamageType type) {
        float total = 0;
        for (ItemStack stack : player.getArmorSlots()) {
            if(stack.getItem() instanceof techguns.items.armors.TGSuitArmor armor)total+=armor.armor(stack,type);
            else if (type == DamageType.PHYSICAL && stack.getItem() instanceof ArmorItem armor) total += armor.getDefense();
        }
        return total;
    }
    public static boolean hasSuitArmor(LivingEntity entity){for(var stack:entity.getArmorSlots())if(stack.getItem() instanceof techguns.items.armors.TGSuitArmor)return true;return false;}
    /** Source ISpecialArmor ratios share priority zero; each piece applies its own toughness/penetration. */
    public static float applySuitArmor(Player player,TGDamageSource source,float amount){
        if(source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR))return amount;
        double total=0;
        for(var stack:player.getArmorSlots())if(stack.getItem() instanceof techguns.items.armors.TGSuitArmor armor){
            double ratio=1-getDamageAfterAbsorb_TGFormula(1,armor.armor(stack,source.damageType),armor.stats.toughness,source.armorPenetration);
            total+=ratio;
            if(ratio>0&&!player.getAbilities().instabuild)stack.hurtAndBreak((int)Math.max(1,amount*ratio),player,p->{});
        }
        return amount*(float)(1-Math.min(1,total));
    }
}
