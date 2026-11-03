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
/** Adapted from original SuperMutantHeavy; equipment branches and damage values are retained. */
public class SuperMutantHeavy extends SuperMutantBasic {
public SuperMutantHeavy(EntityType<? extends GenericNPC> type,Level level){super(type,level);setTGArmorStats(10.0f, 0);}
public float getWeaponPosX() {
		return 0.17f;
	}
public float getWeaponPosZ() {
		return -0.3f;
	}
public float getTotalArmorAgainstType(TGDamageSource dmgsrc) {
		switch(dmgsrc.damageType){
		case EXPLOSION:
		case LIGHTNING:
		case ENERGY:
		case FIRE:
		case ICE:
			return 16.0f;
		case PHYSICAL:
		case PROJECTILE:
			return 12.0f;
		case POISON:
		case RADIATION:
			return 18.0f;
		case UNRESISTABLE:
	default:
		return 0.0f;
		}
	}
public int getArmorValue() {
		return 12;
	}
public int gettype() {
		return 1;
	}
public double getModelHeightOffset(){
		return 0.75d;
	}
public float getModelScale() {
		return 1.5f;
	}
protected float getMutantWidth() {
		return 1.2f;
	}
public static AttributeSupplier.Builder attributes(){return SuperMutantBasic.attributes().add(Attributes.MOVEMENT_SPEED,0.25D).add(Attributes.MAX_HEALTH,45).add(Attributes.ATTACK_DAMAGE,9).add(Attributes.FOLLOW_RANGE,50.0D).add(Attributes.ARMOR_TOUGHNESS,2D);}
}
