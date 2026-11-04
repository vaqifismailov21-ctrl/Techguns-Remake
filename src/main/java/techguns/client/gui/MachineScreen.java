package techguns.client.gui;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import techguns.machines.*;
import static techguns.client.gui.LegacyMachineGui.*;

/**
 * Original AmmoPressGui, MetalPressGui, ChemLabGui, ReactionChamberGui, FabricatorGui, ChargingStationGui, GrinderGui and
 * BlastFurnaceGui on top of the shared TGBaseGui/OwnedTileEntGui/RedstoneTileEntGui/PoweredTileEntGui layers: original
 * artwork, textured redstone/security buttons, text buttons, labels, progress drawing and hover tooltips.
 */
public final class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    public static final int TANK_Y=17,INPUT_TANK_X=18,OUTPUT_TANK_X=157,TANK_W=10,TANK_H=50;
    private static final ResourceLocation CHEM_LAB=ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/chem_lab_gui.png");
    private final ResourceLocation texture;
    public MachineScreen(MachineMenu menu,Inventory inventory,Component title){super(menu,inventory,title);texture=ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/"+menu.machine.kind.texture()+"_gui.png");imageHeight=166;}
    private void send(int id){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    private void text(String label,int id,int x,int y,int width,int height){text(Component.literal(label),id,x,y,width,height);}
    private void text(Component label,int id,int x,int y,int width,int height){addRenderableWidget(LegacyButton.legacy(label,b->send(id)).bounds(leftPos+x,topPos+y,width,height).build());}
    private byte security(){return (byte)(menu.machine.access.restricted()?3:0);}

    @Override protected void init(){
        super.init();var kind=menu.machine.kind;
        // OwnedTileEntGui + RedstoneTileEntGui: GuiButtonSecurity and GuiButtonRedstoneState.
        addRenderableWidget(iconButton(leftPos-22,topPos+SECURITY_Y,()->send(1),MACHINE,234,()->58+16*security()));
        addRenderableWidget(iconButton(leftPos-22,topPos+10,()->send(2),MACHINE,234,()->10+16*menu.machine.redstoneMode));
        switch(kind){
            case AMMO_PRESS->{text(">",3,40,50,20,20);text("<",4,20,50,20,20);}
            case METAL_PRESS->text(Component.translatable("techguns.button.switch"),3,20,50,40,20);
            case CHEM_LAB->{text("x",3,20,7,8,8);text("x",4,159,7,8,8);
                // GuiButtonToggleDrainTank: 12x12 icon from the chemistry laboratory sheet.
                addRenderableWidget(new Button(leftPos+156,topPos+68,14,14,Component.empty(),b->send(5),s->s.get()){
                    @Override protected void renderWidget(GuiGraphics graphics,int mouseX,int mouseY,float partial){super.renderWidget(graphics,mouseX,mouseY,partial);graphics.blit(CHEM_LAB,getX()+1,getY()+1,188,31+12*(menu.machine.drainInput?1:0),12,12);}
                });}
            case REACTION->{text("+",3,33,58,10,10);text("-",4,43,58,10,10);text("+",5,91,36,10,10);text("-",6,101,36,10,10);text("x",7,20,7,8,8);}
            default->{}
        }
    }

    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY){
        var machine=menu.machine;var kind=machine.kind;int k=leftPos,l=topPos;
        graphics.blit(texture,k,l,0,0,imageWidth,imageHeight);
        drawPowered(graphics,k,l,machine.redstoneEnabled(),machine.energy.getEnergyStored(),kind.capacity);
        // PoweredTileEntGui upgrade slot frame (the reaction chamber has none).
        if(kind.upgrade>=0){var xy=kind.slots[kind.upgrade];graphics.blit(MACHINE,k+xy[0]-1,l+xy[1]-1,119,59,20,20);}
        boolean working=machine.running();float fraction=machine.totalTime>0?Math.min(1,machine.progress/(float)machine.totalTime):0;
        switch(kind){
            case AMMO_PRESS,METAL_PRESS->{if(working)graphics.blit(texture,k+119,l+36,176,0,19,scaled(21)+1);}
            case BLAST_FURNACE->{if(working)graphics.blit(texture,k+19,l+53,0,167,scaled(90)+1,11);}
            case FABRICATOR->{if(working)graphics.blit(texture,k+21,l+53,0,167,scaled(90)+1,11);}
            case CHARGING_STATION->{if(working)graphics.blit(texture,k+39,l+19,0,167,scaled(26)+1,10);}
            case GRINDER->{if(working){int i1=scaled(21);graphics.blit(texture,k+31,l+39,0,167,i1+1,22);int i2=20-i1;graphics.blit(texture,k+31+20+i2,l+39,i2,167,i1+1,22);}}
            case CHEM_LAB->{
                tank(graphics,machine.inputTank,k+INPUT_TANK_X+1,l+TANK_Y);tank(graphics,machine.outputTank,k+OUTPUT_TANK_X+1,l+TANK_Y);
                graphics.blit(texture,k+INPUT_TANK_X,l+TANK_Y,176,32,TANK_W+2,TANK_H+2);graphics.blit(texture,k+OUTPUT_TANK_X,l+TANK_Y,176,32,TANK_W+2,TANK_H+2);
                if(working){float prog=fraction;
                    graphics.blit(texture,k+81,l+19,178,5,8,(int)((prog<.2f?prog*5:1f)*25));
                    if(prog>=.2f)graphics.blit(texture,k+88,l+39,186,25,(int)((prog<.4f?(prog-.2f)*5:1f)*9),1);
                    if(prog>=.4f){int i3=(int)((prog<.6f?(prog-.4f)*5:1f)*20);graphics.blit(texture,k+97,l+23+(20-i3),194,9+(20-i3),14,i3);}
                    if(prog>=.6f)graphics.blit(texture,k+108,l+17,205,3,(int)((prog<.8f?(prog-.6f)*5:1f)*16),6);
                    if(prog>=.8f)graphics.blit(texture,k+117,l+20,214,6,14,(int)((prog-.8f)*5*24)+1);}
            }
            case REACTION->{
                var reactor=machine.reactor;tank(graphics,machine.inputTank,k+INPUT_TANK_X+1,l+TANK_Y);
                graphics.blit(texture,k+18,l+17,176,32,12,52);graphics.blit(texture,k+19,l+17+(50-reactor.liquidLevel*5),177,32,7,1);
                int px=(10-reactor.intensity)*4;var recipe=reactor.recipe();
                if(working&&recipe!=null){
                    graphics.blit(texture,k+67,l+61,0,167,Math.round(reactor.completion*1f/recipe.requiredCompletion()*100f),4);
                    graphics.blit(texture,k+67,l+69,0,175,Math.round(fraction*100f),4);
                    graphics.blit(texture,k+114,l+16+px,reactor.requiredIntensity!=reactor.intensity?198:190,px,5,40-px);
                    graphics.blit(texture,k+112,l+15+40-reactor.requiredIntensity*4,178,22,5,3);
                }else graphics.blit(texture,k+114,l+16+px,190,px,5,40-px);
            }
        }
    }
    /** BasicMachineTileEnt.getProgressScaled. */
    private int scaled(int size){var m=menu.machine;return m.totalTime<=0?0:m.progress*size/m.totalTime;}
    private static void tank(GuiGraphics graphics,FluidTank tank,int x,int y){if(tank.getFluidAmount()>0)drawFluid(graphics,tank.getFluid(),x,y,TANK_W,TANK_H,tank.getFluidAmount()*TANK_H/tank.getCapacity());}

    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){
        var machine=menu.machine;int color=4210752;
        // TGBaseGui.drawInventoryContainerName; long translations are kept clear of the dump buttons.
        int start=machine.kind==MachineKind.REACTION||machine.kind==MachineKind.CHEM_LAB?32:20;GuiText.centered(graphics,font,title,start,imageWidth-start,6,color);
        graphics.drawString(font,playerInventoryTitle,8,imageHeight-96+2,color,false);
        switch(machine.kind){
            case AMMO_PRESS->{String[] names={"pistolrounds","shotgunrounds","riflerounds","sniperrounds"};graphics.drawString(font,Component.translatable("techguns.gui.ammopress.build").append(":"),20,30,color,false);GuiText.fitted(graphics,font,Component.translatable("item.techguns."+names[machine.plan]),20,40,94,color);}
            case METAL_PRESS->{graphics.drawString(font,Component.translatable("techguns.container.metalpress.autosplit").append(":"),20,30,color,false);graphics.drawString(font,Component.translatable(machine.autoSplitMode==0?"techguns.container.info.off":"techguns.container.info.on"),20,40,color,false);}
            case REACTION->{graphics.drawString(font,(machine.reactor.liquidLevel*10)+"%",34,50,color,false);graphics.drawString(font,""+machine.reactor.intensity,121,18,color,false);}
            default->{}
        }
    }

    private List<Component> hoverTooltip(int mx,int my){
        var machine=menu.machine;boolean signal=minecraft.level!=null&&minecraft.level.hasNeighborSignal(machine.getBlockPos());
        var tooltip=poweredTooltip(mx,my,signal,machine.redstoneMode,security(),machine.access.ownerId(),machine.energy.getEnergyStored(),machine.kind.capacity);
        if(tooltip!=null)return tooltip;
        if(machine.kind==MachineKind.CHEM_LAB){
            if(in(mx,my,INPUT_TANK_X,TANK_Y-1,TANK_W+1,TANK_H+1))return tankTooltip(machine.inputTank);
            if(in(mx,my,OUTPUT_TANK_X,TANK_Y-1,TANK_W+1,TANK_H+1))return tankTooltip(machine.outputTank);
            if(in(mx,my,20,7,8,8))return List.of(Component.translatable("techguns.chemlab.dumpinput"),Component.translatable("techguns.chemlab.dumpinput.tooltip"));
            if(in(mx,my,159,7,8,8))return List.of(Component.translatable("techguns.chemlab.dumpoutput"),Component.translatable("techguns.chemlab.dumpoutput.tooltip"));
            if(in(mx,my,156,68,14,14))return List.of(Component.translatable("techguns.chemlab.toogledrain"),Component.translatable("techguns.chemlab.toogledrain.tooltip."+(machine.drainInput?1:0)));
        }else if(machine.kind==MachineKind.REACTION){
            if(in(mx,my,18,16,11,51))return tankTooltip(machine.inputTank);
            if(in(mx,my,20,7,8,8))return List.of(Component.translatable("techguns.chemlab.dumpinput"),Component.translatable("techguns.chemlab.dumpinput.tooltip"));
        }
        return null;
    }
    private static List<Component> tankTooltip(FluidTank tank){return List.of(tank.isEmpty()?Component.translatable("techguns.gui.empty"):tank.getFluid().getDisplayName(),Component.literal(tank.getFluidAmount()+"/"+tank.getCapacity()+"mB"));}

    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial){
        renderBackground(graphics);super.render(graphics,mouseX,mouseY,partial);renderTooltip(graphics,mouseX,mouseY);
        var tooltip=hoverTooltip(mouseX-leftPos,mouseY-topPos);if(tooltip!=null)graphics.renderComponentTooltip(font,tooltip,mouseX,mouseY);
    }
}
