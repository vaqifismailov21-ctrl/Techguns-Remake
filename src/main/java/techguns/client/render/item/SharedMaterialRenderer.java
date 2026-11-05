package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import techguns.Techguns;
import techguns.TGItems;
import techguns.client.models.items.*;
import techguns.client.models.projectiles.ModelRocket;

/** All 13 original shared item renderer definitions, including their exact transform values. */
public final class SharedMaterialRenderer extends BlockEntityWithoutLevelRenderer {
    private static SharedMaterialRenderer instance;
    private final Map<Item, RenderItemBase> renderers = new HashMap<>();

    public static SharedMaterialRenderer get() {
        if (instance == null) instance = new SharedMaterialRenderer();
        return instance;
    }

    private SharedMaterialRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        float[][] m4magTranslations = {
				{0,0.25f,0}, 
				{0f,0.25f,0.05f}, 
				{0.1f,0.25f,0}, 
				{0,-0.1f,0}, 
				{0,0,-0.05f} 
		};
		
		float[][] as50magTranslations = {
				{0,0f,0}, 
				{0f,0f,0f}, 
				{0f,0.05f,0}, 
				{0,-0.1f,0}, 
				{0,0,0f} 
		};
		
		float[][] lmgmagTranslations = {
				{0,0.35f,0}, 
				{0f,0.15f,0.0f}, 
				{0.1f,0.25f,0}, 
				{0,0f,0}, 
				{0,0.25f,-0.05f} 
		};
		
		float[][] rocketTranslations = {
				{0,0f,0f}, 
				{0f,-0.1f,0.02f}, 
				{0.0f,0.0f,0}, 
				{0.0f,0.0f,0}, 
				{0,0,0f} 
		};
		
		add("assaultriflemagazine", new RenderItemBase(new ModelARMagazine(false), new ResourceLocation(Techguns.MODID,"textures/guns/ar_mag.png")).setBaseScale(1.25f).setGUIScale(0.85f).setBaseTranslation(0, -0.2f, 0).setTransformTranslations(m4magTranslations));
		add("assaultriflemagazineempty", new RenderItemBase(new ModelARMagazine(true), new ResourceLocation(Techguns.MODID,"textures/guns/ar_mag.png")).setBaseScale(1.25f).setGUIScale(0.85f).setBaseTranslation(0, -0.2f, 0).setTransformTranslations(m4magTranslations));
		
		add("assaultriflemagazine_incendiary", new RenderItemBase(new ModelARMagazine(false), new ResourceLocation(Techguns.MODID,"textures/guns/ar_mag_inc.png")).setBaseScale(1.25f).setGUIScale(0.85f).setBaseTranslation(0, -0.2f, 0).setTransformTranslations(m4magTranslations));
		
		
		add("lmgmagazine", new RenderItemLMGMag(new ModelLmgMag(false), new ResourceLocation(Techguns.MODID,"textures/guns/lmg_mag.png")).setBaseScale(1.25f).setGUIScale(0.75f).setBaseTranslation(0, 0f, 0.2f).setTransformTranslations(lmgmagTranslations));
		add("lmgmagazineempty", new RenderItemLMGMag(new ModelLmgMag(true), new ResourceLocation(Techguns.MODID,"textures/guns/lmg_mag.png")).setBaseScale(1.25f).setGUIScale(0.75f).setBaseTranslation(0, 0f, 0.2f).setTransformTranslations(lmgmagTranslations));	
		
		add("lmgmagazine_incendiary", new RenderItemLMGMag(new ModelLmgMag(false), new ResourceLocation(Techguns.MODID,"textures/guns/lmg_mag_inc.png")).setBaseScale(1.25f).setGUIScale(0.75f).setBaseTranslation(0, 0f, 0.2f).setTransformTranslations(lmgmagTranslations));
		
		
		add("as50magazine", new RenderItemBase(new ModelAS50Mag(false), new ResourceLocation(Techguns.MODID,"textures/guns/as50_mag.png")).setBaseScale(1.5f).setGUIScale(0.75f).setBaseTranslation(0.0325f, -0.2f, 0.33f).setTransformTranslations(as50magTranslations));
		add("as50magazineempty", new RenderItemBase(new ModelAS50Mag(true), new ResourceLocation(Techguns.MODID,"textures/guns/as50_mag.png")).setBaseScale(1.5f).setGUIScale(0.75f).setBaseTranslation(0.0325f, -0.2f, 0.33f).setTransformTranslations(as50magTranslations));
		
		add("as50magazine_incendiary", new RenderItemBase(new ModelAS50Mag(false), new ResourceLocation(Techguns.MODID,"textures/guns/as50_mag_inc.png")).setBaseScale(1.5f).setGUIScale(0.75f).setBaseTranslation(0.0325f, -0.2f, 0.33f).setTransformTranslations(as50magTranslations));
		add("as50magazine_explosive", new RenderItemBase(new ModelAS50Mag(false), new ResourceLocation(Techguns.MODID,"textures/guns/as50_mag_exp.png")).setBaseScale(1.5f).setGUIScale(0.75f).setBaseTranslation(0.0325f, -0.2f, 0.33f).setTransformTranslations(as50magTranslations));
		
		
		add("rocket", new RenderItemBaseRocketItem(new ModelRocket(), new ResourceLocation(Techguns.MODID,"textures/guns/rocket.png")).setBaseScale(1.5f).setGUIScale(0.5f).setBaseTranslation(0, 0, 0.1f).setTransformTranslations(rocketTranslations).setFirstPersonScale(0.35f));
		
		add("rocket_nuke", new RenderItemBaseRocketItem(new ModelRocket(), new ResourceLocation(Techguns.MODID,"textures/guns/rocket_nuke.png")).setBaseScale(1.5f).setGUIScale(0.5f).setBaseTranslation(0, 0, 0.1f).setTransformTranslations(rocketTranslations).setFirstPersonScale(0.35f));
		add("rocket_thermonuke", new RenderItemBaseRocketItem(new ModelRocket(), new ResourceLocation(Techguns.MODID,"textures/guns/rocket_thermonuke.png")).setBaseScale(1.5f).setGUIScale(0.5f).setBaseTranslation(0, 0, 0.1f).setTransformTranslations(rocketTranslations).setFirstPersonScale(0.35f));
		
		add("rocket_high_velocity", new RenderItemBaseRocketItem(new ModelRocket(), new ResourceLocation(Techguns.MODID,"textures/guns/rocket_hv.png")).setBaseScale(1.5f).setGUIScale(0.5f).setBaseTranslation(0, 0, 0.1f).setTransformTranslations(rocketTranslations).setFirstPersonScale(0.35f));
		
		
    }

    private void add(String name, RenderItemBase renderer) {
        renderers.put(TGItems.SHARED_ITEMS.get(name).get(), renderer);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        RenderItemBase renderer = renderers.get(stack.getItem());
        if (renderer != null) renderer.render(context, pose, buffers, light, overlay);
    }
}
