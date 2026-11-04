package techguns.client;

import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
/** Measures the actual Patchouli text layout and explicit headings after a page is displayed. */
final class ManualLayoutVerifier {
    private static final List<String> errors=new ArrayList<>();
    private static final Map<String,Float> scales=new TreeMap<>();
    private static int checked;
    static void reset(){errors.clear();scales.clear();checked=0;}
    static void screen(Minecraft mc,Object screen,String id,int page)throws ReflectiveOperationException{
        String label=mc.options.languageCode+"/"+id+"/"+page;
        renderers(screen,label);
        if(page<0)return;
        for(String side:new String[]{"leftPage","rightPage"}){
            Object active=field(screen,side);if(active==null)continue;renderers(active,label+"/"+side);checked++;
            var source=(com.google.gson.JsonObject)field(active,"sourceObject");
            for(String key:new String[]{"title","name"})if(source!=null&&source.has(key))heading(mc,net.minecraft.client.resources.language.I18n.get(source.get(key).getAsString()),label+"/"+key);
        }
        if(page==0){Object entry=field(screen,"entry");heading(mc,((Component)entry.getClass().getMethod("getName").invoke(entry)).getString(),label+"/entry-name");}
    }
    /** English headings that are already wider than the page in the original 1.12 manual; the original text is kept 1:1. */
    private static final Set<String> ORIGINAL_WIDE_HEADINGS=Set.of("en_us/techguns:guide_tier1/0/entry-name=1. Preparing for Nether");
    private static void heading(Minecraft mc,String text,String label){if(mc.font.width(text)>116&&!ORIGINAL_WIDE_HEADINGS.contains(label+"="+text))errors.add(label+": title width "+mc.font.width(text)+" > 116: "+text);}
    private static void renderers(Object object,String label)throws ReflectiveOperationException{
        for(Class<?> type=object.getClass();type!=null;type=type.getSuperclass())for(Field member:type.getDeclaredFields())if(member.getType().getName().equals("vazkii.patchouli.client.book.gui.BookTextRenderer")){
            member.setAccessible(true);Object renderer=member.get(object);if(renderer==null)continue;
            float scale=(float)field(renderer,"scale");scales.merge(Minecraft.getInstance().options.languageCode,scale,Math::min);
            if(scale<.70f)errors.add(label+": body text scaled below 70%: "+scale);
            var words=(List<?>)field(renderer,"words");if(words.isEmpty())continue;
            Object first=words.get(0);int originX=(int)field(first,"x"),originY=(int)field(first,"y");
            for(Object word:words){int x=(int)field(word,"x"),y=(int)field(word,"y"),height=(int)field(word,"height");var text=(Component)field(word,"text");
                // Patchouli may wrap after a trailing space; only visible glyphs have page bounds.
                var visible=Component.literal(text.getString().stripTrailing()).withStyle(text.getStyle());if(visible.getString().isEmpty())continue;
                float right=originX+(x-originX+Minecraft.getInstance().font.width(visible))*scale,bottom=originY+(y-originY+height)*scale;
                if(right>originX+117||bottom>157){errors.add(label+": text exceeds page bounds at "+right+","+bottom);break;}
            }
        }
    }
    private static Object field(Object object,String name)throws ReflectiveOperationException{
        for(Class<?> type=object.getClass();type!=null;type=type.getSuperclass())try{Field member=type.getDeclaredField(name);member.setAccessible(true);return member.get(object);}catch(NoSuchFieldException ignored){}
        throw new NoSuchFieldException(object.getClass().getName()+"."+name);
    }
    static void finish(Minecraft mc){
        try{var report=new LinkedHashMap<String,Object>();report.put("renderedPages",checked);report.put("minimumBodyScaleByLanguage",scales);report.put("errors",errors);Files.writeString(mc.gameDirectory.toPath().resolve("verification/manual-layout.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));}
        catch(java.io.IOException error){throw new IllegalStateException("Cannot save manual layout report",error);}
        if(!errors.isEmpty())throw new IllegalStateException("Manual layout failed ("+errors.size()+"): "+errors.stream().limit(20).toList());
        techguns.Techguns.LOGGER.info("TECHGUNS_MANUAL_LAYOUT_PASSED: {} displayed pages in two languages; minimum body scales {}",checked,scales);
    }
    private ManualLayoutVerifier(){}
}
