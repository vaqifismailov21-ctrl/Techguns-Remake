package techguns.client.models.machines;
import techguns.client.models.ModelMultipart;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.renderer.block.model.ItemTransforms;
/** Original machine animation entry point over the multipart pose adapter. */
public abstract class ModelMachine extends ModelMultipart {
    @Override public final void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale,int ammo,float reload,ItemTransforms.TransformType context,int part,float fire,float charge){render(entity,limb,amount,age,yaw,pitch,scale,reload);}
    public abstract void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale,float progress);
}
