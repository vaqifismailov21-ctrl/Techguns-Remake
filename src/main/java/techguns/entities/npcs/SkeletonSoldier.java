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
/** Adapted from original SkeletonSoldier; equipment branches and damage values are retained. */
public class SkeletonSoldier extends GenericNPCUndead {
public SkeletonSoldier(EntityType<? extends GenericNPC> type,Level level){super(type,level);setTGArmorStats(0f, 0f);}
public static final ResourceLocation LOOT=ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"entities/skeletonsoldier");
protected void addRandomArmor(int difficulty) {

		
		
		double chance = 0.5;
		if (random.nextDouble() <= chance) {
			this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TGEquipment.ARMOR.get("t1_scout_helmet").get()));
		} else {
			this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TGEquipment.ARMOR.get("t1_combat_helmet").get()));
		};
		if (random.nextDouble() <= chance) {
			this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TGEquipment.ARMOR.get("t1_scout_boots").get()));
		} else {
			this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TGEquipment.ARMOR.get("t1_combat_boots").get()));
		};
		
		
		Random r = new Random(random.nextLong());
		Item weapon = null;
		switch (r.nextInt(3)) {
		case 0:
			weapon = TGuns.revolver.get();
			break;
		case 1:
			weapon = TGuns.thompson.get();
			break;
		case 2:
			weapon = TGuns.handcannon.get();
			break;
		default:
			weapon = Items.STONE_SHOVEL;
			break;
		}
		if (weapon != null) this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
	}
public SoundEvent getAmbientSound() {
		return SoundEvents.SKELETON_AMBIENT;
	}
public SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.SKELETON_HURT;
	}
public SoundEvent getDeathSound() {
		return SoundEvents.SKELETON_DEATH;
	}
public SoundEvent getStepSound() {
		return SoundEvents.SKELETON_STEP;
	}
protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable() {
		return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, LOOT);
	}
public float getWeaponPosX() {
		return -0.06f;
	}
public float getWeaponPosY() {
		return -0.06f;
	}
public static AttributeSupplier.Builder attributes(){return GenericNPCUndead.attributes().add(Attributes.MOVEMENT_SPEED,0.25D).add(Attributes.MAX_HEALTH,25).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,50.0D);}
@Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state){playSound(getStepSound(),.15f,1);}
}
