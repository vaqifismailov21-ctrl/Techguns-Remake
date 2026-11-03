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
/** Adapted from original ZombieMiner; equipment branches and damage values are retained. */
public class ZombieMiner extends GenericNPCUndead {
public ZombieMiner(EntityType<? extends GenericNPC> type,Level level){super(type,level);setTGArmorStats(0f, 0f);}
public static final ResourceLocation LOOT=ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"entities/zombieminer");
protected void addRandomArmor(int difficulty) {

		
		double chance = 0.5;
		
		int camo = randomCamo(TGEquipment.ARMOR.get("t1_miner_chestplate").get());
		
		this.setItemSlot(EquipmentSlot.HEAD, camo(
				TGEquipment.ARMOR.get("t1_miner_helmet").get(),camo));
		if(random.nextDouble() <= chance)
			this.setItemSlot(EquipmentSlot.CHEST, camo(
					TGEquipment.ARMOR.get("t1_miner_chestplate").get(),camo));
		if (random.nextDouble() <= chance)
			this.setItemSlot(EquipmentSlot.LEGS, camo(
					TGEquipment.ARMOR.get("t1_miner_leggings").get(),camo));
		if (random.nextDouble() <= chance)
			this.setItemSlot(EquipmentSlot.FEET, camo(
					TGEquipment.ARMOR.get("t1_miner_boots").get(),camo));
		

		
		Random r = new Random(random.nextLong());
		Item weapon = null;
		switch (r.nextInt(3)) {
		case 0:
			weapon = Items.STONE_PICKAXE;
			break;
		case 1:
			weapon = Items.IRON_PICKAXE;
			break;
		case 3:
		default:
			weapon = TGuns.handcannon.get();
			break;
		}
		if (weapon != null) this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
	}
public SoundEvent getAmbientSound() {
		return SoundEvents.ZOMBIE_AMBIENT;
	}
public SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ZOMBIE_HURT;
	}
public SoundEvent getDeathSound() {
		return SoundEvents.ZOMBIE_DEATH;
	}
public SoundEvent getStepSound() {
		return SoundEvents.ZOMBIE_STEP;
	}
protected ResourceLocation getDefaultLootTable() {
		return LOOT;
	}
public static AttributeSupplier.Builder attributes(){return GenericNPCUndead.attributes().add(Attributes.MOVEMENT_SPEED,0.20D).add(Attributes.MAX_HEALTH,20).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,50.0D);}
@Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state){playSound(getStepSound(),.15f,1);}
}
