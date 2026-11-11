package techguns.power.wire;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

/**
 * Coil of wire. Click the first point (a connector, a transformer, a pylon arm), then the second one: a wire hangs
 * between them. One coil makes one wire up to its kind's length. Sneak-click into the air to let go of the first end.
 */
public final class WireCoilItem extends Item {
    public final WireTier tier;
    /** Insulated wire (copper and steel only) never shocks. */
    public final boolean insulated;
    public WireCoilItem(WireTier tier){this(tier,false);}
    public WireCoilItem(WireTier tier,boolean insulated){super(new Properties().stacksTo(64));this.tier=tier;this.insulated=insulated;}

    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag){
        lines.add(Component.translatable("techguns.wire.tip.rate",tier.rate).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("techguns.wire.tip.length",tier.maxLength).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("techguns.wire.tip.loss",String.format("%.2f",tier.lossPerBlock*100*16)).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(insulated?"techguns.wire.tip.insulated":"techguns.wire.tip.bare").withStyle(insulated?ChatFormatting.GREEN:ChatFormatting.RED));
        var link=stack.getTag()==null?null:stack.getTag().getCompound("Link");
        if(link!=null&&link.contains("Pos")){var p=BlockPos.of(link.getLong("Pos"));lines.add(Component.translatable("techguns.wire.tip.linked",p.getX(),p.getY(),p.getZ()).withStyle(ChatFormatting.AQUA));}
    }
    @Override public boolean isFoil(ItemStack stack){return stack.getTag()!=null&&stack.getTag().contains("Link");}

    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);
        if(player.isShiftKeyDown()&&stack.getTag()!=null&&stack.getTag().contains("Link")){
            stack.removeTagKey("Link");if(!level.isClientSide)player.displayClientMessage(Component.translatable("techguns.wire.unlinked"),true);
            return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override public InteractionResult useOn(UseOnContext ctx){
        var level=ctx.getLevel();var pos=ctx.getClickedPos();var player=ctx.getPlayer();var stack=ctx.getItemInHand();
        var main=WireBlocks.hostAt(level,pos);
        if(main==null)return InteractionResult.PASS;
        if(level.isClientSide)return InteractionResult.SUCCESS;
        var server=(ServerLevel)level;var state=level.getBlockState(main);var block=(WireHost)state.getBlock();
        // the point nearest to where the player clicked that takes this kind of wire
        int best=-1;double dist=Double.MAX_VALUE;
        for(int i=0;i<block.points(state);i++){
            if(!block.accepts(state,i,tier))continue;
            double d=block.point(state,main,i).distanceToSqr(ctx.getClickLocation());if(d<dist){dist=d;best=i;}
        }
        if(best<0){message(player,"techguns.wire.wrong_kind");return InteractionResult.FAIL;}
        var node=new WireNode(main,best);
        var tag=stack.getOrCreateTag();
        if(!tag.contains("Link")){
            var link=new CompoundTag();link.putLong("Pos",main.asLong());link.putInt("Point",best);link.putString("Dim",level.dimension().location().toString());
            tag.put("Link",link);message(player,"techguns.wire.first");return InteractionResult.SUCCESS;
        }
        var link=tag.getCompound("Link");
        if(!link.getString("Dim").equals(level.dimension().location().toString())){stack.removeTagKey("Link");message(player,"techguns.wire.other_world");return InteractionResult.FAIL;}
        var first=new WireNode(BlockPos.of(link.getLong("Pos")),link.getInt("Point"));
        var firstState=level.getBlockState(first.pos());
        if(!(firstState.getBlock() instanceof WireHost firstHost)||first.point()>=firstHost.points(firstState)){stack.removeTagKey("Link");message(player,"techguns.wire.first_gone");return InteractionResult.FAIL;}
        if(first.equals(node)||first.pos().equals(node.pos())){message(player,"techguns.wire.same");return InteractionResult.FAIL;}
        Vec3 a=firstHost.point(firstState,first.pos(),first.point()),b=block.point(state,main,best);
        double length=a.distanceTo(b);
        if(length>tier.maxLength){message(player,"techguns.wire.too_long",tier.maxLength);return InteractionResult.FAIL;}
        var net=WireNetwork.get(server);var wire=new WireConnection(first,node,tier,(float)length,insulated);
        if(net.exists(wire)){message(player,"techguns.wire.exists");return InteractionResult.FAIL;}
        var blocked=blocked(level,a,b,first.pos(),main);
        if(blocked!=null){message(player,"techguns.wire.blocked",blocked.getX(),blocked.getY(),blocked.getZ());return InteractionResult.FAIL;}
        net.add(server,wire);
        stack.removeTagKey("Link");
        if(player==null||!player.getAbilities().instabuild)stack.shrink(1);
        level.playSound(null,pos,SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,0.8F,1.2F);
        message(player,"techguns.wire.done",String.format("%.1f",length));
        return InteractionResult.SUCCESS;
    }
    /** First block standing in the straight line between the ends (the hosts themselves aside), or null. */
    private static BlockPos blocked(Level level,Vec3 a,Vec3 b,BlockPos hostA,BlockPos hostB){
        double len=a.distanceTo(b);int steps=(int)Math.ceil(len/0.2);
        for(int i=1;i<steps;i++){
            var p=a.lerp(b,i/(double)steps);var pos=BlockPos.containing(p);
            if(pos.equals(hostA)||pos.equals(hostB))continue;
            var state=level.getBlockState(pos);
            if(state.isAir()||WireBlocks.isWireBlock(state))continue;
            var shape=state.getCollisionShape(level,pos);if(shape.isEmpty())continue;
            for(var box:shape.toAabbs())if(box.move(pos).inflate(0.02).contains(p))return pos;
        }
        return null;
    }
    private static void message(Player player,String key,Object... args){if(player!=null)player.displayClientMessage(Component.translatable(key,args),true);}

    /** Drops one coil of each removed wire at the block it was taken off. */
    public static void dropWires(ServerLevel level,BlockPos pos,List<WireConnection> wires){
        for(var w:wires)Block.popResource(level,pos,new ItemStack(WireBlocks.coil(w.tier(),w.insulated())));
    }
}
