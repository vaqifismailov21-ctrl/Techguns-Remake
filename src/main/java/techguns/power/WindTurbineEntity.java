package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Every piece of a wind turbine has one; only the base makes and gives energy. The base checks the column every second:
 * 1..WIND_MAX_MASTS masts and a head on top. Output grows from nothing at Y 64 to WIND_RATE at Y 128; every solid
 * block in the 5x5x3 space around the head takes a tenth off; rain adds a fifth, thunder a half.
 */
public final class WindTurbineEntity extends EnergySource {
    public int rate,masts,obstacles,headY;
    /** 0 working, 1 no head, 2 too many masts, 3 too low, 4 blocked. */
    public int problem;
    public WindTurbineEntity(BlockPos pos,BlockState state){super(PowerBlocks.WIND_TURBINE_ENTITY.get(),pos,state,PowerBlocks.WIND_CAPACITY,PowerBlocks.WIND_OUTPUT);}
    private boolean isBase(){return getBlockState().getValue(WindTurbineBlock.PART)==WindTurbineBlock.Part.BASE;}
    @Override protected boolean gives(Direction side){return isBase();}
    /** Base under a piece of the column, or null. */
    public static WindTurbineEntity findBase(Level level,BlockPos pos){
        var cursor=pos.mutable();
        for(int i=0;i<=PowerBlocks.WIND_MAX_MASTS+2;i++){
            var state=level.getBlockState(cursor);if(!state.is(PowerBlocks.WIND_TURBINE.get()))return null;
            if(state.getValue(WindTurbineBlock.PART)==WindTurbineBlock.Part.BASE)return level.getBlockEntity(cursor) instanceof WindTurbineEntity base?base:null;
            cursor.move(Direction.DOWN);
        }
        return null;
    }
    /** Checks the column and the space around the head and works out the output. */
    public void survey(){
        if(level==null)return;
        var cursor=worldPosition.above().mutable();masts=0;obstacles=0;problem=0;headY=0;
        while(level.getBlockState(cursor).is(PowerBlocks.WIND_TURBINE.get())&&level.getBlockState(cursor).getValue(WindTurbineBlock.PART)==WindTurbineBlock.Part.MAST){masts++;cursor.move(Direction.UP);}
        var top=level.getBlockState(cursor);
        if(!top.is(PowerBlocks.WIND_TURBINE.get())||top.getValue(WindTurbineBlock.PART)!=WindTurbineBlock.Part.HEAD||masts==0){problem=1;rate=0;return;}
        if(masts>PowerBlocks.WIND_MAX_MASTS){problem=2;rate=0;return;}
        BlockPos head=cursor.immutable();headY=head.getY();
        for(int dy=-1;dy<=1;dy++)for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){
            if(dx==0&&dz==0)continue;var p=head.offset(dx,dy,dz);if(!level.getBlockState(p).isAir())obstacles++;
        }
        double height=Math.max(0,Math.min(1,(headY-64)/64.0));
        double free=Math.max(0,1-obstacles*0.1);
        double weather=level.isThundering()?1.5:level.isRaining()?1.2:1;
        rate=(int)Math.round(PowerBlocks.WIND_RATE*height*free*weather);
        if(height<=0)problem=3;else if(obstacles>0)problem=4;
    }
    public Component reason(){
        return switch(problem){
            case 1->Component.translatable("techguns.power.wind.no_head");
            case 2->Component.translatable("techguns.power.wind.too_tall",PowerBlocks.WIND_MAX_MASTS);
            case 3->Component.translatable("techguns.power.wind.too_low");
            case 4->Component.translatable("techguns.power.wind.blocked",obstacles);
            default->Component.empty();
        };
    }
    public void tick(){
        if(level==null||!isBase())return;
        if(level.getGameTime()%20==Math.floorMod(worldPosition.asLong(),20L)){int old=rate;survey();if(old!=rate)sync();}
        if(energy.getEnergyStored()<energy.getMaxEnergyStored())produce(rate);
        distribute();
    }
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.putInt("Rate",rate);}
    @Override public void load(CompoundTag tag){super.load(tag);rate=tag.getInt("Rate");}
    @Override public AABB getRenderBoundingBox(){return new AABB(worldPosition).inflate(2);}
}
