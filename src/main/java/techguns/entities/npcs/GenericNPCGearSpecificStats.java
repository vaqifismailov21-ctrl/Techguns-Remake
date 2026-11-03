package techguns.entities.npcs;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.Level;
import techguns.damagesystem.*;
import techguns.items.armors.TGSuitArmor;
public class GenericNPCGearSpecificStats extends GenericNPC {
    public GenericNPCGearSpecificStats(EntityType<? extends GenericNPC> type,Level level){super(type,level);}
    @Override public float getTotalArmorAgainstType(TGDamageSource source){
        float total=0;for(var stack:getArmorSlots()){
            if(stack.getItem() instanceof TGSuitArmor suit)total+=suit.armor(stack,source.damageType);
            else if(stack.getItem() instanceof ArmorItem vanilla)total+=DamageSystem.getArmorAgainstDamageTypeDefault(this,vanilla.getDefense(),source.damageType);
        }return total;
    }
    @Override public float getPenetrationResistance(TGDamageSource source){float total=0;for(var stack:getArmorSlots())if(stack.getItem() instanceof TGSuitArmor suit)total+=suit.stats.getPenetrationResistance();return total;}
}
