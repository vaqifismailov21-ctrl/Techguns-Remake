package techguns.items.guns.ammo;

import net.minecraft.world.item.ItemStack;



import techguns.TGItems;

public class AmmoTypes{

	public static AmmoType STONE_BULLETS;
	public static AmmoType PISTOL_ROUNDS;
	public static AmmoType RIFLE_ROUNDS;
	public static AmmoType SHOTGUN_ROUNDS;
	public static AmmoType ASSAULT_RIFLE_MAGAZINE;
	public static AmmoType SMG_MAGAZINE;
	public static AmmoType PISTOL_MAGAZINE;
	public static AmmoType LMG_MAGAZINE;
	public static AmmoType BIO_TANK;
	public static AmmoType FUEL_TANK;
	public static AmmoType ROCKETS;
	public static AmmoType ROCKETS_NO_NUKES;
	public static AmmoType MINIGUN_AMMO_DRUM;
	public static AmmoType ADVANCED_MAGAZINE;
	public static AmmoType AS50_MAGAZINE;
	public static AmmoType ENERGY_CELL;
	public static AmmoType NETHER_CHARGE;
	public static AmmoType COMPRESSED_AIR_TANK;
	public static AmmoType GRENADES_40MM;
	public static AmmoType NUCLEAR_POWER_CELL;
	public static AmmoType REDSTONE_BATTERY;
	
	public static AmmoType AMMO_GAUSS_RIFLE;
	
	public static final String TYPE_INCENDIARY = "incendiary";
	public static final String TYPE_DEFAULT = "default";
	public static final String TYPE_NUKE = "nuke";
	public static final String TYPE_EXPLOSIVE = "explosive";
	public static final String TYPE_HV = "high_velocity";
	
