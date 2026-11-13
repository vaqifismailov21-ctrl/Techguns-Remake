package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import techguns.TGConfig;
import techguns.api.guns.GunHandType;
import techguns.items.guns.GenericGun;

import java.util.HashMap;
import java.util.Map;

/**
 * The player's arms on a gun in first person. Every gun has its own grip points on its model (the pistol grip for the
 * shooting hand, the handguard or fore-grip for the other one); the hands are put on those points after all of the
 * gun's own transforms, so they stay on the gun while it bobs, kicks and reloads. Each arm runs from the hand back
 * towards its shoulder below the camera.
 */
public final class FirstPersonGunArms {
    private static final float PX = 1F / 16F;
    /** Shoulders in camera space for a right-handed player; x is mirrored for the left hand. */
    private static final Vector3f BACK_SHOULDER = new Vector3f(0.55F, -1.05F, 0.25F);
    private static final Vector3f FRONT_SHOULDER = new Vector3f(-0.75F, -0.95F, 0.15F);
    /** Which way the inside of each arm faces, camera space, right-handed player. */
    private static final Vector3f BACK_INSIDE = new Vector3f(-1F, 0.3F, 0F);
    private static final Vector3f FRONT_INSIDE = new Vector3f(-0.4F, -1F, 0F);
    private static final float ARM_SCALE = 0.5F, UPPER_ARM = 36F;
    private static final Map<String, float[]> GRIPS = grips();
    private static final Map<GenericGun, float[]> CACHE = new HashMap<>();

