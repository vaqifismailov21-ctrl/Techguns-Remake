package techguns.plugins.jei;

import java.util.*;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import techguns.TGConfig;
import techguns.TGNpcs;
import techguns.entities.npcs.GenericNPC;
import techguns.items.guns.GenericGun;

/** JEI page per Techguns NPC: the guns, tools and armour it can spawn with, how often, and what a player's kill can leave behind. */
public final class NpcGearCategory implements IRecipeCategory<NpcGearCategory.Gear> {
    public static final RecipeType<Gear> TYPE=RecipeType.create("techguns","npc_gear",Gear.class);
    private static final int ROLLS=1200,COLUMNS=8,TOP=12;
    /** One NPC: its name, spawn egg, and each possible piece of gear with the share of spawns that get it. */
    public record Gear(EntityType<?> type,ItemStack egg,List<ItemStack> items,List<Float> chances,boolean dropsGun){}

    private final IDrawable background,icon;
    NpcGearCategory(IGuiHelper gui){
        background=gui.createBlankDrawable(162,TOP+18*3+36);
        icon=gui.createDrawableItemStack(new ItemStack(techguns.TGuns.m4.get()));
    }
    @Override public RecipeType<Gear> getRecipeType(){return TYPE;}
    @Override public Component getTitle(){return Component.translatable("techguns.jei.npc_gear");}
    @Override public IDrawable getBackground(){return background;}
    @Override public IDrawable getIcon(){return icon;}
    @Override public void setRecipe(IRecipeLayoutBuilder builder,Gear gear,IFocusGroup focuses){
        if(!gear.egg().isEmpty())builder.addSlot(RecipeIngredientRole.INPUT,1,TOP+1).addItemStack(gear.egg()).setStandardSlotBackground();
        for(int i=0;i<gear.items().size()&&i<COLUMNS*3;i++){
            float chance=gear.chances().get(i);var stack=gear.items().get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT,22+(i%COLUMNS)*17,TOP+1+(i/COLUMNS)*18).addItemStack(stack).setStandardSlotBackground()
                .addTooltipCallback((view,tooltip)->{
                    tooltip.add(Component.translatable("techguns.jei.npc_gear.spawn",percent(chance)).withStyle(net.minecraft.ChatFormatting.GOLD));
                    String drop=dropLine(gear,stack);if(drop!=null)tooltip.add(Component.translatable(drop,percent(stack.getItem() instanceof GenericGun?TGConfig.npcGunDropChance:TGConfig.npcArmorDropChance)).withStyle(net.minecraft.ChatFormatting.GRAY));
                });
        }
    }
    private static String dropLine(Gear gear,ItemStack stack){
        if(stack.getItem() instanceof GenericGun)return gear.dropsGun()&&!TGConfig.disableGunDrops?"techguns.jei.npc_gear.drop_gun":null;
        if(stack.getItem() instanceof net.minecraft.world.item.ArmorItem)return TGConfig.disableArmorDrops?null:"techguns.jei.npc_gear.drop_armor";
        return null;
    }
    @Override public void draw(Gear gear,IRecipeSlotsView slots,GuiGraphics graphics,double mouseX,double mouseY){
        var font=Minecraft.getInstance().font;
        graphics.drawString(font,gear.type().getDescription(),0,1,0x404040,false);
        int y=TOP+18*((Math.max(1,Math.min(gear.items().size(),COLUMNS*3))+COLUMNS-1)/COLUMNS)+3;
        var lines=new ArrayList<Component>();
        lines.add(gear.dropsGun()&&!TGConfig.disableGunDrops?Component.translatable("techguns.jei.npc_gear.gun_line",percent(TGConfig.npcGunDropChance)):Component.translatable("techguns.jei.npc_gear.gun_none"));
        lines.add(TGConfig.disableArmorDrops?Component.translatable("techguns.jei.npc_gear.armor_none"):Component.translatable("techguns.jei.npc_gear.armor_line",percent(TGConfig.npcArmorDropChance)));
        lines.add(Component.translatable("techguns.jei.npc_gear.loot_hint"));
        for(var line:lines){float scale=Math.min(1,162f/Math.max(1,font.width(line)));graphics.pose().pushPose();graphics.pose().translate(0,y,0);graphics.pose().scale(scale,scale,1);graphics.drawString(font,line,0,0,0x404040,false);graphics.pose().popPose();y+=10;}
    }
    private static String percent(float chance){
        float p=chance*100;return p>=10||p==Math.round(p)?String.valueOf(Math.round(p)):String.format(java.util.Locale.ROOT,"%.1f",p);
    }

    /** Rolls each NPC's spawn gear on a client-side copy of it. Empty when no world is loaded. */
    static List<Gear> collect(){
        var level=Minecraft.getInstance().level;var list=new ArrayList<Gear>();if(level==null)return list;
        for(var entry:TGNpcs.TYPES.entrySet()){
            try{
                var type=entry.getValue().get();GenericNPC npc=type.create(level);if(npc==null)continue;
                var counts=npc.sampleSpawnGear(ROLLS);var items=new ArrayList<ItemStack>();var chances=new ArrayList<Float>();
                counts.entrySet().stream().sorted((a,b)->Integer.compare(order(b.getKey()),order(a.getKey()))!=0?Integer.compare(order(b.getKey()),order(a.getKey())):Integer.compare(b.getValue(),a.getValue()))
                    .forEach(e->{items.add(new ItemStack(e.getKey()));chances.add(e.getValue()/(float)ROLLS);});
                npc.discard();
                if(items.isEmpty())continue;
                var egg=TGNpcs.EGGS.get(entry.getKey());
                list.add(new Gear(type,egg!=null?new ItemStack(egg.get()):ItemStack.EMPTY,items,chances,npc.canDropCarriedGun()));
            }catch(RuntimeException e){techguns.Techguns.LOGGER.warn("NPC gear page for {} skipped: {}",entry.getKey(),e.toString());}
        }
        return list;
    }
    /** Guns first, then other hand items, then armour. */
    private static int order(net.minecraft.world.item.Item item){return item instanceof GenericGun?2:item instanceof net.minecraft.world.item.ArmorItem?0:1;}
}
