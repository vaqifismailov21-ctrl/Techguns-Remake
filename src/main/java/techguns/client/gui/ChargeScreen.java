package techguns.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.machines.ChargeMenu;

/** Original ExplosiveChargeGui: no title or inventory label, three value lines, +/- pairs and the detonate button that closes the screen. */
public final class ChargeScreen extends AbstractContainerScreen<ChargeMenu> {
    public ChargeScreen(ChargeMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=176;imageHeight=166;}
    private void button(int id,int x,int y,int width,int height,Component label){addRenderableWidget(Button.builder(label,b->{minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);if(id==0)minecraft.player.closeContainer();}).bounds(leftPos+x,topPos+y,width,height).build());}
    @Override protected void init(){
        super.init();
        button(0,57,73,60,20,Component.translatable("techguns.charge.arm"));
        for(int row=0;row<3;row++){button(row*2+1,120,40+row*10,10,10,Component.literal("+"));button(row*2+2,130,40+row*10,10,10,Component.literal("-"));}
    }
    @Override protected void renderBg(GuiGraphics graphics,float partial,int x,int y){graphics.blit(ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/"+(menu.charge.advanced?"exp_charge":"tnt_charge")+"_gui.png"),leftPos,topPos,0,0,imageWidth,imageHeight);}
    @Override protected void renderLabels(GuiGraphics graphics,int x,int y){
        graphics.drawString(font,Component.translatable("techguns.charge.width",menu.data.get(0)),37,40,4210752,false);
        graphics.drawString(font,Component.translatable("techguns.charge.length",menu.data.get(1)),37,50,4210752,false);
        graphics.drawString(font,Component.translatable("techguns.charge.delay",menu.data.get(2)/20),37,60,4210752,false);
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial){renderBackground(g, x, y, partial);super.render(g,x,y,partial);renderTooltip(g,x,y);}
}
