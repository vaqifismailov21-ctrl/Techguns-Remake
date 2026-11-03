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
/** Adapted from original DictatorDave; equipment branches and damage values are retained. */
public class DictatorDave extends GenericNPC {
public DictatorDave(EntityType<? extends GenericNPC> type,Level level){super(type,level);setTGArmorStats(15.0f, 0f);}
public static final ResourceLocation LOOT=ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"entities/dictatordave");
protected void addRandomArmor(int difficulty) {

		
				this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TGEquipment.ARMOR.get("t2_beret").get()));
				this.setItemSlot(EquipmentSlot.CHEST,new ItemStack(TGEquipment.ARMOR.get("t1_combat_chestplate").get()));
				this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TGEquipment.ARMOR.get("t1_scout_leggings").get()));
			    this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TGEquipment.ARMOR.get("t1_combat_boots").get()));

				
				Item weapon = TGuns.goldenrevolver.get();
				
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
protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable() {
		return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, LOOT);
	}
public static AttributeSupplier.Builder attributes(){return GenericNPC.attributes().add(Attributes.MOVEMENT_SPEED,0.40D).add(Attributes.MAX_HEALTH,100).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.FOLLOW_RANGE,60.0D).add(Attributes.ARMOR_TOUGHNESS,2D);}
@Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state){playSound(getStepSound(),.15f,1);}
}
