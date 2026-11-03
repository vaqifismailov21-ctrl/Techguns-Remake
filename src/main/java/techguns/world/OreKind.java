package techguns.world;

import java.util.function.BooleanSupplier;
import techguns.TGConfig;

/** Values from EnumOreType and OreGenerator; original absolute Y ranges are retained. */
public enum OreKind {
    COPPER(4,1,0,5,3,12,5,80,()->TGConfig.doOreGenCopper),
    TIN(4,1,0,4,3,10,5,60,()->TGConfig.doOreGenTin),
    LEAD(6,2,0,4,2,8,5,50,()->TGConfig.doOreGenLead),
    TITANIUM(8,3,0,4,4,5,4,32,()->TGConfig.doOreGenTitanium),
    URANIUM(7,2,4,4,2,4,4,24,()->TGConfig.doOreGenUranium);

    public final float hardness;
    public final int miningLevel,light,veinBase,veinRandom,attempts,minY,maxY;
    private final BooleanSupplier enabled;
    OreKind(float hardness,int miningLevel,int light,int veinBase,int veinRandom,int attempts,int minY,int maxY,BooleanSupplier enabled){
        this.hardness=hardness;this.miningLevel=miningLevel;this.light=light;this.veinBase=veinBase;this.veinRandom=veinRandom;this.attempts=attempts;this.minY=minY;this.maxY=maxY;this.enabled=enabled;
    }
    public String id(){return "ore_"+name().toLowerCase(java.util.Locale.ROOT);}
    public boolean enabled(){return enabled.getAsBoolean();}
}
