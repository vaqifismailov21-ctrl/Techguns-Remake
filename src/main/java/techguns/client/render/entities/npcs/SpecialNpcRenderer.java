package techguns.client.render.entities.npcs;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import techguns.client.models.npcs.*;
import techguns.entities.npcs.Ghastling;
public final class SpecialNpcRenderer<T extends Mob> extends MobRenderer<T,SpecialNpcModel<T>> {
    private final boolean bug;
    public SpecialNpcRenderer(EntityRendererProvider.Context context,boolean bug){super(context,new SpecialNpcModel<>(bug?new ModelAlienBug():new ModelGhastling()),bug?.8f:.25f);this.bug=bug;}
    @Override public ResourceLocation getTextureLocation(T entity){return bug?ResourceLocation.parse("techguns:textures/entity/alienbug.png"):ResourceLocation.parse("minecraft:textures/entity/ghast/"+(entity instanceof Ghastling g&&g.isAttacking()?"ghast_shooting":"ghast")+".png");}
}
