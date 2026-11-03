package techguns.client.render.fx;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Original TGRenderHelper blend modes, with explicit unlit textured vertex state. */
public abstract class LegacyFxRenderTypes extends RenderType {
    private record Key(ResourceLocation texture, boolean additive, boolean depthWrite, boolean noDepthTest) {}
    private static final java.util.Map<Key, RenderType> TYPES = new java.util.HashMap<>();
    private LegacyFxRenderTypes() { super("techguns_fx", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, false, false, () -> {}, () -> {}); }
    public static RenderType sprite(ResourceLocation texture, boolean additive, boolean depthWrite) { return sprite(texture, additive, depthWrite, false); }
    /** Original NO_Z_TEST particles: additive blending drawn without depth test or depth write. */
    public static RenderType sprite(ResourceLocation texture, boolean additive, boolean depthWrite, boolean noDepthTest) {
        return TYPES.computeIfAbsent(new Key(texture, additive, depthWrite, noDepthTest), key -> create("techguns_fx_" + texture + "_" + additive + "_" + depthWrite + (noDepthTest ? "_nodepth" : ""),
                DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, false, !additive,
                CompositeState.builder().setShaderState(POSITION_COLOR_TEX_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(additive ? ADDITIVE_TRANSPARENCY : TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL).setDepthTestState(noDepthTest ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(depthWrite ? COLOR_DEPTH_WRITE : COLOR_WRITE).createCompositeState(false)));
    }
}
