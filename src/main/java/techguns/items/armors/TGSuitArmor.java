package techguns.items.armors;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import techguns.api.damagesystem.DamageType;

/** Wearable armor stats, original power/NBT, bonus and repair contracts. */
public final class TGSuitArmor extends ArmorItem implements ICamoChangeable {
    public final TGArmorMaterial stats;
    public final String id;
    public final String[] textures;
    public final EquipmentSlot slot;
    public final String powerType;
    public final int maxpower;
    public String modelKey;
    public boolean doubleTexture=true,hideFace,hideBack,hideGloves,use3dIcon;
    private String camoSuffix="";
    private final EnumMap<TGArmorBonus,Float> bonuses=new EnumMap<>(TGArmorBonus.class),unpowered=new EnumMap<>(TGArmorBonus.class);
    private ItemStack battery=ItemStack.EMPTY,emptyBattery=ItemStack.EMPTY,repairMetal=ItemStack.EMPTY,repairCloth=ItemStack.EMPTY;
    private float repairMetalRatio;
    private int repairCount;
    private static Type type(EquipmentSlot slot){return switch(slot){case HEAD->Type.HELMET;case CHEST->Type.CHESTPLATE;case LEGS->Type.LEGGINGS;case FEET->Type.BOOTS;default->throw new IllegalArgumentException("Armor slot required");};}
    public TGSuitArmor(String id,TGArmorMaterial material,String[] textures,EquipmentSlot slot,String powerType,int maxpower){
        super(material,type(slot),new Item.Properties().durability(material.getDurabilityForType(type(slot))));this.id=id;stats=material;this.textures=textures;this.slot=slot;this.powerType=powerType;this.maxpower=maxpower;
    }
    public boolean powered(){return maxpower>0;}
    public int power(ItemStack stack){return Math.max(0,Math.min(maxpower,stack.getOrCreateTag().getInt("power")));}
    public int charge(ItemStack stack,int amount){int before=power(stack),accepted=Math.min(Math.max(0,amount),maxpower-before);stack.getOrCreateTag().putInt("power",before+accepted);return accepted;}
    public void consumePower(ItemStack stack,int amount){stack.getOrCreateTag().putInt("power",Math.max(0,power(stack)-Math.max(0,amount)));}
    public TGSuitArmor setBattery(ItemStack stack){battery=stack.copy();return this;}
    public TGSuitArmor setEmptyBattery(ItemStack stack){emptyBattery=stack.copy();return this;}
    public ItemStack battery(){return battery.copy();}public ItemStack emptyBattery(){return emptyBattery.copy();}
    private void bonus(TGArmorBonus b,float on,float off){bonuses.put(b,on);unpowered.put(b,off);}
    public float bonus(TGArmorBonus b,ItemStack stack,boolean consume,Player player){
        if(stack.getDamageValue()>=stack.getMaxDamage()-1)return 0;
        boolean active=!powered()||power(stack)>0;float value=(active?bonuses:unpowered).getOrDefault(b,0F);
        if(active&&powered()&&consume&&value>0){int amount=b==TGArmorBonus.JUMP&&slot==EquipmentSlot.LEGS||b==TGArmorBonus.FALLDMG&&slot==EquipmentSlot.FEET?5:b==TGArmorBonus.COOLING_SYSTEM&&slot==EquipmentSlot.CHEST?1:0;consumePower(stack,amount);}
        return value;
    }
    public TGSuitArmor setSpeedBoni(float speed,float jump){return setSpeedBoni(speed,jump,0,0);}
    public TGSuitArmor setSpeedBoni(float speed,float jump,float speedOff,float jumpOff){bonus(TGArmorBonus.SPEED,speed,speedOff);bonus(TGArmorBonus.JUMP,jump,jumpOff);return this;}
    public TGSuitArmor setFallProtection(float dmg,float height){return setFallProtection(dmg,height,0,0);}
    public TGSuitArmor setFallProtection(float dmg,float height,float dmgOff,float heightOff){bonus(TGArmorBonus.FALLDMG,dmg,dmgOff);bonus(TGArmorBonus.FREEHEIGHT,height,heightOff);return this;}
    public TGSuitArmor setMiningBoni(float v){return setMiningBoni(v,0);}public TGSuitArmor setMiningBoni(float v,float off){bonus(TGArmorBonus.BREAKSPEED,v,off);return this;}
    public TGSuitArmor setMiningBoniWater(float v){return setMiningBoniWater(v,0);}public TGSuitArmor setMiningBoniWater(float v,float off){bonus(TGArmorBonus.BREAKSPEED_WATER,v,off);return this;}
    public TGSuitArmor setGunBonus(float v){return setGunBonus(v,0);}public TGSuitArmor setGunBonus(float v,float off){bonus(TGArmorBonus.GUN_ACCURACY,v,off);return this;}
    public TGSuitArmor setHealthBonus(int v){return setHealthBonus(v,0);}public TGSuitArmor setHealthBonus(int v,int off){bonus(TGArmorBonus.EXTRA_HEART,v,off);return this;}
    public TGSuitArmor setKnockbackResistance(float v){bonus(TGArmorBonus.KNOCKBACK_RESISTANCE,v,0);return this;}
    public TGSuitArmor setRADResistance(float v){bonus(TGArmorBonus.RAD_RESISTANCE,v,v);return this;}
    public TGSuitArmor setStepAssist(float v){return setStepAssist(v,0);}public TGSuitArmor setStepAssist(float v,float off){bonus(TGArmorBonus.STEPASSIST,v,off);return this;}
    public TGSuitArmor setOxygenGear(float v){return setOxygenGear(v,0);}public TGSuitArmor setOxygenGear(float v,float off){bonus(TGArmorBonus.OXYGEN_GEAR,v,off);return this;}
    public TGSuitArmor setCoolingSystem(float v){return setCoolingSystem(v,0);}public TGSuitArmor setCoolingSystem(float v,float off){bonus(TGArmorBonus.COOLING_SYSTEM,v,off);return this;}
    public TGSuitArmor setWaterspeedBonus(float v){return setWaterspeedBonus(v,0);}public TGSuitArmor setWaterspeedBonus(float v,float off){bonus(TGArmorBonus.SPEED_WATER,v,off);return this;}
    public TGSuitArmor setHideFaceslot(boolean value){hideFace=value;return this;}public TGSuitArmor setHideBackslot(boolean value){hideBack=value;return this;}public TGSuitArmor setHideGloveslot(boolean value){hideGloves=value;return this;}
    public TGSuitArmor setCamoNameSuffix(String value){camoSuffix=value;return this;}
    public TGSuitArmor setArmorModel(String name,boolean twoTextures){modelKey=name;doubleTexture=twoTextures;return this;}
    public TGSuitArmor setUseRenderHack(){use3dIcon=true;return this;}
    public TGSuitArmor setRepairMats(ItemStack metal,ItemStack cloth,float metalRatio,int count){repairMetal=metal.copy();repairCloth=cloth.copy();repairMetalRatio=metalRatio;repairCount=count;return this;}
    public List<ItemStack> repairMaterials(ItemStack stack){
        var result=new ArrayList<ItemStack>();if(stack.getDamageValue()<=0||repairCount<=0)return result;
        int count=(int)Math.ceil(repairCount*stack.getDamageValue()/(float)(stack.getMaxDamage()-1));
        int metal=(int)Math.ceil(count*repairMetalRatio);if(metal>0&&!repairMetal.isEmpty())result.add(repairMetal.copyWithCount(metal));
        int cloth=count-metal;if(cloth>0&&!repairCloth.isEmpty())result.add(repairCloth.copyWithCount(cloth));return result;
    }
    public boolean broken(ItemStack stack){return stack.getDamageValue()>=stack.getMaxDamage()-1;}
    public float armor(ItemStack stack,DamageType type){return stats.armor(slot,type);}
    /** The original ISpecialArmor display hook rounds each physical armor part separately. */
    public int displayArmor(ItemStack stack){return broken(stack)?0:Math.round(armor(stack,DamageType.PHYSICAL));}
    @Override public int getCamoCount(){return textures.length;}
    @Override public Component getCurrentCamoName(ItemStack stack){int camo=Math.floorMod(getCurrentCamoIndex(stack),textures.length);return Component.translatable("techguns.item."+textures[0]+"."+(camoSuffix.isEmpty()?"":camoSuffix+".")+"camoname."+camo);}
    @Override public boolean isFoil(ItemStack stack){return false;}
    @Override public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String layer){return "techguns:textures/models/armor/"+textures[Math.floorMod(getCurrentCamoIndex(stack),textures.length)]+(doubleTexture?"_layer_"+(slot==EquipmentSlot.LEGS?2:1):"")+".png";}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){if(player.isShiftKeyDown()&&textures.length>1){var stack=player.getItemInHand(hand);if(!level.isClientSide)switchCamo(stack);return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);}return super.use(level,player,hand);}
    @Override public boolean isValidRepairItem(ItemStack stack,ItemStack material){return ItemStack.isSameItemSameTags(repairMetal.isEmpty()?repairCloth:repairMetal,material);}
    @Override public <T extends LivingEntity> int damageItem(ItemStack stack,int amount,T entity,java.util.function.Consumer<T> onBroken){return Math.max(0,Math.min(amount,stack.getMaxDamage()-1-stack.getDamageValue()));}
    @Override public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute,net.minecraft.world.entity.ai.attributes.AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot,ItemStack stack){
        var builder=com.google.common.collect.ImmutableMultimap.<net.minecraft.world.entity.ai.attributes.Attribute,net.minecraft.world.entity.ai.attributes.AttributeModifier>builder();builder.putAll(super.getAttributeModifiers(equipmentSlot,stack));
        float resistance=bonuses.getOrDefault(TGArmorBonus.RAD_RESISTANCE,0F);
        if(equipmentSlot==slot&&resistance>0)builder.put(techguns.api.radiation.TGRadiation.RADIATION_RESISTANCE.get(),new net.minecraft.world.entity.ai.attributes.AttributeModifier(java.util.UUID.nameUUIDFromBytes(("techguns.radiation."+slot).getBytes(java.nio.charset.StandardCharsets.UTF_8)),"Techguns radiation resistance",resistance,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        return builder.build();
    }
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flags){
        appendArmorTooltip(stack,lines,net.minecraft.client.gui.screens.Screen.hasShiftDown());
    }
    public void appendArmorTooltip(ItemStack stack,List<Component> lines,boolean detailed){
        if(powered()){
            var type=Component.translatable("techguns.tooltip.powertype."+("STEAM".equals(powerType)?"Steam":powerType));
            var line=Component.translatable("techguns.tooltip.powerType").append(": ").append(type).withStyle(net.minecraft.ChatFormatting.AQUA);
            if(slot==EquipmentSlot.CHEST&&!battery.isEmpty())line.append(" (").append(battery.getHoverName()).append(")");
            lines.add(line);lines.add(Component.translatable("techguns.tooltip.power").append(": "+power(stack)+"/"+maxpower));
        }
        lines.add(Component.translatable("techguns.armorTooltip.durability").append(": "+(stack.getMaxDamage()-stack.getDamageValue())+"/"+stack.getMaxDamage()));
        if(!detailed){lines.add(Component.translatable("techguns.gun.tooltip.shift1").append(" ").append(Component.translatable("techguns.gun.tooltip.shift2").withStyle(net.minecraft.ChatFormatting.GREEN)).append(" ").append(Component.translatable("techguns.gun.tooltip.shift3")));return;}
        if(stats.toughness>0)lines.add(Component.translatable("techguns.armorTooltip.toughness").append(": "+number(stats.toughness)));
        lines.add(Component.translatable("techguns.armorTooltip.resistances").append(":").withStyle(net.minecraft.ChatFormatting.BLUE));
        for(var type:new DamageType[]{DamageType.PHYSICAL,DamageType.PROJECTILE,DamageType.EXPLOSION,DamageType.ENERGY,DamageType.FIRE,DamageType.ICE,DamageType.LIGHTNING,DamageType.POISON,DamageType.DARK,DamageType.RADIATION})lines.add(type.displayName().copy().append(": "+number(stats.armor(slot,type))));
        // Match the original tooltip's health/speed/jump priority and remaining independent bonuses.
        if(baseBonus(TGArmorBonus.EXTRA_HEART)>0)lines.add(Component.translatable("techguns.armorTooltip.healthbonus").append(": +"+number(baseBonus(TGArmorBonus.EXTRA_HEART))+" ").append(Component.translatable("techguns.armorTooltip.hearts")));
        else if(baseBonus(TGArmorBonus.SPEED)>0)bonusLine(lines,TGArmorBonus.SPEED,"movespeed","+",100,"%");
        else bonusLine(lines,TGArmorBonus.JUMP,"jumpheight","+",1,"");
        bonusLine(lines,TGArmorBonus.FALLDMG,"falldamage","-",100,"%");bonusLine(lines,TGArmorBonus.FREEHEIGHT,"fallheight","-",1,"");
        bonusLine(lines,TGArmorBonus.BREAKSPEED,"miningspeed","+",100,"%");bonusLine(lines,TGArmorBonus.BREAKSPEED_WATER,"underwatermining","+",100,"%");bonusLine(lines,TGArmorBonus.KNOCKBACK_RESISTANCE,"knockbackresistance","+",100,"%");
        flagLine(lines,TGArmorBonus.NIGHTVISION,"nightvision");flagLine(lines,TGArmorBonus.STEPASSIST,"stepassist");flagLine(lines,TGArmorBonus.OXYGEN_GEAR,"oxygengear");flagLine(lines,TGArmorBonus.COOLING_SYSTEM,"coolingsystem");
    }
    private float baseBonus(TGArmorBonus bonus){return bonuses.getOrDefault(bonus,0F);}
    private void bonusLine(List<Component> lines,TGArmorBonus bonus,String key,String sign,float multiplier,String suffix){float value=baseBonus(bonus);if(value>0)lines.add(Component.translatable("techguns.armorTooltip."+key).append(": "+sign+number(value*multiplier)+suffix));}
    private void flagLine(List<Component> lines,TGArmorBonus bonus,String key){if(baseBonus(bonus)>0)lines.add(Component.translatable("techguns.armorTooltip."+key));}
    private static String number(float value){return new java.text.DecimalFormat("0.##",java.text.DecimalFormatSymbols.getInstance(Locale.ROOT)).format(value);}
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer){
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions(){
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){return use3dIcon?techguns.client.render.item.ArmorItemRenderer.get():net.minecraft.client.Minecraft.getInstance().getItemRenderer().getBlockEntityRenderer();}
            private techguns.client.models.armor.SuitArmorModel model;
            @Override public net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity,ItemStack stack,EquipmentSlot equipmentSlot,net.minecraft.client.model.HumanoidModel<?> original){
                if(modelKey==null)return original;if(model==null)model=new techguns.client.models.armor.SuitArmorModel(modelKey);
                ((net.minecraft.client.model.HumanoidModel<LivingEntity>)original).copyPropertiesTo(model);model.select(equipmentSlot);return model;
            }
        });
    }
}