	public static void init() {
		STONE_BULLETS = new AmmoType(TGItems.STONE_BULLETS.get().getDefaultInstance());
		PISTOL_ROUNDS = new AmmoType(TGItems.PISTOL_ROUNDS.get().getDefaultInstance());
		PISTOL_ROUNDS.addVariant(TYPE_INCENDIARY, TGItems.PISTOL_ROUNDS_INCENDIARY.get().getDefaultInstance());
		
		RIFLE_ROUNDS = new AmmoType(TGItems.RIFLE_ROUNDS.get().getDefaultInstance());
		RIFLE_ROUNDS.addVariant(TYPE_INCENDIARY, TGItems.RIFLE_ROUNDS_INCENDIARY.get().getDefaultInstance());
		
		SHOTGUN_ROUNDS = new AmmoType(TGItems.SHOTGUN_ROUNDS.get().getDefaultInstance());
		SHOTGUN_ROUNDS.addVariant(TYPE_INCENDIARY,TGItems.SHOTGUN_ROUNDS_INCENDIARY.get().getDefaultInstance());
		
		SMG_MAGAZINE = new AmmoType(TGItems.SMG_MAGAZINE.get().getDefaultInstance(), TGItems.SMG_MAGAZINE_EMPTY.get().getDefaultInstance(), TGItems.PISTOL_ROUNDS.get().getDefaultInstance(),2);
		SMG_MAGAZINE.addVariant(TYPE_INCENDIARY, TGItems.SMG_MAGAZINE_INCENDIARY.get().getDefaultInstance(), TGItems.PISTOL_ROUNDS_INCENDIARY.get().getDefaultInstance());
		
		ASSAULT_RIFLE_MAGAZINE = new AmmoType(TGItems.ASSAULTRIFLE_MAGAZINE.get().getDefaultInstance(), TGItems.ASSAULTRIFLE_MAGAZINE_EMPTY.get().getDefaultInstance(), TGItems.RIFLE_ROUNDS.get().getDefaultInstance(), 3);
		ASSAULT_RIFLE_MAGAZINE.addVariant(TYPE_INCENDIARY, TGItems.ASSAULTRIFLE_MAGAZINE_INCENDIARY.get().getDefaultInstance(), TGItems.RIFLE_ROUNDS_INCENDIARY.get().getDefaultInstance());
		
		PISTOL_MAGAZINE = new AmmoType(TGItems.PISTOL_MAGAZINE.get().getDefaultInstance(), TGItems.PISTOL_MAGAZINE_EMPTY.get().getDefaultInstance(), TGItems.PISTOL_ROUNDS.get().getDefaultInstance(),3);
		PISTOL_MAGAZINE.addVariant(TYPE_INCENDIARY, TGItems.PISTOL_MAGAZINE_INCENDIARY.get().getDefaultInstance(), TGItems.PISTOL_ROUNDS_INCENDIARY.get().getDefaultInstance());
		
		LMG_MAGAZINE = new AmmoType(TGItems.LMG_MAGAZINE.get().getDefaultInstance(), TGItems.LMG_MAGAZINE_EMPTY.get().getDefaultInstance(), TGItems.RIFLE_ROUNDS.get().getDefaultInstance(),8);
		LMG_MAGAZINE.addVariant(TYPE_INCENDIARY, TGItems.LMG_MAGAZINE_INCENDIARY.get().getDefaultInstance(), TGItems.RIFLE_ROUNDS_INCENDIARY.get().getDefaultInstance());
		
		BIO_TANK = new AmmoType(TGItems.BIO_TANK.get().getDefaultInstance(), TGItems.BIO_TANK_EMPTY.get().getDefaultInstance(), ItemStack.EMPTY, 0);
		FUEL_TANK = new AmmoType(TGItems.FUEL_TANK.get().getDefaultInstance(), TGItems.FUEL_TANK_EMPTY.get().getDefaultInstance(), ItemStack.EMPTY, 0);
		COMPRESSED_AIR_TANK = new AmmoType(TGItems.COMPRESSED_AIR_TANK.get().getDefaultInstance(), TGItems.COMPRESSED_AIR_TANK_EMPTY.get().getDefaultInstance(), ItemStack.EMPTY, 0);
		ENERGY_CELL = new AmmoType(TGItems.ENERGY_CELL.get().getDefaultInstance(), TGItems.ENERGY_CELL_EMPTY.get().getDefaultInstance(), ItemStack.EMPTY, 0);
		REDSTONE_BATTERY = new AmmoType(TGItems.REDSTONE_BATTERY.get().getDefaultInstance(), TGItems.REDSTONE_BATTERY_EMPTY.get().getDefaultInstance(), ItemStack.EMPTY, 0);
		MINIGUN_AMMO_DRUM = new AmmoType(TGItems.MINIGUN_DRUM.get().getDefaultInstance(), TGItems.MINIGUN_DRUM_EMPTY.get().getDefaultInstance(), TGItems.RIFLE_ROUNDS.get().getDefaultInstance(),16);
		MINIGUN_AMMO_DRUM.addVariant(TYPE_INCENDIARY, TGItems.MINIGUN_DRUM_INCENDIARY.get().getDefaultInstance(), TGItems.RIFLE_ROUNDS_INCENDIARY.get().getDefaultInstance());
		
		ADVANCED_MAGAZINE = new AmmoType(TGItems.ADVANCED_MAGAZINE.get().getDefaultInstance(), TGItems.ADVANCED_MAGAZINE_EMPTY.get().getDefaultInstance(), TGItems.ADVANCED_ROUNDS.get().getDefaultInstance(), 3);
		AS50_MAGAZINE = new AmmoType(TGItems.AS50_MAGAZINE.get().getDefaultInstance(), TGItems.AS50_MAGAZINE_EMPTY.get().getDefaultInstance(), TGItems.SNIPER_ROUNDS.get().getDefaultInstance(),2);
		AS50_MAGAZINE.addVariant(TYPE_INCENDIARY, TGItems.AS50_MAGAZINE_INCENDIARY.get().getDefaultInstance(), TGItems.SNIPER_ROUNDS_INCENDIARY.get().getDefaultInstance());
		AS50_MAGAZINE.addVariant(TYPE_EXPLOSIVE, TGItems.AS50_MAGAZINE_EXPLOSIVE.get().getDefaultInstance(), TGItems.SNIPER_ROUNDS_EXPLOSIVE.get().getDefaultInstance());
		
		ROCKETS = new AmmoType(TGItems.ROCKET.get().getDefaultInstance());
		ROCKETS.addVariant(TYPE_NUKE, TGItems.ROCKET_NUKE.get().getDefaultInstance());
		ROCKETS.addVariant(TYPE_HV, TGItems.ROCKET_HIGH_VELOCITY.get().getDefaultInstance());
		
		ROCKETS_NO_NUKES = new AmmoType(TGItems.ROCKET.get().getDefaultInstance());
		ROCKETS_NO_NUKES.addVariant(TYPE_HV, TGItems.ROCKET_HIGH_VELOCITY.get().getDefaultInstance());
		
		NETHER_CHARGE = new AmmoType(TGItems.NETHER_CHARGE.get().getDefaultInstance(), TGItems.NETHERCHARGE_EMPTY.get().getDefaultInstance(), ItemStack.EMPTY, 0);
		GRENADES_40MM = new AmmoType(TGItems.GRENADE_40MM.get().getDefaultInstance());
		NUCLEAR_POWER_CELL = new AmmoType(TGItems.NUCLEAR_POWERCELL.get().getDefaultInstance(), TGItems.NUCLEAR_POWERCELL_EMPTY.get().getDefaultInstance(), ItemStack.EMPTY,0);
		
		AMMO_GAUSS_RIFLE = new AmmoType(new ItemStack[] {TGItems.GAUSSRIFLE_SLUGS.get().getDefaultInstance(), TGItems.ENERGY_CELL.get().getDefaultInstance()}, new ItemStack[] {ItemStack.EMPTY, TGItems.ENERGY_CELL_EMPTY.get().getDefaultInstance()}, new ItemStack[] {TGItems.GAUSSRIFLE_SLUGS.get().getDefaultInstance(), ItemStack.EMPTY},  1);
	}	
}
