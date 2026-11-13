package techguns.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import techguns.TGSounds;
import techguns.machines.*;
import static techguns.client.gui.LegacyMachineGui.*;

/**
 * Techguns CE machine screens (AmmoPressGui, MetalPressGui, ChemLabGui, GrinderGui, FabricatorGui, ReactionChamberGui,
 * BlastFurnaceGui, ChargingStationGui) on top of the shared OwnedTileEntGui/RedstoneTileEntGui/PoweredTileEntGui layers.
 */
public final class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final ResourceLocation GAUGES=tex("gauges"),FABRICATOR_EXTRA=tex("fabricator_gui_1");
    private final ResourceLocation texture;
    private final Look look;
    private final boolean advanced;
    private boolean steamPanel;

    public MachineScreen(MachineMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title);var kind=menu.machine.kind;
        texture=tex(switch(kind){case AMMO_PRESS->"ammo_press_gui";case METAL_PRESS->"ce_metal_press_gui";case CHEM_LAB->"ce_chem_lab_gui";case GRINDER->"ce_grinder_gui";
            case FABRICATOR->"ce_fabricator_gui";case REACTION->"ce_reaction_chamber_gui";case BLAST_FURNACE->"blast_furnace_gui";case CHARGING_STATION->"charging_station_gui";});
        look=switch(kind){case AMMO_PRESS,METAL_PRESS->Look.BRONZISH;case FABRICATOR,REACTION->Look.ADVANCED;default->Look.REGULAR;};
        advanced=kind==MachineKind.FABRICATOR||kind==MachineKind.REACTION;
        switch(kind){
            case AMMO_PRESS,METAL_PRESS->{imageWidth=176;imageHeight=198;}
            case CHEM_LAB,GRINDER->{imageWidth=199;imageHeight=188;}
            case FABRICATOR,REACTION->{imageWidth=210;imageHeight=238;}
            default->{imageWidth=176;imageHeight=166;}
        }
        steamPanel=kind==MachineKind.METAL_PRESS&&menu.machine.steamUpgrade();if(steamPanel)imageWidth=201;
        inventoryLabelX=kind==MachineKind.CHEM_LAB||kind==MachineKind.GRINDER?18:8;
    }
    private static ResourceLocation tex(String name){return ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/"+name+".png");}
    private void send(int id){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    private void click(){minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK,1.0F));}
    private byte security(){return (byte)(menu.machine.access.restricted()?3:0);}
    private int securityY(){return advanced?19:SECURITY_Y;}

    @Override protected void init(){
        super.init();
        // OwnedTileEntGui + RedstoneTileEntGui: GuiButtonSecurity and GuiButtonRedstoneState (no redstone control on the advanced machines).
        addRenderableWidget(iconButton(leftPos-22,topPos+securityY(),()->send(1),MACHINE,234,()->58+16*security()));
        if(!advanced)addRenderableWidget(iconButton(leftPos-22,topPos+10,()->send(2),MACHINE,234,()->10+16*menu.machine.redstoneMode));
    }
    /** MetalPressGui.updateLayout: the steam panel widens the screen once a steam upgrade is installed. */
    @Override protected void containerTick(){
        super.containerTick();
        if(menu.machine.kind==MachineKind.METAL_PRESS&&menu.machine.steamUpgrade()!=steamPanel){steamPanel=!steamPanel;imageWidth=steamPanel?201:176;init(minecraft,width,height);}
    }

    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY){
        var machine=menu.machine;var kind=machine.kind;int k=leftPos,l=topPos;
        graphics.blit(texture,k,l,0,0,imageWidth,imageHeight);
        if(advanced)drawSecurityPanel(graphics,k,l,securityY(),look);else drawRedstone(graphics,k,l,machine.redstoneEnabled(),look);
        int energy=machine.energy.getEnergyStored(),max=kind.capacity;
        switch(kind){
            case AMMO_PRESS,METAL_PRESS->drawEnergy(graphics,MACHINE,k,l,8,17,251,1,4,59,energy,max);
            case FABRICATOR,REACTION->drawEnergy(graphics,texture,k,l,186,30,210,0,16,95,energy,max);
            default->drawEnergy(graphics,MACHINE,k,l,8,17,251,1,4,48,energy,max);
        }
        if(kind.upgrade>=0&&!advanced){var xy=kind.slots[kind.upgrade];drawUpgradeFrame(graphics,k,l,xy[0],xy[1],look);}
        boolean working=machine.running();
        switch(kind){
            case AMMO_PRESS->{
                graphics.blit(texture,k+42,l+32,227,132+Math.floorMod(machine.plan,4)*31,29,31);
                if(working)graphics.blit(texture,k+119,l+36,176,0,19,scaled(21)+1);
                if(russian())graphics.blit(RU_LABELS,k+40,l+18,0,33,33,9);
            }
            case METAL_PRESS->{
                if(working)graphics.blit(texture,k+119,l+36,176,0,19,scaled(21)+1);
                if(steamPanel){
                    var tank=machine.inputTank;if(tank.getFluidAmount()>0)drawFluid(graphics,tank.getFluid(),k+185,l+12,10,50,tank.getFluidAmount()*50/tank.getCapacity());
                    graphics.blit(GAUGES,k+181,l+68,0,Math.max(0,Math.min(ProcessingMachine.MAX_PRESSURE,machine.pressure))*18,18,18);
                }
                if(machine.autoSplitMode==1)graphics.blit(GAUGES,k+35,l+56,200,21,44,12);
                if(russian())graphics.blit(RU_LABELS,k+30,l+33,0,24,56,9);
            }
            case CHEM_LAB->chemLab(graphics,k,l,working);
            case GRINDER->{
                int progress=scaled(52);if(progress>0)graphics.blit(texture,k+51,l+29,204,0,progress,31);
                float ratio=machine.totalTime>0?Math.max(0,Math.min(1,machine.progress/(float)machine.totalTime)):0;
                saw(graphics,k+62,l+15,198,-ratio*1080F);saw(graphics,k+62,l+44,227,ratio*1080F);
            }
            case FABRICATOR->fabricator(graphics,k,l,working);
            case REACTION->reaction(graphics,k,l,mouseX-k,mouseY-l,working);
            case BLAST_FURNACE->{if(working)graphics.blit(texture,k+19,l+53,0,167,scaled(90)+1,11);}
            case CHARGING_STATION->{if(working)graphics.blit(texture,k+39,l+19,0,167,scaled(26)+1,10);}
        }
    }
    /** GrinderGui.drawSaw: a 29 px saw blade turned about its centre. */
    private void saw(GuiGraphics graphics,int x,int y,int v,float angle){
        var pose=graphics.pose();pose.pushPose();RenderSystem.enableBlend();
        pose.translate(x+14.5F,y+14.5F,0);if(angle!=0)pose.mulPose(Axis.ZP.rotationDegrees(angle));pose.translate(-14.5F,-14.5F,0);
        graphics.blit(texture,0,0,227,v,29,29);pose.popPose();
    }
    private void chemLab(GuiGraphics graphics,int k,int l,boolean working){
        var machine=menu.machine;
        tank(graphics,machine.inputTank,k+21,l+17);tank(graphics,machine.outputTank,k+178,l+17);
        graphics.blit(texture,k+20,l+16,202,0,12,52);graphics.blit(texture,k+177,l+16,202,0,12,52);
        if(!machine.drainInput)graphics.blit(texture,k+177,l+69,214,29,12,6);
        else{graphics.blit(texture,k+20,l+69,214,29,12,6);graphics.blit(texture,k+91,l+57,214,35,29,17);}
        if(working){
            float prog=machine.totalTime>0?machine.progress/(float)machine.totalTime:0;int x=k+69,y=l+21;
            if(prog>0){int h=(int)(27*Math.min(prog,.2F)*5+.5F);if(h>0)graphics.blit(texture,x,l+25+27-h,185,229+27-h,11,h);}
            if(prog>.2F){int filled=(int)(29*(Math.min(prog,.4F)-.2F)*5+.5F);if(filled>0)graphics.blit(texture,x+11,y,196,225,Math.min(filled,26),31);if(filled>26)graphics.blit(texture,x+37,y,222,225,filled-26,11);}
            if(prog>.4F){int h=(int)(28*(Math.min(prog,.6F)-.4F)*5+.5F);if(h>0)graphics.blit(texture,x+37,y+3,222,228,16,h);}
            if(prog>.6F){int filled=(int)(13*(Math.min(prog,.8F)-.6F)*5+.5F);
                if(filled>0)graphics.blit(texture,x+53,y+9,238,234,Math.min(filled,5),17);
                if(filled>5)graphics.blit(texture,x+58,y+6,243,231,Math.min(filled-5,4),8);
                if(filled>9)graphics.blit(texture,x+62,y+6,247,231,filled-9,3);}
            if(prog>.8F){int h=(int)(22*(Math.min(prog,1F)-.8F)*5+.5F);if(h>0)graphics.blit(texture,x+58,y+9,243,234,13,h);}
        }
        if(russian()){graphics.blit(RU_LABELS,k+14,l+5,112,0,24,9);graphics.blit(RU_LABELS,k+171,l+5,112,0,24,9);}
    }
    private void fabricator(GuiGraphics graphics,int k,int l,boolean working){
        var machine=menu.machine;
        if(machine.energy.getEnergyStored()>0)graphics.blit(texture,k+190,l+16,246,0,9,12);
        if(working){graphics.blit(texture,k+9,l+98,226,12,29,29);graphics.blit(FABRICATOR_EXTRA,k+18,l+58,0,0,140,scaled(50));}
        var recipe=machine.selectedFabricatorRecipe();
        if(recipe!=null)graphics.renderItem(recipe.result(),k+153,l+18);else graphics.blit(FABRICATOR_EXTRA,k+153,l+18,140,0,16,16);
        if(recipe!=null)for(int slot=0;slot<4&&slot<recipe.ingredients().length;slot++){
            if(recipe.amounts()[slot]<=0||!machine.inventory.getStackInSlot(slot).isEmpty())continue;
            var items=recipe.ingredients()[slot].getItems();if(items.length==0)continue;
            var xy=machine.kind.slots[slot];graphics.renderItem(items[0],k+xy[0],l+xy[1]);ghost(graphics,k+xy[0]-1,l+xy[1]-1);
        }
        if(russian())graphics.blit(RU_LABELS,k+59,l,0,12,58,12);
    }
    /** FabricatorGui.renderSlotGhost: the half transparent slot face drawn over a template item. */
    private void ghost(GuiGraphics graphics,int x,int y){
        RenderSystem.enableBlend();graphics.pose().pushPose();graphics.pose().translate(0,0,300);graphics.setColor(1,1,1,.5F);
        graphics.blit(texture,x,y,226,41,18,18);graphics.setColor(1,1,1,1);graphics.pose().popPose();RenderSystem.disableBlend();
    }
    private void reaction(GuiGraphics graphics,int k,int l,int mx,int my,boolean working){
        var machine=menu.machine;var reactor=machine.reactor;var tank=machine.inputTank;var recipe=reactor.recipe();
        if(tank.getFluidAmount()>0)drawFluid(graphics,tank.getFluid(),k+8,l+31,18,86,tank.getFluidAmount()*86/tank.getCapacity());
        if(machine.fluidLimit>0){int px=Math.max(1,Math.min(86,machine.fluidLimit*86/tank.getCapacity()));graphics.blit(texture,k+8,l+31+86-px,226,136,18,1);}
        int required=working&&recipe!=null?reactor.requiredIntensity:0;
        if(machine.energy.getEnergyStored()>0)graphics.blit(texture,k+190,l+16,246,0,9,12);
        for(int i=1;i<11;i++){
            if(reactor.intensity>=i)graphics.blit(texture,k+49+i*7,l+59,58,239,5,6);
            if(in(mx,my,49+i*7,59,5,6))graphics.blit(texture,k+48+i*7,l+58,58,245,7,8);
            if(required!=reactor.intensity&&required==i)graphics.blit(texture,k+49+i*7,l+59,53,250,5,6);
        }
        if(working){
            graphics.blit(texture,k+84,l+68,210,95,12,16);graphics.blit(texture,k+84,l+106,210,111,12,16);
            float completion=recipe!=null?reactor.completion/(float)Math.max(1,recipe.requiredCompletion()):0;
            float duration=machine.totalTime>0?machine.progress/(float)machine.totalTime:0;
            graphics.blit(texture,k+61,l+89,0,239,(int)(58*Math.min(1,completion)),11);
            graphics.blit(texture,k+40,l+125,63,241,(int)(100*Math.min(1,duration)),4);
        }
        if(russian()){graphics.blit(RU_LABELS,k+44,l,0,0,88,12);graphics.blit(RU_LABELS,k+5,l+120,88,0,24,9);}
    }
    /** BasicMachineTileEnt.getProgressScaled. */
    private int scaled(int size){var m=menu.machine;return m.totalTime<=0?0:m.progress*size/m.totalTime;}
    private static void tank(GuiGraphics graphics,FluidTank tank,int x,int y){if(tank.getFluidAmount()>0)drawFluid(graphics,tank.getFluid(),x,y,10,50,tank.getFluidAmount()*50/tank.getCapacity());}

    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){
        int color=4210752;
        // TGBaseGui.drawInventoryContainerName; the advanced machines carry their name in the artwork.
        if(!advanced){int start=menu.machine.kind==MachineKind.CHEM_LAB?40:20;GuiText.centered(graphics,font,title,start,imageWidth-start,6,color);}
        graphics.drawString(font,playerInventoryTitle,inventoryLabelX,imageHeight-96+2,color,false);
    }

    /** CE ReactionChamberGui.getHoveredFluidLimit: whole hundreds of mB under the cursor. */
    private int hoveredLimit(int my){int capacity=menu.machine.inputTank.getCapacity();int fromBottom=Math.max(0,Math.min(86,31+86-my));return Math.max(100,Math.min(capacity,Math.round(fromBottom*capacity/8600.0F)*100));}

    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        int mx=(int)mouseX-leftPos,my=(int)mouseY-topPos;var machine=menu.machine;
        switch(machine.kind){
            case AMMO_PRESS->{if(button==0&&in(mx,my,31,68,21,12)){click();send(4);return true;}if(button==0&&in(mx,my,61,68,21,12)){click();send(3);return true;}}
            case METAL_PRESS->{if(button==0&&in(mx,my,35,56,43,12)){click();send(3);return true;}}
            case CHEM_LAB->{if(button==0){if(in(mx,my,90,57,31,18)){click();send(5);return true;}if(in(mx,my,14,5,24,9)){click();send(3);return true;}if(in(mx,my,171,5,24,9)){click();send(4);return true;}}}
            case FABRICATOR->{if(in(mx,my,152,17,18,18)){minecraft.setScreen(new FabricatorRecipeScreen(this,menu));return true;}}
            case REACTION->{
                for(int i=1;i<11;i++)if(in(mx,my,49+i*7,59,5,6)){
                    if(button==0&&machine.reactor.intensity!=i){minecraft.getSoundManager().play(SimpleSoundInstance.forUI(TGSounds.REACTION_CHAMBER_SELECT.get(),1.0F));send(9+i);machine.reactor.intensity=i;}
                    return true;
                }
                if(in(mx,my,8,31,18,86)){
                    if(button==0){int limit=hoveredLimit(my);click();send(20+limit/100);machine.fluidLimit=limit;}
                    else if(button==1&&machine.fluidLimit>0){click();send(20);machine.fluidLimit=0;}
                    return true;
                }
                if(button==0&&in(mx,my,5,120,24,9)){click();send(7);return true;}
            }
            default->{}
        }
        return super.mouseClicked(mouseX,mouseY,button);
    }

    private List<Component> hoverTooltip(int mx,int my){
        var machine=menu.machine;var kind=machine.kind;boolean signal=minecraft.level!=null&&minecraft.level.hasNeighborSignal(machine.getBlockPos());
        int energy=machine.energy.getEnergyStored();
        var tooltip=switch(kind){case AMMO_PRESS,METAL_PRESS->energyTooltip(mx,my,7,17,5,59,energy,kind.capacity);case FABRICATOR,REACTION->energyTooltip(mx,my,186,30,16,95,energy,kind.capacity);default->energyTooltip(mx,my,8,17,4,48,energy,kind.capacity);};
        if(tooltip!=null)return tooltip;
        if(!advanced&&(tooltip=redstoneTooltip(mx,my,signal,machine.redstoneMode))!=null)return tooltip;
        if((tooltip=securityTooltip(mx,my,securityY(),security(),machine.access.ownerId()))!=null)return tooltip;
        switch(kind){
            case AMMO_PRESS->{if(in(mx,my,42,32,29,31))return List.of(Component.translatable("techguns.gui.ammopress.build_plan."+new String[]{"pistol","shotgun","rifle","sniper"}[Math.floorMod(machine.plan,4)]));}
            case METAL_PRESS->{
                if(in(mx,my,30,33,56,9))return List.of(Component.translatable("techguns.container.metalpress.autosplit").append(": ").append(machine.autoSplitMode==1?Component.translatable("techguns.container.info.on").withStyle(ChatFormatting.GREEN):Component.translatable("techguns.container.info.off").withStyle(ChatFormatting.RED)));
                if(in(mx,my,35,56,43,12))return List.of(Component.translatable("techguns.container.metalpress.toggle"));
                if(steamPanel){if(in(mx,my,185,12,10,50))return tankTooltip(machine.inputTank);if(in(mx,my,181,68,18,18))return List.of(Component.translatable("techguns.gui.pressure").append(": "+machine.pressure));}
            }
            case CHEM_LAB->{
                if(in(mx,my,20,16,11,51))return tankTooltip(machine.inputTank);
                if(in(mx,my,177,16,11,51))return tankTooltip(machine.outputTank);
                if(in(mx,my,14,5,24,9))return List.of(Component.translatable("techguns.chemlab.dumpinput"),Component.translatable("techguns.chemlab.dumpinput.tooltip"));
                if(in(mx,my,171,5,24,9))return List.of(Component.translatable("techguns.chemlab.dumpoutput"),Component.translatable("techguns.chemlab.dumpoutput.tooltip"));
                if(in(mx,my,90,57,31,18))return List.of(Component.translatable("techguns.chemlab.toogledrain"),Component.translatable("techguns.chemlab.toogledrain.tooltip."+(machine.drainInput?1:0)));
            }
            case FABRICATOR->{if(in(mx,my,152,17,18,18))return List.of(Component.translatable("techguns.fabricator.template"));}
            case REACTION->{
                if(in(mx,my,8,31,18,86)){
                    var lines=new ArrayList<Component>(tankTooltip(machine.inputTank));
                    if(machine.fluidLimit>0)lines.add(Component.literal(machine.fluidLimit+" mB").withStyle(ChatFormatting.RED));
                    lines.add(Component.literal(hoveredLimit(my)+" mB").withStyle(ChatFormatting.GOLD));
                    lines.add(Component.translatable("techguns.reactionchamber.fluidlimit.set").withStyle(ChatFormatting.GRAY));
                    lines.add(Component.translatable("techguns.reactionchamber.fluidlimit.remove").withStyle(ChatFormatting.GRAY));
                    return lines;
                }
                if(in(mx,my,5,120,24,9))return List.of(Component.translatable("techguns.chemlab.dumpinput"),Component.translatable("techguns.chemlab.dumpinput.tooltip"));
            }
            default->{}
        }
        return null;
    }
    private static List<Component> tankTooltip(FluidTank tank){return List.of(tank.isEmpty()?Component.translatable("techguns.gui.empty"):tank.getFluid().getDisplayName(),Component.literal(tank.getFluidAmount()+"/"+tank.getCapacity()+"mB"));}

    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial){
        renderBackground(graphics);super.render(graphics,mouseX,mouseY,partial);renderTooltip(graphics,mouseX,mouseY);
        if(hoveredSlot==null||!hoveredSlot.hasItem()){var tooltip=hoverTooltip(mouseX-leftPos,mouseY-topPos);if(tooltip!=null)graphics.renderComponentTooltip(font,tooltip,mouseX,mouseY);}
    }
}
