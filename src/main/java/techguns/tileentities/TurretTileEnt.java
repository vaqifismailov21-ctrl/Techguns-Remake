package techguns.tileentities;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import techguns.TGConfig;
import techguns.TGItems;
import techguns.TGTurrets;
import techguns.api.tginventory.ITGSpecialSlot;
import techguns.api.tginventory.TGSlotType;
import techguns.entities.npcs.NPCTurret;
import techguns.items.guns.GenericGun;
import techguns.machines.TurretMenu;

/**
 * Port of the original TurretTileEnt (BasicPoweredTileEnt, 5000 FE): 9 ammunition inputs, 9 empty-magazine outputs,
 * one gun and one turret-armor slot, repair after death, power-dependent activation and healing.
 */
public class TurretTileEnt extends BlockEntity implements MenuProvider {
    public static final int SLOT_INPUT1=0, INPUTS_SIZE=9;
    public static final int SLOT_OUTPUT1=SLOT_INPUT1+INPUTS_SIZE, OUTPUTS_SIZE=9;
    public static final int SLOT_WEAPON=SLOT_OUTPUT1+OUTPUTS_SIZE, SLOT_ARMOR=SLOT_WEAPON+1;
    public static final int BUTTON_ID_SECURITY=1, BUTTON_ID_REDSTONE=2;
    public static final int BUTTON_ID_TARGET_ANIMALS=BUTTON_ID_REDSTONE+1, BUTTON_ID_PVP_SETTING=BUTTON_ID_REDSTONE+2;
    public static final int MAX_POWER=5000;

    public NPCTurret mountedTurret;
    private int clientTurretId=-1;
    public boolean turretDeath;
    public int repairTime, turretHealTime;
    public boolean attackAnimals;
    protected Direction facing=Direction.UP;
    /** 0: no PvP, 1: enemies, 2: neutrals, 3: team members, 4: everyone except the owner. */
    protected byte pvpsetting;
    /** Original BasicOwnedTileEnt: 0 anyone, 1 allies, 2 team, 3 owner only (without a team mod: 0 or 3). */
    protected byte security;
    protected UUID owner;
    /** Original BasicRedstoneTileEnt: 0 ignore, 1 needs signal, 2 needs no signal. */
    public byte redstoneBehaviour;

    public final ItemStackHandler inventory=new ItemStackHandler(SLOT_ARMOR+1){
        @Override protected void onContentsChanged(int slot){setChanged();if(slot==SLOT_WEAPON)updateWeapon();}
        @Override public boolean isItemValid(int slot,ItemStack stack){return allowItemInSlot(slot,stack);}
    };
    public final EnergyStorage energy=new EnergyStorage(MAX_POWER){
        @Override public int receiveEnergy(int amount,boolean simulate){int n=super.receiveEnergy(amount,simulate);if(n>0&&!simulate)setChanged();return n;}
        @Override public int extractEnergy(int amount,boolean simulate){int n=super.extractEnergy(amount,simulate);if(n>0&&!simulate)setChanged();return n;}
    };
    private LazyOptional<IItemHandler> automation=LazyOptional.of(this::createAutomation);
    private LazyOptional<IEnergyStorage> energyHandle=LazyOptional.of(()->energy);

    public final ContainerData data=new ContainerData(){
        public int getCount(){return 9;}
        public int get(int i){return switch(i){case 0->turretDeath?1:0;case 1->repairTime;case 2->energy.getEnergyStored()&65535;case 3->energy.getEnergyStored()>>>16;case 4->redstoneBehaviour;case 5->security;case 6->pvpsetting;case 7->attackAnimals?1:0;case 8->turretHealthTimes10();default->0;};}
        public void set(int i,int value){switch(i){case 0->turretDeath=value!=0;case 1->repairTime=value;case 2->setEnergy((energy.getEnergyStored()&0xffff0000)|(value&65535));case 3->setEnergy((energy.getEnergyStored()&65535)|((value&65535)<<16));case 4->redstoneBehaviour=(byte)value;case 5->security=(byte)value;case 6->pvpsetting=(byte)value;case 7->attackAnimals=value!=0;case 8->clientHealthTimes10=value;}}
    };
    /** The client GUI shows the mounted turret's health as the original did through its synchronised entity. */
    public int clientHealthTimes10=-1;

