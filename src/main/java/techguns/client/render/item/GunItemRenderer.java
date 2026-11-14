package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import techguns.Techguns;
import techguns.capabilities.TGExtendedPlayer;
import techguns.client.models.guns.ModelPistol;
import techguns.items.guns.GenericGun;

public final class GunItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static GunItemRenderer instance;
    private final java.util.Map<GenericGun,BasicGunRenderer> others = BasicGunRenderer.create();
    public boolean hasScope(GenericGun gun) {return others.containsKey(gun)&&others.get(gun).hasScope();}
    public boolean hasModel(GenericGun gun){return others.containsKey(gun);}
    public static GunItemRenderer get() { if (instance == null) instance = new GunItemRenderer(); return instance; }
    private GunItemRenderer() { super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()); }
    @Override public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if(stack.getItem() instanceof GenericGun gun&&!others.containsKey(gun))throw new IllegalStateException("Weapon has no original model adapter: "+stack.getItem());
        boolean third=context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        var holder=context.firstPerson()?Minecraft.getInstance().player:third?GunRenderContext.holder():null;
        boolean left=context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        if(holder!=null && left==(holder.getMainArm()==net.minecraft.world.entity.HumanoidArm.RIGHT)
                && !techguns.api.guns.GunManager.canUseOffhand(holder)) return;
        if(stack.getItem() instanceof GenericGun gun && others.containsKey(gun)) {
            others.get(gun).renderWeapon(stack,context,pose,buffers,light,overlay); return;
        }
    }
}
