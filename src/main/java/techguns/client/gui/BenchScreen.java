package techguns.client.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import techguns.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import techguns.capabilities.TGExtendedPlayer;
import techguns.items.armors.GenericShield;
import techguns.items.armors.ICamoChangeable;
import techguns.items.armors.TGSuitArmor;
import techguns.machines.*;
import static techguns.client.gui.LegacyMachineGui.*;

/** Original CamoBenchGui, RepairBenchGui and UpgradeBenchGui on the OwnedTileEntGui layer. */
public final class BenchScreen extends AbstractContainerScreen<BenchMenu> {
    private static final EquipmentSlot[] ARMOR={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    private static final String[] DYE_FAMILIES={"wool","concrete","concrete_powder","terracotta","stained_glass","stained_glass_pane","carpet"};
    private static final String[] COLORS={"white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"};
    private final ResourceLocation texture;
    private final int securityY;
    public BenchScreen(BenchMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title);texture=new ResourceLocation("techguns","textures/gui/"+menu.bench.kind.texture()+"_gui.png");imageHeight=166;
        securityY=switch(menu.bench.kind){case CAMO->95+25;case REPAIR->10;default->SECURITY_Y;};
    }
    private void send(int id){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    private void text(String label,int id,int x,int y,int size){addRenderableWidget(new Button(leftPos+x,topPos+y,size,size,Component.literal(label),b->send(id)));}
    private byte security(){return (byte)(menu.bench.access.restricted()?3:0);}

    @Override protected void init(){
        super.init();
        addRenderableWidget(iconButton(leftPos-22,topPos+securityY,()->send(1),MACHINE,234,()->58+16*security()));
        switch(menu.bench.kind){
            case CAMO->{
                text(">",2,14+12,59,12);text("<",3,14,59,12);
                // Armor slots only: the original face/back/hand buttons are commented out, their camo numbers are still shown.
                for(int i=0;i<4;i++){text("+",4+i*2,102+18*i,59,10);text("-",5+i*2,102+18*i,69,10);}
            }
            case REPAIR->{
                int[] xs={90,110,130,150,65,9};
                for(int i=0;i<xs.length;i++){int id=2+i;
                    // GuiButtonRepair: always enabled, 12x12 hammer icon from the repair bench sheet.
                    addRenderableWidget(new Button(leftPos+xs[i],topPos+39,14,14,Component.empty(),b->send(id)){
                        @Override public void renderButton(com.mojang.blaze3d.vertex.PoseStack graphics__pose,int mouseX,int mouseY,float partial){techguns.client.gui.compat.GuiGraphics graphics=new techguns.client.gui.compat.GuiGraphics(graphics__pose);super.renderButton(graphics.pose(),mouseX,mouseY,partial);graphics.blit(texture,x+1,y+1,176,0,12,12);}
                    });}
            }
            default->{}
        }
    }

    @Override protected void renderBg(com.mojang.blaze3d.vertex.PoseStack graphics__pose,float partial,int mouseX,int mouseY){techguns.client.gui.compat.GuiGraphics graphics=new techguns.client.gui.compat.GuiGraphics(graphics__pose);
        int k=leftPos,l=topPos;
        graphics.blit(texture,k,l,0,0,imageWidth,imageHeight);
        drawSecurityPanel(graphics,k,l,securityY);
        switch(menu.bench.kind){
            case CAMO->{
                graphics.blit(MACHINE,k-22,l+90,195,0,22,20);graphics.blit(MACHINE,k-22,l+90+20,195,25,22,5);
                graphics.blit(texture,k-45,l+6,178,0,45,84);
                graphics.blit(texture,k-18,l+93,7,83,18,18);
                graphics.enableScissor(k-42,l+9,k-3,l+86);
                try{InventoryScreen.renderEntityInInventory(k-20,l+77,30,k-mouseX,l-mouseY,minecraft.player);}finally{graphics.disableScissor();}
            }
            case UPGRADE->{
                if(!menu.result.getItem(0).isEmpty()){
                    long time=minecraft.level.getGameTime();int i=3-(int)(time%4);float hue=(time%360)/360f;int x=i>=2?0:89,y=i%2==1?0:31;
                    var c=new Color(Color.HSBtoRGB(hue,1f,1f));
                    graphics.setColor(c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f,1f);graphics.blit(texture,k+37,l+40,x,167+y,89,31);graphics.setColor(1,1,1,1);
                }
            }
            default->{}
        }
    }

    @Override protected void renderLabels(com.mojang.blaze3d.vertex.PoseStack graphics__pose,int mouseX,int mouseY){techguns.client.gui.compat.GuiGraphics graphics=new techguns.client.gui.compat.GuiGraphics(graphics__pose);
        int color=4210752;
        // TGBaseGui.drawInventoryContainerName; the repair bench hides the inventory label.
        if(menu.bench.kind!=BenchKind.REPAIR)graphics.drawString(font,playerInventoryTitle,8,imageHeight-96+2,color,false);
        GuiText.centered(graphics,font,title,8,imageWidth-8,6,color);
        switch(menu.bench.kind){
            case CAMO->{
                String s2=Component.translatable("techguns.item.invalidcamo").getString(),s3="";
                var item=menu.bench.inventory.getStackInSlot(0);
                if(!item.isEmpty()){
                    if(item.getItem() instanceof ICamoChangeable camo&&!(item.getItem() instanceof BlockItem)){s2=camo.getCurrentCamoName(item).getString();s3=(camo.getCurrentCamoIndex(item)+1)+"/"+camo.getCamoCount()+":";}
                    else if(item.getItem() instanceof ICamoChangeable camoblock){int i=camoblock.getCurrentCamoIndex(item)+1;s3=(i>9?"":" ")+i+"/"+camoblock.getCamoCount();s2=camoblock.getCurrentCamoName(item).getString();}
                    else{int i=dyeIndex(item);if(i>=0){i++;s3=(i>9?"":" ")+i+"/16";s2="";}}
                }
                graphics.drawString(font,s3,10,40,color,false);graphics.drawString(font,font.plainSubstrByWidth(s2,160),10,50,color,false);
                for(int i=0;i<4;i++)graphics.drawString(font,""+camoNumber(minecraft.player.getItemBySlot(ARMOR[i])),104+i*18,38,color,false);
                var inv=TGExtendedPlayer.get(minecraft.player).tg_inventory;
                graphics.drawString(font,""+camoNumber(inv.getItem(0)),49,38,color,false);
                graphics.drawString(font,""+camoNumber(inv.getItem(1)),68,38,color,false);
                graphics.drawString(font,""+camoNumber(inv.getItem(2)),86,38,color,false);
            }
            case UPGRADE->{
                int xp=menu.xpCost(),have=minecraft.player.totalExperience;
                xpText(graphics,Component.translatable("techguns.gui.xp").getString()+": "+have,95,17,8453920);
                xpText(graphics,Component.translatable("techguns.gui.xpcost").getString()+": "+xp,95,27,minecraft.player.isCreative()||xp<=have?8453920:16736352);
            }
            default->{}
        }
    }
    /** UpgradeBenchGui.drawXPText: three darkened copies behind the coloured text, like the anvil. */
    private void xpText(GuiGraphics graphics,String s,int x,int y,int color){
        int j=-16777216|(color&16579836)>>2|color&-16777216;
        graphics.drawString(font,s,x,y+1,j,false);graphics.drawString(font,s,x+1,y,j,false);graphics.drawString(font,s,x+1,y+1,j,false);graphics.drawString(font,s,x,y,color,false);
    }
    private static int camoNumber(ItemStack stack){return !stack.isEmpty()&&stack.getItem() instanceof ICamoChangeable camo?camo.getCurrentCamoIndex(stack)+1:0;}
    /** Original metadata of the sixteen-colour blocks handled by the plain camo bench recipes. */
    private static int dyeIndex(ItemStack stack){
        var id=ForgeRegistries.ITEMS.getKey(stack.getItem());if(id==null||!id.getNamespace().equals("minecraft"))return -1;
        for(String family:DYE_FAMILIES)for(int i=0;i<COLORS.length;i++)if(id.getPath().equals(COLORS[i]+"_"+family))return i;
        return -1;
    }

    private List<Component> hoverTooltip(int mx,int my){
        var tooltip=securityTooltip(mx,my,securityY,security(),menu.bench.access.ownerId());if(tooltip!=null)return tooltip;
        if(menu.bench.kind==BenchKind.REPAIR){
            for(int i=0;i<4;i++)if(in(mx,my,90+i*20,39,14,14))return repairTooltip(minecraft.player.getItemBySlot(ARMOR[i]));
            if(in(mx,my,65,39,14,14))return repairTooltip(minecraft.player.getOffhandItem());
            // The original hover area of the general repair slot starts at x=12 although its button is at x=9.
            if(in(mx,my,12,39,14,14))return repairTooltip(menu.bench.inventory.getStackInSlot(9));
        }
        return null;
    }
    private List<Component> repairTooltip(ItemStack item){
        if(item.isEmpty())return List.of(Component.translatable("techguns.repairBench.noarmor"));
        if(!(item.getItem() instanceof TGSuitArmor)&&!(item.getItem() instanceof GenericShield))return List.of(Component.translatable("techguns.repairBench.cantrepair"));
        if(item.getDamageValue()<=0)return List.of(Component.translatable("techguns.repairBench.fullcondition"));
        var mats=menu.bench.repairMaterials(item);if(mats.isEmpty())return List.of(Component.translatable("techguns.repairBench.cantrepair"));
        var lines=new ArrayList<Component>();lines.add(Component.translatable("techguns.repairBench.requiredMats"));
        for(var mat:mats){var line=Component.literal(mat.getCount()+"x ").append(mat.getHoverName());if(missing(mat))line.withStyle(ChatFormatting.RED);lines.add(line);}
        return lines;
    }
    /** InventoryUtil.canConsumeItem(...)>0: the bench inventory does not hold enough of this material. */
    private boolean missing(ItemStack mat){
        int need=mat.getCount();var inv=menu.bench.inventory;
        for(int i=0;i<inv.getSlots()&&need>0;i++){var s=inv.getStackInSlot(i);if(techguns.util.Compat.isSameItem(s,mat))need-=s.getCount();}
        return need>0;
    }

    @Override public void render(com.mojang.blaze3d.vertex.PoseStack graphics__pose,int mouseX,int mouseY,float partial){techguns.client.gui.compat.GuiGraphics graphics=new techguns.client.gui.compat.GuiGraphics(graphics__pose);
        renderBackground(graphics.pose());super.render(graphics.pose(),mouseX,mouseY,partial);renderTooltip(graphics.pose(),mouseX,mouseY);
        var tooltip=hoverTooltip(mouseX-leftPos,mouseY-topPos);if(tooltip!=null)graphics.renderComponentTooltip(font,tooltip,mouseX,mouseY);
    }
}
