package techguns.client.models.machines;

import techguns.client.models.LegacyModelPart;

import net.minecraft.world.entity.Entity;

/** Energy cell. Texture: textures/blocks/energy_cell.png (128x64). */
public class ModelEnergyCell extends PowerModel {

	//fields
	LegacyModelPart Base;
	LegacyModelPart Cell;
	LegacyModelPart Cap;
	LegacyModelPart Plus;
	LegacyModelPart Minus;
	LegacyModelPart FinsE;
	LegacyModelPart FinsW;
	LegacyModelPart Charge0;
	LegacyModelPart Charge1;
	LegacyModelPart Charge2;
	LegacyModelPart Charge3;
	LegacyModelPart LampEmpty;
	LegacyModelPart LampPart;
	LegacyModelPart LampFull;

	public ModelEnergyCell()
	{
		textureWidth = 128;
		textureHeight = 64;

		Base = new LegacyModelPart(this, 0, 23);
		Base.addBox(-16F, -2F, 0F, 16, 2, 16);
		Base.setRotationPoint(8F, 24F, -8F);
		Base.setTextureSize(128, 64);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		Cell = new LegacyModelPart(this, 0, 0);
		Cell.addBox(-12F, -11F, 0F, 12, 11, 12);
		Cell.setRotationPoint(6F, 22F, -6F);
		Cell.setTextureSize(128, 64);
		Cell.mirror = true;
		setRotation(Cell, 0F, 0F, 0F);
		Cap = new LegacyModelPart(this, 64, 23);
		Cap.addBox(-14F, -2F, 0F, 14, 2, 14);
		Cap.setRotationPoint(7F, 11F, -7F);
		Cap.setTextureSize(128, 64);
		Cap.mirror = true;
		setRotation(Cap, 0F, 0F, 0F);
		Plus = new LegacyModelPart(this, 0, 41);
		Plus.addBox(-3F, -2F, 0F, 3, 2, 3);
		Plus.setRotationPoint(5F, 9F, -2F);
		Plus.setTextureSize(128, 64);
		Plus.mirror = true;
		setRotation(Plus, 0F, 0F, 0F);
		Minus = new LegacyModelPart(this, 12, 41);
		Minus.addBox(-3F, -2F, 0F, 3, 2, 3);
		Minus.setRotationPoint(-2F, 9F, -2F);
		Minus.setTextureSize(128, 64);
		Minus.mirror = true;
		setRotation(Minus, 0F, 0F, 0F);
		FinsE = new LegacyModelPart(this, 48, 0);
		FinsE.addBox(-1F, -9F, 0F, 1, 9, 10);
		FinsE.setRotationPoint(-6F, 21F, -5F);
		FinsE.setTextureSize(128, 64);
		FinsE.mirror = true;
		setRotation(FinsE, 0F, 0F, 0F);
		FinsW = new LegacyModelPart(this, 70, 0);
		FinsW.addBox(-1F, -9F, 0F, 1, 9, 10);
		FinsW.setRotationPoint(7F, 21F, -5F);
		FinsW.setTextureSize(128, 64);
		FinsW.mirror = true;
		setRotation(FinsW, 0F, 0F, 0F);
		Charge0 = new LegacyModelPart(this, 24, 41);
		Charge0.addBox(-4F, -1F, 0F, 4, 1, 1);
		Charge0.setRotationPoint(2F, 14F, -6.6F);
		Charge0.setTextureSize(128, 64);
		Charge0.mirror = true;
		setRotation(Charge0, 0F, 0F, 0F);
		Charge1 = new LegacyModelPart(this, 34, 41);
		Charge1.addBox(-4F, -1F, 0F, 4, 1, 1);
		Charge1.setRotationPoint(2F, 16F, -6.6F);
		Charge1.setTextureSize(128, 64);
		Charge1.mirror = true;
		setRotation(Charge1, 0F, 0F, 0F);
		Charge2 = new LegacyModelPart(this, 44, 41);
		Charge2.addBox(-4F, -1F, 0F, 4, 1, 1);
		Charge2.setRotationPoint(2F, 18F, -6.6F);
		Charge2.setTextureSize(128, 64);
		Charge2.mirror = true;
		setRotation(Charge2, 0F, 0F, 0F);
		Charge3 = new LegacyModelPart(this, 54, 41);
		Charge3.addBox(-4F, -1F, 0F, 4, 1, 1);
		Charge3.setRotationPoint(2F, 20F, -6.6F);
		Charge3.setTextureSize(128, 64);
		Charge3.mirror = true;
		setRotation(Charge3, 0F, 0F, 0F);
		LampEmpty = new LegacyModelPart(this, 64, 41);
		LampEmpty.addBox(-1F, -1F, 0F, 1, 1, 1);
		LampEmpty.setRotationPoint(-4F, 13F, -6.6F);
		LampEmpty.setTextureSize(128, 64);
		LampEmpty.mirror = true;
		setRotation(LampEmpty, 0F, 0F, 0F);
		LampPart = new LegacyModelPart(this, 68, 41);
		LampPart.addBox(-1F, -1F, 0F, 1, 1, 1);
		LampPart.setRotationPoint(-4F, 13F, -6.6F);
		LampPart.setTextureSize(128, 64);
		LampPart.mirror = true;
		setRotation(LampPart, 0F, 0F, 0F);
		LampFull = new LegacyModelPart(this, 72, 41);
		LampFull.addBox(-1F, -1F, 0F, 1, 1, 1);
		LampFull.setRotationPoint(-4F, 13F, -6.6F);
		LampFull.setTextureSize(128, 64);
		LampFull.mirror = true;
		setRotation(LampFull, 0F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3,
			float f4, float f5, float progress) {
		Base.render(f5);
		Cell.render(f5);
		Cap.render(f5);
		Plus.render(f5);
		Minus.render(f5);
		FinsE.render(f5);
		FinsW.render(f5);
		if (charge >= 1) { Charge0.render(f5); }
		if (charge >= 2) { Charge1.render(f5); }
		if (charge >= 3) { Charge2.render(f5); }
		if (charge >= 4) { Charge3.render(f5); }
		if (charge == 0) { LampEmpty.render(f5); }
		if (charge > 0 && charge < 4) { LampPart.render(f5); }
		if (charge >= 4) { LampFull.render(f5); }
	}

}
