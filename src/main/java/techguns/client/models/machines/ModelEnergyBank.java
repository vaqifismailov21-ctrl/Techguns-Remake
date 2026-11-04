package techguns.client.models.machines;

import techguns.client.models.LegacyModelPart;

import net.minecraft.world.entity.Entity;

/** Energy bank. Texture: textures/blocks/energy_bank.png (128x64). */
public class ModelEnergyBank extends PowerModel {

	//fields
	LegacyModelPart Cabinet;
	LegacyModelPart Corner0;
	LegacyModelPart Corner1;
	LegacyModelPart Corner2;
	LegacyModelPart Corner3;
	LegacyModelPart Foot;
	LegacyModelPart Bar00;
	LegacyModelPart Bar01;
	LegacyModelPart Bar02;
	LegacyModelPart Bar03;
	LegacyModelPart Bar10;
	LegacyModelPart Bar11;
	LegacyModelPart Bar12;
	LegacyModelPart Bar13;
	LegacyModelPart Bar20;
	LegacyModelPart Bar21;
	LegacyModelPart Bar22;
	LegacyModelPart Bar23;
	LegacyModelPart Bar30;
	LegacyModelPart Bar31;
	LegacyModelPart Bar32;
	LegacyModelPart Bar33;
	LegacyModelPart TerminalA;
	LegacyModelPart TerminalB;

