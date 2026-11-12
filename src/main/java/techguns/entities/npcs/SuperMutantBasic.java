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
/** Adapted from original SuperMutantBasic; equipment branches and damage values are retained. */
public class SuperMutantBasic extends GenericNPC {
public SuperMutantBasic(EntityType<? extends GenericNPC> type,Level level){super(type,level);setTGArmorStats(5.0f, 0f);}
public static final ResourceLocation LOOT=ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"entities/supermutantbasic");
protected void addRandomArmor(int difficulty) {

			
		Random r = new Random(random.nextLong());
		Item weapon = null;
		switch (r.nextInt(5)) {
			case 0:
				weapon = TGuns.rocketlauncher.get();
				break;
			case 1:
				weapon = TGuns.pulserifle.get();
				break;
			case 2:
				weapon = TGuns.blasterrifle.get();
				break;
			default:
				weapon = TGuns.lasergun.get();
				break;
			
		}
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
protected ResourceLocation getDefaultLootTable() {
		return LOOT;
	}
public float getWeaponPosX() {
		return 0.13f;
	}
public float getWeaponPosY() {
		return 0f;
	}
public float getWeaponPosZ() {
		return -0.18f;
	}
public float getTotalArmorAgainstType(TGDamageSource dmgsrc) {
		switch(dmgsrc.damageType){
			case EXPLOSION:
			case LIGHTNING:
			case ENERGY:
			case FIRE:
			case ICE:
				return 10.0f;
			case PHYSICAL:
				return 8.0f;
			case PROJECTILE:
				return 7.0f;
			case POISON:
			case RADIATION:
				return 15.0f;
			case UNRESISTABLE:
		default:
			return 0.0f;
		}
	}
public int getArmorValue() {
		return 7;
	}
public int gettype() {
		return 0;
	}
public double getModelHeightOffset(){
		return 0.55d;
	}
public float getModelScale() {
		return 1.35f;
	}
protected float getMutantWidth() {
		return 1.0f;
	}
public static AttributeSupplier.Builder attributes(){return GenericNPC.attributes().add(Attributes.MOVEMENT_SPEED,0.30D).add(Attributes.MAX_HEALTH,35).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.FOLLOW_RANGE,64.0D).add(Attributes.ARMOR_TOUGHNESS,1.5D);}
@Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state){playSound(getStepSound(),.15f,1);}
}
