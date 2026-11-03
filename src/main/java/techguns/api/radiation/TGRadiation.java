package techguns.api.radiation;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import techguns.Techguns;

public final class TGRadiation {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Techguns.MODID);
    public static final DeferredHolder<Attribute, Attribute> RADIATION_RESISTANCE = ATTRIBUTES.register("radiation_resistance",
            () -> new RangedAttribute("techguns.radiationResistance", 0, 0, Float.MAX_VALUE).setSyncable(true));
    private TGRadiation() {}
}
