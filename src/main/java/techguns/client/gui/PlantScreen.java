package techguns.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;
import techguns.power.plant.FissionReactor;
import techguns.power.plant.PlantKind;
import techguns.power.plant.PlantMenu;

/**
 * Power plants in the Techguns CE machine style: wind and geothermal plants on the grey panel, the reactors on the white
 * panel with the hazard stripes and the tall energy column; water and heat in tanks, the state on the read-out screen.
 */
public final class PlantScreen extends AbstractContainerScreen<PlantMenu> {
    private static final int TANK_Y=31,TANK_W=18,TANK_H=86,WATER_X=8,HEAT_X=31;
    private final ResourceLocation texture;
    private final boolean advanced;
    public PlantScreen(PlantMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title);advanced=PlantMenu.advanced(menu.kind);
        imageWidth=advanced?210:199;imageHeight=advanced?238:188;inventoryLabelY=imageHeight-96+2;inventoryLabelX=advanced?8:18;
        texture=ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/ce_plant_"+switch(menu.kind){case WIND->"wind";case GEOTHERMAL->"geothermal";case FISSION->"fission";case FUSION->"fusion";}+"_gui.png");
    }
    private boolean water(){return menu.kind==PlantKind.FISSION||menu.kind==PlantKind.FUSION;}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){renderBackground(g);super.render(g,mouseX,mouseY,partial);renderTooltip(g,mouseX,mouseY);tooltips(g,mouseX-leftPos,mouseY-topPos,mouseX,mouseY);}
    private void tooltips(GuiGraphics g,int x,int y,int mx,int my){
        var energy=Component.translatable("techguns.plant.energy",menu.energy(),menu.capacity());
        if(advanced?CePowerGui.in(x,y,186,30,16,95):CePowerGui.in(x,y,7,17,6,48))g.renderTooltip(font,energy,mx,my);
        else if(water()&&CePowerGui.in(x,y,WATER_X,TANK_Y,TANK_W,TANK_H))g.renderTooltip(font,Component.translatable("techguns.plant.water",menu.water(),menu.tank()),mx,my);
        else if(menu.kind==PlantKind.FISSION&&CePowerGui.in(x,y,HEAT_X,TANK_Y,TANK_W,TANK_H))g.renderTooltip(font,Component.translatable("techguns.plant.heat",menu.heat(),FissionReactor.HEAT_MAX),mx,my);
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){
        int x=leftPos,y=topPos;g.blit(texture,x,y,0,0,imageWidth,imageHeight);
        if(advanced){
            LegacyMachineGui.drawEnergy(g,texture,x,y,186,30,210,0,16,95,menu.energy(),menu.capacity());
            if(menu.energy()>0)g.blit(texture,x+190,y+16,246,0,9,12);
            if(water()&&menu.water()>0)CePowerGui.fluid(g,Fluids.WATER,x+WATER_X,y+TANK_Y,TANK_W,TANK_H,Math.max(1,(int)((long)menu.water()*TANK_H/Math.max(1,menu.tank()))));
            if(menu.kind==PlantKind.FISSION)CePowerGui.column(g,x+HEAT_X,y+TANK_Y,TANK_W,TANK_H,menu.heat(),FissionReactor.HEAT_MAX,0xFFFFE040,0xFFFF4000);
        }else LegacyMachineGui.drawEnergy(g,LegacyMachineGui.MACHINE,x,y,8,17,251,1,4,48,menu.energy(),menu.capacity());
    }
    @Override protected void renderLabels(GuiGraphics g,int mouseX,int mouseY){
        if(advanced)GuiText.centered(g,font,title,46,130,2,0xFFB4EEF1);else GuiText.centered(g,font,title,15,imageWidth-15,5,4210752);
        g.drawString(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,4210752,false);
        var lines=new java.util.ArrayList<Component>();
        lines.add(!menu.formed()?Component.translatable("techguns.plant.not_formed").withStyle(s->s.withColor(CePowerGui.SCREEN_WARN))
                :menu.problem()==0?Component.translatable("techguns.plant.working").withStyle(net.minecraft.ChatFormatting.GREEN)
                :Component.translatable("techguns.plant."+menu.kind.id()+".problem"+menu.problem()).withStyle(s->s.withColor(CePowerGui.SCREEN_WARN)));
        lines.add(Component.translatable("techguns.plant.output",menu.rate()));
        switch(menu.kind){
            case WIND->{lines.add(Component.translatable("techguns.plant.wind_plant.towers",menu.extra()));lines.add(Component.translatable("techguns.plant.wind_plant.obstacles",menu.heat()));}
            case GEOTHERMAL->lines.add(Component.translatable("techguns.plant.geothermal_plant.lava",menu.extra()));
            case FISSION->{lines.add(Component.translatable("techguns.plant.fission_reactor.fuel",menu.extra()/10));lines.add(Component.translatable("techguns.plant.heat",menu.heat(),FissionReactor.HEAT_MAX));}
            case FUSION->lines.add(menu.extra()>=1000?Component.translatable("techguns.plant.fusion_reactor.burning").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE):Component.translatable("techguns.plant.fusion_reactor.ignition",menu.extra()/10));
        }
        if(!advanced)CePowerGui.screen(g,font,lines,20,19,188,68);
        else if(menu.kind==PlantKind.FISSION)CePowerGui.screen(g,font,lines,57,35,142,112);
        else CePowerGui.screen(g,font,lines,33,35,168,112);
    }
}
