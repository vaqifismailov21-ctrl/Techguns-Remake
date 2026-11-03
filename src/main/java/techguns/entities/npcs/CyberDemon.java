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
/** Adapted from original CyberDemon; equipment branches and damage values are retained. */
public class CyberDemon extends GenericNPCUndead {
public CyberDemon(EntityType<? extends GenericNPC> type,Level level){super(type,level);setTGArmorStats(10.0f, 0f);hasAimedBowAnim=false;}
public static final ResourceLocation LOOT=ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"entities/cyberdemon");
protected void addRandomArmor(int difficulty) {

		
		
		Item weapon = null;
		
		weapon = TGuns.netherblaster.get();
		
		if (weapon != null) this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
	}
public SoundEvent getAmbientSound() {
		return TGSounds.CYBERDEMON_IDLE.get();
	}
public SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return TGSounds.CYBERDEMON_HURT.get();
	}
public SoundEvent getDeathSound() {
		return TGSounds.CYBERDEMON_DEATH.get();
	}
public SoundEvent getStepSound() {
		return TGSounds.CYBERDEMON_STEP.get();
	}
protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable() {
		return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, LOOT);
	}
public float getWeaponPosX() {
		return 0.16f;
	}
public float getWeaponPosY() {
		return 0.72f;
	}
public float getWeaponPosZ() {
		return 0.1f;
	}
public float getGunScale() {
		return 1.5f;
	}
public float getBulletOffsetSide() {
		return 0.3f;
	}
public float getBulletOffsetHeight() {
		return -0.59f;
	}
public boolean hasWeaponArmPose() {
		return false;
	}
protected boolean shouldBurnInDay() {
		return false;
	}
public static AttributeSupplier.Builder attributes(){return GenericNPCUndead.attributes().add(Attributes.MOVEMENT_SPEED,0.35D).add(Attributes.MAX_HEALTH,30).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.FOLLOW_RANGE,50.0D).add(Attributes.ARMOR_TOUGHNESS,1D);}
@Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state){playSound(getStepSound(),.15f,1);}
}
