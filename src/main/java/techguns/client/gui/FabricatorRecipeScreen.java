package techguns.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import techguns.machines.MachineMenu;
import techguns.machines.MachineRecipes;
import techguns.machines.ProcessingRecipe;

/** Techguns CE ScreenRecipeSelectorGui: picks the fabricator template that decides which items the input slots accept. */
public final class FabricatorRecipeScreen extends Screen {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/recipe_selector_gui.png");
    private static final int X_SIZE=176,Y_SIZE=132;
    private final Screen previous;
    private final MachineMenu menu;
    private final List<ProcessingRecipe> recipes=new ArrayList<>();
    private EditBox search;
    private int left,top,pageIndex,size;

    public FabricatorRecipeScreen(Screen previous,MachineMenu menu){super(Component.translatable("techguns.fabricator.template"));this.previous=previous;this.menu=menu;}

    private int selection(){return menu.machine.fabricatorRecipe;}
    private boolean unlocked(ProcessingRecipe recipe){return menu.machine.fabricatorRecipeUnlocked(recipe);}
    private void select(int index){menu.machine.fabricatorRecipe=index;minecraft.gameMode.handleInventoryButtonClick(menu.containerId,index<0?9:10+index);}
    private void click(){minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK,1.0F));}

    @Override protected void init(){
        left=(width-X_SIZE)/2;top=(height-Y_SIZE)/2;
        String text=search==null?"":search.getValue();
        search=new EditBox(font,left+28,top+111,102,12,Component.empty());
        search.setTextColor(0xC5C5C5);search.setTextColorUneditable(0xC5C5C5);search.setBordered(false);search.setMaxLength(32);search.setValue(text);
        search.setResponder(this::search);addRenderableWidget(search);
        var current=menu.machine.selectedFabricatorRecipe();if(current!=null&&!unlocked(current))select(-1);
        search(text);
    }
    private void search(String text){
        recipes.clear();String needle=text==null?"":text.trim().toLowerCase(Locale.ROOT);
        for(var recipe:MachineRecipes.fabricator())if(unlocked(recipe)&&(needle.isEmpty()||recipe.result().getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle)))recipes.add(recipe);
        pageIndex=0;size=Math.max(0,(int)Math.ceil((recipes.size()-40)/8D));
    }
    private static boolean over(int mouseX,int mouseY,int x,int y,int w,int h){return x<=mouseX&&x+w>mouseX&&y<mouseY&&y+h>=mouseY;}
    private int indexOf(ProcessingRecipe recipe){return MachineRecipes.fabricator().indexOf(recipe);}

    /** FabricatorRecipe.print: name, the four inputs and the output. */
    private static List<Component> print(ProcessingRecipe recipe){
        var lines=new ArrayList<Component>();
        lines.add(recipe.result().getHoverName().copy().withStyle(ChatFormatting.YELLOW));
        lines.add(Component.translatable("desc.rec_input").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD));
        for(int i=0;i<recipe.ingredients().length;i++){var items=recipe.ingredients()[i].getItems();if(recipe.amounts()[i]>0&&items.length>0)lines.add(Component.literal("  "+recipe.amounts()[i]+"x ").append(items[0].getHoverName()).withStyle(ChatFormatting.GRAY));}
        lines.add(Component.translatable("desc.rec_output").withStyle(ChatFormatting.GREEN,ChatFormatting.BOLD));
        lines.add(Component.literal("  "+recipe.result().getCount()+"x ").append(recipe.result().getHoverName()).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial){
        renderBackground(graphics);
        graphics.blit(TEXTURE,left,top,0,0,X_SIZE,Y_SIZE);
        if(search.isFocused())graphics.blit(TEXTURE,left+26,top+108,0,132,106,16);
        if(over(mouseX,mouseY,left+152,top+18,16,16))graphics.blit(TEXTURE,left+152,top+18,176,0,16,16);
        if(over(mouseX,mouseY,left+152,top+36,16,16))graphics.blit(TEXTURE,left+152,top+36,176,16,16,16);
        if(over(mouseX,mouseY,left+152,top+90,16,16))graphics.blit(TEXTURE,left+152,top+90,176,32,16,16);
        if(over(mouseX,mouseY,left+134,top+108,16,16))graphics.blit(TEXTURE,left+134,top+108,176,48,16,16);
        if(over(mouseX,mouseY,left+8,top+108,16,16))graphics.blit(TEXTURE,left+8,top+108,176,64,16,16);
        var selected=menu.machine.selectedFabricatorRecipe();
        for(int i=pageIndex*8;i<pageIndex*8+40&&i<recipes.size();i++){int ind=i-pageIndex*8;if(recipes.get(i)==selected)graphics.blit(TEXTURE,left+7+18*(ind%8),top+17+18*(ind/8),192,0,18,18);}
        for(int i=pageIndex*8;i<pageIndex*8+40&&i<recipes.size();i++){int ind=i-pageIndex*8;ItemStack icon=recipes.get(i).result();int x=left+8+18*(ind%8),y=top+18+18*(ind/8);graphics.renderItem(icon,x,y);graphics.renderItemDecorations(font,icon,x,y);}
        if(selected!=null&&recipes.contains(selected)){graphics.renderItem(selected.result(),left+152,top+72);graphics.renderItemDecorations(font,selected.result(),left+152,top+72);}
        super.render(graphics,mouseX,mouseY,partial);
        if(over(mouseX,mouseY,left+7,top+17,144,90))for(int i=pageIndex*8;i<pageIndex*8+40&&i<recipes.size();i++){
            int ind=i-pageIndex*8;if(over(mouseX,mouseY,left+7+18*(ind%8),top+17+18*(ind/8),18,18))graphics.renderComponentTooltip(font,print(recipes.get(i)),mouseX,mouseY);
        }
        if(selected!=null&&recipes.contains(selected)&&over(mouseX,mouseY,left+151,top+71,18,18))graphics.renderComponentTooltip(font,print(selected),mouseX,mouseY);
        if(over(mouseX,mouseY,left+152,top+90,16,16))graphics.renderTooltip(font,Component.literal("Close").withStyle(ChatFormatting.YELLOW),mouseX,mouseY);
        if(over(mouseX,mouseY,left+134,top+108,16,16))graphics.renderTooltip(font,Component.literal("Clear search").withStyle(ChatFormatting.YELLOW),mouseX,mouseY);
        if(over(mouseX,mouseY,left+8,top+108,16,16))graphics.renderTooltip(font,Component.literal("Press ENTER to toggle focus").withStyle(ChatFormatting.ITALIC),mouseX,mouseY);
    }

    @Override public boolean mouseClicked(double mouseXd,double mouseYd,int button){
        boolean handled=super.mouseClicked(mouseXd,mouseYd,button);int x=(int)mouseXd,y=(int)mouseYd;
        if(!search.isMouseOver(mouseXd,mouseYd))search.setFocused(false);
        if(over(x,y,left+152,top+18,16,16)){click();if(pageIndex>0)pageIndex--;return true;}
        if(over(x,y,left+152,top+36,16,16)){click();if(pageIndex<size)pageIndex++;return true;}
        if(over(x,y,left+134,top+108,16,16)){search.setValue("");search("");setFocused(search);search.setFocused(true);return true;}
        for(int i=pageIndex*8;i<pageIndex*8+40&&i<recipes.size();i++){
            int ind=i-pageIndex*8;
            if(over(x,y,left+7+18*(ind%8),top+17+18*(ind/8),18,18)){var recipe=recipes.get(i);select(recipe==menu.machine.selectedFabricatorRecipe()?-1:indexOf(recipe));click();return true;}
        }
        if(over(x,y,left+151,top+71,18,18)&&menu.machine.selectedFabricatorRecipe()!=null){select(-1);click();return true;}
        if(over(x,y,left+152,top+90,16,16)){onClose();return true;}
        return handled;
    }
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double delta){
        if(delta>0&&pageIndex>0)pageIndex--;if(delta<0&&pageIndex<size)pageIndex++;return true;
    }
    @Override public boolean keyPressed(int key,int scan,int modifiers){
        if(key==GLFW.GLFW_KEY_ENTER||key==GLFW.GLFW_KEY_KP_ENTER){boolean focus=!search.isFocused();search.setFocused(focus);setFocused(focus?search:null);return true;}
        if(search.isFocused()&&key!=GLFW.GLFW_KEY_ESCAPE)return search.keyPressed(key,scan,modifiers)||true;
        if(key==GLFW.GLFW_KEY_ESCAPE||minecraft.options.keyInventory.matches(key,scan)){onClose();return true;}
        return super.keyPressed(key,scan,modifiers);
    }
    @Override public void tick(){search.tick();if(minecraft.player==null||minecraft.player.containerMenu!=menu)minecraft.setScreen(null);}
    @Override public void onClose(){minecraft.setScreen(previous);}
    @Override public boolean isPauseScreen(){return false;}
}
