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
/** Adapted from original StormTrooper; equipment branches and damage values are retained. */
public class StormTrooper extends GenericNPCGearSpecificStats {
public StormTrooper(EntityType<? extends GenericNPC> type,Level level){super(type,level);}
@Override protected boolean hears(){return true;}
public static final ResourceLocation LOOT=ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"entities/stormtrooper");
protected void addRandomArmor(int difficulty) {

		
		
		int camo = 1;
		
		this.setItemSlot(EquipmentSlot.HEAD, camo(
			TGEquipment.ARMOR.get("t3_miner_helmet").get(),camo));
		this.setItemSlot(EquipmentSlot.CHEST, camo(
				TGEquipment.ARMOR.get("t3_miner_chestplate").get(),camo));
		this.setItemSlot(EquipmentSlot.LEGS, camo(
				TGEquipment.ARMOR.get("t3_miner_leggings").get(),camo));
		this.setItemSlot(EquipmentSlot.FEET, camo(
				TGEquipment.ARMOR.get("t3_miner_boots").get(),camo));
		
		
		Random r = new Random(random.nextLong());
		Item weapon = null;
		switch (r.nextInt(2)) {
		case 0:
			weapon = TGuns.blasterrifle.get();
			break;
		default:
			weapon = TGuns.lasergun.get();
			break;
		}
		if (weapon != null) this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
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
public static AttributeSupplier.Builder attributes(){return GenericNPCGearSpecificStats.attributes().add(Attributes.MOVEMENT_SPEED,0.30D).add(Attributes.MAX_HEALTH,35).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,75.0D);}
@Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state){playSound(getStepSound(),.15f,1);}
}
