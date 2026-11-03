package techguns.items.armors;
import java.util.EnumMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.entity.EquipmentSlot;
import techguns.api.damagesystem.DamageType;
/** Original material totals and part weights; vanilla armor points stay zero as in createVanillaMaterial. */
public final class TGArmorMaterial implements ArmorMaterial {
    private final String name;
    private final int baseDur,enchantability;
    public final float toughness;
    private final SoundEvent sound;
    private final EnumMap<DamageType,Float> armor=new EnumMap<>(DamageType.class);
    private float penetrationResistance;
    public TGArmorMaterial(String name,int baseDur,int enchantability,float value,SoundEvent sound,float toughness){
        this.name=name;this.baseDur=baseDur;this.enchantability=enchantability;this.sound=sound;this.toughness=toughness;
        for(var type:DamageType.values())armor.put(type,type==DamageType.UNRESISTABLE||type==DamageType.RADIATION?0:type==DamageType.PHYSICAL||type==DamageType.PROJECTILE?value:value*0.75F);
    }
    public TGArmorMaterial setArmorElemental(float value){for(var t:new DamageType[]{DamageType.FIRE,DamageType.LIGHTNING,DamageType.ICE,DamageType.ENERGY})armor.put(t,value);return this;}
    public TGArmorMaterial setArmorExplosion(float v){armor.put(DamageType.EXPLOSION,v);return this;}
    public TGArmorMaterial setArmorPoison(float v){armor.put(DamageType.POISON,v);return this;}
    public TGArmorMaterial setArmorRadiation(float v){armor.put(DamageType.RADIATION,v);return this;}
    public TGArmorMaterial setArmorFire(float v){armor.put(DamageType.FIRE,v);return this;}
    public TGArmorMaterial setArmorEnergy(float v){armor.put(DamageType.ENERGY,v);return this;}
    public TGArmorMaterial setArmorIce(float v){armor.put(DamageType.ICE,v);return this;}
    public TGArmorMaterial setArmorLightning(float v){armor.put(DamageType.LIGHTNING,v);return this;}
    public TGArmorMaterial setPenetrationResistance(float v){penetrationResistance=v;return this;}
    public float getPenetrationResistance(){return penetrationResistance;}
    public float armor(EquipmentSlot slot,DamageType type){return armor.getOrDefault(type,0F)*switch(slot){case HEAD,LEGS->0.25F;case CHEST->0.30F;case FEET->0.20F;default->0;};}
    @Override public int getDurabilityForType(ArmorItem.Type type){return Math.round(baseDur*55*0.25F);}
    @Override public int getDefenseForType(ArmorItem.Type type){return 0;}
    @Override public int getEnchantmentValue(){return enchantability;}
    @Override public SoundEvent getEquipSound(){return sound;}
    @Override public Ingredient getRepairIngredient(){return Ingredient.EMPTY;}
    @Override public String getName(){return "techguns:"+name.toLowerCase(java.util.Locale.ROOT);}
    @Override public float getToughness(){return toughness;}
    @Override public float getKnockbackResistance(){return 0;}
}
