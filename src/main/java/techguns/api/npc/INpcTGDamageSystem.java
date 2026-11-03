package techguns.api.npc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import techguns.damagesystem.TGDamageSource;

public interface INpcTGDamageSystem {
    float getTotalArmorAgainstType(TGDamageSource source);
    float getPenetrationResistance(TGDamageSource source);

    default float getToughnessAfterPentration(LivingEntity entity, TGDamageSource source) {
        float toughness = (float) entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float penetration = Math.max(source.armorPenetration - getPenetrationResistance(source), 0) * 4;
        return Math.max(toughness - penetration, 0);
    }
}
