package techguns.machines;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import techguns.TGMenuTypes;
import techguns.api.tginventory.ITGSpecialSlot;
import techguns.api.tginventory.TGSlotType;
import techguns.items.guns.GenericGun;
import techguns.tileentities.TurretTileEnt;

/** Original TurretContainer: 3x3 ammunition inputs, 3x3 empty-magazine outputs, gun and armor slots, original shift-click routing. */
public final class TurretMenu extends AbstractContainerMenu {
    public final TurretTileEnt tile;
    private static Pair<ResourceLocation,ResourceLocation> icon(String name){return Pair.of(InventoryMenu.BLOCK_ATLAS,ResourceLocation.fromNamespaceAndPath("techguns","gui/emptyslots/"+name));}

    public TurretMenu(int id,Inventory inventory,FriendlyByteBuf buffer){this(id,inventory,require(inventory,buffer));}
    private static TurretTileEnt require(Inventory inventory,FriendlyByteBuf buffer){
        if(inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof TurretTileEnt tile)return tile;
        throw new IllegalArgumentException("Turret menu requires the actual block entity");
    }
    public TurretMenu(int id,Inventory inventory,TurretTileEnt tile){
        super(TGMenuTypes.TURRET.get(),id);this.tile=tile;
        var handler=tile.inventory;
        for(int i=0;i<3;i++)for(int j=0;j<3;j++)addSlot(new SlotItemHandler(handler,i*3+j,18+18*i,17+18*j){
            @Override public Pair<ResourceLocation,ResourceLocation> getNoItemIcon(){return icon("emptyslot_bullet");}
        });
        for(int i=0;i<3;i++)for(int j=0;j<3;j++)addSlot(new SlotItemHandler(handler,9+i*3+j,116+18*i,17+18*j){
            @Override public boolean mayPlace(ItemStack stack){return false;}
            @Override public Pair<ResourceLocation,ResourceLocation> getNoItemIcon(){return icon("emptyslot_bullet_empty");}
        });
        addSlot(new SlotItemHandler(handler,TurretTileEnt.SLOT_WEAPON,85,19){
            @Override public boolean mayPlace(ItemStack stack){return stack.getItem() instanceof GenericGun;}
            @Override public int getMaxStackSize(){return 1;}
            @Override public int getMaxStackSize(ItemStack stack){return 1;}
            @Override public Pair<ResourceLocation,ResourceLocation> getNoItemIcon(){return icon("emptyslot_turretgun");}
        });
        addSlot(new SlotItemHandler(handler,TurretTileEnt.SLOT_ARMOR,85,42){
            @Override public boolean mayPlace(ItemStack stack){return stack.getItem() instanceof ITGSpecialSlot special&&special.getSlot(stack)==TGSlotType.TURRETARMOR;}
            @Override public int getMaxStackSize(){return 1;}
            @Override public int getMaxStackSize(ItemStack stack){return 1;}
            @Override public Pair<ResourceLocation,ResourceLocation> getNoItemIcon(){return icon("emptyslot_turretarmor");}
        });
        for(int row=0;row<3;row++)for(int column=0;column<9;column++)addSlot(new Slot(inventory,column+row*9+9,8+column*18,84+row*18));
        for(int column=0;column<9;column++)addSlot(new Slot(inventory,column,8+column*18,142));
        addDataSlots(tile.data);
    }
    @Override public boolean stillValid(Player player){return tile.isUseableByPlayer(player);}
    @Override public boolean clickMenuButton(Player player,int id){
        if(id<TurretTileEnt.BUTTON_ID_SECURITY||id>TurretTileEnt.BUTTON_ID_PVP_SETTING||!stillValid(player))return false;
        if(!player.level().isClientSide)tile.buttonClicked(id,player);
        return true;
    }
    /**
     * Original transferStackInSlot. Note the original input range SLOT_OUTPUT1..SLOT_INPUT1+INPUTS_SIZE is empty,
     * so shift-clicking anything but a gun or turret armor from the player inventory moves nothing, as in 1.12.2.
     */
    @Override public ItemStack quickMoveStack(Player player,int index){
        ItemStack stack=ItemStack.EMPTY;
        Slot slot=slots.get(index);
        if(slot.hasItem()){
            ItemStack stack1=slot.getItem();stack=stack1.copy();
            if(index<=TurretTileEnt.SLOT_ARMOR){
                if(!moveItemStackTo(stack1,TurretTileEnt.SLOT_ARMOR+1,TurretTileEnt.SLOT_ARMOR+1+36,false))return ItemStack.EMPTY;
                slot.onQuickCraft(stack1,stack);
            }else if(stack.getItem() instanceof GenericGun){
                if(!moveItemStackTo(stack1,TurretTileEnt.SLOT_WEAPON,TurretTileEnt.SLOT_WEAPON+1,false))return ItemStack.EMPTY;
                slot.onQuickCraft(stack1,stack);
            }else if(stack.getItem() instanceof ITGSpecialSlot special&&special.getSlot(stack)==TGSlotType.TURRETARMOR){
                if(!moveItemStackTo(stack1,TurretTileEnt.SLOT_ARMOR,TurretTileEnt.SLOT_ARMOR+1,false))return ItemStack.EMPTY;
                slot.onQuickCraft(stack1,stack);
            }else{
                if(!moveItemStackTo(stack1,TurretTileEnt.SLOT_OUTPUT1,TurretTileEnt.SLOT_INPUT1+TurretTileEnt.INPUTS_SIZE,false))return ItemStack.EMPTY;
                slot.onQuickCraft(stack1,stack);
            }
            if(stack1.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
            if(stack1.getCount()==stack.getCount())return ItemStack.EMPTY;
            slot.onTake(player,stack1);
        }
        return stack;
    }
}
