package techguns.api.radiation;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import techguns.Techguns;

public final class TGRadiation {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Techguns.MODID);
    public static final RegistryObject<Attribute> RADIATION_RESISTANCE = ATTRIBUTES.register("radiation_resistance",
            () -> new RangedAttribute("techguns.radiationResistance", 0, 0, Float.MAX_VALUE).setSyncable(true));
    private TGRadiation() {}
}
