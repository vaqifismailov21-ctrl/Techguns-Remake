package techguns.client.render;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;
import techguns.items.armors.TGSuitArmor;

/** Hides the skin's outer layer under suit armor so it does not poke through the armor model. */
@Mod.EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
public final class ArmorSkinLayers {
    private ArmorSkinLayers(){}
    private static boolean suit(Player player,EquipmentSlot slot){
        return player.getItemBySlot(slot).getItem() instanceof TGSuitArmor armor&&armor.modelKey!=null;
    }
    // runs after PlayerRenderer.setModelProperties, which resets the layers every frame
    @SubscribeEvent(priority=EventPriority.LOW)
    public static void pre(RenderPlayerEvent.Pre event){
        Player player=event.getEntity();
        PlayerModel<?> model=event.getRenderer().getModel();
        if(suit(player,EquipmentSlot.HEAD))model.hat.visible=false;
        if(suit(player,EquipmentSlot.CHEST)){model.jacket.visible=false;model.leftSleeve.visible=false;model.rightSleeve.visible=false;}
        if(suit(player,EquipmentSlot.LEGS)){model.jacket.visible=false;model.leftPants.visible=false;model.rightPants.visible=false;}
        if(suit(player,EquipmentSlot.FEET)){model.leftPants.visible=false;model.rightPants.visible=false;}
    }
}
