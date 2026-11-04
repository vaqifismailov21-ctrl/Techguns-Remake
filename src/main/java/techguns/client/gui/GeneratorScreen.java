package techguns.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import techguns.power.GeneratorMenu;

/** Generator interface in the style of the Techguns machines: energy bar, fuel slot, flame or tank, readouts. */
public final class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/generator_gui.png");
    private static final ResourceLocation FURNACE=ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final int TANK_X=40,TANK_Y=17,TANK_W=16,TANK_H=48;
    public GeneratorScreen(GeneratorMenu menu,Inventory inventory,Component title){super(menu,inventory,title);}
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial){renderBackground(graphics);super.render(graphics,mouseX,mouseY,partial);renderTooltip(graphics,mouseX,mouseY);
        int x=mouseX-leftPos,y=mouseY-topPos;
        if(x>=7&&x<13&&y>=16&&y<66)graphics.renderTooltip(font,Component.translatable("techguns.power.energy",menu.energy(),menu.capacity()),mouseX,mouseY);
        if(menu.nuclear&&x>=TANK_X-1&&x<TANK_X+TANK_W+1&&y>=TANK_Y-1&&y<TANK_Y+TANK_H+1)graphics.renderTooltip(font,java.util.List.of(Component.translatable("techguns.power.heat",menu.heat()*100/techguns.power.PowerBlocks.NUCLEAR_HEAT_MAX),Component.translatable(menu.overheated()?"techguns.power.overheated":menu.cooled()?"techguns.power.cooled":"techguns.power.not_cooled")),java.util.Optional.empty(),mouseX,mouseY);
        if(menu.liquid&&x>=TANK_X-1&&x<TANK_X+TANK_W+1&&y>=TANK_Y-1&&y<TANK_Y+TANK_H+1)graphics.renderTooltip(font,menu.tankAmount()>0?Component.translatable("techguns.power.tank",menu.tankFluid().getFluidType().getDescription(),menu.tankAmount(),techguns.power.PowerBlocks.LIQUID_TANK):Component.translatable("techguns.power.tank_empty"),mouseX,mouseY);
    }
    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY){
        int k=leftPos,l=topPos;graphics.blit(TEXTURE,k,l,0,0,imageWidth,imageHeight);
        int capacity=Math.max(1,menu.capacity()),power=(int)((long)menu.energy()*48/capacity),diff=48-power;
        graphics.blit(LegacyMachineGui.MACHINE,k+8,l+17+diff,251,1+diff,4,48);
        int burn=menu.burn();
        if(menu.nuclear){
            graphics.fill(k+TANK_X-1,l+TANK_Y-1,k+TANK_X+TANK_W+1,l+TANK_Y+TANK_H+1,0xFF373737);graphics.fill(k+TANK_X,l+TANK_Y,k+TANK_X+TANK_W,l+TANK_Y+TANK_H,0xFF8B8B8B);
            int h=Math.min(TANK_H,menu.heat()*TANK_H/techguns.power.PowerBlocks.NUCLEAR_HEAT_MAX);
            if(h>0)graphics.fill(k+TANK_X,l+TANK_Y+TANK_H-h,k+TANK_X+TANK_W,l+TANK_Y+TANK_H,menu.overheated()?0xFFE02020:0xFFE08020);
            for(int mark=1;mark<4;mark++)graphics.fill(k+TANK_X,l+TANK_Y+mark*TANK_H/4,k+TANK_X+5,l+TANK_Y+mark*TANK_H/4+1,0xFF373737);
            int sx=k+GeneratorMenu.SLOT_X-1,sy=l+GeneratorMenu.OUT_Y-1;graphics.fill(sx,sy,sx+18,sy+18,0xFF373737);graphics.fill(sx+1,sy+1,sx+18,sy+18,0xFFFFFFFF);graphics.fill(sx+1,sy+1,sx+17,sy+17,0xFF8B8B8B);
            graphics.blit(FURNACE,k+19,l+40,57,37,14,13);
            if(burn>0){int fh=Math.max(1,burn*13/1000);graphics.blit(FURNACE,k+19,l+40+13-fh,176,13-fh,14,fh);}
        }else if(menu.liquid){
            graphics.fill(k+TANK_X-1,l+TANK_Y-1,k+TANK_X+TANK_W+1,l+TANK_Y+TANK_H+1,0xFF373737);graphics.fill(k+TANK_X,l+TANK_Y,k+TANK_X+TANK_W,l+TANK_Y+TANK_H,0xFF8B8B8B);
            int amount=menu.tankAmount();var fluid=menu.tankFluid();
            if(amount>0&&fluid!=Fluids.EMPTY){
                var ext=IClientFluidTypeExtensions.of(fluid);var sprite=minecraft.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ext.getStillTexture());int color=ext.getTintColor();
                int height=Math.max(1,amount*TANK_H/techguns.power.PowerBlocks.LIQUID_TANK);
                RenderSystem.setShaderColor((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f,1f);
                for(int drawn=0;drawn<height;drawn+=16){int h=Math.min(16,height-drawn);graphics.blit(k+TANK_X,l+TANK_Y+TANK_H-drawn-h,0,TANK_W,h,sprite);}
                RenderSystem.setShaderColor(1f,1f,1f,1f);
            }
            for(int mark=1;mark<4;mark++)graphics.fill(k+TANK_X,l+TANK_Y+mark*TANK_H/4,k+TANK_X+5,l+TANK_Y+mark*TANK_H/4+1,0xFF373737);
            graphics.blit(FURNACE,k+19,l+40,57,37,14,13);if(burn>0)graphics.blit(FURNACE,k+19,l+40,176,0,14,13);
        }else{
            graphics.blit(FURNACE,k+19,l+40,57,37,14,13);
            if(burn>0){int h=Math.max(1,burn*13/1000);graphics.blit(FURNACE,k+19,l+40+13-h,176,13-h,14,h);}
        }
    }
    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){
        graphics.drawString(font,title,8,6,0x404040,false);graphics.drawString(font,playerInventoryTitle,8,imageHeight-94,0x404040,false);
        int x=menu.liquid||menu.nuclear?62:40;
        graphics.drawString(font,Component.translatable("techguns.power.stored",menu.energy()),x,22,0x404040,false);
        graphics.drawString(font,Component.translatable("techguns.power.max",menu.capacity()),x,34,0x404040,false);
        graphics.drawString(font,menu.burn()>0?Component.translatable("techguns.power.rate",menu.rate()):Component.translatable("techguns.power.idle"),x,50,menu.burn()>0?0x207020:0x804040,false);
    }
}