	public ModelEnergyBank()
	{
		textureWidth = 128;
		textureHeight = 64;

		Cabinet = new LegacyModelPart(this, 0, 0);
		Cabinet.addBox(-14F, -16F, 0F, 14, 16, 14);
		Cabinet.setRotationPoint(7F, 24F, -7F);
		Cabinet.setTextureSize(128, 64);
		Cabinet.mirror = true;
		setRotation(Cabinet, 0F, 0F, 0F);
		Corner0 = new LegacyModelPart(this, 120, 0);
		Corner0.addBox(-1F, -16F, 0F, 1, 16, 1);
		Corner0.setRotationPoint(8F, 24F, -8F);
		Corner0.setTextureSize(128, 64);
		Corner0.mirror = true;
		setRotation(Corner0, 0F, 0F, 0F);
		Corner1 = new LegacyModelPart(this, 124, 0);
		Corner1.addBox(-1F, -16F, 0F, 1, 16, 1);
		Corner1.setRotationPoint(-7F, 24F, -8F);
		Corner1.setTextureSize(128, 64);
		Corner1.mirror = true;
		setRotation(Corner1, 0F, 0F, 0F);
		Corner2 = new LegacyModelPart(this, 0, 30);
		Corner2.addBox(-1F, -16F, 0F, 1, 16, 1);
		Corner2.setRotationPoint(8F, 24F, 7F);
		Corner2.setTextureSize(128, 64);
		Corner2.mirror = true;
		setRotation(Corner2, 0F, 0F, 0F);
		Corner3 = new LegacyModelPart(this, 4, 30);
		Corner3.addBox(-1F, -16F, 0F, 1, 16, 1);
		Corner3.setRotationPoint(-7F, 24F, 7F);
		Corner3.setTextureSize(128, 64);
		Corner3.mirror = true;
		setRotation(Corner3, 0F, 0F, 0F);
		Foot = new LegacyModelPart(this, 56, 0);
		Foot.addBox(-16F, -1F, 0F, 16, 1, 16);
		Foot.setRotationPoint(8F, 24F, -8F);
		Foot.setTextureSize(128, 64);
		Foot.mirror = true;
		setRotation(Foot, 0F, 0F, 0F);
		Bar00 = new LegacyModelPart(this, 32, 30);
		Bar00.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar00.setRotationPoint(5F, 12.5F, -7.6F);
		Bar00.setTextureSize(128, 64);
		Bar00.mirror = true;
		setRotation(Bar00, 0F, 0F, 0F);
		Bar01 = new LegacyModelPart(this, 38, 30);
		Bar01.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar01.setRotationPoint(5F, 15.5F, -7.6F);
		Bar01.setTextureSize(128, 64);
		Bar01.mirror = true;
		setRotation(Bar01, 0F, 0F, 0F);
		Bar02 = new LegacyModelPart(this, 44, 30);
		Bar02.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar02.setRotationPoint(5F, 18.5F, -7.6F);
		Bar02.setTextureSize(128, 64);
		Bar02.mirror = true;
		setRotation(Bar02, 0F, 0F, 0F);
		Bar03 = new LegacyModelPart(this, 50, 30);
		Bar03.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar03.setRotationPoint(5F, 21.5F, -7.6F);
		Bar03.setTextureSize(128, 64);
		Bar03.mirror = true;
		setRotation(Bar03, 0F, 0F, 0F);
		Bar10 = new LegacyModelPart(this, 56, 30);
		Bar10.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar10.setRotationPoint(1.5F, 12.5F, -7.6F);
		Bar10.setTextureSize(128, 64);
		Bar10.mirror = true;
		setRotation(Bar10, 0F, 0F, 0F);
		Bar11 = new LegacyModelPart(this, 62, 30);
		Bar11.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar11.setRotationPoint(1.5F, 15.5F, -7.6F);
		Bar11.setTextureSize(128, 64);
		Bar11.mirror = true;
		setRotation(Bar11, 0F, 0F, 0F);
		Bar12 = new LegacyModelPart(this, 68, 30);
		Bar12.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar12.setRotationPoint(1.5F, 18.5F, -7.6F);
		Bar12.setTextureSize(128, 64);
		Bar12.mirror = true;
		setRotation(Bar12, 0F, 0F, 0F);
		Bar13 = new LegacyModelPart(this, 74, 30);
		Bar13.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar13.setRotationPoint(1.5F, 21.5F, -7.6F);
		Bar13.setTextureSize(128, 64);
		Bar13.mirror = true;
		setRotation(Bar13, 0F, 0F, 0F);
		Bar20 = new LegacyModelPart(this, 80, 30);
		Bar20.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar20.setRotationPoint(-2F, 12.5F, -7.6F);
		Bar20.setTextureSize(128, 64);
		Bar20.mirror = true;
		setRotation(Bar20, 0F, 0F, 0F);
		Bar21 = new LegacyModelPart(this, 86, 30);
		Bar21.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar21.setRotationPoint(-2F, 15.5F, -7.6F);
		Bar21.setTextureSize(128, 64);
		Bar21.mirror = true;
		setRotation(Bar21, 0F, 0F, 0F);
		Bar22 = new LegacyModelPart(this, 92, 30);
		Bar22.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar22.setRotationPoint(-2F, 18.5F, -7.6F);
		Bar22.setTextureSize(128, 64);
		Bar22.mirror = true;
		setRotation(Bar22, 0F, 0F, 0F);
		Bar23 = new LegacyModelPart(this, 98, 30);
		Bar23.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar23.setRotationPoint(-2F, 21.5F, -7.6F);
		Bar23.setTextureSize(128, 64);
		Bar23.mirror = true;
		setRotation(Bar23, 0F, 0F, 0F);
		Bar30 = new LegacyModelPart(this, 104, 30);
		Bar30.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar30.setRotationPoint(-5.5F, 12.5F, -7.6F);
		Bar30.setTextureSize(128, 64);
		Bar30.mirror = true;
		setRotation(Bar30, 0F, 0F, 0F);
		Bar31 = new LegacyModelPart(this, 110, 30);
		Bar31.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar31.setRotationPoint(-5.5F, 15.5F, -7.6F);
		Bar31.setTextureSize(128, 64);
		Bar31.mirror = true;
		setRotation(Bar31, 0F, 0F, 0F);
		Bar32 = new LegacyModelPart(this, 116, 30);
		Bar32.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar32.setRotationPoint(-5.5F, 18.5F, -7.6F);
		Bar32.setTextureSize(128, 64);
		Bar32.mirror = true;
		setRotation(Bar32, 0F, 0F, 0F);
		Bar33 = new LegacyModelPart(this, 122, 30);
		Bar33.addBox(-2F, -2F, 0F, 2, 2, 1);
		Bar33.setRotationPoint(-5.5F, 21.5F, -7.6F);
		Bar33.setTextureSize(128, 64);
		Bar33.mirror = true;
		setRotation(Bar33, 0F, 0F, 0F);
		TerminalA = new LegacyModelPart(this, 8, 30);
		TerminalA.addBox(-2F, -1F, 0F, 2, 1, 4);
		TerminalA.setRotationPoint(4F, 8F, -2F);
		TerminalA.setTextureSize(128, 64);
		TerminalA.mirror = true;
		setRotation(TerminalA, 0F, 0F, 0F);
		TerminalB = new LegacyModelPart(this, 20, 30);
		TerminalB.addBox(-2F, -1F, 0F, 2, 1, 4);
		TerminalB.setRotationPoint(-2F, 8F, -2F);
		TerminalB.setTextureSize(128, 64);
		TerminalB.mirror = true;
		setRotation(TerminalB, 0F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3,
			float f4, float f5, float progress) {
		Cabinet.render(f5);
		Corner0.render(f5);
		Corner1.render(f5);
		Corner2.render(f5);
		Corner3.render(f5);
		Foot.render(f5);
		if (charge >= 1) { Bar00.render(f5); }
		if (charge >= 2) { Bar01.render(f5); }
		if (charge >= 3) { Bar02.render(f5); }
		if (charge >= 4) { Bar03.render(f5); }
		if (charge >= 1) { Bar10.render(f5); }
		if (charge >= 2) { Bar11.render(f5); }
		if (charge >= 3) { Bar12.render(f5); }
		if (charge >= 4) { Bar13.render(f5); }
		if (charge >= 1) { Bar20.render(f5); }
		if (charge >= 2) { Bar21.render(f5); }
		if (charge >= 3) { Bar22.render(f5); }
		if (charge >= 4) { Bar23.render(f5); }
		if (charge >= 1) { Bar30.render(f5); }
		if (charge >= 2) { Bar31.render(f5); }
		if (charge >= 3) { Bar32.render(f5); }
		if (charge >= 4) { Bar33.render(f5); }
		TerminalA.render(f5);
		TerminalB.render(f5);
	}

}
