package techguns.plugins.jei;

import java.util.List;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import techguns.TGItems;
import techguns.machines.ReactionControl;
import techguns.machines.ReactionRecipe;
import techguns.plugins.jei.RecipeDisplays.Display;
import techguns.plugins.jei.RecipeDisplays.Kind;

/** Techguns CE JEI pages: the jei/*.png backgrounds with their progress, power, tank and reaction indicators. */
final class CeJeiLayouts {
    private static final ResourceLocation AMMO_PRESS_GUI=tex("ammo_press_gui"),GAUGES=tex("gauges");
    private static ResourceLocation tex(String name){return ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/"+name+".png");}
    private static ResourceLocation jei(String name){return tex("jei/"+name);}

    /** Background area of the CE page, or null for the pages that keep the original layout. */
    record Page(ResourceLocation texture,int u,int v,int width,int height){}
    static Page page(Kind kind){
        return switch(kind){
            case AMMO->new Page(jei("ammo_press"),4,4,95,85);
            case METAL->new Page(jei("metal_press"),4,4,106,85);
            case CHEM->new Page(jei("chem_lab"),4,10,174,69);
            case FABRICATOR->new Page(jei("fabricator"),3,3,175,94);
            case GRINDER->new Page(jei("grinder"),4,4,181,75);
            case REACTION->new Page(jei("reaction_chamber"),4,4,168,100);
            default->null;
        };
    }

    private final Kind kind;private final Page page;private final IDrawable panel,power,progress;
    CeJeiLayouts(IGuiHelper gui,Kind kind){
        this.kind=kind;page=page(kind);panel=gui.createDrawable(page.texture(),page.u(),page.v(),page.width(),page.height());
        power=gui.createAnimatedDrawable(switch(kind){
            case FABRICATOR->gui.createDrawable(page.texture(),251,1,4,83);
            case REACTION->gui.createDrawable(page.texture(),222,1,4,86);
            case AMMO,METAL->gui.createDrawable(AMMO_PRESS_GUI,251,1,4,59);
            default->gui.createDrawable(AMMO_PRESS_GUI,251,1,4,48);
        },100,IDrawableAnimated.StartDirection.TOP,true);
        progress=switch(kind){
            case AMMO,METAL->gui.createAnimatedDrawable(gui.createDrawable(page.texture(),238,0,18,21),100,IDrawableAnimated.StartDirection.TOP,false);
            case FABRICATOR->gui.createAnimatedDrawable(gui.createDrawable(page.texture(),0,207,140,49),100,IDrawableAnimated.StartDirection.TOP,false);
            case GRINDER->gui.createAnimatedDrawable(gui.createDrawable(page.texture(),204,0,52,31),100,IDrawableAnimated.StartDirection.LEFT,false);
            default->null;
        };
    }
    int width(){return page.width();}
    int height(){return page.height();}
    /** Overlay over a fluid slot: tank scale of the chem lab (12x52 around the 10x50 fluid) and the reaction chamber (12x88). */
    IDrawable tankOverlay(IGuiHelper gui){return kind==Kind.REACTION?gui.createDrawable(page.texture(),226,0,12,88):gui.createDrawable(page.texture(),202,0,12,52);}
    int tankWidth(){return kind==Kind.REACTION?12:10;}
    int tankHeight(){return kind==Kind.REACTION?88:50;}

    void draw(Display recipe,GuiGraphics graphics){
        panel.draw(graphics);
        switch(kind){
            case AMMO->{power.draw(graphics,4,13);progress.draw(graphics,41,32);}
            case METAL->{power.draw(graphics,4,13);progress.draw(graphics,45,32);
                var steam=steam(recipe);
                if(steam!=null){graphics.blit(page.texture(),92,7,128,0,12,52);graphics.blit(page.texture(),92,7,116,0,12,52);graphics.blit(GAUGES,88,64,0,Math.max(0,Math.min(12,steam[1]))*18,18,18);}
            }
            case CHEM->{power.draw(graphics,4,7);chemProgress(graphics,recipe);}
            case FABRICATOR->{power.draw(graphics,168,6);progress.draw(graphics,13,23);if(blueprint(recipe))graphics.blit(page.texture(),96,73,240,240,16,16);}
            case GRINDER->{power.draw(graphics,4,13);progress.draw(graphics,47,25);}
            case REACTION->{power.draw(graphics,160,6);reaction(graphics,ReactionRecipe.byId(recipe.id()));}
            default->{}
        }
    }
    /** CE ChemLabJeiRecipe.drawProgressSegments. */
    private void chemProgress(GuiGraphics graphics,Display recipe){
        long cycle=Math.max(1,recipe.ticks())*50L;float prog=(Util.getMillis()%cycle)/(float)cycle;var t=page.texture();int x=56,y=11;
        if(prog>0){int h=(int)(27*Math.min(prog,.2F)*5+.5F);if(h>0)graphics.blit(t,x,15+27-h,185,229+27-h,11,h);}
        if(prog>.2F){int filled=(int)(29*(Math.min(prog,.4F)-.2F)*5+.5F);if(filled>0)graphics.blit(t,x+11,y,196,225,Math.min(filled,26),31);if(filled>26)graphics.blit(t,x+37,y,222,225,filled-26,11);}
        if(prog>.4F){int h=(int)(28*(Math.min(prog,.6F)-.4F)*5+.5F);if(h>0)graphics.blit(t,x+37,y+3,222,228,16,h);}
        if(prog>.6F){int filled=(int)(13*(Math.min(prog,.8F)-.6F)*5+.5F);
            if(filled>0)graphics.blit(t,x+53,y+9,238,234,Math.min(filled,5),17);
            if(filled>5)graphics.blit(t,x+58,y+6,243,231,Math.min(filled-5,4),8);
            if(filled>9)graphics.blit(t,x+62,y+6,247,231,filled-9,3);}
        if(prog>.8F){int h=(int)(22*(Math.min(prog,1F)-.8F)*5+.5F);if(h>0)graphics.blit(t,x+58,y+9,243,234,13,h);}
    }
    /** CE ReactionChamberJeiRecipe: risk sign, stepwise completion, duration and the intensity pips. */
    private void reaction(GuiGraphics graphics,ReactionRecipe recipe){
        if(recipe==null)return;var t=page.texture();
        int level=switch(recipe.risk()){case EXPLOSION_LOW,EXPLOSION_MEDIUM->2;default->0;};
        graphics.blit(t,103,12,226,88+12*level,12,12);
        int steps=Math.max(1,recipe.requiredCompletion());long cycle=steps*60L*50L;float prog=(Util.getMillis()%cycle)/(float)cycle;
        int filled=Math.min(steps,(int)Math.floor(prog*steps+1.0e-4));int width=Math.min(58,filled*Math.max(1,58/steps));if(width>0)graphics.blit(t,51,65,0,240,width,9);
        long total=Math.max(1,recipe.ticks()*60L)*50L;int duration=(int)(100*((Util.getMillis()%total)/(float)total)+.5F);if(duration>0)graphics.blit(t,30,92,63,241,duration,3);
        int required=ReactionControl.clamp(recipe.preferredIntensity()),shown=required;
        if(recipe.instability()!=0&&recipe.margin()>0){long period=3L*60L*50L;float phase=(Util.getMillis()%period)/(float)period;int val=Math.max(-recipe.margin(),Math.min(recipe.margin(),Math.round((float)Math.sin(phase*2*Math.PI)*recipe.margin())));shown=ReactionControl.clamp(required+val);}
        for(int i=1;i<=10;i++){int x=39+i*7;if(i==required)graphics.blit(t,x,39,53,250,5,5);if(shown>=i)graphics.blit(t,x,39,58,239,5,5);}
    }
    private static int[] steam(Display recipe){
        for(var slot:recipe.slots())if(slot.role()==RecipeDisplays.Role.OUTPUT&&slot.items().stream().anyMatch(s->s.is(TGItems.STEAMARMOR_PLATE.get())))return new int[]{4000,7};
        return null;
    }
    private static boolean blueprint(Display recipe){return recipe.slots().stream().anyMatch(s->s.role()==RecipeDisplays.Role.OUTPUT&&s.items().stream().anyMatch(i->i.is(TGItems.CYBERNETIC_PARTS.get())));}

    List<Component> tooltip(Display recipe,double x,double y){
        if(kind==Kind.METAL&&steam(recipe)!=null&&x>=88&&x<=106&&y>=64&&y<=82)
            return List.of(Component.translatable("techguns.gui.pressure").append(": "+steam(recipe)[1]),Component.translatable("techguns.jei.recipe_requires",Component.translatable("item.techguns.machine_upgrade_steam")).withStyle(net.minecraft.ChatFormatting.GRAY));
        if(kind==Kind.FABRICATOR&&blueprint(recipe)&&x>=97&&x<113&&y>=74&&y<90){
            var lines=new java.util.ArrayList<Component>();for(var line:Component.translatable("techguns.tooltip.needsBlueprint").getString().split("\\$"))lines.add(Component.literal(line));return lines;
        }
        return List.of();
    }
}