    /** Grip points in the model space of each gun renderer: back hand x,y,z then front hand x,y,z. */
    private static Map<String, float[]> grips() {
        var m = new HashMap<String, float[]>();
        m.put("handcannon",new float[]{-0.78F,0.16F,0.0312F,0.32F,0.22F,0.0563F});
        m.put("sawedoff",new float[]{-0.74F,0.16F,0.0312F,-0.04F,0.14F,0.0312F});
        m.put("revolver",new float[]{-0.29F,0.44F,0.0625F});
        m.put("ak47",new float[]{-0.0312F,0.3667F,0.5067F,-0.0312F,0.1267F,-1.4933F});
        m.put("m4",new float[]{-0.0313F,0.26F,0.5F,-0.0345F,0.22F,-0.86F});
        m.put("thompson",new float[]{-0.34F,0.36F,0.0625F,0.96F,0.16F,0.0625F});
        m.put("pistol",new float[]{-0.0312F,0.3667F,0.6333F});
        m.put("lmg",new float[]{-0.0313F,0.3F,0.52F,-0.0366F,0.3F,-0.9F});
        m.put("boltaction",new float[]{-0.4889F,0.0963F,0.0312F,0.3556F,0.1111F,0.0312F});
        m.put("flamethrower",new float[]{-0.44F,0.32F,0.0312F,0.76F,0.48F,0.0313F});
        m.put("biogun",new float[]{-0.83F,0.52F,0.0313F,0.51F,0.3F,0.0312F});
        m.put("rocketlauncher",new float[]{0.6F,0.42F,0.0313F,1.3F,0.24F,0.0313F});
        m.put("minigun",new float[]{0.3F,0.2167F,0.0F,0.6333F,0.2333F,0.0F});
        m.put("combatshotgun",new float[]{-0.6F,0.32F,0.0313F,0.76F,0.26F,0.0313F});
        m.put("grimreaper",new float[]{-0.32F,0.6F,-0.0F,0.46F,0.6F,-0.0312F});
        m.put("pdw",new float[]{-0.62F,0.34F,0.0F,0.04F,0.54F,0.0F});
        m.put("as50",new float[]{-0.0312F,0.2412F,0.7059F,-0.0291F,0.0529F,-0.8941F});
        m.put("teslagun",new float[]{-0.81F,0.42F,0.0312F,0.27F,0.46F,0.0312F});
        m.put("m4_infiltrator",new float[]{-0.0313F,0.26F,0.5F,-0.0334F,0.22F,-0.72F});
        m.put("goldenrevolver",new float[]{-0.2633F,0.4133F,0.0625F});
        m.put("pulserifle",new float[]{0.14F,0.48F,0.0F,0.68F,0.52F,0.0F});
        m.put("lasergun",new float[]{-0.4318F,0.4F,0.0625F,0.6955F,0.2182F,0.0625F});
        m.put("alienblaster",new float[]{-0.0312F,0.2733F,0.6267F});
        m.put("netherblaster",new float[]{0.0F,0.3F,0.5875F,-0.0437F,0.32F,-0.2125F});
        m.put("blasterrifle",new float[]{-0.0312F,0.2333F,0.7111F,-0.0344F,0.0556F,-0.5111F});
        m.put("powerhammer",new float[]{-0.31F,0.408F,0.0312F,0.202F,0.344F,0.0313F});
        m.put("aug",new float[]{-0.0312F,0.22F,0.36F,-0.0376F,0.22F,-0.82F});
        m.put("sonicshotgun",new float[]{-0.4F,0.46F,0.0625F,1.04F,0.36F,0.0625F});
        m.put("chainsaw",new float[]{-0.4842F,-0.0105F,0.0547F,0.2526F,-0.7684F,-0.0263F});
        m.put("scatterbeamrifle",new float[]{-0.0312F,0.24F,0.64F,-0.0312F,0.1F,-0.86F});
        m.put("nucleardeathray",new float[]{-0.4833F,0.4F,0.6499F,0.4667F,0.3167F,0.0061F});
        m.put("mac10",new float[]{-0.0312F,0.4167F,0.5833F,-0.0313F,0.25F,0.1167F});
        m.put("mibgun",new float[]{0.0F,0.6267F,0.3033F});
        m.put("scar",new float[]{-0.0312F,0.3F,0.64F,-0.0312F,0.14F,-0.76F});
        m.put("vector",new float[]{-0.0313F,0.2245F,0.6F,-0.0355F,0.5155F,-0.6727F});
        m.put("guidedmissilelauncher",new float[]{0.94F,0.48F,-0.0313F,2.0F,0.7F,-0.0312F});
        m.put("miningdrill",new float[]{-0.22F,0.33F,0.0687F,0.23F,0.35F,0.0313F});
        m.put("tfg",new float[]{-0.3363F,0.417F,0.0625F,1.1822F,0.9911F,0.0626F});
        m.put("laserpistol",new float[]{-0.0312F,0.3833F,0.6333F});
        m.put("shishkebap",new float[]{0.0F,0.2F,0.04F});
        m.put("grenadelauncher",new float[]{-6.88F,-2.24F,0.125F,5.44F,-2.72F,-0.1F});
        m.put("gaussrifle",new float[]{0.4222F,-0.3333F,0.0625F,-1.5556F,-0.4F,0.0625F});
        return m;
    }

    static float[] gripsOf(GenericGun gun) {
        return CACHE.computeIfAbsent(gun, g -> {
            var key = ForgeRegistries.ITEMS.getKey(g);
            return key == null ? null : GRIPS.get(key.getPath());
        });
    }

