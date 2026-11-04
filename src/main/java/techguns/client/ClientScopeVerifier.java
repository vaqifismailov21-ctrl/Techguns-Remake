package techguns.client;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import techguns.*;
import techguns.capabilities.TGExtendedPlayer;
/** Integration check of the actual Forge mouse handler, inside the isolated QA client only. */
public final class ClientScopeVerifier {
    public static void verify(Minecraft mc){
        var player=mc.player;var main=player.getMainHandItem().copy();var off=player.getOffhandItem().copy();int selected=player.getInventory().selected;boolean active=mc.isWindowActive(),grabbed=mc.mouseHandler.isMouseGrabbed(),sneak=player.isShiftKeyDown();var attack=TGExtendedPlayer.get(player).getAttackTime(false);long reload=attack.getReloadTime();int checked=0;
        try{mc.setScreen(null);mc.setWindowActive(true);mc.mouseHandler.grabMouse();player.setShiftKeyDown(false);player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);attack.setReloadTime(0);
            for(var registered:TGuns.guns()){var gun=registered.get();if(!gun.canZoom())continue;player.setItemInHand(InteractionHand.MAIN_HAND,gun.getDefaultInstance());ClientZoom.reset();
                if(gun.isHoldZoom()){player.setShiftKeyDown(true);ClientZoom.tick(new TickEvent.ClientTickEvent(TickEvent.Phase.START));require(ClientZoom.isZoomed(player,gun),"Held scope follows the original sneak control: "+registered.getId());player.setShiftKeyDown(false);ClientZoom.tick(new TickEvent.ClientTickEvent(TickEvent.Phase.START));require(!ClientZoom.isZoomed(player,gun),"Releasing sneak resets held scope");}
                else{var press=new InputEvent.MouseButton.Pre(org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT,org.lwjgl.glfw.GLFW.GLFW_PRESS,0);ClientWeaponEvents.mouse(press);require(press.isCanceled()&&ClientZoom.isZoomed(player,gun),"First ordinary right click activates scope: "+registered.getId());ClientWeaponEvents.mouse(new InputEvent.MouseButton.Pre(1,org.lwjgl.glfw.GLFW.GLFW_RELEASE,0));require(ClientZoom.isZoomed(player,gun),"Right-button release must preserve toggle scope");ClientWeaponEvents.mouse(new InputEvent.MouseButton.Pre(1,org.lwjgl.glfw.GLFW.GLFW_PRESS,0));require(!ClientZoom.isZoomed(player,gun),"Second press exits scope");attack.setReloadTime(System.currentTimeMillis()+1000);ClientWeaponEvents.mouse(new InputEvent.MouseButton.Pre(1,org.lwjgl.glfw.GLFW.GLFW_PRESS,0));require(!ClientZoom.isZoomed(player,gun),"Reload blocks entering the scope");attack.setReloadTime(0);}
                checked++;
            }Techguns.LOGGER.info("TECHGUNS_SCOPE_INPUT_PASSED: {} scoped weapons exercised through the real mouse handler or source hold control; physical mouse comparison remains manual",checked);
        }finally{ClientZoom.reset();attack.setReloadTime(reload);player.getInventory().selected=selected;player.setItemInHand(InteractionHand.MAIN_HAND,main);player.setItemInHand(InteractionHand.OFF_HAND,off);player.setShiftKeyDown(sneak);if(!grabbed)mc.mouseHandler.releaseMouse();mc.setWindowActive(active);}
    }
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}private ClientScopeVerifier(){}
}
