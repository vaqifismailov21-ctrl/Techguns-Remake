package techguns.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import techguns.TGConfig;
import techguns.oredrill.OreDrillMenu;
import techguns.oredrill.OreDrillTileEntMaster;
import static techguns.client.gui.LegacyMachineGui.*;

/** Original OreDrillGui: tanks, progress bar, fuel buffer gauge, "broken"/"drill required" notes and the original tooltips. */
public final class OreDrillScreen extends AbstractContainerScreen<OreDrillMenu> {
    public static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/ore_drill_gui.png");
    /** CE style frame (grinder layout); the old controls sit DX further right in it. Sprites still come from TEXTURE. */
    public static final ResourceLocation BACKGROUND=ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/ce_ore_drill_gui.png");
    public static final int DX=11;
    public static final int INPUT_TANK_X=15+DX, TANK_Y=17, OUTPUT_TANK_X=157+DX, TANK_W=10, TANK_H=50;
    private final OreDrillTileEntMaster tile;
    public OreDrillScreen(OreDrillMenu menu,Inventory inventory,Component title){super(menu,inventory,title);tile=menu.tile;imageWidth=199;imageHeight=188;inventoryLabelX=18;inventoryLabelY=imageHeight-94;}
    private void send(int id){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}

    @Override protected void init(){
        super.init();
        addRenderableWidget(iconButton(leftPos-22,topPos+SECURITY_Y,()->send(OreDrillTileEntMaster.BUTTON_ID_SECURITY),MACHINE,234,()->58+16*tile.getSecurity()));
        addRenderableWidget(iconButton(leftPos-22,topPos+10,()->send(OreDrillTileEntMaster.BUTTON_ID_REDSTONE),MACHINE,234,()->10+16*tile.getRedstoneBehaviour()));
    }

    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY){
        int k=leftPos,l=topPos;
        graphics.blit(BACKGROUND,k,l,0,0,imageWidth,imageHeight);
        drawRedstone(graphics,k,l,tile.isRedstoneEnabled(),Look.REGULAR);
        drawEnergy(graphics,MACHINE,k,l,8+DX,17,251,1,4,48,tile.energy.getEnergyStored(),tile.energy.getMaxEnergyStored());
        if(tile.isFormed()&&tile.getDrillItemMiningLevel()<0)graphics.blit(TEXTURE,k+52+DX,l+14,203,0,6,20);
        tank(graphics,tile.inputTank,k+INPUT_TANK_X+1,l+TANK_Y);
        tank(graphics,tile.outputTank,k+OUTPUT_TANK_X+1,l+TANK_Y);
        graphics.blit(TEXTURE,k+INPUT_TANK_X,l+TANK_Y,176,40,TANK_W+2,TANK_H+2);
        graphics.blit(TEXTURE,k+OUTPUT_TANK_X,l+TANK_Y,176,40,TANK_W+2,TANK_H+2);
        if(tile.isWorking()){int i1=tile.getProgressScaled(36);graphics.blit(TEXTURE,k+73+DX,l+17,177,1,25,i1+1);}
        if(tile.getCurrentFuelBufferMax()>0){int s=12-tile.getFuelBufferScaled(12);graphics.blit(TEXTURE,k+35+DX,l+36+s,188,39+s,6,12-s);}
    }
    private static void tank(GuiGraphics graphics,FluidTank tank,int x,int y){if(tank.getFluidAmount()>0)drawFluid(graphics,tank.getFluid(),x,y,TANK_W,TANK_H,tank.getFluidAmount()*TANK_H/tank.getCapacity());}

    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){
        graphics.drawString(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,4210752,false);
        graphics.drawString(font,title,imageWidth/2-font.width(title)/2,5,4210752,false);
        int red=0xFFFF2020;
        if(!tile.isFormed()){
            String s1=Component.translatable("techguns.container.oredrill.broken").getString();
            graphics.drawString(font,s1,(imageWidth-font.width(s1))/2,72-font.lineHeight,red,false);
        }
    }

    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial){
        renderBackground(graphics);super.render(graphics,mouseX,mouseY,partial);renderTooltip(graphics,mouseX,mouseY);
        int mx=mouseX-leftPos,my=mouseY-topPos;
        boolean signal=minecraft.level!=null&&minecraft.level.hasNeighborSignal(tile.getBlockPos());
        List<Component> tooltip=energyTooltip(mx,my,8+DX,17,4,48,tile.energy.getEnergyStored(),tile.energy.getMaxEnergyStored());
        if(tooltip==null)tooltip=redstoneTooltip(mx,my,signal,tile.getRedstoneBehaviour());
        if(tooltip==null)tooltip=securityTooltip(mx,my,SECURITY_Y,tile.getSecurity(),tile.getOwner());
        if(tooltip==null){
            if(in(mx,my,74+DX,16,27,39)){
                var lines=new ArrayList<Component>();
                // no operation yet (no fitting drill head, no cluster): no division by a zero duration
                boolean running=tile.totaltime>0;
                lines.add(Component.literal(running?String.format("%.2f",tile.progress*1.0f/tile.totaltime*100.0f)+"%":"0.00%"));
                lines.add(Component.translatable("techguns.gui.oredrill.orehour").append(": "+(running?String.format("%.2f",1200.0/tile.totaltime*60.0):"—")));
                if(tile.getPowerPerTick()>0)lines.add(Component.translatable("techguns.gui.oredrill.rftick").append(": "+tile.getPowerPerTick()));
                lines.add(Component.empty());
                lines.add(Component.translatable("techguns.oredrill.drillsize").append(": "+(tile.getRods()+tile.getEngines())));
                lines.add(Component.translatable("techguns.oredrill.drillradius").append(": "+tile.getDrillRadius()));
                tooltip=lines;
            }else if(in(mx,my,INPUT_TANK_X,TANK_Y-1,TANK_W+1,TANK_H+1))tooltip=tankTooltip(tile.inputTank);
            else if(in(mx,my,OUTPUT_TANK_X,TANK_Y-1,TANK_W+1,TANK_H+1))tooltip=tankTooltip(tile.outputTank);
            else if(in(mx,my,27+DX,35,15,15)){
                if(tile.getCurrentFuelBufferMax()>0){
                    int buf=tile.getCurrentFuelBufferMax(),fuel=tile.getFuelBuffer(),pwrtick=tile.getPowerPerTick();float factor=TGConfig.oreDrillMultiplierFuel;
                    int ticksLeft=factor<=0||pwrtick<=0?-1:(int)(fuel/(pwrtick/factor));
                    tooltip=List.of(Component.translatable("techguns.oredrill.currentfuel").append(" "+fuel+"/"+buf),Component.translatable("techguns.oredrill.remaining").append(": "+ticksLeft+" ").append(Component.translatable("techguns.oredrill.ticks")));
                }else tooltip=List.of(Component.translatable("techguns.oredrill.nofuel"));
            }
            else if(in(mx,my,52+DX,14,6,20)&&tile.isFormed()&&tile.getDrillItemMiningLevel()<0){
                int rad=tile.getDrillRadius();String size=rad==0?"0":rad==1||rad==2?"1":"2";
                var lines=new ArrayList<Component>();for(var line:Component.translatable("techguns.container.oredrill.drillrequired."+size).getString().split("\\$"))lines.add(Component.literal(line));tooltip=lines;
            }
        }
        if(tooltip!=null)graphics.renderComponentTooltip(font,tooltip,mouseX,mouseY);
    }
    private static List<Component> tankTooltip(FluidTank tank){
        return List.of(tank.isEmpty()?Component.translatable("techguns.gui.empty"):tank.getFluid().getDisplayName(),Component.literal(tank.getFluidAmount()+"/"+tank.getCapacity()+"mB"));
    }
}
