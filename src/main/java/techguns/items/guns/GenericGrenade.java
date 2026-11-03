package techguns.items.guns;
import java.util.function.Consumer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import techguns.*;
import techguns.capabilities.TGExtendedPlayer;
import techguns.entities.projectiles.*;
public final class GenericGrenade extends Item implements techguns.client.ext.ClientItemExtensible {
    public final float damage,innerRadius,minimum,outerRadius;
    public final float fullChargeTime=30;
    private final boolean frag;
    public GenericGrenade(boolean frag) {super(new Properties().stacksTo(16));this.frag=frag;damage=frag?12:10;innerRadius=frag?3.5F:3;minimum=frag?6:5;outerRadius=frag?7:5;}
    @Override public int getUseDuration(ItemStack stack, LivingEntity tgUser) {return 72000;}
    @Override public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext level,java.util.List<net.minecraft.network.chat.Component> tooltip,TooltipFlag flags) {
        tooltip.add(net.minecraft.network.chat.Component.translatable("techguns.gun.tooltip.damage").append(": "+damage));
        if(net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltip.add(net.minecraft.network.chat.Component.translatable("techguns.gun.tooltip.damageType").append(": ").append(techguns.api.damagesystem.DamageType.EXPLOSION.displayName()));
            tooltip.add(net.minecraft.network.chat.Component.translatable("techguns.gun.tooltip.radius").append(": "+innerRadius));
            tooltip.add(net.minecraft.network.chat.Component.translatable("techguns.gun.tooltip.bounces").append(": "+(frag?2:3)));
        }
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        player.startUsingItem(hand);
        if(frag&&!level.isClientSide)level.playSound(null,player.getX(),player.getEyeY(),player.getZ(),TGSounds.GRENADE_PIN.get(),net.minecraft.sounds.SoundSource.PLAYERS,1,1);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
    @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity entity,int remaining) {
        if(!(entity instanceof Player player)||player.isSpectator())return;
        float charge=Math.min(1,(getUseDuration(stack, null)-remaining)/fullChargeTime);
        if(!player.getAbilities().instabuild)stack.shrink(1);
        if(!level.isClientSide) {
            boolean left=(player.getUsedItemHand()==InteractionHand.OFF_HAND)==(player.getMainArm()==HumanoidArm.RIGHT);
            var projectile=new GrenadeProjectile(level,player,damage,0.75F,200,0.1F,innerRadius,outerRadius,minimum,0,!TGExtendedPlayer.get(player).enableSafemode,left?EnumBulletFirePos.LEFT:EnumBulletFirePos.RIGHT,frag?2:3,frag,false);
            // A same-tick release must remain finite; ordinary throws use the source's 0.015/charge gravity.
            projectile.setProjectileGravity(0.015F/Math.max(charge,1/30F));level.addFreshEntity(projectile);
        }
        player.swing(player.getUsedItemHand());
    }
    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){@Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){return techguns.client.render.item.GrenadeItemRenderer.get();}});
    }
}
