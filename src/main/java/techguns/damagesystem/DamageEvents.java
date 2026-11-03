package techguns.damagesystem;

import java.util.ArrayDeque;
import java.lang.reflect.Field;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import techguns.Techguns;
import techguns.api.npc.INpcTGDamageSystem;

@EventBusSubscriber(modid = Techguns.MODID)
public final class DamageEvents {
    private record ActiveHit(LivingEntity target, TGDamageSource source) {}
    private static final ThreadLocal<ArrayDeque<ActiveHit>> HITS = ThreadLocal.withInitial(ArrayDeque::new);
    private static final Field LAST_HURT = ObfuscationReflectionHelper.findField(LivingEntity.class, "lastHurt");

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onAttack(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;
        var hits = HITS.get();
        if (!hits.isEmpty() && hits.peek().target == event.getEntity() && hits.peek().source == event.getSource()) return;
        if (!(event.getSource() instanceof TGDamageSource) && !(event.getEntity() instanceof INpcTGDamageSystem) && !DamageSystem.hasSuitArmor(event.getEntity())) {
            if (hits.isEmpty()) HITS.remove();
            return;
        }
        var source = TGDamageSource.getFromGenericDamageSource(event.getSource());
        // Explosions may share a source across targets; never leak one target's armor mode into the next hit.
        var hitSource = source.copyForHit(event.getEntity() instanceof INpcTGDamageSystem);
        event.setCanceled(true);
        hits.push(new ActiveHit(event.getEntity(), hitSource));
        var target = event.getEntity();
        int invulnerableTime = target.invulnerableTime, hurtTime = target.hurtTime, hurtDuration = target.hurtDuration;
        float lastHurt = readLastHurt(target);
        try {
            if (target.hurt(hitSource, event.getAmount())) source.setAttackSuccessful();
        } finally {
            // In the original fast-hit path these fields are never changed. Merely tagging a modern source
            // BYPASSES_COOLDOWN still starts a new cooldown and suppresses a following ordinary melee attack.
            if (source.ignoreHurtresistTime) {
                target.invulnerableTime = invulnerableTime; target.hurtTime = hurtTime; target.hurtDuration = hurtDuration;
                writeLastHurt(target, lastHurt);
            }
            hits.pop();
            if (hits.isEmpty()) HITS.remove();
        }
    }

    private static float readLastHurt(LivingEntity entity) {
        try { return LAST_HURT.getFloat(entity); }
        catch (IllegalAccessException error) { throw new IllegalStateException("Cannot read original hurt cooldown state", error); }
    }
    private static void writeLastHurt(LivingEntity entity, float value) {
        try { LAST_HURT.setFloat(entity, value); }
        catch (IllegalAccessException error) { throw new IllegalStateException("Cannot restore original hurt cooldown state", error); }
    }

    /** 1.20.1 LivingHurtEvent: runs after onAttack (which cancels and re-enters hurt with the Techguns source). */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onHurt(LivingIncomingDamageEvent event) {
        if (event.getSource() instanceof TGDamageSource source && source.usesNpcArmor()) {
            event.setAmount(DamageSystem.applyNpcArmor(event.getEntity(), source, event.getAmount(), source.originallyBypassesArmor()));
        }
        else if(event.getSource() instanceof TGDamageSource source&&event.getEntity() instanceof net.minecraft.world.entity.player.Player player&&DamageSystem.hasSuitArmor(player))event.setAmount(DamageSystem.applySuitArmor(player,source,event.getAmount()));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (event.getDamageSource() instanceof TGDamageSource source && !source.wasConverted) {
            float incoming = event.getOriginalBlockedDamage();
            event.setBlockedDamage(incoming - DamageSystem.calculateShieldDamage(event.getEntity(), incoming, source));
            ShieldStats.playBlockSound(event.getEntity(), source);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onKnockback(LivingKnockBackEvent event) {
        var hits = HITS.get();
        if (!hits.isEmpty() && hits.peek().target == event.getEntity()) {
            event.setStrength(event.getStrength() * hits.peek().source.knockbackMultiplier);
        }
        if (hits.isEmpty()) HITS.remove();
    }
}
