package techguns.damagesystem;

import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import techguns.api.damagesystem.DamageType;
import techguns.deatheffects.EntityDeathUtils.DeathType;

/**
 * Original TG categories and per-hit flags. 1.19.2 has no data-driven damage types yet: the flags of the 1.20.1 damage
 * type data and tags (data/techguns/damage_type, data/minecraft/tags/damage_type) are set on the source directly.
 */
public class TGDamageSource extends DamageSource {
    public Entity attacker;
    public DamageType damageType;
    public DeathType deathType;
    public float goreChance = 0.5F, armorPenetration, knockbackMultiplier = 1.0F;
    public boolean knockbackOnShieldBlock = true, ignoreHurtresistTime, wasConverted;
    private boolean attackSuccessful;
    private boolean npcArmor, originallyBypassesArmor;
    private final DamageSource original;
    private final Entity direct;
    private static final Set<String> UNRESISTABLE = Set.of("inWall", "drown", "starve", "fall", "outOfWorld");
    private static final Set<String> ITEM_MESSAGES = Set.of("tg_bullet", "tg_explosion", "tg_fire", "tg_poison", "powerhammer", "tg_lightning", "tg_energy");

    public TGDamageSource(Level level, String name, Entity direct, Entity attacker, DamageType category, DeathType death) {
        this(name, direct, attacker, category, death, null);
    }
    public TGDamageSource(String name, Entity direct, Entity attacker, DamageType category, DeathType death) {
        this(name, direct, attacker, category, death, null);
    }
    private TGDamageSource(String name, Entity direct, Entity attacker, DamageType category, DeathType death, DamageSource original) {
        super(name);
        this.direct = direct; this.attacker = attacker; damageType = category; deathType = death; this.original = original;
        if (original == null) {
            // Flags of the 1.20.1 damage type tags: explosion and projectile categories, unresistable radiation poisoning.
            if (category == DamageType.EXPLOSION) setExplosion();
            if (category == DamageType.PROJECTILE) setProjectile();
            if (category == DamageType.UNRESISTABLE) { bypassArmor(); bypassMagic(); }
        } else {
            if (original.isBypassArmor()) bypassArmor();
            if (original.isBypassInvul()) bypassInvul();
            if (original.isBypassMagic()) bypassMagic();
            if (original.isFire()) setIsFire();
            if (original.isProjectile()) setProjectile();
            if (original.isExplosion()) setExplosion();
            if (original.isMagic()) setMagic();
            if (original.isFall()) setIsFall();
            if (original.isDamageHelmet()) damageHelmet();
            if (original.isNoAggro()) setNoAggro();
        }
    }
    public TGDamageSource(DamageSource source) {
        this(source.getMsgId(), source.getDirectEntity(), source.getEntity(), determineType(source), DeathType.DEFAULT, source);
        wasConverted = true;
        if (source.isProjectile()) knockbackOnShieldBlock = false;
    }
    private static TGDamageSource fast(Level level, String name, Entity direct, Entity attacker, DamageType type, DeathType death) {
        TGDamageSource source = new TGDamageSource(level, name, direct, attacker, type, death);
        source.ignoreHurtresistTime = true; source.knockbackOnShieldBlock = false;
        return source;
    }
    private static Level levelFor(Entity direct, Entity attacker) { return direct != null ? direct.level : attacker != null ? attacker.level : null; }
    public static TGDamageSource causeBulletDamage(Entity direct, Entity attacker, DeathType death) { return fast(levelFor(direct, attacker), "tg_bullet", direct, attacker, DamageType.PROJECTILE, death); }
    public static TGDamageSource causeExplosionDamage(Entity direct, Entity attacker, DeathType death) { return new TGDamageSource("tg_explosion", direct, attacker, DamageType.EXPLOSION, death); }
    public static TGDamageSource causePoisonDamage(Entity direct, Entity attacker, DeathType death) { return fast(levelFor(direct, attacker), "tg_poison", direct, attacker, DamageType.POISON, death); }
    public static TGDamageSource causeFireDamage(Entity direct, Entity attacker, DeathType death) { return fast(levelFor(direct, attacker), "tg_fire", direct, attacker, DamageType.FIRE, death); }
    public static TGDamageSource causeEnergyDamage(Entity direct, Entity attacker, DeathType death) { return fast(levelFor(direct, attacker), "tg_energy", direct, attacker, DamageType.ENERGY, death); }
    public static TGDamageSource causeLightningDamage(Entity direct, Entity attacker, DeathType death) { return fast(levelFor(direct, attacker), "tg_lightning", direct, attacker, DamageType.LIGHTNING, death); }
    public static TGDamageSource causeDarkDamage(Entity direct, Entity attacker, DeathType death) { return fast(levelFor(direct, attacker), "tg_dark", direct, attacker, DamageType.DARK, death); }
    public static TGDamageSource causeRadiationDamage(Level level, Entity direct, Entity attacker, DeathType death) { return fast(level, "tg_rad", direct, attacker, DamageType.RADIATION, death); }
    public static TGDamageSource causeRadiationDamage(Entity direct, Entity attacker, DeathType death) { return causeRadiationDamage(levelFor(direct, attacker), direct, attacker, death); }
    public static TGDamageSource causeLethalRadPoisoningDamage(Level level, Entity direct, Entity attacker, DeathType death) {
        TGDamageSource source = fast(level, "tg_rad_poisoning", direct, attacker, DamageType.UNRESISTABLE, death);
        source.goreChance = 1.0F; return source;
    }
    public static TGDamageSource causeLethalRadPoisoningDamage(Entity direct, Entity attacker, DeathType death) { return causeLethalRadPoisoningDamage(levelFor(direct, attacker), direct, attacker, death); }
    public static TGDamageSource getKnockbackDummyDmgSrc(Entity direct, Entity attacker) { return new TGDamageSource("tg_knockback", direct, attacker, DamageType.PHYSICAL, DeathType.DEFAULT); }
    public static TGDamageSource getFromGenericDamageSource(DamageSource source) { return source instanceof TGDamageSource tg ? tg : new TGDamageSource(source); }

