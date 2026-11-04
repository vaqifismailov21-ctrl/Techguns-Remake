package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import techguns.TGArmors;
import techguns.Techguns;
import techguns.client.models.LegacyModelBase;
import techguns.client.models.armor.*;
import techguns.items.armors.GenericShield;

public final class ShieldItemRenderer extends BlockEntityWithoutLevelRenderer {
    private record Definition(LegacyModelBase model, String[] textures) {}
    private static ShieldItemRenderer instance;
    private final Map<Item, Definition> definitions;

    public static ShieldItemRenderer get() { if (instance == null) instance = new ShieldItemRenderer(); return instance; }
    private ShieldItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        definitions = Map.of(
                TGArmors.riot_shield.get(), new Definition(new ModelRiotShield(), new String[]{"riot_shield", "riot_shield_black", "riot_shield_darkgreen", "riot_shield_grey"}),
                TGArmors.ballistic_shield.get(), new Definition(new ModelBallisticShield(), new String[]{"ballistic_shield", "ballistic_shield_black", "ballistic_shield_green", "ballistic_shield_grey"}),
                TGArmors.advanced_shield.get(), new Definition(new ModelAdvancedShield(), new String[]{"advanced_shield", "advanced_shield_silver", "advanced_shield_green"}));
    }
    @Override public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var def = definitions.get(stack.getItem());
        if (def == null || !(stack.getItem() instanceof GenericShield shield)) return;
        int camo = shield.getCurrentCamoIndex(stack);
        if (camo < 0 || camo >= def.textures.length) return;
        var texture = ResourceLocation.fromNamespaceAndPath(Techguns.MODID, "textures/armors/" + def.textures[camo] + ".png");
        pose.pushPose();
        pose.scale(1, -1, -1);
        // visors and windows are half transparent in the textures (1.12 drew items with blending)
        def.model.renderModel(pose, buffers.getBuffer(RenderType.entityTranslucent(texture)), light, overlay, context);
        pose.popPose();
    }
}
