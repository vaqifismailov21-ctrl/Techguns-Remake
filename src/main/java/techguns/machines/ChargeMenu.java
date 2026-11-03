package techguns.machines;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import techguns.TGMenuTypes;
import techguns.tileentities.MiningChargeEntity;
public final class ChargeMenu extends AbstractContainerMenu {
    public final MiningChargeEntity charge;public final ContainerData data;
    public ChargeMenu(int id,Inventory inventory,FriendlyByteBuf buffer){this(id,inventory,(MiningChargeEntity)inventory.player.level.getBlockEntity(buffer.readBlockPos()));}
    public ChargeMenu(int id,Inventory inventory,MiningChargeEntity charge){super(TGMenuTypes.CHARGE.get(),id);this.charge=charge;data=inventory.player.level.isClientSide?new SimpleContainerData(5):new ContainerData(){public int getCount(){return 5;}public int get(int i){return switch(i){case 0->charge.diameter;case 1->charge.length;case 2->charge.fuse;case 3->charge.armed?1:0;default->charge.access.restricted()?1:0;};}public void set(int i,int value){}};addDataSlots(data);}
    @Override public boolean stillValid(Player p){return charge!=null&&charge.getLevel().getBlockEntity(charge.getBlockPos())==charge&&p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(charge.getBlockPos()))<=64&&charge.access.allows(p);}
    @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
    @Override public boolean clickMenuButton(Player p,int id){if(!stillValid(p)||id<0||id>7)return false;return p.level.isClientSide||charge.button(p,id);}
}
