package techguns.items.armors;
import net.minecraft.world.entity.player.Player;
/** Display-only value: native armor points remain separate from Techguns typed protection. */
public final class ArmorDisplay {
    public static int points(Player player){
        int points=player.getArmorValue();
        for(var stack:player.getArmorSlots())if(stack.getItem() instanceof TGSuitArmor armor)points+=armor.displayArmor(stack);
        return Math.max(0,points);
    }
    private ArmorDisplay(){}
}
