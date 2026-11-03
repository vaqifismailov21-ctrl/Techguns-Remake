package techguns.items.armors;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.entity.EquipmentSlot;
import techguns.api.damagesystem.DamageType;
/**
 * Original material totals and part weights; vanilla armor points stay zero as in createVanillaMaterial.
 * 1.21.1: vanilla ArmorMaterial is a registry record, so every Techguns material registers one ({@link #holder()}).
 */
public final class TGArmorMaterial {
    public static final DeferredRegister<ArmorMaterial> REGISTER = DeferredRegister.create(Registries.ARMOR_MATERIAL, techguns.Techguns.MODID);
    private final DeferredHolder<ArmorMaterial, ArmorMaterial> holder;
    private final String name;
    private final int baseDur,enchantability;
    public final float toughness;
    private final Holder<SoundEvent> sound;
    private final EnumMap<DamageType,Float> armor=new EnumMap<>(DamageType.class);
    private float penetrationResistance;
    public TGArmorMaterial(String name,int baseDur,int enchantability,float value,Holder<SoundEvent> sound,float toughness){
        this.name=name;this.baseDur=baseDur;this.enchantability=enchantability;this.sound=sound;this.toughness=toughness;
        String key=name.toLowerCase(java.util.Locale.ROOT);
        Map<ArmorItem.Type,Integer> defense=new EnumMap<>(ArmorItem.Type.class);for(var t:ArmorItem.Type.values())defense.put(t,0);
        this.holder=REGISTER.register(key,()->new ArmorMaterial(defense,enchantability,sound,()->Ingredient.EMPTY,
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(techguns.Techguns.MODID,key))),toughness,0));
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
    public Holder<ArmorMaterial> holder(){return holder;}
    public int getDurabilityForType(ArmorItem.Type type){return Math.round(baseDur*55*0.25F);}
    public int getDefenseForType(ArmorItem.Type type){return 0;}
    public int getEnchantmentValue(){return enchantability;}
    public Holder<SoundEvent> getEquipSound(){return sound;}
    public Ingredient getRepairIngredient(){return Ingredient.EMPTY;}
    public String getName(){return "techguns:"+name.toLowerCase(java.util.Locale.ROOT);}
    public float getToughness(){return toughness;}
    public float getKnockbackResistance(){return 0;}
}
