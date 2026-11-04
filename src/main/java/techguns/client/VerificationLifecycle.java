package techguns.client;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;
@Mod.EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
public final class VerificationLifecycle {
    private static int titleFrames;
    @SubscribeEvent public static void hide(ScreenEvent.Init.Post event){if(Boolean.getBoolean("techguns.verificationExit")){var mc=Minecraft.getInstance();org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());if(event.getScreen() instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen){mc.options.onboardAccessibility=false;mc.options.save();mc.tell(()->mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen()));}}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void finish(ScreenEvent.Render.Post event){if(Boolean.getBoolean("techguns.verificationExit")&&event.getScreen() instanceof net.minecraft.client.gui.screens.TitleScreen&&++titleFrames==2){if(Boolean.getBoolean("techguns.verifyWorldInterfaces")){WorldVisualVerifier.begin();return;}Techguns.LOGGER.info("TECHGUNS_VERIFICATION_CLIENT_COMPLETE: synthetic rendering finished; no world was opened");Minecraft.getInstance().stop();}}
    private VerificationLifecycle(){}
}
