package techguns.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.machines.TurretMenu;
import techguns.tileentities.TurretTileEnt;
import static techguns.client.gui.LegacyMachineGui.*;

/**
 * Original TurretGui with its PoweredTileEntGui/RedstoneTileEntGui/OwnedTileEntGui layers: power bar, redstone state
 * lamps, textured redstone/security/animal/PvP buttons on side panels, repair bar and the original tooltips.
 */
public final class TurretScreen extends AbstractContainerScreen<TurretMenu> {
    public static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/turret_base_gui.png");
    private final TurretTileEnt tile;

    public TurretScreen(TurretMenu menu,Inventory inventory,Component title){super(menu,inventory,title);tile=menu.tile;imageWidth=176;imageHeight=166;}
    private void send(int id){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}

    @Override protected void init(){
        super.init();
        addRenderableWidget(iconButton(leftPos-22,topPos+SECURITY_Y,()->send(TurretTileEnt.BUTTON_ID_SECURITY),MACHINE,234,()->58+16*tile.getSecurity()));
        addRenderableWidget(iconButton(leftPos-22,topPos+10,()->send(TurretTileEnt.BUTTON_ID_REDSTONE),MACHINE,234,()->10+16*tile.redstoneBehaviour));
        addRenderableWidget(iconButton(leftPos-22,topPos+70,()->send(TurretTileEnt.BUTTON_ID_TARGET_ANIMALS),TEXTURE,176,()->6+16*(tile.attackAnimals?1:0)));
        addRenderableWidget(iconButton(leftPos-22,topPos+100,()->send(TurretTileEnt.BUTTON_ID_PVP_SETTING),TEXTURE,193,()->6+16*tile.getPvpSetting()));
    }

    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY){
        int k=leftPos,l=topPos;
        graphics.blit(TEXTURE,k,l,0,0,imageWidth,imageHeight);
        drawPowered(graphics,k,l,tile.isRedstoneEnabled(),tile.energy.getEnergyStored(),tile.energy.getMaxEnergyStored());
        graphics.blit(MACHINE,k-22-5,l+70-5,195,0,22+5,30);
        graphics.blit(MACHINE,k-22-5,l+100-5,195,0,22+5,30);
        if(tile.turretDeath){int i1=tile.getRepairTimeScaled(52);graphics.blit(TEXTURE,k+116,l+74,177,1,i1+1,4);}
    }

    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){
        graphics.drawString(font,playerInventoryTitle,8,imageHeight-96+2,4210752,false);
        graphics.drawString(font,title,imageWidth/2-font.width(title)/2,6,4210752,false);
    }

    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial){
        renderBackground(graphics, mouseX, mouseY, partial);super.render(graphics,mouseX,mouseY,partial);renderTooltip(graphics,mouseX,mouseY);
        int mx=mouseX-leftPos,my=mouseY-topPos;
        boolean signal=minecraft.level!=null&&minecraft.level.hasNeighborSignal(tile.getBlockPos());
        List<Component> tooltip=poweredTooltip(mx,my,signal,tile.redstoneBehaviour,tile.getSecurity(),tile.getOwner(),tile.energy.getEnergyStored(),tile.energy.getMaxEnergyStored());
        if(tooltip==null){
            if(in(mx,my,115,73,53,5)){
                if(tile.turretDeath)tooltip=List.of(Component.translatable("techguns.container.repair").append(" "+tile.repairTime/20+"s"));
                else if(tile.clientHealthTimes10>=0)tooltip=List.of(Component.translatable("techguns.turret.health").append(": "+tile.clientHealthTimes10/10f+"/"+30.0f));
            }else if(in(mx,my,-22,70,20,20)){
                tooltip=List.of(Component.translatable("techguns.turret.targetAnimals").append(": ").append(tile.attackAnimals?Component.translatable("techguns.yes").withStyle(ChatFormatting.RED):Component.translatable("techguns.no").withStyle(ChatFormatting.GREEN)));
            }else if(in(mx,my,-22,100,20,20)){
                var lines=new ArrayList<Component>();
                lines.add(Component.translatable("techguns.turret.pvpsetting").append(": ").append(Component.translatable("techguns.turret.pvpsetting."+tile.getPvpSetting())));
                lines.add(Component.translatable("techguns.container.security.tooltip.descr2"));
                tooltip=lines;
            }
        }
        if(tooltip!=null)graphics.renderComponentTooltip(font,tooltip,mouseX,mouseY);
    }
}
