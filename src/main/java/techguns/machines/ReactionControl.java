package techguns.machines;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import techguns.*;
/** Original sixty-tick reaction steps, live intensity changes, completion target and failure behavior. */
public final class ReactionControl {
    private final ProcessingMachine machine;
    public int intensity,liquidLevel,completion,requiredIntensity,nextTick=60;
    public String recipeId="";
    public ItemStack input=ItemStack.EMPTY;
    public ReactionControl(ProcessingMachine machine){this.machine=machine;}
    public ReactionRecipe recipe(){return ReactionRecipe.byId(recipeId);}
    public boolean running(){return !recipeId.isEmpty();}
    public void tick(){
        if(!running()){start();return;}var recipe=recipe();if(recipe==null){clear();return;}
        machine.progress++;nextTick--;
        if(nextTick==0&&machine.progress<machine.totalTime){
            boolean powered=TGConfig.machinesNeedNoPower||machine.energy.getEnergyStored()>=recipe.energy();if(powered&&!TGConfig.machinesNeedNoPower)machine.energy.extractEnergy(recipe.energy(),false);
            boolean focus=machine.inventory.getStackInSlot(1).is(recipe.focus()),success=powered&&focus&&intensity==requiredIntensity&&liquidLevel==recipe.liquidLevel();if(success)completion++;
            if(!success&&TGRadiationSystem.isEnabled()){int radiation=(int)Math.ceil(0.5F*intensity);if(radiation>0)TGRadiationSystem.applyRadToEntities(machine,6,62,radiation-1,4,0);}
            var sound=success?TGSounds.REACTION_CHAMBER_BEEP.get():TGSounds.REACTION_CHAMBER_WARNING.get();machine.getLevel().playSound(null,machine.getBlockPos(),sound,SoundSource.BLOCKS,1,1);
            machine.getLevel().playSound(null,machine.getBlockPos(),recipe.focus()==TGItems.RC_HEAT_RAY.get()?TGSounds.REACTION_CHAMBER_HEATRAY_WORK.get():TGSounds.CHEM_LAB_WORK.get(),SoundSource.BLOCKS,1,1);
            if(!focus){machine.progress=machine.totalTime;completion=0;}
            else if(recipe.instability()>0&&machine.getLevel().random.nextFloat()<recipe.instability()){int change=recipe.margin()>1?1+machine.getLevel().random.nextInt(recipe.margin()):1;if(machine.getLevel().random.nextBoolean())change=-change;requiredIntensity=Math.max(0,Math.min(10,requiredIntensity+change));}
            nextTick=60;machine.changed();
        }
        if(completion>=recipe.requiredCompletion()){machine.deliverReactionOutputs(Arrays.asList(recipe.outputs()));if(recipe.liquidConsumption()>0)machine.inputTank.drain(recipe.liquidConsumption(),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);clear();}
        else if(machine.progress>=machine.totalTime){var risk=recipe.risk();clear();if(risk==ReactionRecipe.Risk.EXPLOSION_LOW||risk==ReactionRecipe.Risk.EXPLOSION_MEDIUM)explode(risk==ReactionRecipe.Risk.EXPLOSION_MEDIUM);}
        else if(machine.progress%5==0)machine.changed();
    }
    private void start(){var recipe=ReactionRecipe.all().stream().filter(r->r.matches(machine,intensity,liquidLevel)).findFirst().orElse(null);if(recipe==null||!TGConfig.machinesNeedNoPower&&machine.energy.getEnergyStored()<recipe.energy())return;input=machine.inventory.getStackInSlot(0).copyWithCount(1);machine.inventory.getStackInSlot(0).shrink(1);recipeId=recipe.id();completion=0;requiredIntensity=recipe.preferredIntensity();nextTick=60;machine.progress=0;machine.totalTime=recipe.ticks()*60;machine.changed();}
    private void clear(){recipeId="";input=ItemStack.EMPTY;completion=0;nextTick=60;machine.progress=machine.totalTime=0;machine.changed();}
    private void explode(boolean large){var level=machine.getLevel();var center=machine.getBlockPos().relative(machine.getBlockState().getValue(MachineBlock.FACING)).above();var fluid=machine.inputTank.getFluid().isEmpty()?null:machine.inputTank.getFluid().getFluid();machine.unformStructure();
        if(large){for(var entry:machine.structureParts(machine.getBlockState().getValue(MachineBlock.FACING)).entrySet())if(entry.getValue())level.setBlockAndUpdate(entry.getKey(),Blocks.AIR.defaultBlockState());level.explode(null,center.getX()+0.5,center.getY()+0.5,center.getZ()+0.5,4,net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);if(fluid!=null){level.setBlockAndUpdate(center,fluid.defaultFluidState().createLegacyBlock());level.setBlockAndUpdate(center.above(),Blocks.AIR.defaultBlockState());}}
        else {level.setBlockAndUpdate(center,fluid==null?Blocks.AIR.defaultBlockState():fluid.defaultFluidState().createLegacyBlock());for(var side:Direction.Plane.HORIZONTAL){var pos=center.relative(side);if(level.getBlockState(pos).getBlock() instanceof MachinePartBlock)level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());}level.playSound(null,center,net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),SoundSource.BLOCKS,4,0.7F);TGProjectilePackets.effect(machine.getLevel(),"FragGrenadeExplosion",center.getCenter(),0,0);}
    }
    public void button(int id){switch(id){case 3->liquidLevel=Math.min(10,liquidLevel+1);case 4->liquidLevel=Math.max(0,liquidLevel-1);case 5->intensity=Math.min(10,intensity+1);case 6->intensity=Math.max(0,intensity-1);case 7->machine.inputTank.setFluid(net.neoforged.neoforge.fluids.FluidStack.EMPTY);}machine.changed();}
    public void save(CompoundTag tag){tag.putInt("reactorIntensity",intensity);tag.putInt("reactorLiquidLevel",liquidLevel);tag.putInt("reactorCompletion",completion);tag.putInt("reactorRequired",requiredIntensity);tag.putInt("reactorNextTick",nextTick);tag.putString("reaction",recipeId);tag.put("reactionInput",techguns.util.TGItemNbt.save(input, techguns.util.TGItemNbt.registries()));}
    public void load(CompoundTag tag){intensity=Math.max(0,Math.min(10,tag.getInt("reactorIntensity")));liquidLevel=Math.max(0,Math.min(10,tag.getInt("reactorLiquidLevel")));completion=tag.getInt("reactorCompletion");requiredIntensity=tag.getInt("reactorRequired");nextTick=tag.contains("reactorNextTick")?tag.getInt("reactorNextTick"):60;recipeId=tag.getString("reaction");input=techguns.util.TGItemNbt.load(techguns.util.TGItemNbt.registries(), tag.getCompound("reactionInput"));}
}
