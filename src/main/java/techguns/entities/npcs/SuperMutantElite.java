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
/** Adapted from original SuperMutantElite; equipment branches and damage values are retained. */
public class SuperMutantElite extends SuperMutantBasic {
public SuperMutantElite(EntityType<? extends GenericNPC> type,Level level){super(type,level);setTGArmorStats(15.0f, 1f);}
public float getWeaponPosX() {
		return 0.23f;
	}
public float getWeaponPosZ() {
		return -0.39f;
	}
public float getTotalArmorAgainstType(TGDamageSource dmgsrc) {
		switch(dmgsrc.damageType){
		case EXPLOSION:
		case LIGHTNING:
		case ENERGY:
		case FIRE:
		case ICE:
			return 14.0f;
		case PHYSICAL:
		case PROJECTILE:
			return 10.0f;
		case POISON:
		case RADIATION:
			return 16.0f;
		case UNRESISTABLE:
	default:
		return 0.0f;
		}
	}
public int getArmorValue() {
		return 10;
	}
public int gettype() {
		return 2;
	}
public double getModelHeightOffset(){
		return 0.95d;
	}
public float getModelScale() {
		return 1.65f;
	}
protected float getMutantWidth() {
		return 1.4f;
	}
public static AttributeSupplier.Builder attributes(){return SuperMutantBasic.attributes().add(Attributes.MOVEMENT_SPEED,0.20D).add(Attributes.MAX_HEALTH,60).add(Attributes.ATTACK_DAMAGE,11).add(Attributes.FOLLOW_RANGE,50.0D).add(Attributes.ARMOR_TOUGHNESS,2.5D);}
}