    public void setAttackSuccessful() { attackSuccessful = true; }
    public boolean wasSuccessful() { return attackSuccessful; }
    public TGDamageSource setNoKnockback() { return setKnockback(0); }
    public TGDamageSource setKnockback(float multiplier) { knockbackMultiplier = multiplier; return this; }
    public boolean hasKnockback() { return knockbackMultiplier > 0; }
    public boolean knockbackOnShieldBlock() { return knockbackOnShieldBlock; }
    public TGDamageSource setKnockbackOnShieldBlock(boolean enabled) { knockbackOnShieldBlock = enabled; return this; }
    public String getDamageType() { return getMsgId(); }
    public Entity getTrueSource() { return attacker; }
    public Entity getImmediateSource() { return getDirectEntity(); }
    @Override public Entity getEntity() { return attacker; }
    @Override public Entity getDirectEntity() { return direct; }
    /** 1.20.1 damage type scaling "when_caused_by_living_non_player". */
    @Override public boolean scalesWithDifficulty() { return wasConverted && original != null ? original.scalesWithDifficulty() : attacker instanceof LivingEntity && !(attacker instanceof net.minecraft.world.entity.player.Player); }
    /** 1.20.1 tags: NPC armor handles armor itself; unresistable radiation poisoning bypasses armor. */
    @Override public boolean isBypassArmor() { return npcArmor || super.isBypassArmor(); }
    @Override public net.minecraft.world.phys.Vec3 getSourcePosition() { return wasConverted && original != null ? original.getSourcePosition() : super.getSourcePosition(); }
    @Override public float getFoodExhaustion() { return !wasConverted && damageType == DamageType.UNRESISTABLE ? 0 : super.getFoodExhaustion(); }
    @Override public Component getLocalizedDeathMessage(LivingEntity victim) {
        if (wasConverted && original != null) return original.getLocalizedDeathMessage(victim);
        Entity credited = attacker == null ? getDirectEntity() : attacker;
        Component name = credited == null ? victim.getDisplayName() : credited.getDisplayName();
        ItemStack weapon = attacker instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
        String key = "death.attack." + getMsgId();
        if (weapon.hasCustomHoverName() && ITEM_MESSAGES.contains(getMsgId())) return Component.translatable(key + ".item", victim.getDisplayName(), name, weapon.getDisplayName());
        return Component.translatable(key, victim.getDisplayName(), name);
    }
    public static TGDamageSource copyWithNewEnt(TGDamageSource other, Entity direct, Entity attacker) {
        TGDamageSource copy = new TGDamageSource(other.getMsgId(), direct, attacker, other.damageType, other.deathType, null);
        copy.knockbackMultiplier = other.knockbackMultiplier; copy.armorPenetration = other.armorPenetration;
        copy.goreChance = other.goreChance; copy.ignoreHurtresistTime = other.ignoreHurtresistTime;
        return copy;
    }
    public TGDamageSource copyForHit(boolean npcArmor) {
        TGDamageSource copy = new TGDamageSource(getMsgId(), getDirectEntity(), attacker, damageType, deathType, original);
        copy.knockbackMultiplier = knockbackMultiplier; copy.armorPenetration = armorPenetration;
        copy.goreChance = goreChance; copy.ignoreHurtresistTime = ignoreHurtresistTime;
        copy.knockbackOnShieldBlock = knockbackOnShieldBlock; copy.wasConverted = wasConverted;
        copy.originallyBypassesArmor = isBypassArmor();
        copy.npcArmor = npcArmor;
        return copy;
    }
    public boolean usesNpcArmor() { return npcArmor; }
    public boolean originallyBypassesArmor() { return originallyBypassesArmor; }
    private static DamageType determineType(DamageSource source) {
        if (source.isExplosion()) return DamageType.EXPLOSION;
        if (source.isMagic()) return DamageType.ENERGY;
        if (source.isFire() || source == DamageSource.DRAGON_BREATH) return DamageType.FIRE;
        if (source.isProjectile()) return DamageType.PROJECTILE;
        if (source == DamageSource.WITHER) return DamageType.POISON;
        if (source == DamageSource.LIGHTNING_BOLT) return DamageType.LIGHTNING;
        if (source.isBypassInvul() || UNRESISTABLE.contains(source.getMsgId())) return DamageType.UNRESISTABLE;
        return DamageType.PHYSICAL;
    }
}
