package techguns.plugins.jei;

import java.util.*;
import mezz.jei.api.*;
import mezz.jei.api.gui.handlers.*;
import mezz.jei.api.recipe.*;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import techguns.*;
import techguns.client.gui.*;
import techguns.items.armors.ICamoChangeable;
import techguns.plugins.jei.RecipeDisplays.*;

@JeiPlugin
public final class TechgunsJeiPlugin implements IModPlugin {
    public static final Map<Kind,RecipeType<Display>> TYPES=new EnumMap<>(Kind.class);
    public static IJeiRuntime runtime;
    public static Map<Kind,List<Display>> recipes=Map.of();
    public static final List<String> unavailable=new ArrayList<>();
    static{for(var kind:Kind.values())TYPES.put(kind,RecipeType.create("techguns",kind.id(),Display.class));}
    @Override public ResourceLocation getPluginUid(){return ResourceLocation.fromNamespaceAndPath("techguns","original_recipes");}
    @Override public void registerCategories(IRecipeCategoryRegistration registration){for(var kind:Kind.values())registration.addRecipeCategories(new TechgunsRecipeCategory(registration.getJeiHelpers().getGuiHelper(),kind,TYPES.get(kind)));registration.addRecipeCategories(new NpcGearCategory(registration.getJeiHelpers().getGuiHelper()));}
    @Override public void registerRecipes(IRecipeRegistration registration){
        powerInfo(registration);
        registration.addRecipes(NpcGearCategory.TYPE,NpcGearCategory.collect());
        recipes=RecipeDisplays.all();unavailable.clear();for(var kind:Kind.values()){var entries=recipes.get(kind);entries.stream().filter(r->!r.available()).forEach(r->unavailable.add(r.id()));registration.addRecipes(TYPES.get(kind),entries.stream().filter(Display::available).toList());}
        Techguns.LOGGER.info("TECHGUNS_JEI_REGISTERED: {} categories, {} available recipes; {} external-tag recipes unavailable",TYPES.size(),recipes.values().stream().flatMap(List::stream).filter(Display::available).count(),unavailable.size());
    }
    /** Power blocks have no machine recipes; JEI shows what they do on their info page. */
    private static void powerInfo(IRecipeRegistration registration){
        for(var item:techguns.power.PowerBlocks.tabItems()){var id=net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item.get()).getPath();
            registration.addIngredientInfo(new net.minecraft.world.item.ItemStack(item.get()),mezz.jei.api.constants.VanillaTypes.ITEM_STACK,net.minecraft.network.chat.Component.translatable("techguns.power.jei."+id));}
    }
    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration){for(var kind:Kind.values())registration.addRecipeCatalyst(kind.icon(),TYPES.get(kind));}
    @Override public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration){var crafting=registration.getCraftingCategory();crafting.addCategoryExtension(techguns.recipes.AmmoSwitchRecipe.class,DynamicCraftingExtension::new);crafting.addCategoryExtension(techguns.recipes.MiningHeadRecipe.class,DynamicCraftingExtension::new);}
    @Override public void registerItemSubtypes(ISubtypeRegistration registration){for(var registered:TGItems.ITEMS.getEntries())if(registered.get() instanceof ICamoChangeable)registration.registerSubtypeInterpreter(registered.get(),(mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter<net.minecraft.world.item.ItemStack>)(stack,context)->{var tag=stack.hasTag()?stack.getTag():new net.minecraft.nbt.CompoundTag();return "camo="+tag.getByte("camo")+";ammo="+(tag.contains("ammovariant")?tag.getString("ammovariant"):techguns.items.guns.ammo.AmmoTypes.TYPE_DEFAULT)+";head="+tag.getInt("miningHead")+";ench="+tag.getList("Enchantments",10);});}
    @Override public void registerGuiHandlers(IGuiHandlerRegistration registration){
        registration.addGuiContainerHandler(MachineScreen.class,new IGuiContainerHandler<MachineScreen>(){
            @Override public Collection<IGuiClickableArea> getGuiClickableAreas(MachineScreen screen,double x,double y){Kind kind=Kind.machine(screen.getMenu().machine.kind);int[] area=switch(kind){case AMMO,METAL->new int[]{119,36,19,22};case CHEM->new int[]{69,21,71,31};case GRINDER->new int[]{51,29,52,31};case CHARGING->new int[]{38,18,28,12};case REACTION->new int[]{61,89,58,11};case FABRICATOR->new int[]{18,58,140,50};default->new int[]{20,52,96,12};};return List.of(IGuiClickableArea.createBasic(area[0],area[1],area[2],area[3],TYPES.get(kind)));}
            @Override public List<Rect2i> getGuiExtraAreas(MachineScreen screen){return List.of(new Rect2i(screen.getGuiLeft()-27,screen.getGuiTop(),27,95));}
        });
        // on the title: the progress cone has its own tooltip (ores per hour, power) that the JEI hint would cover
        registration.addRecipeClickArea(OreDrillScreen.class,80,3,40,11,TYPES.get(Kind.ORE_DRILL));
        registration.addGuiContainerHandler(BenchScreen.class,new IGuiContainerHandler<BenchScreen>(){
            @Override public Collection<IGuiClickableArea> getGuiClickableAreas(BenchScreen screen,double x,double y){var kind=switch(screen.getMenu().bench.kind){case CAMO->Kind.CAMO;case REPAIR->Kind.REPAIR;case UPGRADE->Kind.UPGRADE;};return kind==Kind.UPGRADE?List.of(IGuiClickableArea.createBasic(73,40,38,30,TYPES.get(kind))):List.of();}
            @Override public List<Rect2i> getGuiExtraAreas(BenchScreen screen){return List.of(new Rect2i(screen.getGuiLeft()-46,screen.getGuiTop(),45,145));}
        });
    }
    @Override public void onRuntimeAvailable(IJeiRuntime value){runtime=value;}
    @Override public void onRuntimeUnavailable(){runtime=null;}
}
