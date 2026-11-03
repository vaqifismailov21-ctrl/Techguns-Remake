package techguns.world;
import techguns.TGConfig;
/** Ordinal order is retained from EnumOreClusterType. Configuration is read when used. */
public enum ClusterKind {
    COAL,COMMON_METAL,RARE_METAL,SHINY_METAL,URANIUM,COMMON_GEM,SHINY_GEM,NETHER_CRYSTAL,OIL;
    public String id(){return "ore_cluster_"+name().toLowerCase(java.util.Locale.ROOT);}
    public int miningLevel(){return switch(this){case COAL->TGConfig.mininglevel_coal;case COMMON_METAL->TGConfig.mininglevel_common_metal;case RARE_METAL->TGConfig.mininglevel_rare_metal;case SHINY_METAL->TGConfig.mininglevel_shiny_metal;case URANIUM->TGConfig.mininglevel_uranium;case COMMON_GEM->TGConfig.mininglevel_common_gem;case SHINY_GEM->TGConfig.mininglevel_shiny_gem;case NETHER_CRYSTAL->TGConfig.mininglevel_nether_crystal;case OIL->TGConfig.mininglevel_oil;};}
    public double amountMultiplier(){return switch(this){case COAL->TGConfig.oremult_coal;case COMMON_METAL->TGConfig.oremult_common_metal;case RARE_METAL->TGConfig.oremult_rare_metal;case SHINY_METAL->TGConfig.oremult_shiny_metal;case URANIUM->TGConfig.oremult_uranium;case COMMON_GEM->TGConfig.oremult_common_gem;case SHINY_GEM->TGConfig.oremult_shiny_gem;case NETHER_CRYSTAL->TGConfig.oremult_nether_crystal;case OIL->TGConfig.oremult_oil;};}
    public double powerMultiplier(){return switch(this){case COAL->TGConfig.powermult_coal;case COMMON_METAL->TGConfig.powermult_common_metal;case RARE_METAL->TGConfig.powermult_rare_metal;case SHINY_METAL->TGConfig.powermult_shiny_metal;case URANIUM->TGConfig.powermult_uranium;case COMMON_GEM->TGConfig.powermult_common_gem;case SHINY_GEM->TGConfig.powermult_shiny_gem;case NETHER_CRYSTAL->TGConfig.powermult_nether_crystal;case OIL->TGConfig.powermult_oil;};}
}
