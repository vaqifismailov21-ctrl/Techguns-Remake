package techguns.power.plant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkHooks;
import techguns.power.EnergySource;

/**
 * Controller of a multiblock power plant. Click it to form the plant from the parts around it (missing or wrong
 * blocks are pinged); the formed plant hides its parts and is drawn whole. Energy leaves through every outer face
 * of the plant, so cables can be connected anywhere on it.
 */
public abstract class PlantController extends EnergySource {
    public final PlantKind kind;
    public boolean formed;
    /** FE per tick right now; problem: 0 working, otherwise a kind specific reason shown in the GUI. */
    public int rate,problem;
    private final List<BlockPos> outletPos=new ArrayList<>();private final List<Direction> outletSide=new ArrayList<>();
    private final int output;
    protected PlantController(BlockEntityType<?> type,PlantKind kind,BlockPos pos,BlockState state,int capacity,int output){super(type,pos,state,capacity,output);this.kind=kind;this.output=output;}

    private java.util.List<PlantModel.Box> model;private int modelKey=Integer.MIN_VALUE;private volatile Map<BlockPos,net.minecraft.world.phys.shapes.VoxelShape> shapes;
    /** Changes whenever the static model of the plant would change. */
    protected int modelKey(){return getBlockState().hasProperty(PlantControllerBlock.FACING)?facing().ordinal():0;}
    /** Static geometry of the formed plant (see PlantModel), rebuilt when it changes. */
    public java.util.List<PlantModel.Box> model(){
        int key=modelKey();
        if(model==null||key!=modelKey){model=PlantModel.build(this);modelKey=key;shapes=null;}
        return model;
    }
    /** Collision and outline of a block of the formed plant: the solid boxes of the model inside it, or a full block. */
    public net.minecraft.world.phys.shapes.VoxelShape shapeAt(BlockPos pos){
        if(!formed)return net.minecraft.world.phys.shapes.Shapes.block();
        var list=model();var map=shapes;
        if(map==null){map=PlantModel.shapes(worldPosition,facing(),list);shapes=map;}
        var shape=map.get(pos);return shape==null?net.minecraft.world.phys.shapes.Shapes.block():shape;
    }
    /** Shape of a block that belongs to a formed plant (controller or part); full block when the plant is not known here. */
    public static net.minecraft.world.phys.shapes.VoxelShape formedShape(net.minecraft.world.level.BlockGetter level,BlockPos pos){
        var be=level.getBlockEntity(pos);PlantController plant=null;
        if(be instanceof PlantController c)plant=c;
        else if(be instanceof PlantPartEntity part&&part.masterPos()!=null&&level.getBlockEntity(part.masterPos()) instanceof PlantController c)plant=c;
        return plant!=null&&plant.formed?plant.shapeAt(pos):net.minecraft.world.phys.shapes.Shapes.block();
    }

    public Direction facing(){return getBlockState().getValue(PlantControllerBlock.FACING);}
    /** World position: r blocks to the right, y up and b back from the controller. */
    public BlockPos at(int r,int y,int b){var f=facing();var right=f.getClockWise();var back=f.getOpposite();
        return worldPosition.offset(right.getStepX()*r+back.getStepX()*b,y,right.getStepZ()*r+back.getStepZ()*b);}

    /** Block expected at a position: a block, or null for air. */
    public record Need(Predicate<BlockState> test,Block shown){}
    protected static Need part(Block block){return new Need(s->s.is(block),block);}
    protected static Need either(Block a,Block b){return new Need(s->s.is(a)||s.is(b),a);}
    protected static final Need AIR=new Need(BlockState::isAir,null);
    /** Every position of the plant except the controller itself. */
    protected abstract Map<BlockPos,Need> structure();
    /** Kind specific work every tick while formed. */
    protected abstract void work();
    /** Client: particles while working. */
    public void effects(RandomSource random){}

    protected Map<BlockPos,Need> map(){return new LinkedHashMap<>();}
    public List<BlockPos> invalid(){
        var bad=new ArrayList<BlockPos>();if(level==null)return bad;
        for(var e:structure().entrySet()){if(!level.isLoaded(e.getKey())){bad.add(e.getKey());continue;}
            var state=level.getBlockState(e.getKey());
            if(!e.getValue().test().test(state))bad.add(e.getKey());
            else if(e.getValue().shown()!=null&&state.hasProperty(PlantPartBlock.FORMED)&&state.getValue(PlantPartBlock.FORMED)&&level.getBlockEntity(e.getKey()) instanceof PlantPartEntity p&&p.master()!=null&&p.master()!=this)bad.add(e.getKey());}
        return bad;
    }
    public boolean form(){
        if(formed||level==null||!invalid().isEmpty())return false;
        for(var e:structure().entrySet()){if(e.getValue().shown()==null)continue;var pos=e.getKey();var state=level.getBlockState(pos);
            level.setBlock(pos,state.setValue(PlantPartBlock.FORMED,true),3);if(level.getBlockEntity(pos) instanceof PlantPartEntity p)p.setMaster(worldPosition);}
        formed=true;level.setBlock(worldPosition,getBlockState().setValue(PlantControllerBlock.FORMED,true),3);onFormed();findOutlets();sync();setChanged();return true;
    }
    protected void onFormed(){}
    public void unform(){
        if(!formed||level==null)return;formed=false;rate=0;
        for(var e:structure().entrySet()){if(e.getValue().shown()==null)continue;var pos=e.getKey();var state=level.getBlockState(pos);
            if(state.hasProperty(PlantPartBlock.FORMED)&&state.getValue(PlantPartBlock.FORMED)){level.setBlock(pos,state.setValue(PlantPartBlock.FORMED,false),3);if(level.getBlockEntity(pos) instanceof PlantPartEntity p)p.setMaster(null);}}
        if(level.getBlockState(worldPosition).getBlock() instanceof PlantControllerBlock)level.setBlock(worldPosition,getBlockState().setValue(PlantControllerBlock.FORMED,false).setValue(PlantControllerBlock.LIT,false),3);
        outletPos.clear();outletSide.clear();onUnformed();sync();setChanged();
    }
    protected void onUnformed(){}
    public void dropContents(){}

