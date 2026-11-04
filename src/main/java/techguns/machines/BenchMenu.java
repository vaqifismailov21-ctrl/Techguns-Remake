package techguns.machines;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.items.SlotItemHandler;
import techguns.*;
import techguns.capabilities.TGExtendedPlayer;
import techguns.gui.widgets.SlotTG;
import techguns.api.tginventory.TGSlotType;
import techguns.items.armors.ICamoChangeable;
public final class BenchMenu extends AbstractContainerMenu {
    public final BenchEntity bench;public final Player player;
    public final ResultContainer result=new ResultContainer();
    private final DataSlot xp=DataSlot.standalone();
    private int playerStart;
    private static final EquipmentSlot[] ARMOR={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    public BenchMenu(int id,Inventory inventory,FriendlyByteBuf buffer){this(id,inventory,require(inventory,buffer));}
    private static BenchEntity require(Inventory inventory,FriendlyByteBuf buffer){var entity=inventory.player.level().getBlockEntity(buffer.readBlockPos());if(!(entity instanceof BenchEntity bench))throw new IllegalArgumentException("Workbench menu requires its block entity");return bench;}
    public BenchMenu(int id,Inventory inventory,BenchEntity bench){
        super(TGMenuTypes.BENCH.get(),id);this.bench=bench;player=inventory.player;
        switch(bench.kind){
            case CAMO->input(0,17,18);
            case REPAIR->{for(int i=0;i<9;i++)input(i,8+i*18,57);input(9,8,18);}
            case UPGRADE->{input(0,17,47);input(1,53,47);addSlot(new Slot(result,0,116,47){@Override public boolean mayPlace(ItemStack stack){return false;}@Override public boolean mayPickup(Player p){var preview=ArmorUpgrades.result(bench.inventory.getStackInSlot(0),bench.inventory.getStackInSlot(1));return !preview.stack().isEmpty()&&ItemStack.isSameItemSameTags(getItem(),preview.stack())&&(p.getAbilities().instabuild||p.totalExperience>=preview.xp());}@Override public void onTake(Player p,ItemStack output){var preview=ArmorUpgrades.result(bench.inventory.getStackInSlot(0),bench.inventory.getStackInSlot(1));if(!preview.stack().isEmpty()){bench.inventory.extractItem(0,1,false);bench.inventory.extractItem(1,1,false);if(!p.getAbilities().instabuild)p.giveExperiencePoints(-preview.xp());bench.getLevel().playSound(null,bench.getBlockPos(),net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_DIAMOND,net.minecraft.sounds.SoundSource.PLAYERS,1,1);}super.onTake(p,output);}});}
        }
        playerStart=slots.size();for(int row=0;row<3;row++)for(int column=0;column<9;column++)addSlot(new Slot(inventory,column+row*9+9,8+column*18,84+row*18));for(int column=0;column<9;column++)addSlot(new Slot(inventory,column,8+column*18,142));
        for(int i=0;i<4;i++){var equipmentSlot=ARMOR[i];int x=bench.kind==BenchKind.CAMO?99+i*18:bench.kind==BenchKind.REPAIR?89+i*20:17+i*18;addSlot(new Slot(inventory,39-i,x,18){@Override public int getMaxStackSize(){return 1;}@Override public boolean mayPlace(ItemStack stack){return stack.canEquip(equipmentSlot,player);}@Override public boolean mayPickup(Player p){return p.isCreative()||!EnchantmentHelper.hasBindingCurse(getItem());}});}
        if(bench.kind==BenchKind.CAMO){var extra=TGExtendedPlayer.get(player).tg_inventory;addSlot(new SlotTG(extra,0,44,18,TGSlotType.FACESLOT));addSlot(new SlotTG(extra,1,62,18,TGSlotType.BACKSLOT));addSlot(new SlotTG(extra,2,80,18,TGSlotType.HANDSLOT));}
        if(bench.kind!=BenchKind.UPGRADE)addSlot(new Slot(inventory,40,bench.kind==BenchKind.CAMO?-17:64,bench.kind==BenchKind.CAMO?94:18));addDataSlot(xp);
    }
    private void input(int index,int x,int y){addSlot(new SlotItemHandler(bench.inventory,index,x,y){@Override public boolean mayPlace(ItemStack stack){return bench.accepts(index,stack);}@Override public int getMaxStackSize(){return bench.kind==BenchKind.UPGRADE&&index==0?1:super.getMaxStackSize();}});}
    private boolean toBench(ItemStack stack){boolean moved=false;for(int i=0;i<bench.inventory.getSlots()&&!stack.isEmpty();i++)if(bench.accepts(i,stack))moved=moveItemStackTo(stack,i,i+1,false)||moved;return moved;}
    public int xpCost(){return xp.get();}
    @Override public void broadcastChanges(){if(!player.level().isClientSide&&bench.kind==BenchKind.UPGRADE){var preview=ArmorUpgrades.result(bench.inventory.getStackInSlot(0),bench.inventory.getStackInSlot(1));result.setItem(0,preview.stack());xp.set(preview.xp());}super.broadcastChanges();}
    @Override public boolean stillValid(Player p){return bench.getLevel()!=null&&bench.getLevel().getBlockEntity(bench.getBlockPos())==bench&&p.distanceToSqr(bench.getBlockPos().getCenter())<=64&&bench.access.allows(p);}
    @Override public boolean clickMenuButton(Player p,int id){if(!stillValid(p))return false;if(id==1){if(!p.level().isClientSide)return bench.access.toggle(p);return true;}if(p.level().isClientSide)return true;
        if(bench.kind==BenchKind.REPAIR){if(id>=2&&id<=5)bench.repair(p.getItemBySlot(ARMOR[id-2]));else if(id==6)bench.repair(p.getOffhandItem());else if(id==7)bench.repair(bench.inventory.getStackInSlot(9));else return false;}
        else if(bench.kind==BenchKind.CAMO){
            if(id==2||id==3)change(bench.inventory.getStackInSlot(0),id==3,stack->bench.inventory.setStackInSlot(0,stack));
            else if(id>=4&&id<=11){int index=(id-4)/2;change(p.getItemBySlot(ARMOR[index]),id%2==1,stack->p.setItemSlot(ARMOR[index],stack));}
            else if(id>=12&&id<=17){int extra=id<14?1:id<16?0:2;var inventory=TGExtendedPlayer.get(p).tg_inventory;change(inventory.getItem(extra),id%2==1,stack->{inventory.setItem(extra,stack);TGExtendedPlayer.get(p).markDirty();});}
            else return false;
        }else return false;bench.changed();broadcastChanges();return true;
    }
    private static final String[] COLORS={"white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"};
    public static void change(ItemStack stack,boolean backwards,Consumer<ItemStack> set){if(stack.getItem() instanceof ICamoChangeable camo&&camo.getCamoCount()>0){set.accept(camo.cycleStack(stack,backwards));return;}
        var id=BuiltInRegistries.ITEM.getKey(stack.getItem());if(!id.getNamespace().equals("minecraft"))return;String name=id.getPath();for(String family:new String[]{"wool","concrete","concrete_powder","terracotta","stained_glass","stained_glass_pane","carpet"})for(int i=0;i<COLORS.length;i++)if(name.equals(COLORS[i]+"_"+family)){var item=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("minecraft",COLORS[Math.floorMod(i+(backwards?-1:1),16)]+"_"+family));var output=new ItemStack(item,stack.getCount());if(stack.hasTag())output.setTag(stack.getTag().copy());set.accept(output);return;}}
    @Override public ItemStack quickMoveStack(Player p,int index){if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;var stack=slot.getItem();var original=stack.copy();
        if(index<playerStart){if(!moveItemStackTo(stack,playerStart,playerStart+36,false))return ItemStack.EMPTY;}
        else if(index>=playerStart+36){
            // worn armor / Techguns / offhand slots: into the bench first (camo or upgrade input), else the player inventory
            if(!toBench(stack)&&!moveItemStackTo(stack,playerStart,playerStart+36,false))return ItemStack.EMPTY;
        }
        else if(!toBench(stack)){
            // nothing in the bench takes it: shift-click equips armor into its free armor slot
            int armor=stack.getItem() instanceof net.minecraft.world.item.ArmorItem a?java.util.Arrays.asList(ARMOR).indexOf(a.getEquipmentSlot()):-1;
            if(armor<0||!moveItemStackTo(stack,playerStart+36+armor,playerStart+37+armor,false))return ItemStack.EMPTY;
        }
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,original);broadcastChanges();return original;
    }
}
