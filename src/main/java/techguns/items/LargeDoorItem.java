package techguns.items;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import techguns.blocks.LargeDoorBlock;
public final class LargeDoorItem extends BlockItem {
    public LargeDoorItem(LargeDoorBlock block){super(block,new Item.Properties());}
    @Override public InteractionResult useOn(UseOnContext context){if(context.getClickedFace()!=Direction.UP)return InteractionResult.FAIL;
        var level=context.getLevel();var player=context.getPlayer();if(player==null)return InteractionResult.FAIL;
        var pos=context.getClickedPos();if(!level.getBlockState(pos).canBeReplaced())pos=pos.above();
        if(!((LargeDoorBlock)getBlock()).place(level,pos,player.getDirection().getAxis()==Direction.Axis.X,player))return InteractionResult.FAIL;
        if(!level.isClientSide){level.playSound(null,pos,net.minecraft.sounds.SoundEvents.METAL_PLACE,net.minecraft.sounds.SoundSource.BLOCKS,1,.8f);if(!player.isCreative())context.getItemInHand().shrink(1);}
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
