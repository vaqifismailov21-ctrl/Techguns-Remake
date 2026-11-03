package techguns.oredrill;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.CommonHooks;
import techguns.capabilities.compat.Capability;
import techguns.capabilities.compat.Capabilities;
import techguns.capabilities.compat.LazyOptional;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import techguns.TGConfig;
import techguns.TGItems;
import techguns.TGOreClusters;
import techguns.TGOreDrill;
import techguns.TGSounds;
import techguns.api.tginventory.ITGSpecialSlot;
import techguns.api.tginventory.TGSlotType;
import techguns.blocks.OreClusterBlock;
import techguns.world.ClusterKind;

/**
 * Port of the original OreDrillTileEntMaster (MultiBlockMachineTileEntMaster, 500000 FE): drill head and furnace fuel
 * slots, nine outputs, 16 bucket input and 32 bucket output tanks, an internal fuel buffer, and cluster operations
 * whose rate depends on cluster size, drill length, drill radius and mining levels.
 */
public class OreDrillTileEntMaster extends BlockEntity implements MenuProvider, techguns.capabilities.compat.ICapabilityHost{
    public static final float SOUND_VOLUME=0.5f;
    public static final int CAPACITY_INPUT_TANK=16*1000, CAPACITY_OUTPUT_TANK=32*1000;
    public static final int SLOT_DRILL=0, SLOT_FURNACE=1, SLOT_OUTPUT1=2, SLOT_OUTPUT_COUNT=9;
    public static final int MAX_POWER=500000, USE_RANGE_SQUARED=144;
    public static final int BUTTON_ID_SECURITY=1, BUTTON_ID_REDSTONE=2;

    protected int engines, rods, radius;
    protected Direction drill_direction, multiblockDirection;
    protected boolean formed, hasPower, hasDrill;
    protected int drillType;
    protected int currentFuelBufferMax, fuelBuffer;
    protected int soundLoopDelay;
    public ItemStack lastDrill=ItemStack.EMPTY;
    protected boolean contentsChanged=true;
    /** Original MachineOperation for the drill: one output item or one bucket of fluid, power per tick. */
    public record Operation(ItemStack item,FluidStack fluid,int powerPerTick){
        CompoundTag save(){var tag=new CompoundTag();tag.put("item",techguns.util.TGItemNbt.save(item, techguns.util.TGItemNbt.registries()));tag.put("fluid",techguns.util.TGFluids2.save(fluid));tag.putInt("power",powerPerTick);return tag;}
        static Operation load(CompoundTag tag){return new Operation(techguns.util.TGItemNbt.load(techguns.util.TGItemNbt.registries(), tag.getCompound("item")),techguns.util.TGFluids2.load(tag.getCompound("fluid")),tag.getInt("power"));}
    }
    public Operation currentOperation;
    public int totaltime, progress;
    protected byte security, redstoneBehaviour;
    protected UUID owner;

    public final ItemStackHandler inventory=new ItemStackHandler(11){
        @Override protected void onContentsChanged(int slot){setChanged();contentsChanged=true;}
        @Override public boolean isItemValid(int slot,ItemStack stack){return slot<=SLOT_FURNACE&&isItemValidForSlot(slot,stack);}
    };
    public final EnergyStorage energy=new EnergyStorage(MAX_POWER){
        @Override public int receiveEnergy(int amount,boolean simulate){int n=super.receiveEnergy(amount,simulate);if(n>0&&!simulate)setChanged();return n;}
        @Override public int extractEnergy(int amount,boolean simulate){int n=super.extractEnergy(amount,simulate);if(n>0&&!simulate)setChanged();return n;}
    };
    public final FluidTank inputTank=new FluidTank(CAPACITY_INPUT_TANK){@Override protected void onContentsChanged(){tanksChanged();}};
    public final FluidTank outputTank=new FluidTank(CAPACITY_OUTPUT_TANK){@Override protected void onContentsChanged(){tanksChanged();}};
    private LazyOptional<IItemHandler> automation=LazyOptional.of(this::createAutomation);
    private LazyOptional<IEnergyStorage> energyHandle=LazyOptional.of(()->energy);
    private LazyOptional<IFluidHandler> fluidHandle=LazyOptional.of(this::createFluidHandler);

