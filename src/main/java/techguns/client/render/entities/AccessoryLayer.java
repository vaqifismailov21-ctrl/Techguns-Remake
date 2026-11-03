package techguns.client.render.entities;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import techguns.TGItems;
import techguns.capabilities.TGExtendedPlayer;
import techguns.items.armors.*;
import techguns.client.models.armor.*;

public final class AccessoryLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    private record Entry(ModelAdditionalSlotBase model,String texture){}
    private final Map<AccessoryItem.Kind,Entry> entries=new EnumMap<>(AccessoryItem.Kind.class);
    private final Entry oxygen=new Entry(new ModelFaceMask(true),"armors/oxygenmask"),gloves;
    public AccessoryLayer(PlayerRenderer parent,boolean slim){
        super(parent);gloves=new Entry(new ModelGloves(0.45F,slim),"models/armor/working_gloves"+(slim?"_slim":""));
        entries.put(AccessoryItem.Kind.GAS_MASK,new Entry(new ModelGasMask(),"armors/gasmask"));entries.put(AccessoryItem.Kind.GLIDER,new Entry(new ModelGlider(),"armors/glider"));
        entries.put(AccessoryItem.Kind.JUMPPACK,new Entry(new ModelJetPack(1),"armors/jetpack"));entries.put(AccessoryItem.Kind.JETPACK,new Entry(new ModelJetPack(0),"armors/jetpack"));
        entries.put(AccessoryItem.Kind.SCUBA,new Entry(new ModelOxygenTanks(),"armors/oxygentanks"));entries.put(AccessoryItem.Kind.NIGHTVISION,new Entry(new ModelNightVisionGoggles(),"armors/nightvisiongoggles"));
        entries.put(AccessoryItem.Kind.ANTIGRAV,new Entry(new ModelAntiGravPack(),"armors/antigravpack"));entries.put(AccessoryItem.Kind.TACTICAL_MASK,new Entry(new ModelFaceMask(true),"armors/tacticalmask"));
    }
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer player,float limb,float amount,float partial,float age,float yaw,float pitch){
        var data=TGExtendedPlayer.find(player).orElse(null);if(data==null||player.isInvisible())return;
        for(int slot=0;slot<3;slot++){
            var worn=player.getItemBySlot(slot==0?EquipmentSlot.HEAD:EquipmentSlot.CHEST);
            if(worn.getItem() instanceof TGSuitArmor armor&&(slot==0?armor.hideFace:slot==1?armor.hideBack:armor.hideGloves))continue;
            var stack=data.tg_inventory.getItem(slot);Entry entry=null;int camo=0;
            if(stack.getItem() instanceof AccessoryItem accessory){entry=entries.get(accessory.kind);camo=Math.floorMod(accessory.getCurrentCamoIndex(stack),accessory.getCamoCount());}
            else if(stack.is(TGItems.OXYGEN_MASK.get()))entry=oxygen;else if(stack.is(TGItems.WORKING_GLOVES.get()))entry=gloves;
            if(entry==null)continue;
            var texture=ResourceLocation.fromNamespaceAndPath("techguns","textures/"+entry.texture+(camo==0?"":"_"+camo)+".png");
            entry.model.sync(getParentModel(),slot==0?EquipmentSlot.HEAD:EquipmentSlot.CHEST);
            entry.model.renderModel(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,OverlayTexture.NO_OVERLAY,ItemDisplayContext.NONE,player,0,0,0,0,0);
        }
    }
}
