package techguns.oredrill;

/** Original EnumOreDrillType (meta 0-4); SCAFFOLD_HIDDEN (meta 5) is the scaffold block's hidden state here. */
public enum OreDrillType {
    FRAME("frame",true),SCAFFOLD("scaffold",false),ROD("rod",true),ENGINE("engine",true),CONTROLLER("controller",true);
    public final String name;public final boolean fullCube;
    OreDrillType(String name,boolean fullCube){this.name=name;this.fullCube=fullCube;}
    public String id(){return "oredrill_"+name;}
}
