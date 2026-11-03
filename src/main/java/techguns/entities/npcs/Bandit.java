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
/** Adapted from original Bandit; equipment branches and damage values are retained. */
public class Bandit extends GenericNPC {
public Bandit(EntityType<? extends GenericNPC> type,Level level){super(type,level);setTGArmorStats(5.0f, 0f);}
public static final ResourceLocation LOOT=ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"entities/bandit");
protected void addRandomArmor(int difficulty) {

		
		
		this.setItemSlot(EquipmentSlot.CHEST,new ItemStack(TGEquipment.ARMOR.get("t1_scout_chestplate").get()));
		this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TGEquipment.ARMOR.get("t1_scout_leggings").get()));
		this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TGEquipment.ARMOR.get("t1_scout_boots").get()));
		
		double chance = 0.5;
		if (random.nextDouble() <= chance) {
			this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TGEquipment.ARMOR.get("t1_scout_helmet").get()));
		}			 

		
		Random r = new Random(random.nextLong());
		Item weapon = null;
		switch (r.nextInt(6)) {
		case 0:
			weapon = TGuns.pistol.get();
			break;
		case 1:
			weapon = TGuns.ak47.get();
			break;
		case 2:
			weapon = TGuns.sawedoff.get();
			break;
		case 3:
			weapon = TGuns.thompson.get();
			break;
		case 4:
			weapon = TGuns.revolver.get();
			break;
		default:
			weapon = TGuns.boltaction.get();
			break;
		}
		if (weapon != null) this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
	}
protected ResourceLocation getDefaultLootTable() {
		return LOOT;
	}
public static AttributeSupplier.Builder attributes(){return GenericNPC.attributes().add(Attributes.MOVEMENT_SPEED,0.30D).add(Attributes.MAX_HEALTH,20).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.FOLLOW_RANGE,40.0D);}
}
