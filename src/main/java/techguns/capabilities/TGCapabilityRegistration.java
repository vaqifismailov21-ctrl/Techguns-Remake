package techguns.capabilities;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import techguns.TGBenches;
import techguns.TGFabricator;
import techguns.TGFluids;
import techguns.TGMachines;
import techguns.TGOreDrill;
import techguns.TGTurrets;
import techguns.capabilities.compat.Capabilities;
import techguns.power.PowerBlocks;

/** 1.21.1 capability providers: machines, generators, cells, drill and turret expose items/energy/fluids to other mods. */
public final class TGCapabilityRegistration {
    public static void register(RegisterCapabilitiesEvent event) {
        Capabilities.register(event, TGMachines.ENTITY.get());
        Capabilities.register(event, TGBenches.ENTITY.get());
        Capabilities.register(event, TGFabricator.ENTITY.get());
        Capabilities.register(event, TGOreDrill.MASTER.get());
        Capabilities.register(event, TGTurrets.ENTITY.get());
        Capabilities.register(event, PowerBlocks.FUEL_GENERATOR_ENTITY.get());
        Capabilities.register(event, PowerBlocks.LIQUID_GENERATOR_ENTITY.get());
        Capabilities.register(event, PowerBlocks.SOLAR_PANEL_ENTITY.get());
        Capabilities.register(event, PowerBlocks.ENERGY_CELL_ENTITY.get());
        // BucketItem subclasses do not get NeoForge's automatic bucket handler.
        event.registerItem(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack),
                TGFluids.OIL_BUCKET.get(), TGFluids.ACID_BUCKET.get());
    }
    private TGCapabilityRegistration() {}
}
