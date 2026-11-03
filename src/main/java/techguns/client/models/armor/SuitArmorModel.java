package techguns.client.models.armor;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.models.LegacyHumanoidGeometry;
/** Forge armor-model hook using original suit geometry and the current player animation pose. */
public final class SuitArmorModel extends HumanoidModel<LivingEntity> {
    private final LegacyHumanoidGeometry geometry;
    private EquipmentSlot slot;
    public SuitArmorModel(String name){
        super(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
        geometry=switch(name){
            case "steam_armor_0"->new ModelSteamArmor(0);case "steam_armor_1"->new ModelSteamArmor(1);case "steam_armor_2"->new ModelSteamArmor(0,0.01F);
            case "power_armor_0"->new ModelT3PowerArmor(0);case "power_armor_1"->new ModelT3PowerArmor(1);case "power_armor_2"->new ModelT3PowerArmor(0,0.01F);
            case "power_armor_mk2_0"->new ModelT4PowerArmorMk2(0);case "power_armor_mk2_1"->new ModelT4PowerArmorMk2(1);case "power_armor_mk2_2"->new ModelT4PowerArmorMk2(0,0.01F);
            case "exo_suit_0"->new ModelExoSuit(0,1);case "exo_suit_1"->new ModelExoSuit(1,0.5F);case "exo_suit_2"->new ModelExoSuit(0,0.75F);
            case "beret_0"->new ModelBeret();
            case "coat_0"->new ModelArmorCoat(0,1);case "coat_1"->new ModelArmorCoat(1,0.51F);case "coat_2"->new ModelArmorCoat(2,0.49F);case "coat_3"->new ModelArmorCoat(3,0.75F);
            default->throw new IllegalArgumentException("Original armor model not mapped: "+name);
        };
    }
    public void select(EquipmentSlot slot){this.slot=slot;}
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer vertices,int light,int overlay,int tgColor){float red=net.minecraft.util.FastColor.ARGB32.red(tgColor)/255F,green=net.minecraft.util.FastColor.ARGB32.green(tgColor)/255F,blue=net.minecraft.util.FastColor.ARGB32.blue(tgColor)/255F,alpha=net.minecraft.util.FastColor.ARGB32.alpha(tgColor)/255F;
        geometry.sync(this,slot);geometry.renderModel(pose,vertices,light,overlay,ItemDisplayContext.NONE);
    }
}
