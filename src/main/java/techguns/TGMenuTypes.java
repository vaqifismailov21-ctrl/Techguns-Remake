package techguns;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import techguns.gui.player.TGPlayerInventoryContainer;

public final class TGMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Techguns.MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<techguns.machines.DoorMenu>> DOOR=MENUS.register("door",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(techguns.machines.DoorMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<techguns.machines.ChargeMenu>> CHARGE=MENUS.register("charge",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(techguns.machines.ChargeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<techguns.machines.MachineMenu>> MACHINE=MENUS.register("machine",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(techguns.machines.MachineMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<techguns.machines.TurretMenu>> TURRET=MENUS.register("turret",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(techguns.machines.TurretMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<techguns.oredrill.OreDrillMenu>> ORE_DRILL=MENUS.register("oredrill",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(techguns.oredrill.OreDrillMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<techguns.machines.BenchMenu>> BENCH=MENUS.register("workbench",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(techguns.machines.BenchMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<TGPlayerInventoryContainer>> PLAYER_INVENTORY = MENUS.register(
            "player_inventory", () -> new MenuType<>(TGPlayerInventoryContainer::new, FeatureFlags.DEFAULT_FLAGS));
    private TGMenuTypes() {}
}
