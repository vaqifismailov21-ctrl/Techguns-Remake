package techguns.items.armors;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public interface ICamoChangeable {
	/** Flattened block variants replace the stack's item; wearable variants keep their existing NBT path. */
	public default ItemStack cycleStack(ItemStack stack,boolean backwards){switchCamo(stack,backwards);return stack;}
	public int getCamoCount();
	
	public default int switchCamo(ItemStack item){
		return this.switchCamo(item, false);
	}
	
	public default int switchCamo(ItemStack item,boolean back){
		CompoundTag tags = techguns.util.TGItemNbt.tagOrEmpty(item);
		byte camoID=0;
		if (tags.contains("camo")){
			camoID=tags.getByte("camo");
		}
		
		ICamoChangeable it = (ICamoChangeable) item.getItem();
		
		if(back){
			camoID--;
		} else {
			camoID++;
		}
		
		if (camoID>=it.getCamoCount()){
			camoID=0;
		} else if(camoID<0){
			camoID=(byte) (it.getCamoCount()-1);
		}
		
		tags.putByte("camo", camoID);
		techguns.util.TGItemNbt.set(item, tags);
		return camoID;
	}
	
	public default int getCurrentCamoIndex(ItemStack item) {
		CompoundTag tags = techguns.util.TGItemNbt.tag(item);
		byte camoID=0;
		if (tags!=null && tags.contains("camo")){
			camoID=tags.getByte("camo");
		}
		return camoID;
	};
	
	public net.minecraft.network.chat.Component getCurrentCamoName(ItemStack item);
	
	public default int getFirstItemCamoDamageValue() {
		return 0;
	}
	
	public default boolean addBlockCamoChangeRecipes() {
		return true;
	}
}
