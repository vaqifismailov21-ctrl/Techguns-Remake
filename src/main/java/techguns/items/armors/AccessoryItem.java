package techguns.items.armors;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import techguns.*;
import techguns.api.tginventory.*;
import techguns.capabilities.TGExtendedPlayer;
import techguns.util.InventoryUtil;

/** The original eight additional-slot devices, retaining their separate fuel/durability semantics. */
public final class AccessoryItem extends Item implements ITGSpecialSlot,ICamoChangeable {
    public enum Kind {GAS_MASK,GLIDER,JUMPPACK,SCUBA,NIGHTVISION,JETPACK,TACTICAL_MASK,ANTIGRAV}
    public final Kind kind;public final int camos;
    public AccessoryItem(Kind kind,int camos,int durability){super(properties(durability));this.kind=kind;this.camos=camos;}
    // The durability bar is fuel, air, filter or charge, refilled with the matching item: no enchanting (Mending, Unbreaking) and no anvil repair.
    @Override public boolean isEnchantable(net.minecraft.world.item.ItemStack stack){return false;}
    @Override public boolean isBookEnchantable(net.minecraft.world.item.ItemStack stack,net.minecraft.world.item.ItemStack book){return false;}
    @Override public boolean canApplyAtEnchantingTable(net.minecraft.world.item.ItemStack stack,net.minecraft.world.item.enchantment.Enchantment enchantment){return false;}
    @Override public boolean isRepairable(net.minecraft.world.item.ItemStack stack){return false;}
    private static Properties properties(int durability){var p=new Properties().stacksTo(1);if(durability>0)p.durability(durability);return p;}
    @Override public TGSlotType getSlot(ItemStack stack){return kind==Kind.GAS_MASK||kind==Kind.NIGHTVISION||kind==Kind.TACTICAL_MASK?TGSlotType.FACESLOT:TGSlotType.BACKSLOT;}
    @Override public int getCamoCount(){return camos;}
    @Override public Component getCurrentCamoName(ItemStack stack){int i=getCurrentCamoIndex(stack);return i==0?Component.translatable("techguns.item.defaultcamo"):Component.translatable(getDescriptionId()+".camoname."+i);}
    public ItemStack ammo(){return switch(kind){case JUMPPACK,SCUBA->TGItems.COMPRESSED_AIR_TANK.get().getDefaultInstance();case NIGHTVISION->TGItems.REDSTONE_BATTERY.get().getDefaultInstance();case TACTICAL_MASK->TGItems.ENERGY_CELL.get().getDefaultInstance();case JETPACK->TGItems.FUEL_TANK.get().getDefaultInstance();case ANTIGRAV->TGItems.NUCLEAR_POWERCELL.get().getDefaultInstance();default->ItemStack.EMPTY;};}
    public ItemStack emptyAmmo(){return switch(kind){case JUMPPACK,SCUBA->TGItems.COMPRESSED_AIR_TANK_EMPTY.get().getDefaultInstance();case NIGHTVISION->TGItems.REDSTONE_BATTERY_EMPTY.get().getDefaultInstance();case TACTICAL_MASK->TGItems.ENERGY_CELL_EMPTY.get().getDefaultInstance();case JETPACK->TGItems.FUEL_TANK_EMPTY.get().getDefaultInstance();case ANTIGRAV->TGItems.NUCLEAR_POWERCELL_EMPTY.get().getDefaultInstance();default->ItemStack.EMPTY;};}
    public boolean reload(ItemStack stack,Player p){
        if(ammo().isEmpty()||!InventoryUtil.consumeAmmoPlayer(p,new ItemStack[]{ammo()}))return false;
        stack.setDamageValue(0);var empty=emptyAmmo();if(!empty.isEmpty()){int left=InventoryUtil.addAmmoToPlayerInventory(p,empty);if(left>0&&!p.level().isClientSide)p.drop(empty.copyWithCount(left),false);}return true;
    }
    private static boolean ready(ItemStack stack){return !stack.isDamageableItem()||stack.getDamageValue()<stack.getMaxDamage();}
    @Override public void onPlayerTick(ItemStack stack,Player p){
        var data=TGExtendedPlayer.find(p).orElse(null);if(data==null)return;
        if(kind==Kind.GLIDER){glide(p);return;}
        boolean emptyBeforeTick=!ready(stack);
        if(kind!=Kind.GAS_MASK&&(stack.getDamageValue()>=stack.getMaxDamage()||kind==Kind.ANTIGRAV&&stack.getDamageValue()+1>=stack.getMaxDamage()))reload(stack,p);
        // In the original goggles and tactical mask, refilling is the alternative to consuming power this tick.
        if(emptyBeforeTick&&(kind==Kind.NIGHTVISION||kind==Kind.TACTICAL_MASK))return;
        if(kind==Kind.GAS_MASK||kind==Kind.TACTICAL_MASK){
            for(var effect:new net.minecraft.world.effect.MobEffect[]{MobEffects.POISON,MobEffects.WITHER})if(ready(stack)&&p.hasEffect(effect)){p.removeEffect(effect);stack.setDamageValue(stack.getDamageValue()+1);}
            if(kind==Kind.GAS_MASK){var attribute=p.getAttribute(techguns.api.radiation.TGRadiation.RADIATION_RESISTANCE.get());if(attribute!=null){var id=java.util.UUID.fromString("1B428AAE-2ECA-42C6-A14F-0A8B3F507276");if(attribute.getModifier(id)==null)attribute.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id,"techguns.radresist.faceslot",1,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));}}
        }
        if(ready(stack)&&(kind==Kind.NIGHTVISION||kind==Kind.TACTICAL_MASK)&&data.enableNightVision)stack.setDamageValue(stack.getDamageValue()+1);
        if(ready(stack)&&kind==Kind.SCUBA&&p.getAirSupply()<=280&&TGEquipmentBonuses.getBonus(p,TGArmorBonus.OXYGEN_GEAR,false)>0){p.setAirSupply(p.getAirSupply()+20);stack.setDamageValue(stack.getDamageValue()+1);}
        if(ready(stack)&&kind==Kind.ANTIGRAV&&p.getAbilities().flying&&!p.getAbilities().instabuild)stack.setDamageValue(stack.getDamageValue()+1);
        if(ready(stack)&&kind==Kind.JETPACK){
            if(data.enableJetpack&&data.isJumpkeyPressed()){var v=p.getDeltaMovement();p.setDeltaMovement(v.x,Math.min(v.y+0.15F,0.4F),v.z);p.fallDistance=0;stack.setDamageValue(stack.getDamageValue()+1);}
            glide(p);
        }
    }
    public static void glide(Player p){
        if(p.level().isClientSide&&!p.isLocalPlayer())return;
        TGExtendedPlayer.get(p).isGliding=false;
        if(!p.onGround()&&p.isShiftKeyDown()){
            var v=p.getDeltaMovement();if(v.y< -0.1F){TGExtendedPlayer.get(p).isGliding=true;double amount=Math.min(0.08,-0.1-v.y);var look=p.getLookAngle();p.setDeltaMovement(v.add(look.x*amount,amount,look.z*amount));}
            p.fallDistance=0;
        }
    }
    /** Modern travel takes air acceleration as a method value instead of the old mutable jumpMovementFactor. */
    public static void glideSteering(Player player,float strafe,float forward){var stack=TGExtendedPlayer.get(player).tg_inventory.getItem(1);if(stack.getItem() instanceof AccessoryItem device&&(device.kind==Kind.GLIDER||device.kind==Kind.JETPACK&&ready(stack))&&!player.onGround()&&player.isShiftKeyDown()&&player.getDeltaMovement().y<-.1)player.moveRelative(.03f,new net.minecraft.world.phys.Vec3(strafe,0,forward));}
    @Override public float getBonus(TGArmorBonus type,ItemStack stack,boolean consume,Player p){
        if(!ready(stack))return 0;
        if((kind==Kind.NIGHTVISION||kind==Kind.TACTICAL_MASK)&&type==TGArmorBonus.NIGHTVISION)return 1;
        if(kind==Kind.TACTICAL_MASK&&type==TGArmorBonus.OXYGEN_GEAR)return 1;
        if(kind==Kind.ANTIGRAV)return type==TGArmorBonus.CREATIVE_FLIGHT?1:type==TGArmorBonus.FLYSPEED?0.25F:0;
        if(kind==Kind.JUMPPACK){
            if(type==TGArmorBonus.FREEHEIGHT)return 8;
            if(type==TGArmorBonus.JUMP||type==TGArmorBonus.FALLDMG){
                if(consume){if(!TGExtendedPlayer.get(p).enableJetpack||stack.getDamageValue()+1>stack.getMaxDamage())return 0;stack.setDamageValue(stack.getDamageValue()+1);
                    if(!p.level().isClientSide){TGProjectilePackets.effect(p,type==TGArmorBonus.JUMP?"JumpPackBoost":"JumpPackFall",p.position(),0,0);if(type==TGArmorBonus.JUMP)p.level().playSound(null,p.getX(),p.getY(),p.getZ(),TGSounds.STEAM_JUMP_2.get(),net.minecraft.sounds.SoundSource.PLAYERS,1,1);}}
                return type==TGArmorBonus.JUMP?0.8F:0.2F;
            }
        }return 0;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){var stack=p.getItemInHand(hand);var data=TGExtendedPlayer.get(p);int slot=getSlot(stack)==TGSlotType.FACESLOT?0:1;if(data.tg_inventory.getItem(slot).isEmpty()){if(!level.isClientSide){data.tg_inventory.setItem(slot,stack.copy());stack.setCount(0);data.markDirty();}return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);}return InteractionResultHolder.pass(stack);}
    @Override public void appendHoverText(ItemStack stack,Level level,java.util.List<Component> lines,TooltipFlag flags){lines.add(Component.translatable("techguns.tooltip.slot").append(": ").append(getSlot(stack).displayName()));if(!ammo().isEmpty())lines.add(Component.translatable("techguns.tooltip.reloads_with").append(": ").append(ammo().getHoverName()));if(stack.isDamageableItem())lines.add(Component.literal((stack.getMaxDamage()-stack.getDamageValue())+"/"+stack.getMaxDamage()));}
}
