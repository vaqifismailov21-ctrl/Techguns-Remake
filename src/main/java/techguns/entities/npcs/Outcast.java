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
/** Adapted from original Outcast; equipment branches and damage values are retained. */
public class Outcast extends GenericNPCGearSpecificStats {
public Outcast(EntityType<? extends GenericNPC> type,Level level){super(type,level);}
public static final ResourceLocation LOOT=ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"entities/outcast");
protected void addRandomArmor(int difficulty) {

		Random r = new Random(random.nextLong());
		

		int camo=r.nextInt(2);
		this.setItemSlot(EquipmentSlot.HEAD, camo(TGEquipment.ARMOR.get("t3_power_helmet").get(),camo+1));
		camo=r.nextInt(2);
		this.setItemSlot(EquipmentSlot.CHEST, camo(TGEquipment.ARMOR.get("t3_power_chestplate").get(),camo+1));
		camo=r.nextInt(2);
		this.setItemSlot(EquipmentSlot.LEGS, camo(TGEquipment.ARMOR.get("t3_power_leggings").get(),camo+1));
		camo=r.nextInt(2);
		this.setItemSlot(EquipmentSlot.FEET, camo(TGEquipment.ARMOR.get("t3_power_boots").get(),camo+1));
		

		

		Item weapon = null;
		switch (r.nextInt(7)) {
		case 0:
			weapon = TGuns.lasergun.get();
			break;
		case 1:
			weapon = TGuns.grimreaper.get();
			break;
		case 2:
			weapon = TGuns.minigun.get();
			break;
		case 3:
			weapon = TGuns.biogun.get();
			break;
		case 4:
			weapon = TGuns.flamethrower.get();
			break;
		case 5:
			weapon = TGuns.teslagun.get();
			break;
		case 6:
			weapon = TGuns.lmg.get();
			break;
		default:
			weapon = TGuns.lasergun.get();
			break;
		}
		if (weapon != null) this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
	}
protected ResourceLocation getDefaultLootTable() {
		return LOOT;
	}
public static AttributeSupplier.Builder attributes(){return GenericNPCGearSpecificStats.attributes().add(Attributes.MOVEMENT_SPEED,0.2D).add(Attributes.MAX_HEALTH,30).add(Attributes.ATTACK_DAMAGE,8).add(Attributes.FOLLOW_RANGE,60.0D);}
}
