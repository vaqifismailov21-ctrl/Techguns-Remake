package techguns.machines;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import techguns.TGMenuTypes;
import techguns.tileentities.LargeDoorEntity;
public final class DoorMenu extends AbstractContainerMenu {
    public final LargeDoorEntity door;public final ContainerData data;
    public DoorMenu(int id,Inventory inventory,FriendlyByteBuf buffer){this(id,inventory,(LargeDoorEntity)inventory.player.level().getBlockEntity(buffer.readBlockPos()));}
    public DoorMenu(int id,Inventory inventory,LargeDoorEntity door){super(TGMenuTypes.DOOR.get(),id);this.door=door;
        data=inventory.player.level().isClientSide?new SimpleContainerData(4):new ContainerData(){public int getCount(){return 4;}public int get(int i){return switch(i){case 0->door.mode;case 1->door.autoClose?1:0;case 2->door.redstone;default->door.access.restricted()?1:0;};}public void set(int i,int value){}};addDataSlots(data);
    }
    @Override public ItemStack quickMoveStack(Player player,int index){return ItemStack.EMPTY;}
    @Override public boolean stillValid(Player player){return door!=null&&door.getLevel().getBlockEntity(door.getBlockPos())==door&&player.distanceToSqr(door.getBlockPos().getCenter())<=64&&door.access.allows(player);}
    @Override public boolean clickMenuButton(Player player,int id){if(!stillValid(player)||id<0||id>3)return false;if(player.level().isClientSide)return true;
        switch(id){case 0->door.mode=(door.mode+1)%3;case 1->door.autoClose=!door.autoClose;case 2->door.redstone=(door.redstone+1)%3;case 3->{return door.access.toggle(player);}}door.changed();return true;
    }
}
