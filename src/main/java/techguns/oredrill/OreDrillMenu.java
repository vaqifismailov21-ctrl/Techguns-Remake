package techguns.oredrill;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.SlotItemHandler;
import techguns.TGMenuTypes;
import techguns.api.tginventory.ITGSpecialSlot;
import techguns.api.tginventory.TGSlotType;

/** Original OreDrillContainer: drill slot (30,17), furnace fuel slot (30,51), 3x3 outputs at (102,17). */
public final class OreDrillMenu extends AbstractContainerMenu {
    public static final int SLOT_DRILL_X=30, SLOTS_ROW1_Y=17, SLOTS_FURNACE_Y=51, SLOTS_OUTPUT_X=102;
    public final OreDrillTileEntMaster tile;
    public OreDrillMenu(int id,Inventory inventory,FriendlyByteBuf buffer){this(id,inventory,require(inventory,buffer));}
    private static OreDrillTileEntMaster require(Inventory inventory,FriendlyByteBuf buffer){
        if(inventory.player.level.getBlockEntity(buffer.readBlockPos()) instanceof OreDrillTileEntMaster tile)return tile;
        throw new IllegalArgumentException("Ore drill menu requires the actual controller");
    }
    public OreDrillMenu(int id,Inventory inventory,OreDrillTileEntMaster tile){
        super(TGMenuTypes.ORE_DRILL.get(),id);this.tile=tile;
        var handler=tile.inventory;
        addSlot(new SlotItemHandler(handler,OreDrillTileEntMaster.SLOT_DRILL,SLOT_DRILL_X,SLOTS_ROW1_Y){
            @Override public boolean mayPlace(ItemStack item){return isValidDrill(tile,item);}
            @Override public int getMaxStackSize(){return 1;}
            @Override public int getMaxStackSize(ItemStack stack){return 1;}
        });
        addSlot(new SlotItemHandler(handler,OreDrillTileEntMaster.SLOT_FURNACE,SLOT_DRILL_X,SLOTS_FURNACE_Y){
            @Override public boolean mayPlace(ItemStack item){return ForgeHooks.getBurnTime(item,null)>0;}
        });
        for(int i=0;i<3;i++)for(int j=0;j<3;j++)addSlot(new SlotItemHandler(handler,OreDrillTileEntMaster.SLOT_OUTPUT1+i*3+j,SLOTS_OUTPUT_X+18*i,SLOTS_ROW1_Y+18*j){
            @Override public boolean mayPlace(ItemStack item){return false;}
        });
        for(int row=0;row<3;row++)for(int column=0;column<9;column++)addSlot(new Slot(inventory,column+row*9+9,8+column*18,84+row*18));
        for(int column=0;column<9;column++)addSlot(new Slot(inventory,column,8+column*18,142));
        addDataSlots(tile.data);
    }
    /** Original SlotDrill: only on a formed drill, and only the head size matching the drill radius. */
    public static boolean isValidDrill(OreDrillTileEntMaster tile,ItemStack item){
        if(!tile.isFormed())return false;
        if(!item.isEmpty()&&item.getItem() instanceof ITGSpecialSlot special){
            int radius=tile.getDrillRadius();var type=special.getSlot(item);
            return type==TGSlotType.DRILL_SMALL&&radius==0||type==TGSlotType.DRILL_MEDIUM&&radius>0&&radius<3||type==TGSlotType.DRILL_LARGE&&radius>=3;
        }
        return false;
    }
    @Override public boolean stillValid(Player player){return tile.isUseableByPlayer(player);}
    @Override public boolean clickMenuButton(Player player,int id){
        if((id!=OreDrillTileEntMaster.BUTTON_ID_SECURITY&&id!=OreDrillTileEntMaster.BUTTON_ID_REDSTONE)||!stillValid(player))return false;
        if(!player.level.isClientSide)tile.buttonClicked(id,player);
        return true;
    }
    /** Original transferStackInSlot: the drill never shift-clicks out; outputs/fuel go to the player; player items to their valid slot. */
    @Override public ItemStack quickMoveStack(Player player,int index){
        ItemStack stack=ItemStack.EMPTY;Slot slot=slots.get(index);
        if(!slot.hasItem())return stack;
        ItemStack stack1=slot.getItem();stack=stack1.copy();
        int end=OreDrillTileEntMaster.SLOT_OUTPUT1+OreDrillTileEntMaster.SLOT_OUTPUT_COUNT;
        if(index==OreDrillTileEntMaster.SLOT_DRILL)return ItemStack.EMPTY;
        else if(index<end){if(!moveItemStackTo(stack1,end,end+36,false))return ItemStack.EMPTY;slot.onQuickCraft(stack1,stack);}
        else{
            int valid=tile.getValidSlotForItemInMachine(stack1);
            if(valid<0||!moveItemStackTo(stack1,valid,valid+1,false))return ItemStack.EMPTY;
            slot.onQuickCraft(stack1,stack);
        }
        if(stack1.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();
        if(stack1.getCount()==stack.getCount())return ItemStack.EMPTY;
        slot.onTake(player,stack1);
        return stack;
    }
}
