package techguns.items.guns.ammo;

import techguns.TGItems;

/** Original TGItems.init magazine unloading table. */
public final class MagazineUnloading {
    public static void init() {
        TGItems.PISTOL_MAGAZINE.get().setAmmoType(AmmoTypes.PISTOL_MAGAZINE, AmmoTypes.TYPE_DEFAULT);
        TGItems.ASSAULTRIFLE_MAGAZINE.get().setAmmoType(AmmoTypes.ASSAULT_RIFLE_MAGAZINE, AmmoTypes.TYPE_DEFAULT);
        TGItems.AS50_MAGAZINE.get().setAmmoType(AmmoTypes.AS50_MAGAZINE, AmmoTypes.TYPE_DEFAULT);
        TGItems.SMG_MAGAZINE.get().setAmmoType(AmmoTypes.SMG_MAGAZINE, AmmoTypes.TYPE_DEFAULT);
        TGItems.LMG_MAGAZINE.get().setAmmoType(AmmoTypes.LMG_MAGAZINE, AmmoTypes.TYPE_DEFAULT);
        TGItems.MINIGUN_DRUM.get().setAmmoType(AmmoTypes.MINIGUN_AMMO_DRUM, AmmoTypes.TYPE_DEFAULT);
        TGItems.PISTOL_MAGAZINE_INCENDIARY.get().setAmmoType(AmmoTypes.PISTOL_MAGAZINE, AmmoTypes.TYPE_INCENDIARY);
        TGItems.ASSAULTRIFLE_MAGAZINE_INCENDIARY.get().setAmmoType(AmmoTypes.ASSAULT_RIFLE_MAGAZINE, AmmoTypes.TYPE_INCENDIARY);
        TGItems.AS50_MAGAZINE_INCENDIARY.get().setAmmoType(AmmoTypes.AS50_MAGAZINE, AmmoTypes.TYPE_INCENDIARY);
        TGItems.SMG_MAGAZINE_INCENDIARY.get().setAmmoType(AmmoTypes.SMG_MAGAZINE, AmmoTypes.TYPE_INCENDIARY);
        TGItems.LMG_MAGAZINE_INCENDIARY.get().setAmmoType(AmmoTypes.LMG_MAGAZINE, AmmoTypes.TYPE_INCENDIARY);
        TGItems.MINIGUN_DRUM_INCENDIARY.get().setAmmoType(AmmoTypes.MINIGUN_AMMO_DRUM, AmmoTypes.TYPE_INCENDIARY);
        TGItems.AS50_MAGAZINE_EXPLOSIVE.get().setAmmoType(AmmoTypes.AS50_MAGAZINE, AmmoTypes.TYPE_EXPLOSIVE);
        TGItems.ADVANCED_MAGAZINE.get().setAmmoType(AmmoTypes.ADVANCED_MAGAZINE, AmmoTypes.TYPE_DEFAULT);
    }
    private MagazineUnloading() {}
}
