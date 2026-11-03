package techguns.client;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.TGPackets;
import techguns.capabilities.TGExtendedPlayer;
import techguns.items.armors.AccessoryItem;
import techguns.keybind.TGKeybindsID;
@Mod.EventBusSubscriber(modid="techguns",value=Dist.CLIENT)
public final class ClientEquipmentInput {
    private static boolean lastJump;
    @SubscribeEvent public static void steering(net.minecraftforge.client.event.MovementInputUpdateEvent event){if(event.getEntity().isLocalPlayer())AccessoryItem.glideSteering(event.getEntity(),event.getInput().leftImpulse,event.getInput().forwardImpulse);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.START)return;var game=Minecraft.getInstance();var player=game.player;
        if(player==null){lastJump=false;return;}var state=TGExtendedPlayer.find(player).orElse(null);if(state==null)return;
        boolean jetpack=state.tg_inventory.getItem(1).getItem() instanceof AccessoryItem item&&item.kind==AccessoryItem.Kind.JETPACK;
        boolean pressed=jetpack&&state.enableJetpack&&player.isAlive()&&game.screen==null&&game.options.keyJump.isDown();
        if(lastJump!=pressed){state.setJumpkeyPressed(pressed);TGPackets.sendKey(pressed?TGKeybindsID.JETPACK_BOOST_START:TGKeybindsID.JETPACK_BOOST_STOP,false);lastJump=pressed;}
    }
    private ClientEquipmentInput(){}
}