    /** Called by the gun renderer with the pose its model was just drawn with. */
    static void render(GenericGun gun, boolean leftHandContext, PoseStack pose, MultiBufferSource buffers, int light, float fire, float reload) {
        if (!TGConfig.cl_firstPersonArms) return;
        var game = Minecraft.getInstance();
        AbstractClientPlayer player = game.player;
        if (player == null || player.isInvisible() || !game.options.getCameraType().isFirstPerson()) return;
        float[] grip = gripsOf(gun);
        if (grip == null) return;
        HumanoidArm main = player.getMainArm();
        // the gun in the off hand (dual wielding) gets no arms of its own drawn twice
        boolean mainHandGun = leftHandContext == (main == HumanoidArm.LEFT);
        if (!mainHandGun) return;
        float o = main == HumanoidArm.RIGHT ? 1 : -1;
        boolean slim = "slim".equals(player.getModelName());
        Matrix4f model = new Matrix4f(pose.last().pose());
        var skin = player.getSkinTextureLocation();
        arm(buffers, skin, light, slim, main == HumanoidArm.RIGHT, model.transformPosition(new Vector3f(grip[0], grip[1], grip[2])),
                mirror(BACK_SHOULDER, o), inside(BACK_INSIDE, o),
                player.isModelPartShown(main == HumanoidArm.RIGHT ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE));
        boolean twoHanded = gun.getGunHandType() != GunHandType.ONE_HANDED && grip.length >= 6 && player.getOffhandItem().isEmpty();
        if (twoHanded) {
            float foreX = grip[3];
            // the pump of the combat shotgun slides back after each shot and at the end of a reload; the hand rides on it
            if (gun == techguns.TGuns.combatshotgun.get()) foreX -= 0.8F * pumpProgress(fire, reload);
            Vector3f fore = model.transformPosition(new Vector3f(foreX, grip[4], grip[5]));
            if (reload > 0) fore = reloadHand(model, grip, fore, o, reload);
            arm(buffers, skin, light, slim, main != HumanoidArm.RIGHT, fore,
                    mirror(FRONT_SHOULDER, o), inside(FRONT_INSIDE, o),
                    player.isModelPartShown(main == HumanoidArm.RIGHT ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE));
        }
    }


    /** Same pump travel as the combat shotgun model. */
    private static float pumpProgress(float fire, float reload) {
        float pump = 0;
        if (fire >= 0.65F && fire <= 0.95F) pump = (fire - 0.65F) * 3.333333F;
        else if (reload >= 0.9F) pump = (reload - 0.9F) * 10F;
        return pump > 0.5F ? 1 - pump : pump;
    }

    /**
     * While reloading the front hand lets go of the gun: it goes to the magazine, takes it away below the screen,
     * brings the new one back, pushes it in and returns to the handguard. The shooting hand stays on the grip.
     */
    private static Vector3f reloadHand(Matrix4f model, float[] grip, Vector3f fore, float o, float t) {
        Vector3f mag = model.transformPosition(new Vector3f(
                grip[0] + (grip[3] - grip[0]) * 0.45F, grip[1] + (grip[4] - grip[1]) * 0.45F, grip[2] + (grip[5] - grip[2]) * 0.45F));
        mag.add(0, -0.12F, 0);
        Vector3f away = new Vector3f(-0.3F * o, -0.85F, -0.5F);
        if (t < 0.15F) return lerp(fore, mag, ease(t / 0.15F));
        if (t < 0.4F) return lerp(mag, away, ease((t - 0.15F) / 0.25F));
        if (t < 0.6F) return lerp(away, mag, ease((t - 0.4F) / 0.2F));
        if (t < 0.82F) return mag.add(0, 0.03F * (float) Math.sin((t - 0.6F) / 0.22F * Math.PI), 0);
        return lerp(mag, fore, ease((t - 0.82F) / 0.18F));
    }
    private static float ease(float x) { x = Math.max(0, Math.min(1, x)); return x * x * (3 - 2 * x); }
    private static Vector3f lerp(Vector3f a, Vector3f b, float t) { return new Vector3f(a).lerp(b, t); }

    private static Vector3f mirror(Vector3f v, float o) { return new Vector3f(v.x * o, v.y, v.z); }
    /** The arm's +x is its inside for the right arm and its outside for the left one, so mirroring flips y and z. */
    private static Vector3f inside(Vector3f v, float o) { return o > 0 ? new Vector3f(v) : new Vector3f(v.x, -v.y, -v.z); }

