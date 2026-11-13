package techguns.client.render.tileentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import techguns.fluids.FluidTankEntity;

/** Draws the stored fluid as a box inside the tank glass, as high as the tank is full. */
public class FluidTankRenderer implements BlockEntityRenderer<FluidTankEntity> {
    private static final float IN = 1.5F / 16F;
    public FluidTankRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(FluidTankEntity tank, float partial, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var fluid = tank.fluid();
        if (fluid.isEmpty()) return;
        var ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ext.getStillTexture(fluid));
        int colour = ext.getTintColor(fluid);
        float fill = Math.max(0.02F, fluid.getAmount() / (float) FluidTankEntity.CAPACITY);
        float top = IN + (1 - 2 * IN) * fill;
        int lit = fluid.getFluid().getFluidType().getLightLevel(fluid) > 0 ? 0xF000F0 : light;
        VertexConsumer vc = buffers.getBuffer(RenderType.translucent());
        var m = pose.last().pose(); var n = pose.last().normal();
        float a = IN, b = 1 - IN;
        float r = (colour >> 16 & 255) / 255F, g = (colour >> 8 & 255) / 255F, bl = (colour & 255) / 255F, al = (colour >>> 24) / 255F;
        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1(), vs = sprite.getV(16 * (1 - (top - IN)));
        // sides (texture cut to the fluid height), top, bottom
        quad(vc, m, n, a, IN, a, b, IN, a, b, top, a, a, top, a, u0, u1, v1, vs, r, g, bl, al, lit, 0, 0, -1);
        quad(vc, m, n, b, IN, b, a, IN, b, a, top, b, b, top, b, u0, u1, v1, vs, r, g, bl, al, lit, 0, 0, 1);
        quad(vc, m, n, a, IN, b, a, IN, a, a, top, a, a, top, b, u0, u1, v1, vs, r, g, bl, al, lit, -1, 0, 0);
        quad(vc, m, n, b, IN, a, b, IN, b, b, top, b, b, top, a, u0, u1, v1, vs, r, g, bl, al, lit, 1, 0, 0);
        quad(vc, m, n, a, top, a, b, top, a, b, top, b, a, top, b, u0, u1, v0, v1, r, g, bl, al, lit, 0, 1, 0);
        quad(vc, m, n, a, IN, b, b, IN, b, b, IN, a, a, IN, a, u0, u1, v0, v1, r, g, bl, al, lit, 0, -1, 0);
    }
    /** Corners in order bottom-left, bottom-right, top-right, top-left seen from outside. */
    private static void quad(VertexConsumer vc, org.joml.Matrix4f m, org.joml.Matrix3f n, float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3, float u0, float u1, float vBottom, float vTop,
                             float r, float g, float b, float a, int light, float nx, float ny, float nz) {
        float[][] c = {{x0, y0, z0, u0, vBottom}, {x1, y1, z1, u1, vBottom}, {x2, y2, z2, u1, vTop}, {x3, y3, z3, u0, vTop}};
        // both windings, so the face shows whichever way it was wound and from inside the glass as well
        for (int pass = 0; pass < 2; pass++) for (int i = 0; i < 4; i++) {
            var p = c[pass == 0 ? i : 3 - i]; float s = pass == 0 ? 1 : -1;
            vc.vertex(m, p[0], p[1], p[2]).color(r, g, b, a).uv(p[3], p[4]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n, nx * s, ny * s, nz * s).endVertex();
        }
    }
}
