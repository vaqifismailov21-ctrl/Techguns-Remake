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
/** Adapted from original Commando; equipment branches and damage values are retained. */
public class Commando extends GenericNPC {
public Commando(EntityType<? extends GenericNPC> type,Level level){super(type,level);setTGArmorStats(8.0f, 0f);}
public static final ResourceLocation LOOT=new ResourceLocation(Techguns.MODID,"entities/commando");
protected void addRandomArmor(int difficulty) {

		
		this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TGEquipment.ARMOR.get("t2_commando_helmet").get()));
		this.setItemSlot(EquipmentSlot.CHEST,new ItemStack(TGEquipment.ARMOR.get("t2_commando_chestplate").get()));
		this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TGEquipment.ARMOR.get("t2_commando_leggings").get()));
	    this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TGEquipment.ARMOR.get("t2_commando_boots").get()));

		
		Item weapon = TGuns.m4_infiltrator.get();
		
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
		return SoundEvents.ZOMBIE_VILLAGER_STEP;
	}
protected ResourceLocation getDefaultLootTable() {
		return LOOT;
	}
public static AttributeSupplier.Builder attributes(){return GenericNPC.attributes().add(Attributes.MOVEMENT_SPEED,0.35D).add(Attributes.MAX_HEALTH,30).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.FOLLOW_RANGE,75.0D);}
@Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state){playSound(getStepSound(),.15f,1);}
}
