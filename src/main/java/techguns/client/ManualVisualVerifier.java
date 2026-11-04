package techguns.client;
import java.nio.file.*;
import java.lang.reflect.Method;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.fml.common.Mod;
/** Optional dependency is accessed only inside the opt-in integration test. */
@Mod.EventBusSubscriber(modid="techguns",value=Dist.CLIENT)
public final class ManualVisualVerifier {
    private record Spread(ResourceLocation entry,int page){}
    private static final List<Spread> spreads=new ArrayList<>();
    private static Object registry,book;private static Map<?,?> categories;private static Method display;private static Runnable complete;private static boolean running,open;private static int index,frames,languagesDone,pageSpreads;
    public static boolean begin(Minecraft mc,Runnable done){
        if(!Boolean.getBoolean("techguns.verifyManual"))return false;
        if(!net.minecraftforge.fml.ModList.get().isLoaded("patchouli"))throw new IllegalStateException("Manual integration test needs Patchouli");
        complete=done;languagesDone=0;ManualLayoutVerifier.reset();initialize(mc);return true;
    }
    private static void initialize(Minecraft mc){
        try{
            spreads.clear();var common=Class.forName("vazkii.patchouli.common.book.BookRegistry");var books=(Map<?,?>)common.getField("books").get(common.getField("INSTANCE").get(null));book=books.get(ResourceLocation.fromNamespaceAndPath("techguns","techguns_manual"));if(book==null)throw new IllegalStateException("Techguns manual was not registered");
            book.getClass().getMethod("reloadContents",net.minecraft.world.level.Level.class,boolean.class).invoke(book,mc.level,false);
            var contents=book.getClass().getMethod("getContents").invoke(book);
            if((boolean)contents.getClass().getMethod("isErrored").invoke(contents))throw new IllegalStateException("Manual content failed to load",(Exception)contents.getClass().getMethod("getException").invoke(contents));
            var entries=(Map<?,?>)contents.getClass().getField("entries").get(contents);if(entries.size()!=39)throw new IllegalStateException("Expected 39 manual entries, got "+entries.size());
            for(Object entry:entries.values())if(mc.options.languageCode.equals("ru_ru")&&!((net.minecraft.network.chat.Component)entry.getClass().getMethod("getName").invoke(entry)).getString().matches(".*[А-Яа-яЁё].*"))throw new IllegalStateException("Russian manual fell back to English");
            var ordered=new ArrayList<>(entries.keySet());ordered.sort(Comparator.comparing(Object::toString));
            for(var id:ordered){var entry=entries.get(id);var pages=(List<?>)entry.getClass().getMethod("getPages").invoke(entry);for(int page=0;page<pages.size();page+=2)spreads.add(new Spread((ResourceLocation)id,page));}
            pageSpreads=spreads.size();categories=(Map<?,?>)contents.getClass().getField("categories").get(contents);for(Object id:categories.keySet())spreads.add(new Spread((ResourceLocation)id,-1));
            var client=Class.forName("vazkii.patchouli.client.book.ClientBookRegistry");registry=client.getField("INSTANCE").get(null);display=client.getMethod("displayBookGui",ResourceLocation.class,ResourceLocation.class,int.class);running=true;open=false;index=0;
        }catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot initialize Patchouli verification",error);}
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(!running||open||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        if(index>=spreads.size()){
            running=false;techguns.Techguns.LOGGER.info("TECHGUNS_MANUAL_RENDER_PASSED: 39 entries and {} page spreads opened in Patchouli; language {}; {} category screens",pageSpreads,mc.options.languageCode,categories.size());
            if(++languagesDone<2){mc.setScreen(null);String next=mc.options.languageCode.equals("ru_ru")?"en_us":"ru_ru";mc.options.languageCode=next;mc.getLanguageManager().setSelected(next);mc.reloadResourcePacks().thenRun(()->mc.tell(()->initialize(mc)));return;}
            ManualLayoutVerifier.finish(mc);complete.run();return;
        }
        var spread=spreads.get(index);try{
            if(spread.page<0){var screen=Class.forName("vazkii.patchouli.client.book.gui.GuiBookCategory").getConstructor(book.getClass(),Class.forName("vazkii.patchouli.client.book.BookCategory")).newInstance(book,categories.get(spread.entry));mc.setScreen((net.minecraft.client.gui.screens.Screen)screen);}
            else display.invoke(registry,ResourceLocation.fromNamespaceAndPath("techguns","techguns_manual"),spread.entry,spread.page);open=true;frames=0;
        }catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot open manual entry "+spread,error);}
    }
    @SubscribeEvent public static void rendered(ScreenEvent.Render.Post event){
        if(!running||!open||!event.getScreen().getClass().getName().startsWith("vazkii.patchouli.")||++frames<2)return;
        var mc=Minecraft.getInstance();var spread=spreads.get(index);
        try{ManualLayoutVerifier.screen(mc,event.getScreen(),spread.entry.toString(),spread.page);var output=mc.gameDirectory.toPath().resolve("verification");Files.createDirectories(output);event.getGuiGraphics().flush();try(var image=GuiVisualCapture.capture(mc,event.getScreen())){image.writeToFile(output.resolve("manual-"+mc.options.languageCode+"-"+(spread.page<0?"category-":"")+spread.entry.getPath().replace('/','_')+"-"+spread.page+".png"));}}catch(Exception error){throw new IllegalStateException("Manual GPU check failed at "+spread,error);}
        index++;open=false;
    }
    private ManualVisualVerifier(){}
}
