package techguns.client;

import java.nio.file.*;
import java.util.*;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import techguns.Techguns;
import techguns.plugins.jei.*;
import techguns.plugins.jei.RecipeDisplays.*;

/** Registered only by the isolated JEI test, so JEI is not needed by ordinary clients. */
public final class JeiVisualVerifier {
    private record Fixture(Kind kind,Display recipe){}
    private final List<Fixture> fixtures=new ArrayList<>();private final Runnable done;
    private int index,frames,languagesDone,checked,craftingChecked;private boolean open,switching;
    private final Set<String> seenLanguages=new HashSet<>();
    private JeiVisualVerifier(Runnable done){this.done=done;}
    public static boolean begin(Minecraft mc,Runnable complete){
        if(!Boolean.getBoolean("techguns.verifyJei"))return false;
        if(TechgunsJeiPlugin.runtime==null)throw new IllegalStateException("JEI runtime did not start");
        var verifier=new JeiVisualVerifier(complete);verifier.validate(mc);MinecraftForge.EVENT_BUS.register(verifier);return true;
    }
    private void validate(Minecraft mc){
        var runtime=TechgunsJeiPlugin.runtime;var manager=runtime.getRecipeManager();var focuses=runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup();
        for(var kind:Kind.values()){
            var type=TechgunsJeiPlugin.TYPES.get(kind);var category=manager.getRecipeCategory(type);var registered=manager.createRecipeLookup(type).includeHidden().get().toList();
            var expected=TechgunsJeiPlugin.recipes.get(kind).stream().filter(Display::available).toList();if(category==null||registered.size()!=expected.size()||registered.isEmpty())throw new IllegalStateException("Missing JEI category/recipes "+kind+" "+registered.size()+"/"+expected.size());
            if(manager.createRecipeCatalystLookup(type).get().findAny().isEmpty())throw new IllegalStateException("Missing JEI machine catalyst "+kind);
            for(var recipe:registered){if(manager.createRecipeLayoutDrawable(category,recipe,focuses).isEmpty())throw new IllegalStateException("JEI layout failed "+recipe.id());checked++;}
            fixtures.add(new Fixture(kind,registered.get(0)));
            if(kind==Kind.CHEM)registered.stream().filter(r->r.slots().stream().anyMatch(s->s.role()==Role.OUTPUT&&!s.fluid().isEmpty())).findFirst().ifPresent(r->fixtures.add(new Fixture(kind,r)));
        }
        var craftingCategory=manager.getRecipeCategory(RecipeTypes.CRAFTING);
        for(var recipe:mc.level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING))if(recipe.getId().getNamespace().equals("techguns")){
            if(manager.createRecipeLayoutDrawable(craftingCategory,recipe,focuses).isEmpty())throw new IllegalStateException("JEI crafting layout failed "+recipe.getId());craftingChecked++;
        }
        if(craftingChecked<346)throw new IllegalStateException("JEI omitted original crafting recipes: "+craftingChecked);
        Techguns.LOGGER.info("TECHGUNS_JEI_LAYOUTS_PASSED: {} machine/bench layouts, {} crafting layouts, {} catalysts; {} unavailable external-tag recipes",checked,craftingChecked,Kind.values().length,TechgunsJeiPlugin.unavailable.size());
    }
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END||open||switching)return;var mc=Minecraft.getInstance();
        if(index==fixtures.size()){
            Techguns.LOGGER.info("TECHGUNS_JEI_RENDER_PASSED: {} real JEI screens; language {}",fixtures.size(),mc.options.languageCode);seenLanguages.add(mc.options.languageCode);
            if(++languagesDone<2){switching=true;mc.setScreen(null);String next=mc.options.languageCode.equals("ru_ru")?"en_us":"ru_ru";mc.options.languageCode=next;mc.getLanguageManager().setSelected(next);mc.reloadResourcePacks().thenRun(()->mc.tell(()->{index=0;switching=false;}));return;}
            try{var report=new LinkedHashMap<String,Object>();report.put("machineLayouts",checked);report.put("craftingLayouts",craftingChecked);report.put("categories",Kind.values().length);report.put("screensPerLanguage",fixtures.size());report.put("languages",seenLanguages);report.put("unavailableExternalRecipes",TechgunsJeiPlugin.unavailable);Files.writeString(mc.gameDirectory.toPath().resolve("verification/jei-report.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception error){throw new IllegalStateException(error);}
            MinecraftForge.EVENT_BUS.unregister(this);done.run();return;
        }
        var fixture=fixtures.get(index);var category=TechgunsJeiPlugin.runtime.getRecipeManager().getRecipeCategory(TechgunsJeiPlugin.TYPES.get(fixture.kind));TechgunsJeiPlugin.runtime.getRecipesGui().showRecipes(category,List.of(fixture.recipe),List.of());open=true;frames=0;
    }
    @SubscribeEvent public void render(ScreenEvent.Render.Post event){
        if(!open||!event.getScreen().getClass().getName().startsWith("mezz.jei.")||++frames<3)return;var mc=Minecraft.getInstance();var fixture=fixtures.get(index);
        for(var detail:fixture.recipe.details())if(detail.getString().contains("techguns.jei."))throw new IllegalStateException("Untranslated JEI text "+detail.getString());
        try{var output=mc.gameDirectory.toPath().resolve("verification");Files.createDirectories(output);event.getGuiGraphics().flush();try(var image=GuiVisualCapture.capture(mc,event.getScreen())){image.writeToFile(output.resolve("jei-"+mc.options.languageCode+"-"+index+"-"+fixture.kind.name().toLowerCase(Locale.ROOT)+".png"));}}
        catch(Exception error){throw new IllegalStateException("JEI screenshot failed "+fixture,error);}index++;open=false;
    }
}
