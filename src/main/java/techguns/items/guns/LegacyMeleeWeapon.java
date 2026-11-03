package techguns.items.guns;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.damagesystem.TGDamageSource;
/** Shared source attack hooks for powered tools and steel melee tools. */
public interface LegacyMeleeWeapon {
    default boolean shootsWithLeftClick(){return false;}
    default void animateMelee(Player player){}
    default boolean hasSwordSweep(){return true;}
    TGDamageSource getMeleeDamageSource(Player player,ItemStack stack);
    default void consumeAmmoOnMeleeHit(LivingEntity player,ItemStack stack){}
    default void onMeleeHitTarget(ItemStack stack,Entity target){}
    default int getCurrentAmmo(ItemStack stack){return 1;}
    default void doSweepAttackEffect(Player player){player.sweepAttack();player.level().playSound(null,player.getX(),player.getY(),player.getZ(),net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,player.getSoundSource(),1,1);}
}
