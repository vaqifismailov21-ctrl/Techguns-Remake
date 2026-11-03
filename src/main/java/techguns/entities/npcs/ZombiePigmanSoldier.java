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
/** Adapted from original ZombiePigmanSoldier; equipment branches and damage values are retained. */
public class ZombiePigmanSoldier extends GenericNPCUndead {
public ZombiePigmanSoldier(EntityType<? extends GenericNPC> type,Level level){super(type,level);setTGArmorStats(10.0f, 0f);}
public static final ResourceLocation LOOT=ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"entities/zombiepigmansoldier");
protected void addRandomArmor(int difficulty) {

		
		double chance = 0.5;
		int camo=3;
		if (random.nextDouble() <= chance)
			camo(TGEquipment.ARMOR.get("t2_combat_helmet").get(), camo);
			this.setItemSlot(EquipmentSlot.HEAD,
					camo(TGEquipment.ARMOR.get("t2_combat_helmet").get(), camo));
		if (random.nextDouble() <= chance)
			this.setItemSlot(EquipmentSlot.CHEST, 
					camo(TGEquipment.ARMOR.get("t2_combat_chestplate").get(), camo));
		if (random.nextDouble() <= chance)
			this.setItemSlot(EquipmentSlot.LEGS, camo(TGEquipment.ARMOR.get("t2_combat_leggings").get(), camo));
		if (random.nextDouble() <= chance)
			this.setItemSlot(EquipmentSlot.FEET, camo(TGEquipment.ARMOR.get("t2_combat_boots").get(), camo));

		
		Random r = new Random(random.nextLong());
		Item weapon = null;
		int bound = 9;
		
		switch (r.nextInt(bound)) {
		case 0:
		case 1:
		case 2:
			weapon = TGuns.thompson.get();
			break;
		case 3:
		case 4:
			weapon = TGuns.revolver.get();
			break;
		case 5:
		case 6:
			weapon = TGuns.ak47.get();
			break;
		case 7:
		case 8:
			weapon = TGuns.pistol.get();
			break;
		case 9:
		case 10:
			weapon = TGuns.flamethrower.get();
			break;
		case 11:
		case 12:
			weapon = TGuns.combatshotgun.get();
			break;
		case 13:
			weapon = TGuns.lmg.get();
			break;
		case 14:
		default:
			weapon = TGuns.rocketlauncher.get();
			break;
		}
		if (weapon != null) this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
	}
public SoundEvent getAmbientSound() {
		return SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT;
	}
public SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ZOMBIFIED_PIGLIN_HURT;
	}
public SoundEvent getDeathSound() {
		return SoundEvents.ZOMBIFIED_PIGLIN_DEATH;
	}
public SoundEvent getStepSound() {
		return SoundEvents.ZOMBIE_STEP;
	}
protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable() {
		return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, LOOT);
	}
protected boolean shouldBurnInDay() {
		return false;
	}
public static AttributeSupplier.Builder attributes(){return GenericNPCUndead.attributes().add(Attributes.MOVEMENT_SPEED,0.30D).add(Attributes.MAX_HEALTH,25).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.FOLLOW_RANGE,50.0D);}
@Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state){playSound(getStepSound(),.15f,1);}
}
