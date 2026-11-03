package techguns.items.armors;
import java.util.UUID;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.capabilities.TGExtendedPlayer;
import techguns.util.InventoryUtil;

/** Original per-player armor bonuses and chest-powered suit distribution, outside NPC/world systems. */
@Mod.EventBusSubscriber(modid="techguns")
public final class EquipmentEvents {
    private static UUID key(String name){return UUID.nameUUIDFromBytes(("techguns."+name).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    private static void attribute(Player p,Attribute type,String name,float value,AttributeModifier.Operation operation){
        var a=p.getAttribute(type);if(a==null)return;var id=key(name);var previous=a.getModifier(id);
        if(previous!=null&&previous.getAmount()==value)return;
        if(previous!=null)a.removeModifier(id);if(value>0)a.addTransientModifier(new AttributeModifier(id,name,value,operation));
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!event.player.isAlive())return;var player=event.player;
        var state=TGExtendedPlayer.find(player).orElse(null);if(state==null)return;
        var back=state.tg_inventory.getItem(1);
        if(!(back.getItem() instanceof AccessoryItem device&&(device.kind==AccessoryItem.Kind.GLIDER||device.kind==AccessoryItem.Kind.JETPACK&&back.getDamageValue()<back.getMaxDamage())))state.isGliding=false;
        flight(player,state);
        if(player.level.isClientSide){
            float assist=TGEquipmentBonuses.getBonus(player,TGArmorBonus.STEPASSIST,false);
            if(state.enableStepAssist&&assist>0)player.maxUpStep=Math.max(1.1F,player.maxUpStep);else if(player.maxUpStep==1.1F)player.maxUpStep=0.6F;
            return;
        }
        distributePower(player);
        if(!(state.tg_inventory.getItem(0).getItem() instanceof AccessoryItem accessory&&accessory.kind==AccessoryItem.Kind.GAS_MASK)){
            var resistance=player.getAttribute(techguns.api.radiation.TGRadiation.RADIATION_RESISTANCE.get());if(resistance!=null)resistance.removeModifier(UUID.fromString("1B428AAE-2ECA-42C6-A14F-0A8B3F507276"));
        }
        if(player.isOnFire()&&TGEquipmentBonuses.getBonus(player,TGArmorBonus.COOLING_SYSTEM,false)>=1)player.clearFire();
        float speed=TGEquipmentBonuses.getBonus(player,TGArmorBonus.SPEED,false);
        if(player.isInWater()||player.isInLava())speed+=TGEquipmentBonuses.getBonus(player,TGArmorBonus.SPEED_WATER,false);
        if(player.isSprinting())speed*=2;
        attribute(player,Attributes.MOVEMENT_SPEED,"equipment_speed",speed,AttributeModifier.Operation.MULTIPLY_TOTAL);
        attribute(player,Attributes.MAX_HEALTH,"equipment_health",TGEquipmentBonuses.getBonus(player,TGArmorBonus.EXTRA_HEART,false),AttributeModifier.Operation.ADDITION);
        attribute(player,Attributes.KNOCKBACK_RESISTANCE,"equipment_knockback",TGEquipmentBonuses.getBonus(player,TGArmorBonus.KNOCKBACK_RESISTANCE,false),AttributeModifier.Operation.ADDITION);
        if(player.getHealth()>player.getMaxHealth())player.setHealth(player.getMaxHealth());
        if(state.enableNightVision&&TGEquipmentBonuses.getBonus(player,TGArmorBonus.NIGHTVISION,true)>0)player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,220,0,false,false));
    }
    private static void flight(Player player,TGExtendedPlayer state){
        boolean allowed=TGEquipmentBonuses.getBonus(player,TGArmorBonus.CREATIVE_FLIGHT,false)>0;boolean changed=false;
        if(allowed){changed=!player.getAbilities().mayfly;player.getAbilities().mayfly=true;state.gotCreativeFlightLastTick=true;}
        else if(state.gotCreativeFlightLastTick){if(!player.getAbilities().instabuild&&!player.isSpectator()){player.getAbilities().mayfly=false;player.getAbilities().flying=false;changed=true;}state.gotCreativeFlightLastTick=false;state.wasFlying=false;}
        if(allowed&&player.getAbilities().flying){
            if(!state.wasFlying&&!player.level.isClientSide){player.level.playSound(null,player.getX(),player.getY(),player.getZ(),techguns.TGSounds.ANTI_GRAV_START.get(),net.minecraft.sounds.SoundSource.PLAYERS,1,1);techguns.TGProjectilePackets.effect(player,"AntiGravRing",player.position(),0,0);}state.wasFlying=true;
        }else state.wasFlying=false;
        if(player.level.isClientSide)player.getAbilities().setFlyingSpeed(0.05F*(1+(state.enableJetpack?TGEquipmentBonuses.getBonus(player,TGArmorBonus.FLYSPEED,false):0)));
        else if(changed)player.onUpdateAbilities();
    }
    public static void distributePower(Player player){
        var chest=player.getItemBySlot(EquipmentSlot.CHEST);var powerChest=chest.getItem() instanceof TGSuitArmor a&&a.powered()?a:null;
        boolean moving=Math.abs(player.getDeltaMovement().x)>0.001||Math.abs(player.getDeltaMovement().z)>0.001;
        boolean consume=player.level.getGameTime()%20==0&&moving;
        for(var slot:new EquipmentSlot[]{EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD}){
            var stack=player.getItemBySlot(slot);if(!(stack.getItem() instanceof TGSuitArmor armor)||!armor.powered())continue;
            if(slot!=EquipmentSlot.CHEST&&powerChest!=null&&powerChest.powerType.equals(armor.powerType)){
                int transfer=armor.charge(stack,powerChest.power(chest));powerChest.consumePower(chest,transfer);
            }
            if(consume)armor.consumePower(stack,1);
        }
        if(powerChest!=null&&powerChest.power(chest)==0&&!powerChest.battery().isEmpty()&&InventoryUtil.consumeAmmoPlayer(player,new ItemStack[]{powerChest.battery()})){
            var empty=powerChest.emptyBattery();if(!empty.isEmpty()){int left=InventoryUtil.addAmmoToPlayerInventory(player,empty);if(left>0)player.drop(techguns.util.Compat.copyWithCount(empty, left),false);}
            powerChest.charge(chest,powerChest.maxpower);
            for(var stack:player.getArmorSlots())if(stack!=chest&&stack.getItem() instanceof TGSuitArmor armor&&armor.powered()&&powerChest.powerType.equals(armor.powerType))powerChest.consumePower(chest,armor.charge(stack,powerChest.power(chest)));
        }
    }
    @SubscribeEvent public static void jump(LivingEvent.LivingJumpEvent event){if(event.getEntity() instanceof Player p)p.setDeltaMovement(p.getDeltaMovement().add(0,TGEquipmentBonuses.getBonus(p,TGArmorBonus.JUMP,true),0));}
    @SubscribeEvent public static void fall(LivingFallEvent event){if(event.getEntity() instanceof Player p){float reduction=1-Math.min(1,TGEquipmentBonuses.getBonus(p,TGArmorBonus.FALLDMG,event.getDistance()>3));float height=TGEquipmentBonuses.getBonus(p,TGArmorBonus.FREEHEIGHT,false);event.setDistance(Math.max(0,event.getDistance()-height)*reduction);}}
    @SubscribeEvent public static void mining(PlayerEvent.BreakSpeed event){var p=event.getEntity();float factor=1+TGEquipmentBonuses.getBonus(p,TGArmorBonus.BREAKSPEED,true);if(p.isInWater()||p.isInLava())factor*=1+TGEquipmentBonuses.getBonus(p,TGArmorBonus.BREAKSPEED_WATER,true);event.setNewSpeed(event.getNewSpeed()*factor);}
    private EquipmentEvents(){}
}
