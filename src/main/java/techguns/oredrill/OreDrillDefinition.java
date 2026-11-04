package techguns.oredrill;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import techguns.TGOreDrill;
import techguns.TGPackets;
import techguns.TGProjectilePackets;
import techguns.blocks.OreClusterBlock;

/**
 * Port of the original OreDrillDefinition. A controller points along one axis at a line of engines and rods ending
 * at an ore cluster; engines form a (2r+1)^2 block, surrounded by a frame/scaffold box of radius r+1, with air around
 * the rods. Validation, error pings/messages and slave linking follow the original order.
 */
public final class OreDrillDefinition {
    static final Direction[][] DIRECTIONS_AROUND_AXIS={
        {Direction.DOWN,Direction.NORTH,Direction.UP,Direction.SOUTH},
        {Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST},
        {Direction.DOWN,Direction.EAST,Direction.UP,Direction.WEST}};
    public static final int MAX_RADIUS=3, MAX_SIZE=16;
    public static final int MSG_ENGINE=1, MSG_FRAME=2, MSG_SCAFFOLD=3, MSG_AIR=4, MSG_CONTROLLER_ALONE=7, MSG_ROD_SIZE=9;
    private OreDrillDefinition(){}

    public static boolean checkForm(Level w,Player player,BlockPos masterPos){
        int dir=getDirection(w,player,masterPos);
        if(dir<0){sendErrorMSG(player,MSG_CONTROLLER_ALONE);return false;}
        Direction drill=Direction.from3DDataValue(dir);
        int[] length=checkLength(w,masterPos,drill);
        int engines=length[0],rods=length[1];
        if(engines>=0&&rods>0&&rods+engines<=MAX_SIZE){
            if(engines==0&&rods==1)return true;
            int rad=checkWidth(w,masterPos,drill);
            if(checkDrillSize(rods,engines,rad)){
                if(allBlocksMatch(w,player,getEngineBlocks(masterPos,drill,rad,engines),state(OreDrillType.ENGINE))){
                    if(allBlocksMatch(w,player,getFrameBlocks(masterPos,drill,rad+1,engines+rods,true,true),state(OreDrillType.FRAME))){
                        if(allBlocksMatch(w,player,getFrameBlocks(masterPos,drill,rad+1,engines+rods,false,true),state(OreDrillType.SCAFFOLD))){
                            if(allBlocksMatch(w,player,getAirBlocks(masterPos.relative(drill,engines),drill,rad,rods-1),Blocks.AIR.defaultBlockState()))return true;
                            sendErrorMSG(player,MSG_AIR);
                        }else sendErrorMSG(player,MSG_SCAFFOLD);
                    }else sendErrorMSG(player,MSG_FRAME);
                }else sendErrorMSG(player,MSG_ENGINE);
            }else sendErrorMSG(player,MSG_ROD_SIZE);
            return false;
        }
        if(engines<0)sendErrorPing(player,masterPos.relative(drill,-engines+1));
        else if(rods<0)sendErrorPing(player,masterPos.relative(drill,(-rods)+Math.abs(engines)+1));
        return false;
    }

    public static boolean checkDrillSize(int rods,int engines,int radius){return rods>=(radius*2+1)&&rods>=engines&&engines>radius;}

    public static Direction getSide1(Direction facing){return switch(facing.getAxis()){case X->Direction.DOWN;case Y->Direction.NORTH;case Z->Direction.DOWN;};}
    public static Direction getSide2(Direction facing){return switch(facing.getAxis()){case X->Direction.NORTH;case Y->Direction.EAST;case Z->Direction.EAST;};}
    private static BlockPos at(BlockPos start,Direction dir,int h,int r1,int r2){return start.relative(dir,h).relative(getSide1(dir),r1).relative(getSide2(dir),r2);}

