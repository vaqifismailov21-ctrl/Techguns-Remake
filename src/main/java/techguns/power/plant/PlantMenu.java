package techguns.power.plant;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

/** Power plant GUI: energy, output, state and (reactor) fuel slots, water and heat. Values sync as 16-bit halves. */
public final class PlantMenu extends AbstractContainerMenu {
    public static final int FUEL_X=152,FUEL_Y=18,SPENT_Y=56;
    private static final int DATA=14;
    public final PlantKind kind;
    public final PlantController plant;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final boolean slotted;
    public record Provider(PlantController plant) implements MenuProvider {
        @Override public Component getDisplayName(){return plant.getBlockState().getBlock().getName();}
        @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new PlantMenu(id,inventory,plant);}
    }
    public PlantMenu(int id,Inventory inventory,FriendlyByteBuf buffer){this(id,inventory,inventory.player.level().getBlockEntity(buffer.readBlockPos()));}
    private PlantMenu(int id,Inventory inventory,BlockEntity entity){
        super(PlantBlocks.MENU.get(),id);
        plant=entity instanceof PlantController c?c:null;kind=plant==null?PlantKind.WIND:plant.kind;
        access=plant==null||plant.getLevel()==null?ContainerLevelAccess.NULL:ContainerLevelAccess.create(plant.getLevel(),plant.getBlockPos());
        var slots=plant==null?null:plant.slots();slotted=slots!=null;
        if(slotted){addSlot(new SlotItemHandler(slots,0,FUEL_X,FUEL_Y));addSlot(new SlotItemHandler(slots,1,FUEL_X,SPENT_Y){@Override public boolean mayPlace(ItemStack stack){return false;}});}
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,col+row*9+9,8+col*18,84+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,8+col*18,142));
        data=plant!=null&&!plant.getLevel().isClientSide?server(plant):new SimpleContainerData(DATA);
        addDataSlots(data);
    }
    private static ContainerData server(PlantController p){
        return new ContainerData(){
            /** 0-1 energy, 2-3 capacity, 4-5 output, 6 state, 8-9 water, 10 heat, 11 kind value, 12-13 tank size. */
            @Override public int get(int i){
                long v=switch(i){case 0,1->p.energyStored();case 2,3->p.energyCapacity();case 4,5->p.rate;case 6->p.problem|(p.formed?0x100:0);
                    case 8,9->p.value(0);case 10->p.value(2);case 11->p.value(3);case 12,13->p.value(1);default->0;};
                boolean high=i==1||i==3||i==5||i==9||i==13;
                return (int)((high?v>>>16:v)&0xFFFF);
            }
            @Override public void set(int i,int v){}
            @Override public int getCount(){return DATA;}
        };
    }
    private int whole(int i){return (data.get(i)&0xFFFF)|(data.get(i+1)&0xFFFF)<<16;}
    public int energy(){return whole(0);}
    public int capacity(){return Math.max(1,whole(2));}
    public int rate(){return whole(4);}
    public int problem(){return data.get(6)&0xFF;}
    public boolean formed(){return (data.get(6)&0x100)!=0;}
    public int water(){return whole(8);}
    public int heat(){return data.get(10)&0xFFFF;}
    public int extra(){return data.get(11)&0xFFFF;}
    public int tank(){return whole(12);}
    public boolean slotted(){return slotted;}
    @Override public boolean stillValid(Player player){return stillValid(access,player,plant==null?PlantBlocks.WIND.get():plant.getBlockState().getBlock());}
    @Override public ItemStack quickMoveStack(Player player,int index){
        var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();int own=slotted?2:0;
        if(index<own){if(!moveItemStackTo(stack,own,own+36,true))return ItemStack.EMPTY;}
        else if(slotted&&slots.get(0).mayPlace(stack)){if(!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;}
        else return ItemStack.EMPTY;
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();return copy;
    }
}
