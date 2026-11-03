package techguns.world;
public enum MilitaryCrateKind {
    AMMO("ammo"),GUN("gun"),ARMOR("armor"),MEDICAL("medical"),EXPLOSIVE("explosives"),GENERIC_OAK("generic"),GENERIC_JUNGLE("generic"),GENERIC_BIRCH("generic"),GENERIC_SPRUCE("generic");
    public final String loot;
    MilitaryCrateKind(String loot){this.loot=loot;}
    public String id(){return "military_crate_"+name().toLowerCase(java.util.Locale.ROOT);}
}
