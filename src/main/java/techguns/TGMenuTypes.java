package techguns;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import techguns.gui.player.TGPlayerInventoryContainer;

public final class TGMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Techguns.MODID);
    public static final RegistryObject<MenuType<techguns.machines.DoorMenu>> DOOR=MENUS.register("door",()->net.minecraftforge.common.extensions.IForgeMenuType.create(techguns.machines.DoorMenu::new));
    public static final RegistryObject<MenuType<techguns.machines.ChargeMenu>> CHARGE=MENUS.register("charge",()->net.minecraftforge.common.extensions.IForgeMenuType.create(techguns.machines.ChargeMenu::new));
    public static final RegistryObject<MenuType<techguns.machines.MachineMenu>> MACHINE=MENUS.register("machine",()->net.minecraftforge.common.extensions.IForgeMenuType.create(techguns.machines.MachineMenu::new));
    public static final RegistryObject<MenuType<techguns.machines.TurretMenu>> TURRET=MENUS.register("turret",()->net.minecraftforge.common.extensions.IForgeMenuType.create(techguns.machines.TurretMenu::new));
    public static final RegistryObject<MenuType<techguns.oredrill.OreDrillMenu>> ORE_DRILL=MENUS.register("oredrill",()->net.minecraftforge.common.extensions.IForgeMenuType.create(techguns.oredrill.OreDrillMenu::new));
    public static final RegistryObject<MenuType<techguns.machines.BenchMenu>> BENCH=MENUS.register("workbench",()->net.minecraftforge.common.extensions.IForgeMenuType.create(techguns.machines.BenchMenu::new));
    public static final RegistryObject<MenuType<TGPlayerInventoryContainer>> PLAYER_INVENTORY = MENUS.register(
            "player_inventory", () -> new MenuType<>(TGPlayerInventoryContainer::new));
    private TGMenuTypes() {}
}
