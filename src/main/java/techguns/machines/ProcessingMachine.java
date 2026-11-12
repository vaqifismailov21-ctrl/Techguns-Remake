package techguns.machines;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.*;
import net.minecraftforge.items.*;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import techguns.*;
/** Server authoritative machine transaction, retained across power loss, redstone pause and save/reload. */
public final class ProcessingMachine extends BlockEntity implements MenuProvider {
    public final MachineKind kind;
    public final ItemStackHandler inventory;
    public final EnergyStorage energy;
    public final MachineSecurity access=new MachineSecurity(this::changed);
    private LazyOptional<IItemHandler> automation;
    private LazyOptional<IEnergyStorage> energyHandle;
    private LazyOptional<IFluidHandler> fluidHandle;
    public final ReactionControl reactor=new ReactionControl(this);
    long nextChargeSound;
    public final FluidTank inputTank=new FluidTank(8000){@Override protected void onContentsChanged(){inputsChanged=true;changed();}@Override public int fill(FluidStack resource,IFluidHandler.FluidAction action){if(kind==MachineKind.REACTION){int space=Math.max(0,reactor.liquidLevel*1000-getFluidAmount());if(space==0)return 0;resource=new FluidStack(resource,Math.min(resource.getAmount(),space));}return super.fill(resource,action);}};
    public final FluidTank outputTank=new FluidTank(16000){@Override protected void onContentsChanged(){inputsChanged=true;changed();}};
    private FluidStack pendingFluid=FluidStack.EMPTY;
    public boolean drainInput;
    public boolean formed;
    private ItemStack pending=ItemStack.EMPTY;
    private final List<ItemStack> consumed=new ArrayList<>();
    private final List<ItemStack> pendingMultiple=new ArrayList<>();
    public int progress,totalTime,powerPerTick,plan,redstoneMode,autoSplitMode;
    public final ContainerData data=new ContainerData(){
        public int getCount(){return 15;}
        public int get(int i){return switch(i){case 0->progress;case 1->totalTime;case 2->energy.getEnergyStored()&65535;case 3->energy.getEnergyStored()>>>16;case 4->plan;case 5->redstoneMode;case 6->autoSplitMode;case 7->powerPerTick;case 8->running()?1:0;case 9->drainInput?1:0;case 10->reactor.intensity;case 11->reactor.liquidLevel;case 12->reactor.completion;case 13->reactor.requiredIntensity;case 14->reactor.nextTick;default->0;};}
        public void set(int i,int value){switch(i){case 0->progress=value;case 1->totalTime=value;case 2->setEnergy((energy.getEnergyStored()&0xffff0000)|(value&65535));case 3->setEnergy((energy.getEnergyStored()&65535)|((value&65535)<<16));case 4->plan=value;case 5->redstoneMode=value;case 6->autoSplitMode=value;case 7->powerPerTick=value;case 9->drainInput=value!=0;case 10->reactor.intensity=value;case 11->reactor.liquidLevel=value;case 12->reactor.completion=value;case 13->reactor.requiredIntensity=value;case 14->reactor.nextTick=value;}}
    };
    public ProcessingMachine(BlockPos pos,BlockState state){
        super(TGMachines.ENTITY.get(),pos,state);kind=((MachineBlock)state.getBlock()).kind;
        if(kind==MachineKind.REACTION)inputTank.setCapacity(10000);
        inventory=new ItemStackHandler(kind.slots.length){@Override protected void onContentsChanged(int slot){inputsChanged=true;changed();}@Override public boolean isItemValid(int slot,ItemStack stack){return accepts(slot,stack);}};
        energy=new EnergyStorage(kind.capacity,kind.capacity,kind.capacity){@Override public int receiveEnergy(int amount,boolean simulate){int n=super.receiveEnergy(amount,simulate);if(n>0&&!simulate){setChanged();energyDirty=true;}return n;}@Override public int extractEnergy(int amount,boolean simulate){int n=super.extractEnergy(amount,simulate);if(n>0&&!simulate)setChanged();return n;}};
        createHandles();
    }
    private void createHandles(){
        automation=LazyOptional.of(()->new IItemHandler(){public int getSlots(){return inventory.getSlots();}public ItemStack getStackInSlot(int slot){return inventory.getStackInSlot(slot);}public int getSlotLimit(int slot){return inventory.getSlotLimit(slot);}public boolean isItemValid(int slot,ItemStack stack){return accepts(slot,stack);}public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return inventory.insertItem(slot,stack,simulate);}public ItemStack extractItem(int slot,int amount,boolean simulate){return kind.outputSlot(slot)?inventory.extractItem(slot,amount,simulate):ItemStack.EMPTY;}});
        energyHandle=LazyOptional.of(()->new IEnergyStorage(){public int receiveEnergy(int amount,boolean simulate){return energy.receiveEnergy(amount,simulate);}public int extractEnergy(int amount,boolean simulate){return 0;}public int getEnergyStored(){return energy.getEnergyStored();}public int getMaxEnergyStored(){return energy.getMaxEnergyStored();}public boolean canExtract(){return false;}public boolean canReceive(){return true;}});
        fluidHandle=LazyOptional.of(()->new IFluidHandler(){public int getTanks(){return kind==MachineKind.REACTION?1:2;}public FluidStack getFluidInTank(int tank){return (tank==0?inputTank:outputTank).getFluid();}public int getTankCapacity(int tank){return (tank==0?inputTank:outputTank).getCapacity();}public boolean isFluidValid(int tank,FluidStack stack){return tank==0;}public int fill(FluidStack stack,FluidAction action){return inputTank.fill(stack,action);}public FluidStack drain(FluidStack stack,FluidAction action){if(kind==MachineKind.REACTION)return inputTank.getFluid().isFluidEqual(stack)?drain(stack.getAmount(),action):FluidStack.EMPTY;return (drainInput?inputTank:outputTank).drain(stack,action);}public FluidStack drain(int amount,FluidAction action){if(kind==MachineKind.REACTION)return inputTank.drain(Math.min(amount,Math.max(0,inputTank.getFluidAmount()-reactor.liquidLevel*1000)),action);return (drainInput?inputTank:outputTank).drain(amount,action);}});
    }
    public boolean accepts(int slot,ItemStack stack){
        if(kind.outputSlot(slot)||stack.isEmpty())return false;if(slot==kind.upgrade)return stack.is(TGItems.MACHINE_UPGRADE_STACK.get());
        if(kind==MachineKind.CHEM_LAB)return ChemicalRecipes.accepts(slot,stack);
        if(kind==MachineKind.GRINDER)return slot==0&&GrindingRecipe.find(stack)!=null;
        if(kind==MachineKind.REACTION){boolean focus=stack.is(TGItems.RC_HEAT_RAY.get())||stack.is(TGItems.RC_UV_EMITTER.get());return slot==1?focus:slot==0&&!focus;}
        if(kind==MachineKind.CHARGING_STATION&&slot==0&&stack.getCapability(ForgeCapabilities.ENERGY).isPresent())return true;
        return MachineRecipes.accepts(kind,slot,stack);
    }
    public boolean running(){return totalTime>0;}
    public boolean redstoneEnabled(){boolean signal=level!=null&&level.hasNeighborSignal(worldPosition);return redstoneMode==0||redstoneMode==1&&signal||redstoneMode==2&&!signal;}
    /** Set when items or fluids changed, so an idle machine looks for a recipe again at once. */
    private boolean inputsChanged=true;
    private int idleTicks;
    /** Energy taken in since the clients were last told. */
    private boolean energyDirty;
    /** A part of the multiblock was in an unloaded chunk at the last check. */
    private boolean partsUnloaded;
    private boolean inputsEmpty(){
        for(int i=0;i<inventory.getSlots();i++)if(!kind.outputSlot(i)&&i!=kind.upgrade&&!inventory.getStackInSlot(i).isEmpty())return false;
        return inputTank.isEmpty();
    }
    public void tick(){
        if(level==null||level.isClientSide)return;
        // incoming energy is shown to the clients once a second, not with a full update every tick
        if(energyDirty&&Math.floorMod(level.getGameTime()+worldPosition.asLong(),20L)==0){energyDirty=false;sync();}
        if(!redstoneEnabled())return;
        if(kind.multiblock()){
            if(!formed)return;
            // parts tell the controller when they are taken away; the whole structure is looked over once a second
            if(Math.floorMod(level.getGameTime()+worldPosition.asLong(),20L)==0){
                var parts=structureParts(getBlockState().getValue(MachineBlock.FACING));partsUnloaded=false;
                for(var pos:parts.keySet())if(!level.hasChunkAt(pos)){partsUnloaded=true;break;}
                if(!partsUnloaded&&!structureValid(parts,true)){unformStructure();return;}
            }
            if(partsUnloaded)return;
        }
        if(kind==MachineKind.REACTION){reactor.tick();return;}
        if(kind==MachineKind.METAL_PRESS&&autoSplitMode>0&&!running())split();
        if(!running()){
            if(chargeItem())return;
            // nothing to work on: no recipe search; a failed search is repeated when something changed, else twice a second
            if(inputsEmpty())return;
            if(!inputsChanged&&++idleTicks<10)return;
            inputsChanged=false;idleTicks=0;start();return;
        }
        if(progress>=totalTime){finish();return;}
        if(!TGConfig.machinesNeedNoPower&&energy.getEnergyStored()<powerPerTick)return;
        if(!TGConfig.machinesNeedNoPower)energy.extractEnergy(powerPerTick,false);progress++;MachineWorkEffects.work(this);setChanged();if(progress>=totalTime)finish();else if(progress%5==0)sync();
    }
    private void split(){var a=inventory.getStackInSlot(0);var b=inventory.getStackInSlot(1);if(a.isEmpty()&&b.getCount()>1){int n=b.getCount()/2;inventory.setStackInSlot(0,b.copyWithCount(n));b.shrink(n);changed();}else if(b.isEmpty()&&a.getCount()>1){int n=a.getCount()/2;inventory.setStackInSlot(1,a.copyWithCount(n));a.shrink(n);changed();}}
    private boolean chargeItem(){
        if(kind!=MachineKind.CHARGING_STATION)return false;var input=inventory.getStackInSlot(0);var handler=input.getCapability(ForgeCapabilities.ENERGY).orElse(null);if(handler==null)return false;
        int received=handler.receiveEnergy(Math.min(1600,energy.getEnergyStored()),true);if(received>0){int actual=handler.receiveEnergy(received,false);energy.extractEnergy(actual,false);if(actual>0)MachineWorkEffects.charge(this);changed();}
        if(handler.getEnergyStored()>=handler.getMaxEnergyStored()&&fits(input)){inventory.setStackInSlot(kind.output,input.copy());inventory.setStackInSlot(0,ItemStack.EMPTY);}return true;
    }
    public void start(){
        if(kind==MachineKind.CHEM_LAB){startChemical();return;}
        if(kind==MachineKind.GRINDER){startGrinding();return;}
        var recipe=MachineRecipes.find(this);if(recipe==null)return;var amounts=recipe.match(inventory);if(amounts==null)return;
        int max=1+inventory.getStackInSlot(kind.upgrade).getCount();for(int i=0;i<amounts.length;i++)if(amounts[i]>0)max=Math.min(max,inventory.getStackInSlot(i).getCount()/amounts[i]);
        var out=inventory.getStackInSlot(kind.output);if(!out.isEmpty()&&!ItemStack.isSameItemSameTags(out,recipe.result()))return;
        max=Math.min(max,(recipe.result().getMaxStackSize()-out.getCount())/recipe.result().getCount());if(max<=0)return;
        pending=recipe.result().copyWithCount(recipe.result().getCount()*max);consumed.clear();for(int i=0;i<amounts.length;i++){var stack=inventory.getStackInSlot(i);consumed.add(stack.copyWithCount(amounts[i]*max));stack.shrink(amounts[i]*max);}
        totalTime=recipe.ticks();powerPerTick=recipe.power()*max;progress=0;changed();
    }
    private void startChemical(){
        var recipe=ChemicalRecipes.all().stream().filter(r->r.match(this)!=null).findFirst().orElse(null);if(recipe==null)return;var amounts=recipe.match(this);int max=1+inventory.getStackInSlot(kind.upgrade).getCount();
        for(int i=0;i<amounts.length;i++)if(amounts[i]>0)max=Math.min(max,inventory.getStackInSlot(i).getCount()/amounts[i]);
        if(!recipe.fluidInput().isEmpty())max=Math.min(max,inputTank.getFluidAmount()/recipe.fluidInput().getAmount());
        if(!recipe.output().isEmpty()){if(!fits(recipe.output()))return;var out=inventory.getStackInSlot(kind.output);max=Math.min(max,(recipe.output().getMaxStackSize()-out.getCount())/recipe.output().getCount());}
        if(!recipe.fluidOutput().isEmpty())max=Math.min(max,outputTank.fill(new FluidStack(recipe.fluidOutput(),recipe.fluidOutput().getAmount()*max),IFluidHandler.FluidAction.SIMULATE)/recipe.fluidOutput().getAmount());
        if(max<=0)return;pending=recipe.output().isEmpty()?ItemStack.EMPTY:recipe.output().copyWithCount(recipe.output().getCount()*max);pendingFluid=recipe.fluidOutput().isEmpty()?FluidStack.EMPTY:new FluidStack(recipe.fluidOutput(),recipe.fluidOutput().getAmount()*max);
        consumed.clear();for(int i=0;i<amounts.length;i++){var stack=inventory.getStackInSlot(i);consumed.add(stack.copyWithCount(amounts[i]*max));stack.shrink(amounts[i]*max);}if(!recipe.fluidInput().isEmpty())inputTank.drain(recipe.fluidInput().getAmount()*max,IFluidHandler.FluidAction.EXECUTE);
        totalTime=100;powerPerTick=recipe.power()*max;progress=0;changed();
    }
    private boolean fits(ItemStack stack){if(stack.isEmpty())return true;var out=inventory.getStackInSlot(kind.output);return out.isEmpty()||ItemStack.isSameItemSameTags(out,stack)&&out.getCount()+stack.getCount()<=out.getMaxStackSize();}
    private void startGrinding(){var input=inventory.getStackInSlot(0);var recipe=GrindingRecipe.find(input);if(recipe==null)return;int max=Math.min(input.getCount(),1+inventory.getStackInSlot(kind.upgrade).getCount());while(max>0&&mergeGrinding(recipe.maximum(max))==null)max--;if(max==0)return;pendingMultiple.clear();pendingMultiple.addAll(recipe.roll(level.random,max));consumed.clear();consumed.add(input.copyWithCount(max));input.shrink(max);progress=0;totalTime=100;powerPerTick=5*max;changed();}
    /** Plan the complete merge on copies. A changed output inventory can never partially commit a recipe. */
    private List<ItemStack> mergeGrinding(List<ItemStack> outputs){var planned=new ArrayList<ItemStack>();for(int i=2;i<inventory.getSlots();i++)planned.add(inventory.getStackInSlot(i).copy());for(var output:outputs){int left=output.getCount();for(var stack:planned)if(ItemStack.isSameItemSameTags(stack,output)){int n=Math.min(left,stack.getMaxStackSize()-stack.getCount());stack.grow(n);left-=n;}for(int i=0;i<planned.size()&&left>0;i++)if(planned.get(i).isEmpty()){int n=Math.min(left,output.getMaxStackSize());planned.set(i,output.copyWithCount(n));left-=n;}if(left>0)return null;}return planned;}
    private void finish(){
        if(kind==MachineKind.GRINDER){var merged=mergeGrinding(pendingMultiple);if(merged==null)return;for(int i=0;i<merged.size();i++)inventory.setStackInSlot(i+2,merged.get(i));pendingMultiple.clear();}
        else {if(!fits(pending)||!pendingFluid.isEmpty()&&outputTank.fill(pendingFluid,IFluidHandler.FluidAction.SIMULATE)!=pendingFluid.getAmount())return;if(!pending.isEmpty()){var out=inventory.getStackInSlot(kind.output);inventory.setStackInSlot(kind.output,out.isEmpty()?pending.copy():out.copyWithCount(out.getCount()+pending.getCount()));}if(!pendingFluid.isEmpty())outputTank.fill(pendingFluid,IFluidHandler.FluidAction.EXECUTE);}
        pendingFluid=FluidStack.EMPTY;pending=ItemStack.EMPTY;consumed.clear();progress=totalTime=powerPerTick=0;changed();
    }
    public ItemStack pending(){return pending.copy();}
    public ItemStack consumed(int slot){return slot>=0&&slot<consumed.size()?consumed.get(slot).copy():ItemStack.EMPTY;}
    public Map<BlockPos,Boolean> structureParts(Direction facing){var result=new LinkedHashMap<BlockPos,Boolean>();var left=facing.getClockWise();if(kind==MachineKind.REACTION){for(int y=0;y<4;y++)for(int x=-1;x<=1;x++)for(int z=0;z<3;z++)if(y!=0||x!=0||z!=0)result.put(worldPosition.relative(left,x).relative(facing,z).above(y),y==1||y==2);}else for(int y=0;y<2;y++)for(int x=0;x<2;x++)for(int z=0;z<2;z++)if(y!=0||x!=0||z!=0)result.put(worldPosition.relative(left,x).relative(facing.getOpposite(),z).above(y),y==1);return result;}
    private boolean structureValid(Map<BlockPos,Boolean> parts,boolean linked){for(var entry:parts.entrySet()){var pos=entry.getKey();if(!level.hasChunkAt(pos)||!(level.getBlockState(pos).getBlock() instanceof MachinePartBlock part)||part.glass!=entry.getValue()||part.reactor!=(kind==MachineKind.REACTION)||!(level.getBlockEntity(pos) instanceof MachinePartEntity entity)||(linked?!entity.linkedTo(worldPosition):entity.linked()))return false;}return true;}
    private boolean partValid(BlockPos pos,boolean glass){return level.hasChunkAt(pos)&&level.getBlockState(pos).getBlock() instanceof MachinePartBlock part&&part.glass==glass&&part.reactor==(kind==MachineKind.REACTION)&&level.getBlockEntity(pos) instanceof MachinePartEntity entity&&!entity.linked();}
    /** Blocks that keep the structure from forming, in the original order of checks: the first row of the wrong kind that fails (fabricator: housings then glass, reaction chamber: glass then housings). */
    public java.util.List<BlockPos> invalidParts(Direction facing){
        var bad=new java.util.ArrayList<BlockPos>();if(level==null||!kind.multiblock()||facing.getAxis().isVertical())return bad;
        if(kind==MachineKind.REACTION)facing=facing.getOpposite();var parts=structureParts(facing);
        for(boolean glass:kind==MachineKind.REACTION?new boolean[]{true,false}:new boolean[]{false,true}){for(var e:parts.entrySet())if(e.getValue()==glass&&!partValid(e.getKey(),glass))bad.add(e.getKey());if(!bad.isEmpty())break;}
        return bad;
    }
    public boolean formStructure(Direction facing){if(!kind.multiblock()||formed||facing.getAxis().isVertical()||level==null)return false;if(kind==MachineKind.REACTION)facing=facing.getOpposite();var parts=structureParts(facing);if(!structureValid(parts,false))return false;formed=true;level.setBlock(worldPosition,getBlockState().setValue(MachineBlock.FORMED,true).setValue(MachineBlock.FACING,facing),3);for(var pos:parts.keySet())((MachinePartEntity)level.getBlockEntity(pos)).link(worldPosition);changed();return true;}
    public void unformStructure(){if(!formed||level==null)return;formed=false;for(var pos:structureParts(getBlockState().getValue(MachineBlock.FACING)).keySet())if(level.hasChunkAt(pos)&&level.getBlockEntity(pos) instanceof MachinePartEntity part&&part.linkedTo(worldPosition))part.link(null);if(level.getBlockState(worldPosition).getBlock() instanceof MachineBlock)level.setBlock(worldPosition,getBlockState().setValue(MachineBlock.FORMED,false),3);changed();}
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(){return kind.multiblock()&&formed?new net.minecraft.world.phys.AABB(worldPosition).inflate(4):super.getRenderBoundingBox();}
    public void deliverReactionOutputs(List<ItemStack> outputs){for(var output:outputs){int remaining=output.getCount();for(int i=2;i<inventory.getSlots()&&remaining>0;i++){var slot=inventory.getStackInSlot(i);if(slot.isEmpty()){int n=Math.min(remaining,output.getMaxStackSize());inventory.setStackInSlot(i,output.copyWithCount(n));remaining-=n;}else if(ItemStack.isSameItemSameTags(slot,output)){int n=Math.min(remaining,slot.getMaxStackSize()-slot.getCount());slot.grow(n);remaining-=n;}}if(remaining>0)Containers.dropItemStack(level,worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()+0.5,output.copyWithCount(remaining));}changed();}
    public void button(int id){inputsChanged=true;if(id==2)redstoneMode=(redstoneMode+1)%3;else if(kind==MachineKind.REACTION){reactor.button(id);}else if(kind==MachineKind.CHEM_LAB){if(id==3)inputTank.setFluid(FluidStack.EMPTY);else if(id==4)outputTank.setFluid(FluidStack.EMPTY);else if(id==5)drainInput=!drainInput;}else if(id==3&&kind==MachineKind.AMMO_PRESS)plan=(plan+1)%4;else if(id==4&&kind==MachineKind.AMMO_PRESS)plan=(plan+3)%4;else if(id==3&&kind==MachineKind.METAL_PRESS)autoSplitMode=(autoSplitMode+1)%2;changed();}
    public void setEnergy(int value){int old=energy.getEnergyStored();if(value>old)energy.receiveEnergy(value-old,false);else energy.extractEnergy(old-value,false);}
    public void dropContents(){if(level==null||level.isClientSide)return;for(int i=0;i<inventory.getSlots();i++){Containers.dropItemStack(level,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),inventory.getStackInSlot(i));inventory.setStackInSlot(i,ItemStack.EMPTY);}for(var stack:consumed)Containers.dropItemStack(level,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),stack);consumed.clear();if(reactor.running())Containers.dropItemStack(level,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),reactor.input);pending=ItemStack.EMPTY;}
    public void changed(){setChanged();sync();}private void sync(){if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag tag){
        super.saveAdditional(tag);tag.put("inventory",inventory.serializeNBT());tag.putInt("energy",energy.getEnergyStored());tag.putInt("progress",progress);tag.putInt("total",totalTime);tag.putInt("power",powerPerTick);tag.putInt("plan",plan);tag.putInt("redstone",redstoneMode);tag.putInt("autoSplit",autoSplitMode);tag.put("pending",pending.save(new CompoundTag()));var list=new ListTag();for(var stack:consumed)list.add(stack.save(new CompoundTag()));tag.put("consumed",list);
        tag.put("inputTank",inputTank.writeToNBT(new CompoundTag()));tag.put("outputTank",outputTank.writeToNBT(new CompoundTag()));tag.put("pendingFluid",pendingFluid.writeToNBT(new CompoundTag()));tag.putBoolean("drainInput",drainInput);
        access.save(tag);
        tag.putBoolean("formed",formed);
        reactor.save(tag);
        var multiple=new ListTag();for(var stack:pendingMultiple)multiple.add(stack.save(new CompoundTag()));tag.put("pendingMultiple",multiple);
    }
    @Override public void load(CompoundTag tag){
        super.load(tag);inventory.deserializeNBT(tag.getCompound("inventory"));setEnergy(tag.getInt("energy"));progress=tag.getInt("progress");totalTime=tag.getInt("total");powerPerTick=tag.getInt("power");plan=Math.floorMod(tag.getInt("plan"),4);redstoneMode=Math.floorMod(tag.getInt("redstone"),3);autoSplitMode=Math.floorMod(tag.getInt("autoSplit"),2);pending=ItemStack.of(tag.getCompound("pending"));consumed.clear();for(var entry:tag.getList("consumed",Tag.TAG_COMPOUND))consumed.add(ItemStack.of((CompoundTag)entry));
        inputTank.readFromNBT(tag.getCompound("inputTank"));outputTank.readFromNBT(tag.getCompound("outputTank"));pendingFluid=FluidStack.loadFluidStackFromNBT(tag.getCompound("pendingFluid"));drainInput=tag.getBoolean("drainInput");
        access.load(tag);
        formed=tag.getBoolean("formed");
        reactor.load(tag);
        pendingMultiple.clear();for(var entry:tag.getList("pendingMultiple",Tag.TAG_COMPOUND))pendingMultiple.add(ItemStack.of((CompoundTag)entry));
    }
    @Override public CompoundTag getUpdateTag(){return saveWithoutMetadata();}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side){if(capability==ForgeCapabilities.ITEM_HANDLER)return automation.cast();if(capability==ForgeCapabilities.ENERGY)return energyHandle.cast();if(capability==ForgeCapabilities.FLUID_HANDLER&&(kind==MachineKind.CHEM_LAB||kind==MachineKind.REACTION))return fluidHandle.cast();return super.getCapability(capability,side);}
    @Override public void invalidateCaps(){super.invalidateCaps();automation.invalidate();energyHandle.invalidate();fluidHandle.invalidate();}@Override public void reviveCaps(){super.reviveCaps();createHandles();}
    @Override public Component getDisplayName(){return Component.translatable(kind.titleKey());}
    @Override public AbstractContainerMenu createMenu(int id,Inventory playerInventory,Player player){return new MachineMenu(id,playerInventory,this);}
}
