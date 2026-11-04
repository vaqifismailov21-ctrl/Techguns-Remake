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
    private final PistolRenderer renderer = new PistolRenderer();
    private final java.util.Map<GenericGun,BasicGunRenderer> others = BasicGunRenderer.create();
    public boolean hasScope(GenericGun gun) {return others.containsKey(gun)&&others.get(gun).hasScope();}
    public boolean hasModel(GenericGun gun){return others.containsKey(gun);}
    public void verifyResources(net.minecraft.server.packs.resources.ResourceManager resources){others.forEach((gun,renderer)->renderer.verifyResources(resources,gun));}
    public void renderSpecialVerification(ItemStack stack,PoseStack pose,MultiBufferSource buffers,boolean scope){
        var renderer=others.get((GenericGun)stack.getItem());
        if(scope)renderer.renderScopeVerification(stack,pose,buffers);else renderer.renderChargeVerification(stack,pose,buffers);
    }
    public static GunItemRenderer get() { if (instance == null) instance = new GunItemRenderer(); return instance; }
    private GunItemRenderer() { super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()); }
    /** Offscreen development fixture, independent of the player's current stack and wall clock. */
    public void renderVerificationFrame(ItemStack stack, PoseStack pose, MultiBufferSource buffers, float fire, float reload, float muzzle) {
        renderVerificationFrame(stack,ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,pose,buffers,fire,reload,muzzle);
    }
    public void renderVerificationFrame(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, float fire, float reload, float muzzle) {
        if(stack.getItem() instanceof GenericGun gun && others.containsKey(gun)) {
            others.get(gun).renderVerification(stack,context,pose,buffers,fire,reload,muzzle); return;
        }
        renderer.verification = true; renderer.fire = fire; renderer.reload = reload; renderer.muzzle = muzzle;
        try { renderByItem(stack, context, pose, buffers, 15728880, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY); }
        finally { renderer.verification = false; }
    }
    public void renderMotionVerificationFrame(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers) {
        if(stack.getItem() instanceof GenericGun gun && others.containsKey(gun)) {
            others.get(gun).renderVerification(stack,context,pose,buffers,0,0,0);
        } else throw new IllegalArgumentException("Motion fixture needs a ballistic model");
    }
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
        renderer.stack = stack;
        renderer.render(context, pose, buffers, light, overlay);
        if ((context.firstPerson()||third) && renderer.muzzle > 0) {
            pose.pushPose(); pose.translate(0.5, 0.5, 0.5);
            techguns.client.render.fx.PistolMuzzleFlash.render(pose, buffers, renderer.muzzle,
                    left, third);
            pose.popPose();
        }
    }
    private static final class PistolRenderer extends RenderItemBase {
        private ItemStack stack;
        private float fire, reload, muzzle;
        private boolean verification;
        private PistolRenderer() {
            super(new ModelPistol(), ResourceLocation.fromNamespaceAndPath(Techguns.MODID, "textures/guns/pistol3.png"));
            baseScale = 1.2F; guiScale = 0.9F; groundScale = 0.35F;
            baseTranslation = new float[]{0.03125F, -0.3F, -0.4F};
            translations = new float[][]{{0,0.09F,-0.02F},{0,-0.03F,0},{0.02F,-0.08F,0},{0.02F,-0.08F,0},{0,0,0}};
        }
        @Override protected void applyAnimation(PoseStack pose, ItemDisplayContext context) {
            boolean first=context.firstPerson(),third=context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            boolean left = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            if (verification) {
                if (reload > 0) GunAnimation.genericReload.play(pose,reload,left,first?1:0,40);
                else if (fire > 0) GunAnimation.genericRecoil.play(pose,fire,left,first?0.025F:0,first?12:5);
                return;
            }
            fire = reload = muzzle = 0;
            var entity = first?Minecraft.getInstance().player:third?GunRenderContext.holder():null;
            if (!(entity instanceof net.minecraft.world.entity.player.Player player)) return;
            boolean offhand = left == (player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT);
            var state=TGExtendedPlayer.find(player).orElse(null);
            if(state==null) return;
            var attack = state.getAttackTime(offhand);
            long now = System.currentTimeMillis();
            long remainingMuzzle = attack.getMuzzleFlashTime() - now;
            if (remainingMuzzle > 0 && remainingMuzzle <= attack.getMuzzleFlashTimeTotal())
                muzzle = 1 - remainingMuzzle / (float) attack.getMuzzleFlashTimeTotal();
            if (attack.getReloadTime() > now && attack.getReloadTimeTotal() > 0) {
                reload = 1 - (attack.getReloadTime() - now) / (float) attack.getReloadTimeTotal();
                GunAnimation.genericReload.play(pose, reload, left, first?1:0, 40);
            } else if (attack.getRecoilTime() > now && attack.getRecoilTimeTotal() > 0) {
                fire = 1 - (attack.getRecoilTime() - now) / (float) attack.getRecoilTimeTotal();
                GunAnimation.genericRecoil.play(pose, fire, left, first?0.025F:0, first?12:5);
            } else if (first && offhand && player.isShiftKeyDown()) pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-35));
        }
        @Override protected void renderContents(ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            int ammo = stack.getItem() instanceof GenericGun gun ? gun.getCurrentAmmo(stack) : 0;
            var vertices = buffers.getBuffer(RenderType.entityCutout(texture));
            for (int part = 0; part < 2; part++) model.renderModel(pose, vertices, light, overlay, context, null, ammo, reload, part, fire, 0);
        }
    }
}
