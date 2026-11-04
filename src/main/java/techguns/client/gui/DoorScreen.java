package techguns.client.gui;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.machines.DoorMenu;
import static techguns.client.gui.LegacyMachineGui.*;

/**
 * Original Door3x3Gui on the RedstoneTileEntGui layer: dungeon scanner artwork, security and redstone side buttons,
 * "Mode" and yes/no auto-close buttons, mode and auto-close lines and the red warning for redstone mode without a
 * redstone setting.
 */
public final class DoorScreen extends AbstractContainerScreen<DoorMenu> {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/dungeon_scanner_gui.png");
    // Menu button ids of the port: 0 mode, 1 auto close, 2 redstone mode, 3 security.
    private Button autoClose;
    public DoorScreen(DoorMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=176;imageHeight=166;}
    private void send(int id){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    private byte security(){return (byte)(menu.data.get(3)==1?3:0);}
    private int redstoneMode(){return menu.data.get(2);}
    private boolean signal(){return minecraft.level!=null&&minecraft.level.hasNeighborSignal(menu.door.getBlockPos());}

    @Override protected void init(){
        super.init();
        addRenderableWidget(iconButton(leftPos-22,topPos+SECURITY_Y,()->send(3),MACHINE,234,()->58+16*security()));
        addRenderableWidget(iconButton(leftPos-22,topPos+10,()->send(2),MACHINE,234,()->10+16*redstoneMode()));
        addRenderableWidget(LegacyButton.legacy(Component.translatable("techguns.mode"),b->send(0)).bounds(leftPos+8,topPos+16,32,16).build());
        autoClose=addRenderableWidget(LegacyButton.legacy(Component.empty(),b->send(1)).bounds(leftPos+8,topPos+16+20,32,16).build());
    }
    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY){
        graphics.blit(TEXTURE,leftPos,topPos,0,0,imageWidth,imageHeight);
        int mode=redstoneMode();boolean signal=signal();
        drawRedstone(graphics,leftPos,topPos,mode==0||signal&&mode==1||!signal&&mode==2);
    }
    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){
        int color=4210752,doorMode=menu.data.get(0);
        graphics.drawString(font,Component.translatable("techguns.container.door3x3.mode."+doorMode),44,20,color,false);
        var close=Component.translatable("techguns.container.door3x3.autoclose");if(doorMode!=0)close.withStyle(ChatFormatting.STRIKETHROUGH);
        graphics.drawString(font,close,44,40,color,false);
        if(doorMode==1&&redstoneMode()==0)graphics.drawString(font,Component.translatable("techguns.container.door3x3.redstonewarning").withStyle(ChatFormatting.RED),44,60,color,false);
        // TGBaseGui.drawInventoryContainerName without the inventory line.
        graphics.drawString(font,title,imageWidth/2-font.width(title)/2,6,color,false);
    }
    @Override public void render(GuiGraphics graphics,int x,int y,float partial){
        autoClose.setMessage(Component.translatable(menu.data.get(1)==1?"techguns.yes":"techguns.no"));
        renderBackground(graphics);super.render(graphics,x,y,partial);renderTooltip(graphics,x,y);
        int mx=x-leftPos,my=y-topPos;
        List<Component> tooltip=redstoneTooltip(mx,my,signal(),redstoneMode());if(tooltip==null)tooltip=securityTooltip(mx,my,SECURITY_Y,security(),menu.door.access.ownerId());
        if(tooltip!=null)graphics.renderComponentTooltip(font,tooltip,x,y);
    }
}