    public final ContainerData data=new ContainerData(){
        public int getCount(){return 9;}
        public int get(int i){return switch(i){case 0->progress;case 1->totaltime;case 2->energy.getEnergyStored()&65535;case 3->energy.getEnergyStored()>>>16;case 4->fuelBuffer&65535;case 5->fuelBuffer>>>16;case 6->currentFuelBufferMax&65535;case 7->currentFuelBufferMax>>>16;case 8->redstoneBehaviour|security<<4;default->0;};}
        public void set(int i,int v){switch(i){case 0->progress=v;case 1->totaltime=v;case 2->setEnergy((energy.getEnergyStored()&0xffff0000)|(v&65535));case 3->setEnergy((energy.getEnergyStored()&65535)|((v&65535)<<16));case 4->fuelBuffer=(fuelBuffer&0xffff0000)|(v&65535);case 5->fuelBuffer=(fuelBuffer&65535)|((v&65535)<<16);case 6->currentFuelBufferMax=(currentFuelBufferMax&0xffff0000)|(v&65535);case 7->currentFuelBufferMax=(currentFuelBufferMax&65535)|((v&65535)<<16);case 8->{redstoneBehaviour=(byte)(v&15);security=(byte)(v>>4);}}}
    };

    public OreDrillTileEntMaster(BlockPos pos,BlockState state){super(TGOreDrill.MASTER.get(),pos,state);}

