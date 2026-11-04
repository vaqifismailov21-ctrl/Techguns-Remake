package techguns.client;

import java.nio.file.Files;
import java.util.*;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.*;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Matrix4f;
import techguns.*;
import techguns.items.armors.ArmorDisplay;

/** Isolated QA: exercise the Forge overlay event and compare every pixel with vanilla armor icons. */
public final class ArmorHudVisualVerifier {
    private record Fixture(String name,int nativePoints,int expected,String... suit){}
    private static final EquipmentSlot[] SLOTS={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    public static void verify(Minecraft mc){
        var gui=(ForgeGui)mc.gui;var attribute=mc.player.getAttribute(Attributes.ARMOR);
        var oldMode=mc.gameMode.getPlayerMode();var oldPrevious=mc.gameMode.getPreviousPlayerMode();
        double oldBase=attribute.getBaseValue();boolean oldHidden=mc.options.hideGui;int oldHeight=gui.leftHeight;
        var previous=new EnumMap<EquipmentSlot,ItemStack>(EquipmentSlot.class);for(var slot:SLOTS)previous.put(slot,mc.player.getItemBySlot(slot).copy());
        // Hidden Windows clients can report a 1x1 framebuffer. Use explicit QA dimensions.
        var target=new TextureTarget(1280,720,true,Minecraft.ON_OSX);
        var view=RenderSystem.getModelViewStack();RenderSystem.backupProjectionMatrix();view.pushPose();view.setIdentity();RenderSystem.applyModelViewMatrix();
        var fixtures=List.of(new Fixture("empty",0,0),new Fixture("vanilla-helmet",2,2),new Fixture("scout-helmet",0,3,"t1_scout_helmet"),new Fixture("mixed",6,9,"t1_scout_helmet"),new Fixture("steam-suit",0,20,"steam_helmet","steam_chestplate","steam_leggings","steam_boots"),new Fixture("broken",0,0,"t1_scout_helmet"),new Fixture("over-twenty",10,30,"steam_helmet","steam_chestplate","steam_leggings","steam_boots"));
        try{
            var output=mc.gameDirectory.toPath().resolve("verification");Files.createDirectories(output);
            mc.gameMode.setLocalMode(GameType.SURVIVAL);mc.options.hideGui=false;
            for(var fixture:fixtures){
                clear(mc);attribute.setBaseValue(fixture.expected);
                try(var vanilla=frame(mc,gui,target,fixture.name,false)){
                    if(vanilla.getWidth()!=1280||vanilla.getHeight()!=720)throw new IllegalStateException("Invalid HUD fixture dimensions");
                    int foreground=0;for(int x=458;x<618;x++)for(int y=642;y<660;y++)if(vanilla.getPixelRGBA(x,y)!=vanilla.getPixelRGBA(0,0))foreground++;
                    if(fixture.expected>0&&foreground<20)throw new IllegalStateException("Native armor icons did not render: "+fixture.name);
                    clear(mc);attribute.setBaseValue(fixture.nativePoints);
                    if(fixture.name.equals("vanilla-helmet"))mc.player.setItemSlot(EquipmentSlot.HEAD,Items.IRON_HELMET.getDefaultInstance());
                    for(String id:fixture.suit){var armor=TGEquipment.ARMOR.get(id).get();var stack=armor.getDefaultInstance();if(fixture.name.equals("broken"))stack.setDamageValue(stack.getMaxDamage()-1);mc.player.setItemSlot(armor.slot,stack);}
                    if(ArmorDisplay.points(mc.player)!=fixture.expected)throw new IllegalStateException("Wrong armor HUD points: "+fixture.name);
                    int nativeBefore=mc.player.getArmorValue();
                    try(var actual=frame(mc,gui,target,fixture.name,fixture.suit.length>0)){
                        for(int x=0;x<actual.getWidth();x++)for(int y=0;y<actual.getHeight();y++)if(actual.getPixelRGBA(x,y)!=vanilla.getPixelRGBA(x,y))throw new IllegalStateException("Armor icon differs from vanilla at "+fixture.name+" "+x+","+y);
                        actual.writeToFile(output.resolve("armor-hud-"+fixture.name+".png"));
                    }
                    if(mc.player.getArmorValue()!=nativeBefore)throw new IllegalStateException("HUD changed native armor mitigation");
                }
            }
            Techguns.LOGGER.info("TECHGUNS_ARMOR_HUD_RENDER_PASSED: seven real Forge overlay fixtures at 1280x720 match vanilla pixels; mixed suits, broken parts, per-piece rounding and native mitigation preserved");
        }catch(Exception error){throw new IllegalStateException("Armor HUD GPU verification failed",error);}
        finally{
            for(var entry:previous.entrySet())mc.player.setItemSlot(entry.getKey(),entry.getValue());attribute.setBaseValue(oldBase);mc.gameMode.setLocalMode(oldMode,oldPrevious);mc.options.hideGui=oldHidden;gui.leftHeight=oldHeight;
            target.destroyBuffers();mc.getMainRenderTarget().bindWrite(true);view.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.restoreProjectionMatrix();RenderSystem.disableDepthTest();RenderSystem.disableBlend();
        }
    }
    private static void clear(Minecraft mc){for(var slot:SLOTS)mc.player.setItemSlot(slot,ItemStack.EMPTY);}
    private static NativeImage frame(Minecraft mc,ForgeGui gui,TextureTarget target,String name,boolean overridden){
        int w=640,h=360;target.setClearColor(.12f,.15f,.18f,1);target.clear(Minecraft.ON_OSX);target.bindWrite(true);
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,w,h,0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);RenderSystem.setShaderColor(1,1,1,1);
        var graphics=new GuiGraphics(mc,mc.renderBuffers().bufferSource()){@Override public int guiWidth(){return 640;}@Override public int guiHeight(){return 360;}};graphics.pose().translate(0,0,-11000);graphics.drawString(mc.font,"Armor HUD: "+name,20,20,0xffffff,false);
        gui.leftHeight=39;var overlay=VanillaGuiOverlay.ARMOR_LEVEL.type();var event=new RenderGuiOverlayEvent.Pre(mc.getWindow(),graphics,0,overlay);
        boolean canceled=MinecraftForge.EVENT_BUS.post(event);if(canceled!=overridden)throw new IllegalStateException("Wrong Forge armor overlay dispatch: "+name);
        if(!canceled)overlay.overlay().render(gui,graphics,0,w,h);
        if(gui.leftHeight!=49)throw new IllegalStateException("Armor HUD did not reserve its vanilla row");graphics.flush();return Screenshot.takeScreenshot(target);
    }
    private ArmorHudVisualVerifier(){}
}