    public static List<BlockPos> getEngineBlocks(BlockPos masterPos,Direction dir,int radius,int len){
        var positions=new ArrayList<BlockPos>();
        for(int r1=-radius;r1<=radius;r1++)for(int r2=-radius;r2<=radius;r2++)for(int h=0;h<len;h++)positions.add(at(masterPos,dir,h+1,r1,r2));
        return positions;
    }
    public static List<BlockPos> getAirBlocks(BlockPos startPos,Direction dir,int radius,int len){
        var positions=new ArrayList<BlockPos>();
        for(int r1=-radius;r1<=radius;r1++)for(int r2=-radius;r2<=radius;r2++)for(int h=0;h<len;h++)if(!(r1==0&&r2==0))positions.add(at(startPos,dir,h+1,r1,r2));
        return positions;
    }
    public static List<BlockPos> getFrameBlocks(BlockPos masterPos,Direction dir,int radius,int len,boolean outer,boolean lastRow){
        var positions=new ArrayList<BlockPos>();
        for(int r1=-radius;r1<=radius;r1++)for(int r2=-radius;r2<=radius;r2++)for(int h=0;h<len;h++){
            if(outer&&isFramePos(r1,r2,h,radius,len))positions.add(at(masterPos,dir,h+1,r1,r2));
            else if(!outer&&isScaffoldPos(r1,r2,h,radius,len)&&(!lastRow||h!=len-1))positions.add(at(masterPos,dir,h+1,r1,r2));
        }
        return positions;
    }
    public static List<BlockPos> getFinalScaffoldRow(BlockPos masterPos,Direction dir,int radius,int len){
        var positions=new ArrayList<BlockPos>();
        for(int r1=-radius;r1<=radius;r1++)for(int r2=-radius;r2<=radius;r2++)if(!(r1==0&&r2==0))positions.add(at(masterPos,dir,len,r1,r2));
        return positions;
    }
    static boolean isFramePos(int r1,int r2,int h,int radius,int len){
        int minc=0;if(Math.abs(r1)==radius)minc++;if(Math.abs(r2)==radius)minc++;if(h==0||h==len-1)minc++;return minc>=2;
    }
    static boolean isScaffoldPos(int r1,int r2,int h,int radius,int len){
        if(h==0)return false;
        int minc=0;if(Math.abs(r1)==radius)minc++;if(Math.abs(r2)==radius)minc++;
        if(h==len-1){minc++;return minc==1&&!(r1==0&&r2==0);}
        return minc==1;
    }

    /** Re-evaluates the structure and links all parts; the caller has validated it with checkForm. */
    public static boolean form(Level w,BlockPos masterPos){
        if(!(w.getBlockEntity(masterPos) instanceof OreDrillTileEntMaster master))return false;
        int dir=getDirection(w,null,masterPos);if(dir<0)return false;
        Direction drill=Direction.from3DDataValue(dir);
        int[] length=checkLength(w,masterPos,drill);int engines=length[0],rods=length[1];
        if(engines<0||rods<=0)return false;
        if(engines==0&&rods==1){master.formOreDrill(engines,rods,0,drill);linkSlave(w,masterPos.relative(drill,1),0,masterPos);return true;}
        int rad=checkWidth(w,masterPos,drill);
        master.formOreDrill(engines,rods,rad,drill);
        for(int i=engines;i<engines+rods+1;i++)linkSlave(w,masterPos.relative(drill,i),0,masterPos);
        getEngineBlocks(masterPos,drill,rad,engines).forEach(p->linkSlave(w,p,1,masterPos));
        getFrameBlocks(masterPos,drill,rad+1,engines+rods,true,true).forEach(p->linkSlave(w,p,2,masterPos));
        getFrameBlocks(masterPos,drill,rad+1,engines+rods,false,false).forEach(p->linkSlave(w,p,3,masterPos));
        getFinalScaffoldRow(masterPos,drill,rad,engines+rods).forEach(p->linkSlave(w,p,4,masterPos));
        // The original returned false here after a successful form; callers ignore the value.
        return false;
    }

    public static void unform(Level w,OreDrillTileEntMaster master){
        Direction drill=master.getDrill_direction();int engines=master.getEngines(),rods=master.getRods();BlockPos masterPos=master.getBlockPos();
        if(drill==null){master.unform();return;}
        if(engines==0&&rods==1){unlinkSlave(w,masterPos.relative(drill,1));master.unform();return;}
        int rad=master.getRadius();
        for(int i=engines;i<engines+rods+1;i++)unlinkSlave(w,masterPos.relative(drill,i));
        getEngineBlocks(masterPos,drill,rad,engines).forEach(p->unlinkSlave(w,p));
        getFrameBlocks(masterPos,drill,rad+1,engines+rods,true,true).forEach(p->unlinkSlave(w,p));
        getFrameBlocks(masterPos,drill,rad+1,engines+rods,false,false).forEach(p->unlinkSlave(w,p));
        getFinalScaffoldRow(masterPos,drill,rad,engines+rods).forEach(p->unlinkSlave(w,p));
        master.unform();
    }

    private static void linkSlave(Level w,BlockPos pos,int type,BlockPos masterPos){
        if(!(w.getBlockEntity(pos) instanceof OreDrillSlaveEntity slave))return;
        if(type==4){
            // Original: the final interior row becomes SCAFFOLD_HIDDEN regardless of which drill part was there.
            var hidden=TGOreDrill.SCAFFOLD.get().defaultBlockState().setValue(OreDrillBlock.FORMED,true).setValue(OreDrillScaffoldBlock.HIDDEN,true);
            if(!(w.getBlockState(pos).getBlock() instanceof OreDrillScaffoldBlock)){slave.unform();w.setBlock(pos,hidden,3);if(!(w.getBlockEntity(pos) instanceof OreDrillSlaveEntity replaced))return;slave=replaced;}
            else w.setBlock(pos,w.getBlockState(pos).setValue(OreDrillBlock.FORMED,true).setValue(OreDrillScaffoldBlock.HIDDEN,true),3);
        }else w.setBlock(pos,w.getBlockState(pos).setValue(OreDrillBlock.FORMED,true),3);
        slave.form(masterPos,(byte)type);slave.needUpdate();
    }
    private static void unlinkSlave(Level w,BlockPos pos){
        if(!(w.getBlockEntity(pos) instanceof OreDrillSlaveEntity slave))return;
        slave.unform();
        BlockState bs=w.getBlockState(pos);
        if(bs.getBlock() instanceof OreDrillBlock){
            if(OreDrillScaffoldBlock.isHidden(bs))bs=bs.setValue(OreDrillScaffoldBlock.HIDDEN,false);
            w.setBlock(pos,bs.setValue(OreDrillBlock.FORMED,false),3);
            slave.needUpdate();
        }
    }

