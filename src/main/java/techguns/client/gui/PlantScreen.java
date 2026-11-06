package techguns.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import techguns.power.plant.FissionReactor;
import techguns.power.plant.PlantKind;
import techguns.power.plant.PlantMenu;

/** Power plant GUI: energy, water and heat bars, state, output and the plant's own numbers; the reactor has fuel slots. */
public final class PlantScreen extends AbstractContainerScreen<PlantMenu> {
    private static final int BAR_Y=17,BAR_H=52,TEXT_X=58;
    public PlantScreen(PlantMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageHeight=166;inventoryLabelY=imageHeight-94;}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){renderBackground(g);super.render(g,mouseX,mouseY,partial);renderTooltip(g,mouseX,mouseY);tooltips(g,mouseX-leftPos,mouseY-topPos,mouseX,mouseY);}
    private boolean water(){return menu.kind==PlantKind.FISSION||menu.kind==PlantKind.FUSION;}
    private void tooltips(GuiGraphics g,int x,int y,int mx,int my){
        if(y<BAR_Y||y>=BAR_Y+BAR_H)return;
        if(x>=8&&x<20)g.renderTooltip(font,Component.translatable("techguns.plant.energy",menu.energy(),menu.capacity()),mx,my);
        else if(water()&&x>=24&&x<36)g.renderTooltip(font,Component.translatable("techguns.plant.water",menu.water(),menu.tank()),mx,my);
        else if(menu.kind==PlantKind.FISSION&&x>=40&&x<52)g.renderTooltip(font,Component.translatable("techguns.plant.heat",menu.heat(),FissionReactor.HEAT_MAX),mx,my);
    }
    private static void panel(GuiGraphics g,int x,int y,int w,int h){
        g.fill(x,y,x+w,y+h,0xFFC6C6C6);g.fill(x,y,x+w-1,y+1,0xFFFFFFFF);g.fill(x,y,x+1,y+h-1,0xFFFFFFFF);
        g.fill(x+1,y+h-1,x+w,y+h,0xFF555555);g.fill(x+w-1,y+1,x+w,y+h,0xFF555555);
    }
    private static void slot(GuiGraphics g,int x,int y){g.fill(x,y,x+18,y+18,0xFF8B8B8B);g.fill(x,y,x+17,y+1,0xFF373737);g.fill(x,y,x+1,y+17,0xFF373737);g.fill(x+1,y+17,x+18,y+18,0xFFFFFFFF);g.fill(x+17,y+1,x+18,y+18,0xFFFFFFFF);}
    private static void bar(GuiGraphics g,int x,int fill,int max,int colourTop,int colourBottom){
        g.fill(x,BAR_Y,x+12,BAR_Y+BAR_H,0xFF373737);g.fill(x+1,BAR_Y+1,x+11,BAR_Y+BAR_H-1,0xFF1E1E1E);
        int h=max<=0?0:(int)Math.min(BAR_H-2,(long)fill*(BAR_H-2)/max);
        if(h>0)g.fillGradient(x+1,BAR_Y+BAR_H-1-h,x+11,BAR_Y+BAR_H-1,colourTop,colourBottom);
        for(int i=1;i<5;i++)g.fill(x+8,BAR_Y+1+i*(BAR_H-2)/5,x+11,BAR_Y+2+i*(BAR_H-2)/5,0x80FFFFFF);
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){
        int x=leftPos,y=topPos;panel(g,x,y,imageWidth,imageHeight);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(g,x+7+col*18,y+83+row*18);
        for(int col=0;col<9;col++)slot(g,x+7+col*18,y+141);
        if(menu.slotted()){slot(g,x+PlantMenu.FUEL_X-1,y+PlantMenu.FUEL_Y-1);slot(g,x+PlantMenu.FUEL_X-1,y+PlantMenu.SPENT_Y-1);}
        g.pose().pushPose();g.pose().translate(x,y,0);
        bar(g,8,menu.energy(),menu.capacity(),0xFFFF4040,0xFF8B0000);
        if(water())bar(g,24,menu.water(),menu.tank(),0xFF5080FF,0xFF1030A0);
        if(menu.kind==PlantKind.FISSION)bar(g,40,menu.heat(),FissionReactor.HEAT_MAX,0xFFFFE040,0xFFFF4000);
        g.pose().popPose();
    }
    @Override protected void renderLabels(GuiGraphics g,int mouseX,int mouseY){
        g.drawString(font,title,8,6,0x404040,false);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,0x404040,false);
        int x=menu.kind==PlantKind.WIND||menu.kind==PlantKind.GEOTHERMAL?24:menu.kind==PlantKind.FUSION?40:56;int y=18;int width=(menu.slotted()?148:170)-x;
        var lines=new java.util.ArrayList<Component>();
        lines.add(!menu.formed()?Component.translatable("techguns.plant.not_formed").withStyle(net.minecraft.ChatFormatting.DARK_RED)
                :menu.problem()==0?Component.translatable("techguns.plant.working").withStyle(net.minecraft.ChatFormatting.DARK_GREEN)
                :Component.translatable("techguns.plant."+menu.kind.id()+".problem"+menu.problem()).withStyle(net.minecraft.ChatFormatting.DARK_RED));
        lines.add(Component.translatable("techguns.plant.output",menu.rate()));
        switch(menu.kind){
            case WIND->{lines.add(Component.translatable("techguns.plant.wind_plant.towers",menu.extra()));lines.add(Component.translatable("techguns.plant.wind_plant.obstacles",menu.heat()));}
            case GEOTHERMAL->lines.add(Component.translatable("techguns.plant.geothermal_plant.lava",menu.extra()));
            case FISSION->{lines.add(Component.translatable("techguns.plant.fission_reactor.fuel",menu.extra()/10));lines.add(Component.translatable("techguns.plant.heat",menu.heat(),FissionReactor.HEAT_MAX));}
            case FUSION->lines.add(menu.extra()>=1000?Component.translatable("techguns.plant.fusion_reactor.burning").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE):Component.translatable("techguns.plant.fusion_reactor.ignition",menu.extra()/10));
        }
        for(var line:lines)for(var part:font.split(line,width)){g.drawString(font,part,x,y,0x404040,false);y+=10;}
    }
}
