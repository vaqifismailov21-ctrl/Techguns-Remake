package techguns.plugins.jei;

import java.util.*;
import mezz.jei.api.gui.drawable.*;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import techguns.plugins.jei.RecipeDisplays.*;

public final class TechgunsRecipeCategory implements IRecipeCategory<Display> {
    private final Kind kind;private final RecipeType<Display> type;private final IDrawable background,icon,panel,power;private final IGuiHelper gui;
    TechgunsRecipeCategory(IGuiHelper gui,Kind kind,RecipeType<Display> type){this.gui=gui;this.kind=kind;this.type=type;background=gui.createBlankDrawable(162,123);panel=gui.createDrawable(kind.texture(),7,15,162,63);icon=gui.createDrawableItemStack(kind.icon());power=gui.createAnimatedDrawable(gui.createDrawable(Kind.AMMO.texture(),251,1,4,48),100,IDrawableAnimated.StartDirection.TOP,true);}
    @Override public RecipeType<Display> getRecipeType(){return type;}
    @Override public Component getTitle(){return kind.title();}
    @Override public IDrawable getBackground(){return background;}
    @Override public IDrawable getIcon(){return icon;}
    @Override public void setRecipe(IRecipeLayoutBuilder builder,Display recipe,IFocusGroup focuses){
        for(var slot:recipe.slots()){
            RecipeIngredientRole role=switch(slot.role()){case INPUT->RecipeIngredientRole.INPUT;case OUTPUT->RecipeIngredientRole.OUTPUT;case CATALYST->RecipeIngredientRole.CATALYST;};
            var target=builder.addSlot(role,slot.x(),slot.y());if(!slot.items().isEmpty())target.addItemStacks(slot.items()).setStandardSlotBackground();
            else if(!slot.fluid().isEmpty())target.addFluidStack(slot.fluid().getFluid(),slot.fluid().getAmount()).setFluidRenderer(capacity(slot),false,10,50).setOverlay(tankOverlay(slot),-1,0);
            if(slot.note()!=null)target.addTooltipCallback((view,tooltip)->tooltip.add(slot.note()));
        }
    }
    /** Tank sizes of the machines, so the fluid stands as high as it would in the machine (1.12 tank positions 19/158, y 17). */
    private int capacity(RecipeDisplays.Slot slot){
        int tank=switch(kind){case REACTION->10000;case CHEM->slot.role()==RecipeDisplays.Role.INPUT?8000:16000;case ORE_DRILL->16000;default->0;};
        return Math.max(Math.max(1000,tank),slot.fluid().getAmount());
    }
    /** The tank scale is drawn over the fluid as in the machine GUI; the reaction chamber also marks the liquid level. */
    private IDrawable tankOverlay(RecipeDisplays.Slot slot){
        var texture=kind.texture();int v=kind==Kind.ORE_DRILL?40:32;int level=slot.fluid().getAmount()/1000;boolean marker=kind==Kind.REACTION;
        return new IDrawable(){
            @Override public int getWidth(){return 12;}
            @Override public int getHeight(){return 52;}
            @Override public void draw(GuiGraphics g,int x,int y){
                if(kind!=Kind.CHEM&&kind!=Kind.REACTION&&kind!=Kind.ORE_DRILL)return;
                g.blit(texture,x,y,176,v,12,52);
                if(marker)g.blit(texture,x+1,y+50-Math.min(10,level)*5,177,32,7,1);
            }
        };
    }
    private List<Component> details(Display recipe){// the original JEI pages showed no time/energy lines
        var lines=new ArrayList<Component>();lines.addAll(recipe.details());return lines;}
    @Override public void draw(Display recipe,IRecipeSlotsView slots,GuiGraphics graphics,double mouseX,double mouseY){
        if(kind==Kind.CAMO||kind==Kind.REPAIR){graphics.fill(0,0,162,63,0xffc6c6c6);graphics.drawString(Minecraft.getInstance().font,">",70,5,0x404040,false);}else panel.draw(graphics);
        if(recipe.power()>0)power.draw(graphics,1,2);drawProgress(graphics);
        int y=65;for(var line:details(recipe)){var font=Minecraft.getInstance().font;float scale=Math.min(1,162f/Math.max(1,font.width(line)));graphics.pose().pushPose();graphics.pose().translate(0,y,0);graphics.pose().scale(scale,scale,1);graphics.drawString(font,line,0,0,0x404040,false);graphics.pose().popPose();y+=11;}
    }
    private void drawProgress(GuiGraphics graphics){
        float f=(net.minecraft.Util.getMillis()%5000)/5000F;var texture=kind.texture();graphics.pose().pushPose();graphics.pose().translate(-7,-15,0);
        switch(kind){
            case AMMO,METAL->graphics.blit(texture,119,36,176,0,19,(int)(21*f)+1);
            case BLAST->graphics.blit(texture,19,53,0,167,(int)(90*f)+1,11);
            case FABRICATOR->graphics.blit(texture,21,53,0,167,(int)(90*f)+1,11);
            case CHARGING->graphics.blit(texture,39,19,0,167,(int)(26*f)+1,10);
            case ORE_DRILL->graphics.blit(texture,73,17,177,1,25,(int)(36*f)+1);
            case REACTION->{graphics.blit(texture,67,61,0,167,(int)(100*f),4);graphics.blit(texture,67,69,0,175,(int)(100*f),4);}
            case GRINDER->{int width=(int)(21*f);graphics.blit(texture,31,39,0,167,width+1,22);int reversed=20-width;graphics.blit(texture,51+reversed,39,reversed,167,width+1,22);}
            case CHEM->{int a=(int)(Math.min(f*5,1)*25);graphics.blit(texture,81,19,178,5,8,a);if(f>=.2F)graphics.blit(texture,88,39,186,25,(int)(Math.min((f-.2F)*5,1)*9),1);if(f>=.4F){int h=(int)(Math.min((f-.4F)*5,1)*20);graphics.blit(texture,97,43-h,194,29-h,14,h);}if(f>=.6F)graphics.blit(texture,108,17,205,3,(int)(Math.min((f-.6F)*5,1)*16),6);if(f>=.8F)graphics.blit(texture,117,20,214,6,14,(int)((f-.8F)*5*24)+1);}
            default->{}
        }
        graphics.pose().popPose();
    }
    @Override public List<Component> getTooltipStrings(Display recipe,IRecipeSlotsView slots,double x,double y){return List.of();}
}
