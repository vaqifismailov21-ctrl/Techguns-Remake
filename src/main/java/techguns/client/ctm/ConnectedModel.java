package techguns.client.ctm;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

/** Block model whose connected-texture quads are rebuilt from the neighbours at chunk build time. */
public final class ConnectedModel extends BakedModelWrapper<BakedModel> {
    private static final ModelProperty<BlockAndTintGetter> LEVEL=new ModelProperty<>();
    private static final ModelProperty<BlockPos> POS=new ModelProperty<>();
    public ConnectedModel(BakedModel original){super(original);}
    @Override public ModelData getModelData(BlockAndTintGetter level,BlockPos pos,BlockState state,ModelData data){
        return super.getModelData(level,pos,state,data).derive().with(LEVEL,level).with(POS,pos.immutable()).build();
    }
    @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random,ModelData data,RenderType type){
        var quads=super.getQuads(state,side,random,data,type);
        var level=data.get(LEVEL);var pos=data.get(POS);
        if(level==null||pos==null||state==null)return quads;
        List<BakedQuad> out=null;
        for(int i=0;i<quads.size();i++){
            var quad=quads.get(i);
            if(!ConnectedTextures.isConnected(quad.getSprite())){if(out!=null)out.add(quad);continue;}
            if(out==null)out=new ArrayList<>(quads.subList(0,i));
            ConnectedTextures.split(quad,level,pos,out);
        }
        return out==null?quads:out;
    }
}
