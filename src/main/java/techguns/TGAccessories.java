package techguns;
import java.util.*;
import net.minecraftforge.registries.RegistryObject;
import techguns.items.armors.AccessoryItem;
public final class TGAccessories {
    public static final Map<String,RegistryObject<AccessoryItem>> ITEMS=new LinkedHashMap<>();
    private static RegistryObject<AccessoryItem> item(String id,AccessoryItem.Kind kind,int camos,int durability){var entry=TGItems.ITEMS.register(id,()->new AccessoryItem(kind,camos,durability));ITEMS.put(id,entry);return entry;}
    public static final RegistryObject<AccessoryItem> GAS_MASK=item("gasmask",AccessoryItem.Kind.GAS_MASK,1,300);
    public static final RegistryObject<AccessoryItem> GLIDER=item("glider",AccessoryItem.Kind.GLIDER,1,-1);
    public static final RegistryObject<AccessoryItem> JUMPPACK=item("jumppack",AccessoryItem.Kind.JUMPPACK,4,1000);
    public static final RegistryObject<AccessoryItem> SCUBA_TANKS=item("scubatanks",AccessoryItem.Kind.SCUBA,1,600);
    public static final RegistryObject<AccessoryItem> NIGHTVISION_GOGGLES=item("nightvisiongoggles",AccessoryItem.Kind.NIGHTVISION,1,24000);
    public static final RegistryObject<AccessoryItem> JETPACK=item("jetpack",AccessoryItem.Kind.JETPACK,4,5000);
    public static final RegistryObject<AccessoryItem> TACTICAL_MASK=item("tacticalmask",AccessoryItem.Kind.TACTICAL_MASK,4,24000);
    public static final RegistryObject<AccessoryItem> ANTI_GRAV_PACK=item("antigravpack",AccessoryItem.Kind.ANTIGRAV,5,12000);
    public static void init(){}private TGAccessories(){}
}