    static int getDirection(Level w,Player player,BlockPos start){
        int dir=-1;
        for(Direction f:Direction.values())if(checkBlock(w,start.relative(f))){
            if(dir==-1)dir=f.get3DDataValue();
            else{sendErrorPing(player,start.relative(f,1));return -1;}
        }
        return dir;
    }
    static int checkWidth(Level w,BlockPos p,Direction dir){
        BlockPos enginePos=p.relative(dir,1);int rad=0;
        for(Direction facing:DIRECTIONS_AROUND_AXIS[dir.getAxis().ordinal()]){int result=countEngines(w,enginePos,facing);if(result>rad)rad=result;}
        return rad;
    }
    static int countEngines(Level w,BlockPos p,Direction dir){int count=0;while(isDrillType(w,p.relative(dir,count+1),OreDrillType.ENGINE)&&count<MAX_RADIUS)count++;return count;}

    /** [engines, rods]; negative values mark the failing position as in the original. */
    static int[] checkLength(Level w,BlockPos p,Direction dir){
        int engines=0,rods=0;
        if(isDrillType(w,p.relative(dir,1),OreDrillType.ROD)){
            rods++;
            return isOreCluster(w,p.relative(dir,2))?new int[]{0,1}:new int[]{-1,-1};
        }else if(isDrillType(w,p.relative(dir,1),OreDrillType.ENGINE)){
            engines++;
            while(isDrillType(w,p.relative(dir,engines+1),OreDrillType.ENGINE)&&engines<MAX_SIZE)engines++;
            if(isDrillType(w,p.relative(dir,engines+rods+1),OreDrillType.ROD)){
                rods++;
                while(isDrillType(w,p.relative(dir,engines+rods+1),OreDrillType.ROD)&&rods<MAX_SIZE)rods++;
                return isOreCluster(w,p.relative(dir,engines+rods+1))?new int[]{engines,rods}:new int[]{engines,-rods};
            }
            return new int[]{-engines,-1};
        }
        return new int[]{-1,-1};
    }

    public static boolean isOreCluster(Level w,BlockPos p){return w.getBlockState(p).getBlock() instanceof OreClusterBlock;}
    static boolean isDrillType(Level w,BlockPos p,OreDrillType type){
        var bs=w.getBlockState(p);
        return bs.getBlock() instanceof OreDrillBlock block&&block.type==type&&!bs.getValue(OreDrillBlock.FORMED)&&!OreDrillScaffoldBlock.isHidden(bs);
    }
    static boolean checkBlock(Level w,BlockPos p){return isDrillType(w,p,OreDrillType.ROD)||isDrillType(w,p,OreDrillType.ENGINE);}
    static BlockState state(OreDrillType type){return TGOreDrill.BLOCKS.get(type).get().defaultBlockState();}

    static boolean allBlocksMatch(Level w,Player player,List<BlockPos> positions,BlockState state){
        boolean valid=true;
        for(var p:positions)if(w.getBlockState(p)!=state){sendErrorPing(player,p);valid=false;}
        return valid;
    }
    /** The original MultiblockInvalidPing particle system, sent only to the forming player. */
    public static void sendErrorPing(Player player,BlockPos p){
        if(player instanceof ServerPlayer server)TGPackets.NETWORK.send(PacketDistributor.PLAYER.with(()->server),new TGProjectilePackets.Effect("MultiblockInvalidPing",Vec3.atCenterOf(p),0,0));
    }
    static void sendErrorMSG(Player player,int type){
        if(!(player instanceof ServerPlayer))return;
        String key=switch(type){
            case MSG_ENGINE->"techguns.multiblock.oredrill.invalid.engines";
            case MSG_FRAME->"techguns.multiblock.oredrill.invalid.frame";
            case MSG_SCAFFOLD->"techguns.multiblock.oredrill.invalid.scaffold";
            case MSG_AIR->"techguns.multiblock.oredrill.invalid.air";
            case MSG_CONTROLLER_ALONE->"techguns.msg.error.oredrill.controlleralone";
            case MSG_ROD_SIZE->"techguns.msg.error.oredrill.rodsize";
            default->"techguns.msg.error.multiblockinvalid";
        };
        player.displayClientMessage(Component.translatable(key),true);
    }
}