    private static void arm(MultiBufferSource buffers, net.minecraft.resources.ResourceLocation skin, int light, boolean slim, boolean right,
                            Vector3f hand, Vector3f shoulder, Vector3f insideHint, boolean sleeve) {
        Vector3f down = new Vector3f(hand).sub(shoulder);
        if (down.lengthSquared() < 1e-6F) return;
        down.normalize();
        Vector3f ex = new Vector3f(insideHint).sub(new Vector3f(down).mul(insideHint.dot(down)));
        if (ex.lengthSquared() < 1e-6F) return;
        ex.normalize();
        Vector3f ez = new Vector3f(ex).cross(down);
        Matrix4f m = new Matrix4f().translation(hand)
                .mul(new Matrix4f(ex.x, ex.y, ex.z, 0, down.x, down.y, down.z, 0, ez.x, ez.y, ez.z, 0, 0, 0, 0, 1));
        float width = slim ? 3 : 4;
        float x0 = right ? (slim ? -2 : -3) : -1;
        float centre = x0 + width / 2;
        m.scale(ARM_SCALE).translate(-centre * PX, -8 * PX, 0);
        Matrix3f normal = new Matrix3f(ex.x, ex.y, ex.z, down.x, down.y, down.z, ez.x, ez.y, ez.z);
        int u = right ? 40 : 32, v = right ? 16 : 48;
        draw(buffers.getBuffer(RenderType.entityCutoutNoCull(skin)), m, normal, light, x0, width, u, v, 0F);
        if (sleeve) draw(buffers.getBuffer(RenderType.entityTranslucent(skin)), m, normal, light, x0, width, right ? 40 : 48, right ? 32 : 48, 0.25F);
    }

    /** Forearm with the hand (lower two thirds of the arm texture) and a long upper arm towards the shoulder. */
    private static void draw(VertexConsumer out, Matrix4f m, Matrix3f n, int light, float x0, float w, int u, int v, float grow) {
        box(out, m, n, light, x0 - grow, 2, -2 - grow, x0 + w + grow, 10 + grow, 2 + grow, u, v, w, 4, 12, true);
        box(out, m, n, light, x0 - grow, 2 - UPPER_ARM, -2 - grow, x0 + w + grow, 2, 2 + grow, u, v, w, 0, 4, false);
    }

    /** Minecraft cube texture layout; vs0..vs1 picks which rows of the arm's side texture span this box. */
    private static void box(VertexConsumer out, Matrix4f m, Matrix3f n, int light, float x0, float y0, float z0, float x1, float y1, float z1,
                            int u, int v, float w, float vs0, float vs1, boolean hand) {
        float d = 4;
        float f0 = u, f1 = u + d, f2 = u + d + w, f3 = u + d + w + w, f4 = u + d + w + d, f5 = u + d + w + d + w;
        float s0 = v + d + vs0, s1 = v + d + vs1;
        quad(out, m, n, light, 1, 0, 0, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, f2, s0, f4, s1);
        quad(out, m, n, light, -1, 0, 0, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1, f0, s0, f1, s1);
        quad(out, m, n, light, 0, 0, -1, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, f1, s0, f2, s1);
        quad(out, m, n, light, 0, 0, 1, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1, f4, s0, f5, s1);
        if (hand) quad(out, m, n, light, 0, 1, 0, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, f2, v, f3, v + d);
    }

    private static void quad(VertexConsumer out, Matrix4f m, Matrix3f n, int light, float nx, float ny, float nz,
                             float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz,
                             float u0, float v0, float u1, float v1) {
        vertex(out, m, n, light, nx, ny, nz, ax, ay, az, u0, v0);
        vertex(out, m, n, light, nx, ny, nz, bx, by, bz, u1, v0);
        vertex(out, m, n, light, nx, ny, nz, cx, cy, cz, u1, v1);
        vertex(out, m, n, light, nx, ny, nz, dx, dy, dz, u0, v1);
    }

    private static void vertex(VertexConsumer out, Matrix4f m, Matrix3f n, int light, float nx, float ny, float nz, float x, float y, float z, float u, float v) {
        out.vertex(m, x * PX, y * PX, z * PX).color(255, 255, 255, 255).uv(u / 64F, v / 64F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n, nx, ny, nz).endVertex();
    }

    private FirstPersonGunArms() {}
}
