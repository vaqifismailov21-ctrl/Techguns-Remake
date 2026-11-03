package techguns.client.models.machines;
import techguns.client.models.ModelMultipart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
/** Original machine animation entry point over the multipart pose adapter. */
public abstract class ModelMachine extends ModelMultipart {
    @Override public final void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale,int ammo,float reload,ItemDisplayContext context,int part,float fire,float charge){render(entity,limb,amount,age,yaw,pitch,scale,reload);}
    public abstract void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale,float progress);
}
