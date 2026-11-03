package techguns.client.models;

import net.minecraft.world.entity.Entity;
import net.minecraft.client.renderer.block.model.ItemTransforms;

/** Adapter for original unanimated ModelBase geometry. */
public abstract class LegacyModelBase extends ModelMultipart {
    public abstract void render(Entity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch, float scale);
    @Override public final void render(Entity entity, float swing, float amount, float age, float yaw, float pitch,
                                      float scale, int ammo, float reload, ItemTransforms.TransformType context, int part, float fire, float charge) {
        render(entity, swing, amount, age, yaw, pitch, scale);
    }
}
