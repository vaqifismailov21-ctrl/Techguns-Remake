package techguns.machines;
/** Source slot order, energy capacity and physical GUI slot positions. */
public enum MachineKind {
    AMMO_PRESS("ammo_press",20000,3,4,new int[][]{{100,17},{120,17},{140,17},{120,60},{153,60}}),
    METAL_PRESS("metal_press",20000,2,3,new int[][]{{110,17},{130,17},{120,60},{152,60}}),
    CHEM_LAB("chem_lab",20000,3,4,new int[][]{{40,16},{40,37},{40,58},{153,37},{153,58}}),
    GRINDER("grinder",20000,2,1,new int[][]{{19,36},{177,54},{119,18},{137,18},{155,18},{119,36},{137,36},{155,36},{119,54},{137,54},{155,54}}),
    FABRICATOR("fabricator_controller",100000,4,5,new int[][]{{11,41},{57,41},{103,41},{149,41},{80,109},{149,109}}),
    REACTION("reactionchamber_controller",1000000,2,-1,new int[][]{{37,87},{82,35},{127,87},{145,69},{145,87},{145,105}}),
    BLAST_FURNACE("blast_furnace",40000,2,3,new int[][]{{19,17},{47,17},{116,50},{150,50}}),
    CHARGING_STATION("charging_station",100000,1,2,new int[][]{{19,17},{68,17},{152,60}});
    public final String id;public final int capacity,output,upgrade;public final int[][] slots;
    /** Top-left slot of the player inventory in this machine's screen (Techguns CE layouts). */
    public int[] inventoryOrigin(){return switch(this){case AMMO_PRESS,METAL_PRESS->new int[]{8,116};case CHEM_LAB,GRINDER->new int[]{19,106};case FABRICATOR,REACTION->new int[]{8,156};default->new int[]{8,84};};}
    MachineKind(String id,int capacity,int output,int upgrade,int[][] slots){this.id=id;this.capacity=capacity;this.output=output;this.upgrade=upgrade;this.slots=slots;}
    public String titleKey(){return switch(this){
        case AMMO_PRESS->"techguns.container.ammopress";
        case METAL_PRESS->"techguns.container.metalpress";
        case CHEM_LAB->"techguns.container.chemlab";
        case GRINDER->"techguns.container.grinder";
        case FABRICATOR->"techguns.container.fabricator";
        case REACTION->"techguns.container.reactionchamber";
        case BLAST_FURNACE->"techguns.container.blast_furnace";
        case CHARGING_STATION->"techguns.container.chargingstation";
    };}
    public String texture(){return this==FABRICATOR?"fabricator":this==REACTION?"reaction_chamber":id;}
    public boolean outputSlot(int slot){return this==GRINDER||this==REACTION?slot>=2&&slot<slots.length:slot==output;}
    public boolean multiblock(){return this==FABRICATOR||this==REACTION;}
}
