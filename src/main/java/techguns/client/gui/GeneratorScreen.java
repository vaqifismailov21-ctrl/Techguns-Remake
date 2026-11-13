package techguns.client.gui;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;
import techguns.power.GeneratorMenu;
import techguns.power.PowerBlocks;

/** Generators in the Techguns CE machine style: bronze panel, energy column, fuel slot and flame, tank or heat column, read-out screen. */
public final class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    private static final int TANK_X=46,TANK_Y=18,TANK_W=10,TANK_H=50;
    private final ResourceLocation texture;
    public GeneratorScreen(GeneratorMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title);imageWidth=176;imageHeight=198;inventoryLabelY=imageHeight-96+2;
        texture=ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/ce_generator_"+(menu.nuclear?"nuclear":menu.liquid?"liquid":"fuel")+"_gui.png");
    }
    private Component cooling(){return Component.translatable(menu.overheated()?"techguns.power.overheated":menu.cooled()?"techguns.power.cooled":"techguns.power.not_cooled");}
    private boolean tank(){return menu.liquid||menu.nuclear;}

    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){
        int k=leftPos,l=topPos;g.blit(texture,k,l,0,0,imageWidth,imageHeight);
        LegacyMachineGui.drawEnergy(g,LegacyMachineGui.MACHINE,k,l,8,17,251,1,4,59,menu.energy(),Math.max(1,menu.capacity()));
        int burn=menu.burn();
        if(burn>0){int h=menu.liquid?14:Math.max(1,Math.min(14,burn*14/1000));g.blit(texture,k+27,l+39+14-h,176,14-h,14,h);}
        if(menu.liquid&&menu.tankAmount()>0&&menu.tankFluid()!=Fluids.EMPTY)
            CePowerGui.fluid(g,menu.tankFluid(),k+TANK_X,l+TANK_Y,TANK_W,TANK_H,Math.max(1,menu.tankAmount()*TANK_H/PowerBlocks.LIQUID_TANK));
        if(menu.nuclear)CePowerGui.column(g,k+TANK_X,l+TANK_Y,TANK_W,TANK_H,menu.heat(),PowerBlocks.NUCLEAR_HEAT_MAX,menu.overheated()?0xFFFF3020:0xFFFFC040,menu.overheated()?0xFF901010:0xFFE06010);
    }
    @Override protected void renderLabels(GuiGraphics g,int mouseX,int mouseY){
        GuiText.centered(g,font,title,15,imageWidth-15,6,4210752);
        g.drawString(font,playerInventoryTitle,8,inventoryLabelY,4210752,false);
        var lines=new ArrayList<Component>();
        lines.add(Component.translatable("techguns.power.stored",menu.energy()));
        lines.add(Component.translatable("techguns.power.max",menu.capacity()));
        lines.add(menu.burn()>0?Component.translatable("techguns.power.rate",menu.rate()):Component.translatable("techguns.power.idle").withStyle(s->s.withColor(CePowerGui.SCREEN_WARN)));
        if(menu.liquid)lines.add(menu.tankAmount()>0?Component.translatable("techguns.power.tank",menu.tankFluid().getFluidType().getDescription(),menu.tankAmount(),PowerBlocks.LIQUID_TANK):Component.translatable("techguns.gui.empty"));
        if(menu.nuclear){lines.add(Component.translatable("techguns.power.heat",menu.heat()*100/PowerBlocks.NUCLEAR_HEAT_MAX).withStyle(s->s.withColor(menu.overheated()?CePowerGui.SCREEN_WARN:CePowerGui.SCREEN_TEXT)));lines.add(cooling().copy().withStyle(s->s.withColor(menu.overheated()||!menu.cooled()?CePowerGui.SCREEN_WARN:CePowerGui.SCREEN_DIM)));}
        int x0=tank()?66:48;CePowerGui.screen(g,font,lines,x0,20,164,72);
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        renderBackground(g);super.render(g,mouseX,mouseY,partial);renderTooltip(g,mouseX,mouseY);
        int x=mouseX-leftPos,y=mouseY-topPos;
        if(CePowerGui.in(x,y,7,17,6,59))g.renderTooltip(font,Component.translatable("techguns.power.energy",menu.energy(),menu.capacity()),mouseX,mouseY);
        if(menu.nuclear&&CePowerGui.in(x,y,TANK_X-1,TANK_Y-1,TANK_W+3,TANK_H+3))g.renderComponentTooltip(font,java.util.List.of(Component.translatable("techguns.power.heat",menu.heat()*100/PowerBlocks.NUCLEAR_HEAT_MAX),cooling()),mouseX,mouseY);
        if(menu.liquid&&CePowerGui.in(x,y,TANK_X-1,TANK_Y-1,TANK_W+3,TANK_H+3))g.renderTooltip(font,menu.tankAmount()>0?Component.translatable("techguns.power.tank",menu.tankFluid().getFluidType().getDescription(),menu.tankAmount(),PowerBlocks.LIQUID_TANK):Component.translatable("techguns.gui.empty"),mouseX,mouseY);
    }
}
