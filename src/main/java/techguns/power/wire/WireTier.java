package techguns.power.wire;

/**
 * Kinds of power wire, from thin to thick: how much energy a wire carries per tick, how long it may be, how much it
 * loses per block of length, and how it looks (colour, thickness, sag).
 */
public enum WireTier {
    COPPER("copper",2048,16,0.0025F,0xFFB86A35,0.022F),
    STEEL("steel",16384,24,0.001F,0xFF7A7F86,0.032F),
    HIGH_VOLTAGE("high_voltage",131072,48,0.0002F,0xFF2A2B2F,0.048F);
    public final String id;public final int rate,maxLength;public final float lossPerBlock;public final int colour;public final float thickness;
    WireTier(String id,int rate,int maxLength,float lossPerBlock,int colour,float thickness){this.id=id;this.rate=rate;this.maxLength=maxLength;this.lossPerBlock=lossPerBlock;this.colour=colour;this.thickness=thickness;}
    /** Share of the energy that arrives at the other end of a wire of this length. */
    public float efficiency(double length){return (float)Math.max(0.5,1-lossPerBlock*length);}
    public static WireTier of(int ordinal){return values()[Math.max(0,Math.min(values().length-1,ordinal))];}
}
