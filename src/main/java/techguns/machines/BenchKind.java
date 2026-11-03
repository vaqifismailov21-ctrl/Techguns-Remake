package techguns.machines;
public enum BenchKind {
    CAMO("camo_bench",1),REPAIR("repair_bench",10),UPGRADE("armor_bench",2);
    public final String id;public final int slots;
    BenchKind(String id,int slots){this.id=id;this.slots=slots;}
    public String titleKey(){return "techguns.container."+id.replace("_","");}
    public String texture(){return this==UPGRADE?"upgrade_bench":id;}
}