    public TurretTileEnt(BlockPos pos,BlockState state){super(TGTurrets.ENTITY.get(),pos,state);}

    protected boolean allowItemInSlot(int slot,ItemStack stack){
        if(slot==SLOT_WEAPON)return stack.getItem() instanceof GenericGun;
        if(slot==SLOT_ARMOR)return stack.getItem() instanceof ITGSpecialSlot special&&special.getSlot(stack)==TGSlotType.TURRETARMOR;
        return slot>=SLOT_INPUT1&&slot<SLOT_INPUT1+INPUTS_SIZE;
    }
    protected boolean allowExtractFromSlot(int slot){return slot>=SLOT_OUTPUT1&&slot<SLOT_OUTPUT1+OUTPUTS_SIZE;}
    private IItemHandler createAutomation(){return new IItemHandler(){
        public int getSlots(){return inventory.getSlots();}
        public ItemStack getStackInSlot(int slot){return inventory.getStackInSlot(slot);}
        public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return allowItemInSlot(slot,stack)?inventory.insertItem(slot,stack,simulate):stack;}
        public ItemStack extractItem(int slot,int amount,boolean simulate){return allowExtractFromSlot(slot)?inventory.extractItem(slot,amount,simulate):ItemStack.EMPTY;}
        public int getSlotLimit(int slot){return inventory.getSlotLimit(slot);}
        public boolean isItemValid(int slot,ItemStack stack){return allowItemInSlot(slot,stack);}
    };}
    private void setEnergy(int value){int old=energy.getEnergyStored();if(value>old)energy.receiveEnergy(value-old,false);else if(value<old)energy.extractEnergy(old-value,false);}
    private int turretHealthTimes10(){return mountedTurret==null?-1:Math.round(mountedTurret.getHealth()*10);}

    @Override public Component getDisplayName(){return Component.translatable("techguns.container.turretbase");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory playerInventory,Player player){return new TurretMenu(id,playerInventory,this);}

    public int getRepairTimeScaled(int max){return max-(repairTime*max/getRepairTimeMax());}

    // --- Ownership and redstone, as in BasicOwnedTileEnt/BasicRedstoneTileEnt ----------------------------------
    public void setOwner(Player player){owner=player.getGameProfile().getId();security=0;setChanged();}
    public UUID getOwner(){return owner;}
    public byte getSecurity(){return security;}
    public boolean isOwnedByPlayer(Player player){return owner!=null&&owner.equals(player.getGameProfile().getId());}
    public boolean isUseableByPlayer(Player player){
        if(level==null||level.getBlockEntity(worldPosition)!=this||player.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)>64)return false;
        if(owner==null||security==0||isOwnedByPlayer(player))return true;
        // TGNpcFactions.canAccess without a team integration: allies/team settings admit everyone, owner-only does not.
        return security==1||security==2;
    }
    /** Original BasicRedstoneTileEnt keeps the signal it last saw on a neighbour change; saving never reads the world. */
    protected boolean hasSignal;
    public void onNeighborBlockChange(){if(level!=null&&!level.isClientSide){boolean signal=level.hasNeighborSignal(worldPosition);if(signal!=hasSignal){hasSignal=signal;needUpdate();}}}
    public boolean isRedstoneEnabled(){return redstoneBehaviour==0||hasSignal&&redstoneBehaviour==1||!hasSignal&&redstoneBehaviour==2;}
    public byte getPvpSetting(){return pvpsetting;}

    public void buttonClicked(int id,Player player){
        if(id==BUTTON_ID_SECURITY){if(isOwnedByPlayer(player)){security=(byte)(security==0?3:0);needUpdate();}}
        else if(id==BUTTON_ID_REDSTONE){if(isUseableByPlayer(player)){redstoneBehaviour=(byte)((redstoneBehaviour+1)%3);needUpdate();}}
        else if(id==BUTTON_ID_TARGET_ANIMALS&&isUseableByPlayer(player)){attackAnimals=!attackAnimals;needUpdate();}
        else if(id==BUTTON_ID_PVP_SETTING&&isOwnedByPlayer(player)){pvpsetting=(byte)(pvpsetting==0?4:0);needUpdate();}
    }

    // --- Turret logic -------------------------------------------------------------------------------------------
    public Direction getFacing(){return facing;}
    public void setFacing(Direction facing){this.facing=facing;setChanged();}
    public ItemStack getWeapon(){return inventory.getStackInSlot(SLOT_WEAPON);}

    public boolean consumeAmmo(){
        ItemStack gunstack=getWeapon();
        if(!gunstack.isEmpty()&&gunstack.getItem() instanceof GenericGun gun){
            if(gun.getCurrentAmmo(gunstack)<=0)doReload(gunstack);
            if(gun.getCurrentAmmo(gunstack)>=1){gun.useAmmo(gunstack,1);return true;}
        }
        return false;
    }

    protected void doReload(ItemStack gunstack){
        GenericGun gun=(GenericGun)gunstack.getItem();
        ItemStack[] ammo=gun.getAmmoType().getAmmo(gun.getCurrentAmmoVariant(gunstack));
        ItemStack[] emptyMag=gun.getAmmoType().getEmptyMag();
        boolean canConsume=true;
        for(int i=0;i<emptyMag.length;i++)if(TurretInventory.canConsumeItem(inventory,ammo[i],SLOT_INPUT1,SLOT_INPUT1+INPUTS_SIZE)>0){canConsume=false;break;}
        if(!canConsume)return;
        for(int i=0;i<emptyMag.length;i++){
            if(TurretInventory.consumeAmmo(inventory,ammo[i],SLOT_INPUT1,SLOT_INPUT1+INPUTS_SIZE)){
                if(!emptyMag[i].isEmpty()){
                    int tooMuch=TurretInventory.addItemToInventory(inventory,techguns.util.Compat.copyWithCount(emptyMag[i], 1),SLOT_OUTPUT1,SLOT_OUTPUT1+OUTPUTS_SIZE);
                    // Original: whatever does not fit into the outputs is dropped above the base.
                    if(tooMuch>0&&level!=null&&!level.isClientSide)level.addFreshEntity(new ItemEntity(level,worldPosition.getX(),worldPosition.getY()+1,worldPosition.getZ(),techguns.util.Compat.copyWithCount(emptyMag[i], tooMuch)));
                }
                if(gun.getAmmoCount()>1){
                    int j=1;
                    while(j<gun.getAmmoCount()&&TurretInventory.consumeAmmo(inventory,ammo[i],SLOT_INPUT1,SLOT_INPUT1+INPUTS_SIZE))j++;
                    if(i==0)gun.setAmmo(gunstack,gun.getCurrentAmmo(gunstack)+j);
                }else if(i==0)gun.setAmmo(gunstack,gun.getCurrentAmmo(gunstack)+gun.clipsize);
                needUpdate();
            }
        }
    }

    public int getRepairTimeMax(){return 10*20;}
    public int getRepairPowerAmount(){return 50;}
    public int getOperationPowerAmount(){return 5;}
    public int getPowerAmountHealTick(){return 500;}
    public int getHealTickDelay(){return 20*3;}

    public void setMountedTurret(NPCTurret turret){mountedTurret=turret;}

    public int getTurretArmorValue(){
        ItemStack armor=inventory.getStackInSlot(SLOT_ARMOR);
        if(armor.isEmpty())return 0;
        if(armor.is(TGItems.TURRET_ARMOR_IRON.get()))return 5;
        if(armor.is(TGItems.TURRET_ARMOR_STEEL.get()))return 10;
        if(armor.is(TGItems.TURRET_ARMOR_OBSIDIAN_STEEL.get()))return 15;
        if(armor.is(TGItems.TURRET_ARMOR_CARBON.get()))return 20;
        return 0;
    }
    public int getTurretTier(){
        ItemStack armor=inventory.getStackInSlot(SLOT_ARMOR);
        if(armor.is(TGItems.TURRET_ARMOR_IRON.get()))return 1;
        if(armor.is(TGItems.TURRET_ARMOR_STEEL.get()))return 2;
        if(armor.is(TGItems.TURRET_ARMOR_OBSIDIAN_STEEL.get()))return 3;
        if(armor.is(TGItems.TURRET_ARMOR_CARBON.get()))return 4;
        return 0;
    }
    public ResourceLocation getTurretBaseTexture(){return NPCTurret.getTexture(getTurretTier());}

    protected boolean consumePower(int amount){
        if(TGConfig.machinesNeedNoPower)return true;
        if(energy.extractEnergy(amount,true)>=amount)return energy.extractEnergy(amount,false)>=amount;
        return false;
    }

    /** Called from the turret entity when it dies. */
    public void onTurretDeath(){
        if(level!=null&&!level.isClientSide){mountedTurret=null;turretDeath=true;repairTime=getRepairTimeMax();needUpdate();}
    }

    public void updateWeapon(){
        if(mountedTurret!=null){mountedTurret.setCombatTask();if(level!=null&&!level.isClientSide)needUpdate();}
        else if(level!=null&&!level.isClientSide)needUpdate();
    }

    /** Wrench rotation: only up/down mounting, re-spawning the turret with its health. */
    public void rotateTile(Direction sideHit){
        if(facing!=sideHit&&(sideHit==Direction.UP||sideHit==Direction.DOWN)){
            facing=sideHit;
            if(mountedTurret!=null){float health=mountedTurret.getHealth();mountedTurret.setHealth(0);mountedTurret.discard();spawnTurret(level,worldPosition,health);}
            if(level!=null&&!level.isClientSide)needUpdate();
        }
    }

    public static void serverTick(Level level,BlockPos pos,BlockState state,TurretTileEnt tile){tile.tickServer();}
    public static void clientTick(Level level,BlockPos pos,BlockState state,TurretTileEnt tile){tile.tickClient();}

    public void tickServer(){
        if(turretDeath){
            if(isRedstoneEnabled()){
                if(consumePower(getRepairPowerAmount()))repairTime--;
                if(repairTime<=0){repairTime=0;turretDeath=false;spawnTurret(level,worldPosition,10.0f);needUpdate();}
            }
        }else if(mountedTurret!=null&&mountedTurret.isRemoved()){
            onTurretDeath();
        }else if(mountedTurret!=null){
            if(isRedstoneEnabled()){
                if(!consumePower(getOperationPowerAmount())){
                    if(mountedTurret.active){mountedTurret.disable();mountedTurret.active=false;}
                }else if(!mountedTurret.active){
                    mountedTurret.active=true;mountedTurret.setAITasks();mountedTurret.setCombatTask();
                }
                if(turretHealTime<=0){
                    if(mountedTurret.getHealth()<mountedTurret.getMaxHealth()&&consumePower(getPowerAmountHealTick())){mountedTurret.heal(1.0f);turretHealTime=getHealTickDelay();}
                }else turretHealTime--;
            }else if(mountedTurret.active){mountedTurret.disable();mountedTurret.active=false;}
        }
    }

    void tickClient(){
        if(clientTurretId>=0&&(mountedTurret==null||mountedTurret.getId()!=clientTurretId)&&level.getEntity(clientTurretId) instanceof NPCTurret turret)mountedTurret=turret;
    }

    public void setTurretPosition(){
        double x=worldPosition.getX()+.5d,y=worldPosition.getY()+.5d,z=worldPosition.getZ()+.5d;
        switch(facing){
            case DOWN->{y-=.5d;y-=mountedTurret.getBbHeight();}
            case EAST->x+=.5d;
            case NORTH->z-=.5d;
            case SOUTH->z+=.5d;
            case UP->y+=.5d;
            case WEST->x-=.5d;
        }
        mountedTurret.setPos(x,y,z);mountedTurret.xo=x;mountedTurret.yo=y;mountedTurret.zo=z;
    }

    public void spawnTurret(Level w,BlockPos p,float health){spawn(w,health);}
    public void spawnTurret(Level w,BlockPos p){spawn(w,-1);}
    private void spawn(Level w,float health){
        if(w==null||w.isClientSide)return;
        mountedTurret=new NPCTurret(w,this);
        mountedTurret.setTurretFacing(facing);
        setTurretPosition();
        if(health>=0)mountedTurret.setHealth(health);
        turretDeath=false;
        w.addFreshEntity(mountedTurret);
        needUpdate();
    }

    /** Block broken or replaced: drop the inventory and remove the mounted turret without a death callback. */
    public void onBlockBreak(){
        if(level!=null&&!level.isClientSide)for(int i=0;i<inventory.getSlots();i++){Containers.dropItemStack(level,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),inventory.getStackInSlot(i));inventory.setStackInSlot(i,ItemStack.EMPTY);}
        breakTurret();
    }
    public void breakTurret(){if(mountedTurret!=null){mountedTurret.setHealth(0);mountedTurret.discard();}}
    public void emptyContent(){for(int i=0;i<inventory.getSlots();i++)inventory.setStackInSlot(i,ItemStack.EMPTY);}

    public void needUpdate(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}

    // --- Persistence ---------------------------------------------------------------------------------------------
    /** The original "client data": everything except the inventory (which BasicInventoryTileEnt stores separately). */
    public void writeClientData(CompoundTag tags,boolean forClient){
        tags.putInt("powerStored",energy.getEnergyStored());
        tags.putBoolean("hasSignal",hasSignal);
        tags.putByte("redstoneBehaviour",redstoneBehaviour);
        if(owner!=null){tags.putByte("security",security);tags.putString("owner",owner.toString());}
        if(forClient&&mountedTurret!=null)tags.putInt("mountedTurret",mountedTurret.getId());
        tags.putByte("facing",(byte)facing.get3DDataValue());
        tags.putBoolean("turretDeath",turretDeath);
        tags.putInt("repairTime",repairTime);
        tags.putInt("turretHealTime",turretHealTime);
        tags.putBoolean("attackAnimals",attackAnimals);
        tags.putByte("pvpsetting",pvpsetting);
    }
    public void readClientData(CompoundTag tags){
        setEnergy(tags.getInt("powerStored"));
        redstoneBehaviour=tags.getByte("redstoneBehaviour");hasSignal=tags.getBoolean("hasSignal");
        security=tags.getByte("security");
        String own=tags.getString("owner");
        if(!own.isEmpty())try{owner=UUID.fromString(own);}catch(IllegalArgumentException e){owner=null;}
        clientTurretId=tags.contains("mountedTurret")?tags.getInt("mountedTurret"):-1;
        facing=Direction.from3DDataValue(tags.getByte("facing"));
        turretDeath=tags.getBoolean("turretDeath");
        repairTime=tags.getInt("repairTime");
        turretHealTime=tags.getInt("turretHealTime");
        attackAnimals=tags.getBoolean("attackAnimals");
        pvpsetting=tags.getByte("pvpsetting");
    }
    /** Original wrench dismantling tag: inventory plus client data, without the owner. */
    public CompoundTag writeNBTforDismantling(){var tag=new CompoundTag();tag.put("inventory",inventory.serializeNBT());writeClientData(tag,false);tag.remove("owner");return tag;}
    public void readNBTfromStackTag(CompoundTag tag){inventory.deserializeNBT(tag.getCompound("inventory"));UUID placer=owner;readClientData(tag);if(placer!=null&&!tag.contains("owner"))owner=placer;}

    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.put("inventory",inventory.serializeNBT());writeClientData(tag,false);}
    @Override public void load(CompoundTag tag){super.load(tag);inventory.deserializeNBT(tag.getCompound("inventory"));readClientData(tag);if(level!=null&&level.isClientSide)tickClient();}
    @Override public CompoundTag getUpdateTag(){var tag=new CompoundTag();tag.put("inventory",inventory.serializeNBT());writeClientData(tag,true);return tag;}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}

    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side){
        if(capability==ForgeCapabilities.ITEM_HANDLER)return automation.cast();
        if(capability==ForgeCapabilities.ENERGY)return energyHandle.cast();
        return super.getCapability(capability,side);
    }
    @Override public void invalidateCaps(){super.invalidateCaps();automation.invalidate();energyHandle.invalidate();}
    @Override public void reviveCaps(){super.reviveCaps();automation=LazyOptional.of(this::createAutomation);energyHandle=LazyOptional.of(()->energy);}
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(){return new net.minecraft.world.phys.AABB(worldPosition).inflate(1);}
}
