package techguns.client.render.tileentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import techguns.client.models.machines.*;
import techguns.power.*;

/** Draws the power blocks with their box models; turning parts keep their angle per block between frames. */
public final class PowerRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private record Look(PowerModel model,ResourceLocation texture){}
    private static Map<Block,Look> looks;
    private static final Map<BlockEntity,float[]> angles=new WeakHashMap<>();

    private static ResourceLocation tex(String id){return ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/"+id+".png");}
    private static Map<Block,Look> looks(){
        if(looks==null){
            looks=new java.util.HashMap<>();
            looks.put(PowerBlocks.FUEL_GENERATOR.get(),new Look(new ModelFuelGenerator(),tex("fuel_generator")));
            looks.put(PowerBlocks.LIQUID_GENERATOR.get(),new Look(new ModelLiquidGenerator(),tex("liquid_generator")));
            looks.put(PowerBlocks.GEOTHERMAL_GENERATOR.get(),new Look(new ModelGeothermalGenerator(),tex("geothermal_generator")));
            looks.put(PowerBlocks.NUCLEAR_GENERATOR.get(),new Look(new ModelNuclearGenerator(),tex("nuclear_generator")));
            looks.put(PowerBlocks.SOLAR_PANEL.get(),new Look(new ModelSolarPanel(),tex("solar_panel")));
            looks.put(PowerBlocks.SOLAR_PANEL_ADVANCED.get(),new Look(new ModelSolarPanelAdvanced(),tex("solar_panel_advanced")));
            looks.put(PowerBlocks.SOLAR_PANEL_INDUSTRIAL.get(),new Look(new ModelSolarPanelIndustrial(),tex("solar_panel_industrial")));
            looks.put(PowerBlocks.ENERGY_CELL.get(),new Look(new ModelEnergyCell(),tex("energy_cell")));
            looks.put(PowerBlocks.ENERGY_BANK.get(),new Look(new ModelEnergyBank(),tex("energy_bank")));
            looks.put(PowerBlocks.FLOODLIGHT.get(),new Look(new ModelFloodlight(),tex("floodlight")));
        }
        return looks;
    }
    private static Look look(BlockState state){
        var map=looks();
        return map.get(state.getBlock());
    }

    public PowerRenderer(BlockEntityRendererProvider.Context context){}

    /** Draws one block: front toward FACING, model space as in the original machine renderers. */
    public static void draw(BlockState state,PoseStack pose,MultiBufferSource buffers,int light,int overlay,boolean active,int charge,float spin,float level){
        var look=look(state);if(look==null)return;
        var model=look.model;model.active=active;model.charge=charge;model.spin=spin;model.level=level;
        pose.pushPose();
        pose.translate(0.5,0.5,0.5);
        Direction facing=state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)?state.getValue(BlockStateProperties.HORIZONTAL_FACING):state.hasProperty(BlockStateProperties.FACING)?state.getValue(BlockStateProperties.FACING):Direction.NORTH;
        if(facing==Direction.UP)pose.mulPose(Axis.XP.rotationDegrees(90));
        else if(facing==Direction.DOWN)pose.mulPose(Axis.XP.rotationDegrees(-90));
        else pose.mulPose(Axis.YP.rotationDegrees(180-facing.toYRot()));
        pose.translate(0,1,0);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        var texture=look.texture;
        model.setLayers((alpha,doubleSided)->buffers.getBuffer(alpha?RenderType.entityTranslucent(texture):RenderType.entityCutoutNoCull(texture)));
        model.renderModel(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,overlay,ItemDisplayContext.NONE,null,0,0,0,0,0);
        pose.popPose();
    }

    @Override public void render(T entity,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var state=entity.getBlockState();var level=entity.getLevel();if(level==null)return;
        boolean active=state.hasProperty(BlockStateProperties.LIT)&&state.getValue(BlockStateProperties.LIT);
        int charge=state.hasProperty(EnergyCellBlock.CHARGE)?state.getValue(EnergyCellBlock.CHARGE):0;
        float speed=active?0.25F:0;
        float time=level.getGameTime()+partial;
        float[] a=angles.computeIfAbsent(entity,e->new float[]{0,time});
        a[0]+=(time-a[1])*speed;a[1]=time;a[0]%=(float)(Math.PI*2000);
        float fill=entity instanceof LiquidGeneratorEntity liquid?liquid.fill():0;
        draw(state,pose,buffers,light,overlay,active,charge,a[0],fill);
    }
    @Override public int getViewDistance(){return 96;}
}