    protected boolean isItemValidForSlot(int slot,ItemStack stack){
        if(stack.isEmpty())return false;
        if(slot==SLOT_DRILL&&stack.getItem() instanceof ITGSpecialSlot special){var type=special.getSlot(stack);return type==TGSlotType.DRILL_SMALL||type==TGSlotType.DRILL_MEDIUM||type==TGSlotType.DRILL_LARGE;}
        if(slot==SLOT_FURNACE)return stack.getBurnTime(null)>0;
        return false;
    }
    private IItemHandler createAutomation(){return new IItemHandler(){
        public int getSlots(){return inventory.getSlots();}
        public ItemStack getStackInSlot(int slot){return inventory.getStackInSlot(slot);}
        public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return inventory.isItemValid(slot,stack)?inventory.insertItem(slot,stack,simulate):stack;}
        public ItemStack extractItem(int slot,int amount,boolean simulate){return slot>=SLOT_OUTPUT1&&slot<SLOT_OUTPUT1+SLOT_OUTPUT_COUNT?inventory.extractItem(slot,amount,simulate):ItemStack.EMPTY;}
        public int getSlotLimit(int slot){return inventory.getSlotLimit(slot);}
        public boolean isItemValid(int slot,ItemStack stack){return inventory.isItemValid(slot,stack);}
    };}
    /** Original OreDrillFluidHandler: fill the input tank, drain the output tank. */
    private IFluidHandler createFluidHandler(){return new IFluidHandler(){
        public int getTanks(){return 2;}
        public FluidStack getFluidInTank(int tank){return (tank==0?inputTank:outputTank).getFluid();}
        public int getTankCapacity(int tank){return (tank==0?inputTank:outputTank).getCapacity();}
        public boolean isFluidValid(int tank,FluidStack stack){return tank==0;}
        public int fill(FluidStack resource,FluidAction action){return inputTank.fill(resource,action);}
        public FluidStack drain(FluidStack resource,FluidAction action){return outputTank.drain(resource,action);}
        public FluidStack drain(int maxDrain,FluidAction action){return outputTank.drain(maxDrain,action);}
    };}
    private void setEnergy(int value){int old=energy.getEnergyStored();if(value>old)energy.receiveEnergy(value-old,false);else if(value<old)energy.extractEnergy(old-value,false);}
    private void tanksChanged(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}

    @Override public Component getDisplayName(){return Component.translatable("techguns.container.oredrill");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new OreDrillMenu(id,inventory,this);}

    // --- Ownership and redstone (BasicOwnedTileEnt/BasicRedstoneTileEnt) -----------------------------------------
    public void setOwner(Player player){owner=player.getGameProfile().getId();security=0;setChanged();}
    public UUID getOwner(){return owner;}
    public byte getSecurity(){return security;}
    public byte getRedstoneBehaviour(){return redstoneBehaviour;}
    public boolean isOwnedByPlayer(Player player){return owner!=null&&owner.equals(player.getGameProfile().getId());}
    public boolean isUseableByPlayer(Player player){
        if(level==null||level.getBlockEntity(worldPosition)!=this||player.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)>USE_RANGE_SQUARED)return false;
        return owner==null||security==0||isOwnedByPlayer(player)||security==1||security==2;
    }
    public boolean isRedstoneEnabled(){boolean signal=level!=null&&level.hasNeighborSignal(worldPosition);return redstoneBehaviour==0||signal&&redstoneBehaviour==1||!signal&&redstoneBehaviour==2;}
    public void buttonClicked(int id,Player player){
        if(id==BUTTON_ID_SECURITY){if(isOwnedByPlayer(player)){security=(byte)(security==0?3:0);needUpdate();}}
        else if(id==BUTTON_ID_REDSTONE&&isUseableByPlayer(player)){redstoneBehaviour=(byte)((redstoneBehaviour+1)%3);needUpdate();}
    }

    // --- Multiblock --------------------------------------------------------------------------------------------
    public void formOreDrill(int engines,int rods,int radius,Direction drillDirection){
        this.engines=engines;this.rods=rods;this.radius=radius;this.drill_direction=drillDirection;
        formed=true;multiblockDirection=drillDirection;
        if(level!=null&&!level.isClientSide){level.setBlock(worldPosition,getBlockState().setValue(OreDrillBlock.FORMED,true),3);needUpdate();}
    }
    public void unform(){
        formed=false;
        if(level!=null&&!level.isClientSide&&level.getBlockState(worldPosition).getBlock()==TGOreDrill.CONTROLLER.get())level.setBlock(worldPosition,getBlockState().setValue(OreDrillBlock.FORMED,false),3);
        engines=rods=radius=0;drill_direction=null;
    }
    public void onMultiBlockBreak(){
        if(level!=null&&!level.isClientSide&&formed)OreDrillDefinition.unform(level,this);
        currentOperation=null;progress=0;totaltime=0;needUpdate();
    }
    /** Breaking the controller drops its inventory, then breaks the multiblock. */
    public void onBlockBreak(){
        if(level!=null&&!level.isClientSide)for(int i=0;i<inventory.getSlots();i++){Containers.dropItemStack(level,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),inventory.getStackInSlot(i));inventory.setStackInSlot(i,ItemStack.EMPTY);}
        onMultiBlockBreak();
    }
    public boolean isFormed(){return formed;}
    public int getEngines(){return engines;}
    public int getRods(){return rods;}
    public int getRadius(){return radius;}
    public Direction getDrill_direction(){return drill_direction;}
    /** Including the middle block: 1 for 1xN engines, 2 for 3x3, 3 for 5x5, 4 for 7x7; 0 for a controller-and-rod drill. */
    public int getDrillRadius(){return engines==0?0:radius+1;}
    public int getTotalDrillLength(){return engines+rods;}
    public boolean hasDrill(){return hasDrill;}
    public int getDrillType(){return drillType;}
    public boolean hasPower(){return hasPower;}
    public boolean isWorking(){return currentOperation!=null;}
    public int getPowerPerTick(){return currentOperation!=null?currentOperation.powerPerTick:0;}
    public int getFuelBuffer(){return fuelBuffer;}
    public void setFuelBuffer(int fuelBuffer){this.fuelBuffer=fuelBuffer;}
    public void setCurrentFuelBufferMax(int max){currentFuelBufferMax=max;}
    public int getCurrentFuelBufferMax(){return currentFuelBufferMax;}
    public int getFuelBufferScaled(int limit){return currentFuelBufferMax>0?fuelBuffer*limit/currentFuelBufferMax:0;}
    public int getProgressScaled(int limit){return totaltime!=0?progress*limit/totaltime:0;}

    public AABB getRenderBoundingBox(){
        if(formed&&drill_direction!=null){
            BlockPos rodEnd=worldPosition.relative(drill_direction,rods+engines+1);
            BlockPos other=rodEnd.relative(OreDrillDefinition.getSide1(drill_direction),radius+2).relative(OreDrillDefinition.getSide2(drill_direction),radius+1);
            BlockPos first=worldPosition.relative(OreDrillDefinition.getSide1(drill_direction),-(radius+2)).relative(OreDrillDefinition.getSide2(drill_direction),-(radius+1));
            return AABB.encapsulatingFullBlocks(first,other);
        }
        return net.minecraft.world.phys.AABB.unitCubeFromLowerCorner(net.minecraft.world.phys.Vec3.atLowerCornerOf(worldPosition));
    }

    // --- Power and fuel ------------------------------------------------------------------------------------------
    protected boolean consumePower(int amount){
        if(TGConfig.machinesNeedNoPower)return true;
        float factor=TGConfig.oreDrillMultiplierFuel;
        if(fuelBuffer*factor>=amount){fuelBuffer-=Math.max(amount/factor,1);return true;}
        amount-=fuelBuffer*factor;fuelBuffer=0;currentFuelBufferMax=0;
        ItemStack fuel=inventory.getStackInSlot(SLOT_FURNACE);
        int fuelValue=fuel.isEmpty()?0:fuel.getBurnTime(null);
        if(fuelValue>0){
            ItemStack container=fuel.getCraftingRemainingItem();
            if(!container.isEmpty()){
                int leftover=addItemToOutputs(container.copy());
                if(leftover>0&&level!=null&&!level.isClientSide)level.addFreshEntity(new ItemEntity(level,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,container.copyWithCount(leftover)));
            }
            var rest=fuel.copy();rest.shrink(1);inventory.setStackInSlot(SLOT_FURNACE,rest.isEmpty()?ItemStack.EMPTY:rest);
            fuelBuffer=fuelValue;currentFuelBufferMax=fuelValue;
        }else{
            float tankValue=getFuelValueFromInputTank();
            if(tankValue>0){FluidStack drained=inputTank.drain(1000,IFluidHandler.FluidAction.EXECUTE);fuelBuffer=(int)(drained.getAmount()*tankValue);currentFuelBufferMax=(int)(drained.getAmount()*tankValue);}
        }
        if(fuelBuffer*factor>=amount){fuelBuffer-=Math.max(amount/factor,1);return true;}
        if(amount>0&&energy.getEnergyStored()>=amount){energy.extractEnergy(amount,false);return true;}
        return amount==0;
    }
    /** Lava 20 per mB as in a lava bucket; configured fuel fluids use oreDrillFuelValueFuel. */
    protected float getFuelValueFromInputTank(){
        if(inputTank.isEmpty())return 0;
        var fluid=inputTank.getFluid().getFluid();
        if(fluid.isSame(Fluids.LAVA))return 20f;
        var id=BuiltInRegistries.FLUID.getKey(fluid);
        if(id!=null)for(String name:TGConfig.fluidListFuel)if(id.getPath().equals(name))return TGConfig.oreDrillFuelValueFuel;
        return 0;
    }
    /** Original InventoryUtil.addItemToInventory over the output slots: merge first, then one empty slot. */
    private int addItemToOutputs(ItemStack item){
        for(int i=SLOT_OUTPUT1;i<SLOT_OUTPUT1+SLOT_OUTPUT_COUNT;i++){var slot=inventory.getStackInSlot(i);if(!slot.isEmpty()&&slot.getItem()==item.getItem()&&slot.getCount()<item.getMaxStackSize()){int total=slot.getCount()+item.getCount();if(total<=item.getMaxStackSize()){inventory.setStackInSlot(i,slot.copyWithCount(total));return 0;}inventory.setStackInSlot(i,slot.copyWithCount(item.getMaxStackSize()));item.setCount(total-item.getMaxStackSize());}}
        for(int i=SLOT_OUTPUT1;i<SLOT_OUTPUT1+SLOT_OUTPUT_COUNT;i++)if(inventory.getStackInSlot(i).isEmpty()){inventory.setStackInSlot(i,item.copy());return 0;}
        return item.getCount();
    }

    // --- Operation -----------------------------------------------------------------------------------------------
    protected boolean checkDrill(){
        if(level.getGameTime()%20==0){
            ItemStack drill=inventory.getStackInSlot(SLOT_DRILL);
            if(lastDrill.isEmpty()&&drill.isEmpty()){hasDrill=false;needUpdate();return false;}
            else if(lastDrill.isEmpty()){lastDrill=drill;return true;}
            else if(drill.isEmpty()||!ItemStack.isSameItem(lastDrill,drill)){lastDrill=drill.copy();currentOperation=null;progress=0;totaltime=0;needUpdate();return false;}
        }
        return true;
    }

    public void tick(){
        if(level==null||level.isClientSide||!isRedstoneEnabled())return;
        if(currentOperation!=null){
            if(checkDrill()){
                if(currentOperation!=null&&consumePower(getPowerPerTick())){
                    progress++;
                    playAmbientSound();
                    if(!hasPower){hasPower=true;needUpdate();}
                    if(progress>=totaltime){finishedOperation();progress=0;totaltime=0;currentOperation=null;checkAndStartOperation();needUpdate();}
                }else if(hasPower){hasPower=false;needUpdate();}
            }
        }else if(contentsChanged)checkAndStartOperation();
    }

    protected void checkAndStartOperation(){
        if(!formed)return;
        contentsChanged=false;
        BlockPos target=worldPosition.relative(drill_direction,rods+engines+1);
        if(OreDrillDefinition.isOreCluster(level,target)){
            BlockState bs=level.getBlockState(target);
            ClusterKind cluster=((OreClusterBlock)bs.getBlock()).kind;
            int drillLevel=getDrillItemMiningLevel();
            if(drillLevel>0){
                hasDrill=true;
                int effective=drillLevel+getDrillRadius()-cluster.miningLevel();
                drillType=getDrillHeadType();
                if(effective>=0){
                    int clusterSize=getClusterSize(target,bs);
                    int effectiveSize=Math.min(clusterSize,getTotalDrillLength());
                    double orePerHour=((effectiveSize*3)+(effectiveSize*effective*0.5))*TGConfig.oreDrillMultiplierOres*cluster.amountMultiplier();
                    currentOperation=newOperation(cluster,orePerHour,getDrillRadius());
                    progress=0;
                    totaltime=(int)(60.0/orePerHour*1200.0);
                }else{currentOperation=cobblestoneOperation();progress=0;totaltime=20*20;}
            }else{hasDrill=false;drillType=0;}
            needUpdate();
        }else{
            // Original: only the controller unforms here; its parts keep their formed state.
            unform();formed=false;needUpdate();
        }
    }
    /** OreCluster.getNewOperation: a weighted ore item or one bucket of fluid; empty clusters yield cobblestone. */
    protected Operation newOperation(ClusterKind cluster,double orePerHour,int drillRadius){
        var entries=TGOreClusters.entries(cluster);
        if(entries.isEmpty())return cobblestoneOperation();
        var entry=TGOreClusters.choose(cluster,level.random);
        int power=(int)(8*orePerHour*(1+Math.max(drillRadius-1,0)*0.2)*cluster.powerMultiplier()*TGConfig.oreDrillMultiplierPower);
        if(!entry.item().isEmpty())return new Operation(entry.item().copyWithCount(1),FluidStack.EMPTY,power);
        return new Operation(ItemStack.EMPTY,new FluidStack(entry.fluid().getFluid(),1000),power);
    }
    protected Operation cobblestoneOperation(){return new Operation(new ItemStack(Blocks.COBBLESTONE),FluidStack.EMPTY,(int)(24*TGConfig.oreDrillMultiplierPower));}

    protected int getDrillHeadType(){int level=getDrillItemMiningLevel();return level<0?0:level;}
    public int getDrillItemMiningLevel(){
        ItemStack drill=inventory.getStackInSlot(SLOT_DRILL);
        if(drill.isEmpty()||!formed)return -1;
        int r=getDrillRadius();
        if(r==0){if(drill.is(TGItems.OREDRILLHEAD_STEEL.get()))return 1;if(drill.is(TGItems.OREDRILLHEAD_OBSIDIANSTEEL.get()))return 2;if(drill.is(TGItems.OREDRILLHEAD_CARBON.get()))return 3;}
        else if(r==1||r==2){if(drill.is(TGItems.OREDRILLHEAD_MEDIUM_STEEL.get()))return 1;if(drill.is(TGItems.OREDRILLHEAD_MEDIUM_OBSIDIANSTEEL.get()))return 2;if(drill.is(TGItems.OREDRILLHEAD_MEDIUM_CARBON.get()))return 3;}
        else if(r==3||r==4){if(drill.is(TGItems.OREDRILLHEAD_LARGE_STEEL.get()))return 1;if(drill.is(TGItems.OREDRILLHEAD_LARGE_OBSIDIANSTEEL.get()))return 2;if(drill.is(TGItems.OREDRILLHEAD_LARGE_CARBON.get()))return 3;}
        return -1;
    }
    /** Original getClusterSize: number of face-connected blocks with exactly the cluster's state. */
    public int getClusterSize(BlockPos start,BlockState clusterState){
        var visited=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();queue.add(start);int count=0;
        while(!queue.isEmpty()){var p=queue.poll();if(visited.contains(p)||level.getBlockState(p)!=clusterState)continue;visited.add(p);count++;for(var d:Direction.values())queue.add(p.relative(d));}
        return count;
    }
    /** insertItemNoCheck across the outputs; anything left is dropped at the controller. */
    protected void finishedOperation(){
        ItemStack output=currentOperation.item().copy();
        if(!output.isEmpty()){
            for(int s=SLOT_OUTPUT1;s<SLOT_OUTPUT1+SLOT_OUTPUT_COUNT&&!output.isEmpty();s++){
                var slot=inventory.getStackInSlot(s);
                if(slot.isEmpty()){int n=Math.min(output.getCount(),output.getMaxStackSize());inventory.setStackInSlot(s,output.copyWithCount(n));output.shrink(n);}
                else if(ItemStack.isSameItemSameComponents(slot,output)){int n=Math.min(output.getCount(),slot.getMaxStackSize()-slot.getCount());if(n>0){inventory.setStackInSlot(s,slot.copyWithCount(slot.getCount()+n));output.shrink(n);}}
            }
            if(!output.isEmpty())level.addFreshEntity(new ItemEntity(level,worldPosition.getX()+.5d,worldPosition.getY()+.5d,worldPosition.getZ()+.5d,output));
        }
        if(!currentOperation.fluid().isEmpty())outputTank.fill(currentOperation.fluid().copy(),IFluidHandler.FluidAction.EXECUTE);
    }
    protected void playAmbientSound(){
        if(soundLoopDelay--<=0){
            BlockPos soundPos=worldPosition.relative(drill_direction,engines).relative(drill_direction,(int)(rods*0.5));
            SoundEvent sound=radius==0?TGSounds.ORE_DRILL_WORK_SMALL.get():radius<=2?TGSounds.ORE_DRILL_WORK_MEDIUM.get():TGSounds.ORE_DRILL_WORK_LARGE.get();
            level.playSound(null,soundPos.getX(),soundPos.getY(),soundPos.getZ(),sound,SoundSource.BLOCKS,SOUND_VOLUME,1.0f);
            soundLoopDelay=61;
        }
    }
    public int getValidSlotForItemInMachine(ItemStack stack){if(isItemValidForSlot(SLOT_DRILL,stack))return SLOT_DRILL;if(isItemValidForSlot(SLOT_FURNACE,stack))return SLOT_FURNACE;return -1;}

    public void needUpdate(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}

    // --- Persistence -------------------------------------------------------------------------------------------
    public void writeClientData(CompoundTag tags){
        tags.putInt("powerStored",energy.getEnergyStored());
        tags.putByte("redstoneBehaviour",redstoneBehaviour);
        if(owner!=null){tags.putByte("security",security);tags.putString("owner",owner.toString());}
        tags.putInt("totaltime",totaltime);tags.putInt("progress",progress);
        if(currentOperation!=null)tags.put("operation",currentOperation.save());
        tags.putBoolean("formed",formed);
        if(formed&&drill_direction!=null){
            tags.putByte("multiblockDirection",(byte)multiblockDirection.get3DDataValue());
            tags.putByte("drill_direction",(byte)drill_direction.get3DDataValue());
            tags.putByte("engines",(byte)engines);tags.putByte("rods",(byte)rods);tags.putByte("radius",(byte)radius);
            tags.putBoolean("haspower",hasPower);tags.putBoolean("hasDrill",hasDrill);tags.putByte("drillType",(byte)drillType);
        }
        tags.putInt("fuelBuffer",fuelBuffer);tags.putInt("fuelBufferMax",currentFuelBufferMax);
        tags.put("inputTank",inputTank.writeToNBT(techguns.util.TGItemNbt.registries(), new CompoundTag()));tags.put("outputTank",outputTank.writeToNBT(techguns.util.TGItemNbt.registries(), new CompoundTag()));
    }
    public void readClientData(CompoundTag tags){
        setEnergy(tags.getInt("powerStored"));
        redstoneBehaviour=tags.getByte("redstoneBehaviour");
        security=tags.getByte("security");
        String own=tags.getString("owner");if(!own.isEmpty())try{owner=UUID.fromString(own);}catch(IllegalArgumentException e){owner=null;}
        totaltime=tags.getInt("totaltime");progress=tags.getInt("progress");
        currentOperation=tags.contains("operation")?Operation.load(tags.getCompound("operation")):null;
        formed=tags.getBoolean("formed");
        if(formed){
            multiblockDirection=Direction.from3DDataValue(tags.getByte("multiblockDirection"));
            drill_direction=Direction.from3DDataValue(tags.getByte("drill_direction"));
            engines=tags.getByte("engines");rods=tags.getByte("rods");radius=tags.getByte("radius");
            hasPower=tags.getBoolean("haspower");hasDrill=tags.getBoolean("hasDrill");drillType=tags.getByte("drillType");
        }
        fuelBuffer=tags.getInt("fuelBuffer");currentFuelBufferMax=tags.getInt("fuelBufferMax");
        inputTank.readFromNBT(techguns.util.TGItemNbt.registries(), tags.getCompound("inputTank"));outputTank.readFromNBT(techguns.util.TGItemNbt.registries(), tags.getCompound("outputTank"));
    }
    @Override protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(tag, registries);tag.put("inventory",inventory.serializeNBT(techguns.util.TGItemNbt.registries()));writeClientData(tag);}
    @Override protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.loadAdditional(tag, registries);inventory.deserializeNBT(techguns.util.TGItemNbt.registries(), tag.getCompound("inventory"));readClientData(tag);}
    @Override public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries){var tag=new CompoundTag();tag.put("inventory",inventory.serializeNBT(techguns.util.TGItemNbt.registries()));writeClientData(tag);return tag;}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}

    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side){
        if(capability==Capabilities.ITEM_HANDLER)return automation.cast();
        if(capability==Capabilities.ENERGY)return energyHandle.cast();
        if(capability==Capabilities.FLUID_HANDLER)return fluidHandle.cast();
        return LazyOptional.empty();
    }
    @Override public void invalidateCaps(){invalidateCapabilities();automation.invalidate();energyHandle.invalidate();fluidHandle.invalidate();}
    @Override public void reviveCaps(){automation=LazyOptional.of(this::createAutomation);energyHandle=LazyOptional.of(()->energy);fluidHandle=LazyOptional.of(this::createFluidHandler);}
    static String fluidName(FluidStack stack){var id=BuiltInRegistries.FLUID.getKey(stack.getFluid());return id==null?"":id.toString();}
}
