package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/** Fuel slot or fluid container slot, energy, burn progress and (liquid generator) tank. Values sync as 16-bit halves. */
public final class GeneratorMenu extends AbstractContainerMenu {
    public static final int SLOT_X=18,SLOT_Y=17;
    private static final int DATA=8;
    public final boolean liquid;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final net.minecraft.world.level.block.Block block;

    public record Provider(EnergySource source) implements MenuProvider {
        @Override public Component getDisplayName(){return source.getBlockState().getBlock().getName();}
        @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new GeneratorMenu(id,inventory,source);}
    }

    /** Client side. */
    public GeneratorMenu(int id,Inventory inventory,FriendlyByteBuf buffer){this(id,inventory,inventory.player.level().getBlockEntity(buffer.readBlockPos()));}
    private GeneratorMenu(int id,Inventory inventory,net.minecraft.world.level.block.entity.BlockEntity entity){
        super(PowerBlocks.GENERATOR_MENU.get(),id);
        liquid=entity instanceof LiquidGeneratorEntity;
        block=entity==null?PowerBlocks.FUEL_GENERATOR.get():entity.getBlockState().getBlock();
        access=entity==null||entity.getLevel()==null?ContainerLevelAccess.NULL:ContainerLevelAccess.create(entity.getLevel(),entity.getBlockPos());
        ItemStackHandler handler=entity instanceof FuelGeneratorEntity fuel?fuel.fuel:entity instanceof LiquidGeneratorEntity tank?tank.container:new ItemStackHandler(1);
        addSlot(new SlotItemHandler(handler,0,SLOT_X,SLOT_Y));
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,col+row*9+9,8+col*18,84+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,8+col*18,142));
        data=entity instanceof EnergySource source&&!entity.getLevel().isClientSide?serverData(source):new SimpleContainerData(DATA);
        addDataSlots(data);
    }
    private static ContainerData serverData(EnergySource source){
        return new ContainerData(){
            @Override public int get(int index){
                int energy=source.energyStored(),capacity=source.energyCapacity();
                return switch(index){
                    case 0->energy&0xFFFF;case 1->energy>>>16;case 2->capacity&0xFFFF;case 3->capacity>>>16;
                    case 4->source instanceof FuelGeneratorEntity fuel&&fuel.burnTotal>0?fuel.burnTime*1000/fuel.burnTotal:source instanceof LiquidGeneratorEntity liquid&&liquid.burnEnergy>0?1000:0;
                    case 5->source instanceof LiquidGeneratorEntity liquid?liquid.tank.getFluidAmount():0;
                    case 6->source instanceof LiquidGeneratorEntity liquid&&!liquid.tank.isEmpty()?BuiltInRegistries.FLUID.getId(liquid.tank.getFluid().getFluid()):0;
                    default->source instanceof LiquidGeneratorEntity?PowerBlocks.LIQUID_RATE:PowerBlocks.GENERATOR_RATE;
                };
            }
            @Override public void set(int index,int value){}
            @Override public int getCount(){return DATA;}
        };
    }
    private int part(int index){return data.get(index)&0xFFFF;}
    public int energy(){return part(0)|part(1)<<16;}
    public int capacity(){return part(2)|part(3)<<16;}
    /** Remaining burn of the current fuel, 0..1000. */
    public int burn(){return part(4);}
    public int tankAmount(){return part(5);}
    public Fluid tankFluid(){return BuiltInRegistries.FLUID.byId(part(6));}
    public int rate(){return part(7);}
    @Override public boolean stillValid(Player player){return stillValid(access,player,block);}
    @Override public ItemStack quickMoveStack(Player player,int index){
        var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();
        if(index==0){if(!moveItemStackTo(stack,1,37,true))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();
        return copy;
    }
}
