package techguns.client.models.machines;

import techguns.client.models.LegacyModelPart;

import net.minecraft.world.entity.Entity;

/** Liquid generator. Texture: textures/blocks/liquid_generator.png (128x64). */
public class ModelLiquidGenerator extends PowerModel {

	//fields
	LegacyModelPart Base;
	LegacyModelPart Engine;
	LegacyModelPart PistonBlock;
	LegacyModelPart Piston1;
	LegacyModelPart Piston2;
	LegacyModelPart Exhaust;
	LegacyModelPart ExhaustCap;
	LegacyModelPart TankBottom;
	LegacyModelPart TankTop;
	LegacyModelPart TankRod1;
	LegacyModelPart TankRod2;
	LegacyModelPart TankRod3;
	LegacyModelPart TankRod4;
	LegacyModelPart Oil;
	LegacyModelPart Tank;
	LegacyModelPart FuelPipe;
	LegacyModelPart LedOn;

	public ModelLiquidGenerator()
	{
		textureWidth = 128;
		textureHeight = 64;

		Base = new LegacyModelPart(this, 0, 23);
		Base.addBox(-16F, -2F, 0F, 16, 2, 16);
		Base.setRotationPoint(8F, 24F, -8F);
		Base.setTextureSize(128, 64);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		Engine = new LegacyModelPart(this, 0, 0);
		Engine.addBox(-8F, -9F, 0F, 8, 9, 14);
		Engine.setRotationPoint(7F, 22F, -7F);
		Engine.setTextureSize(128, 64);
		Engine.mirror = true;
		setRotation(Engine, 0F, 0F, 0F);
		PistonBlock = new LegacyModelPart(this, 92, 41);
		PistonBlock.addBox(-6F, -2F, 0F, 6, 2, 6);
		PistonBlock.setRotationPoint(6F, 13F, -5F);
		PistonBlock.setTextureSize(128, 64);
		PistonBlock.mirror = true;
		setRotation(PistonBlock, 0F, 0F, 0F);
		Piston1 = new LegacyModelPart(this, 20, 54);
		Piston1.addBox(-1F, -3F, 0F, 1, 3, 1);
		Piston1.setRotationPoint(5F, 11F, -4F);
		Piston1.setTextureSize(128, 64);
		Piston1.mirror = true;
		setRotation(Piston1, 0F, 0F, 0F);
		Piston2 = new LegacyModelPart(this, 24, 54);
		Piston2.addBox(-1F, -3F, 0F, 1, 3, 1);
		Piston2.setRotationPoint(2F, 11F, -2F);
		Piston2.setTextureSize(128, 64);
		Piston2.mirror = true;
		setRotation(Piston2, 0F, 0F, 0F);
		Exhaust = new LegacyModelPart(this, 80, 41);
		Exhaust.addBox(-3F, -6F, 0F, 3, 6, 3);
		Exhaust.setRotationPoint(6F, 13F, 2F);
		Exhaust.setTextureSize(128, 64);
		Exhaust.mirror = true;
		setRotation(Exhaust, 0F, 0F, 0F);
		ExhaustCap = new LegacyModelPart(this, 0, 54);
		ExhaustCap.addBox(-5F, -1F, 0F, 5, 1, 5);
		ExhaustCap.setRotationPoint(7F, 7F, 1F);
		ExhaustCap.setTextureSize(128, 64);
		ExhaustCap.mirror = true;
		setRotation(ExhaustCap, 0F, 0F, 0F);
		TankBottom = new LegacyModelPart(this, 64, 23);
		TankBottom.addBox(-7F, -1F, 0F, 7, 1, 12);
		TankBottom.setRotationPoint(-1F, 22F, -6F);
		TankBottom.setTextureSize(128, 64);
		TankBottom.mirror = true;
		setRotation(TankBottom, 0F, 0F, 0F);
		TankTop = new LegacyModelPart(this, 0, 41);
		TankTop.addBox(-7F, -1F, 0F, 7, 1, 12);
		TankTop.setRotationPoint(-1F, 11F, -6F);
		TankTop.setTextureSize(128, 64);
		TankTop.mirror = true;
		setRotation(TankTop, 0F, 0F, 0F);
		TankRod1 = new LegacyModelPart(this, 38, 41);
		TankRod1.addBox(-1F, -10F, 0F, 1, 10, 1);
		TankRod1.setRotationPoint(-1F, 21F, -6F);
		TankRod1.setTextureSize(128, 64);
		TankRod1.mirror = true;
		setRotation(TankRod1, 0F, 0F, 0F);
		TankRod2 = new LegacyModelPart(this, 42, 41);
		TankRod2.addBox(-1F, -10F, 0F, 1, 10, 1);
		TankRod2.setRotationPoint(-7F, 21F, -6F);
		TankRod2.setTextureSize(128, 64);
		TankRod2.mirror = true;
		setRotation(TankRod2, 0F, 0F, 0F);
		TankRod3 = new LegacyModelPart(this, 46, 41);
		TankRod3.addBox(-1F, -10F, 0F, 1, 10, 1);
		TankRod3.setRotationPoint(-1F, 21F, 5F);
		TankRod3.setTextureSize(128, 64);
		TankRod3.mirror = true;
		setRotation(TankRod3, 0F, 0F, 0F);
		TankRod4 = new LegacyModelPart(this, 50, 41);
		TankRod4.addBox(-1F, -10F, 0F, 1, 10, 1);
		TankRod4.setRotationPoint(-7F, 21F, 5F);
		TankRod4.setTextureSize(128, 64);
		TankRod4.mirror = true;
		setRotation(TankRod4, 0F, 0F, 0F);
		Oil = new LegacyModelPart(this, 54, 41);
		Oil.addBox(-4F, -1F, 0F, 4, 1, 9);
		Oil.setRotationPoint(-2.5F, 21F, -4.5F);
		Oil.setTextureSize(128, 64);
		Oil.mirror = true;
		setRotation(Oil, 0F, 0F, 0F);
		Tank = new LegacyModelPart(this, 44, 0);
		Tank.addBox(-5F, -10F, 0F, 5, 10, 10);
		Tank.setRotationPoint(-2F, 21F, -5F);
		Tank.setTextureSize(128, 64);
		Tank.mirror = true;
		setRotation(Tank, 0F, 0F, 0F);
		FuelPipe = new LegacyModelPart(this, 28, 54);
		FuelPipe.addBox(-2F, -1F, 0F, 2, 1, 2);
		FuelPipe.setRotationPoint(0F, 15F, -1F);
		FuelPipe.setTextureSize(128, 64);
		FuelPipe.mirror = true;
		setRotation(FuelPipe, 0F, 0F, 0F);
		LedOn = new LegacyModelPart(this, 36, 54);
		LedOn.addBox(-1F, -1F, 0F, 1, 1, 1);
		LedOn.setRotationPoint(1F, 21F, -7.6F);
		LedOn.setTextureSize(128, 64);
		LedOn.mirror = true;
		setRotation(LedOn, 0F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3,
			float f4, float f5, float progress) {
		Base.render(f5);
		Engine.render(f5);
		PistonBlock.render(f5);
		Piston1.rotationPointY = 11F - (active ? Math.abs((float) Math.sin(spin * 2F + 0F)) * 1.5F : 0F); Piston1.render(f5);
		Piston2.rotationPointY = 11F - (active ? Math.abs((float) Math.sin(spin * 2F + 1.5708F)) * 1.5F : 0F); Piston2.render(f5);
		Exhaust.render(f5);
		ExhaustCap.render(f5);
		TankBottom.render(f5);
		TankTop.render(f5);
		TankRod1.render(f5);
		TankRod2.render(f5);
		TankRod3.render(f5);
		TankRod4.render(f5);
		int OilCount = Math.round(level * 9F);
		pushMatrix();
		for (int i = 0; i < OilCount; i++) {
			Oil.render(f5);
			translate(0, -0.0625F, 0);
		}
		popMatrix();
		FuelPipe.render(f5);
		if (active) { LedOn.render(f5); }

		useAlphaLayer(true);
		Tank.render(f5);
		useAlphaLayer(false);
	}

}
