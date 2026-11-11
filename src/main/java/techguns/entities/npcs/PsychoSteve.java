package techguns.entities.npcs;
import java.util.Random;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import techguns.*;
import techguns.damagesystem.TGDamageSource;
/** Adapted from original PsychoSteve; equipment branches and damage values are retained. */
public class PsychoSteve extends GenericNPC {
public PsychoSteve(EntityType<? extends GenericNPC> type,Level level){super(type,level);setTGArmorStats(5.0f, 0f);}
public static final ResourceLocation LOOT=ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"entities/psychosteve");
protected void addRandomArmor(int difficulty) {

		
		
		int camo = randomCamo(TGEquipment.ARMOR.get("t1_miner_chestplate").get());
			
			this.setItemSlot(EquipmentSlot.HEAD, camo(
				TGEquipment.ARMOR.get("t1_miner_helmet").get(),camo));
			this.setItemSlot(EquipmentSlot.CHEST, camo(
					TGEquipment.ARMOR.get("t1_miner_chestplate").get(),camo));
			this.setItemSlot(EquipmentSlot.LEGS, camo(
					TGEquipment.ARMOR.get("t1_miner_leggings").get(),camo));
			this.setItemSlot(EquipmentSlot.FEET, camo(
					TGEquipment.ARMOR.get("t1_miner_boots").get(),camo));
			
			this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TGuns.chainsaw.get()));
			this.setDropChance(EquipmentSlot.MAINHAND, 0F); // the chainsaw drops from the loot table only, never twice

	}
public SoundEvent getAmbientSound() {
		return SoundEvents.VILLAGER_AMBIENT;
	}
public SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.VILLAGER_HURT;
	}
public SoundEvent getDeathSound() {
		return SoundEvents.VILLAGER_DEATH;
	}
public SoundEvent getStepSound() {
		return SoundEvents.ZOMBIE_STEP;
	}
protected ResourceLocation getDefaultLootTable() {
		return LOOT;
	}
public static AttributeSupplier.Builder attributes(){return GenericNPC.attributes().add(Attributes.MOVEMENT_SPEED,0.60D).add(Attributes.MAX_HEALTH,75).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.FOLLOW_RANGE,60.0D).add(Attributes.ARMOR_TOUGHNESS,1D);}
@Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state){playSound(getStepSound(),.15f,1);}
}