    public InteractionResult interact(Player player,InteractionHand hand,BlockPos clicked,Direction side){
        if(level==null)return InteractionResult.PASS;
        // buckets and tanks pour into (or take from) the plant's tank, assembled or not when clicking the controller
        if(net.minecraftforge.fluids.FluidUtil.getFluidHandler(player.getItemInHand(hand)).isPresent()&&getCapability(ForgeCapabilities.FLUID_HANDLER,side).isPresent()){
            if(net.minecraftforge.fluids.FluidUtil.interactWithFluidHandler(player,hand,level,worldPosition,side))return InteractionResult.sidedSuccess(level.isClientSide);
            if(!level.isClientSide)player.displayClientMessage(Component.translatable("techguns.plant.fill_wrong"),true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if(!formed){
            if(level.isClientSide)return InteractionResult.SUCCESS;
            if(!form()){var bad=invalid();for(int i=0;i<Math.min(bad.size(),40);i++)techguns.oredrill.OreDrillDefinition.sendErrorPing(player,bad.get(i));
                var first=bad.isEmpty()?null:structure().get(bad.get(0));
                player.displayClientMessage(first!=null&&first.shown()!=null?Component.translatable("techguns.plant.missing",first.shown().getName(),bad.size()):Component.translatable("techguns.plant.blocked",bad.size()),true);}
            else player.displayClientMessage(Component.translatable("techguns.plant.formed",getBlockState().getBlock().getName()),true);
            return InteractionResult.CONSUME;
        }
        if(!level.isClientSide&&player instanceof ServerPlayer server)NetworkHooks.openScreen(server,new PlantMenu.Provider(this),buf->buf.writeBlockPos(worldPosition));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Server tick. */
    public void tick(){
        if(level==null)return;
        if(!formed){if(rate!=0){rate=0;sync();}return;}
        if(level.getGameTime()%40==Math.floorMod(worldPosition.asLong(),40L)){if(!invalid().isEmpty()){unform();return;}findOutlets();}
        int oldRate=rate,oldProblem=problem;
        work();
        if(oldRate!=rate||oldProblem!=problem)sync();
        var state=getBlockState();boolean lit=rate>0;if(state.getValue(PlantControllerBlock.LIT)!=lit)level.setBlock(worldPosition,state.setValue(PlantControllerBlock.LIT,lit),3);
        push();
    }
    /** Neighbours outside the plant that take energy. */
    private void findOutlets(){
        outletPos.clear();outletSide.clear();if(level==null)return;
        var inside=new java.util.HashSet<BlockPos>(structure().keySet());inside.add(worldPosition);
        for(var pos:inside)for(var side:Direction.values()){
            var n=pos.relative(side);if(inside.contains(n))continue;var be=level.getBlockEntity(n);if(be==null||be instanceof PlantPartEntity)continue;
            if(be instanceof EnergySource s&&!(s instanceof techguns.power.EnergyCableEntity)&&!(s instanceof techguns.power.EnergyCellEntity))continue;
            if(be.getCapability(ForgeCapabilities.ENERGY,side.getOpposite()).isPresent()){outletPos.add(n);outletSide.add(side.getOpposite());}
        }
    }
    protected boolean pushes(){return true;}
    private void push(){
        if(level==null||!pushes()||energy.getEnergyStored()<=0)return;int budget=Math.min(output,energy.getEnergyStored());
        for(int i=0;i<outletPos.size()&&budget>0;i++){var be=level.getBlockEntity(outletPos.get(i));if(be==null)continue;
            var target=be.getCapability(ForgeCapabilities.ENERGY,outletSide.get(i)).resolve();if(target.isEmpty()||!target.get().canReceive())continue;
            int given=target.get().receiveEnergy(budget,false);if(given>0){energy.extractEnergy(given,false);budget-=given;setChanged();}}
    }
    @Override protected boolean gives(Direction side){return formed&&pushes();}

    /** GUI values: 0 water mB, 1 water capacity, 2 heat (0..1000), 3 kind specific number. */
    public int value(int index){return 0;}
    public Component problemText(){return problem==0?Component.translatable("techguns.plant.working"):Component.translatable("techguns.plant."+kind.id()+".problem"+problem);}
    public net.minecraftforge.items.ItemStackHandler slots(){return null;}

    @Override public AABB getRenderBoundingBox(){return new AABB(worldPosition).inflate(8,12,8);}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.putBoolean("Formed",formed);tag.putInt("Rate",rate);tag.putInt("Problem",problem);}
    @Override public void load(CompoundTag tag){super.load(tag);formed=tag.getBoolean("Formed");rate=tag.getInt("Rate");problem=tag.getInt("Problem");}
}
