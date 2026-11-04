package techguns.client.models.machines;

import techguns.client.models.LegacyModelPart;

import net.minecraft.world.entity.Entity;

/** Geothermal generator. Texture: textures/blocks/geothermal_generator.png (128x64). */
public class ModelGeothermalGenerator extends PowerModel {

	//fields
	LegacyModelPart Base;
	LegacyModelPart Core;
	LegacyModelPart HeatWindow;
	LegacyModelPart HeatGlow;
	LegacyModelPart HotPipe0;
	LegacyModelPart HotPipe1;
	LegacyModelPart HotPipe2;
	LegacyModelPart HotPipe3;
	LegacyModelPart Ring;
	LegacyModelPart FanA;
	LegacyModelPart FanB;
	LegacyModelPart FanHub;
	LegacyModelPart Gauge;
	LegacyModelPart ValveWheel;

	public ModelGeothermalGenerator()
	{
		textureWidth = 128;
		textureHeight = 64;

		Base = new LegacyModelPart(this, 40, 0);
		Base.addBox(-16F, -3F, 0F, 16, 3, 16);
		Base.setRotationPoint(8F, 24F, -8F);
		Base.setTextureSize(128, 64);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		Core = new LegacyModelPart(this, 0, 0);
		Core.addBox(-10F, -10F, 0F, 10, 10, 10);
		Core.setRotationPoint(5F, 21F, -5F);
		Core.setTextureSize(128, 64);
		Core.mirror = true;
		setRotation(Core, 0F, 0F, 0F);
		HeatWindow = new LegacyModelPart(this, 0, 38);
		HeatWindow.addBox(-6F, -5F, 0F, 6, 5, 1);
		HeatWindow.setRotationPoint(3F, 19F, -6F);
		HeatWindow.setTextureSize(128, 64);
		HeatWindow.mirror = true;
		setRotation(HeatWindow, 0F, 0F, 0F);
		HeatGlow = new LegacyModelPart(this, 30, 38);
		HeatGlow.addBox(-4F, -3F, 0F, 4, 3, 0);
		HeatGlow.setRotationPoint(2F, 18F, -6.1F);
		HeatGlow.setTextureSize(128, 64);
		HeatGlow.mirror = true;
		setRotation(HeatGlow, 0F, 0F, 0F);
		HotPipe0 = new LegacyModelPart(this, 64, 20);
		HotPipe0.addBox(-2F, -10F, 0F, 2, 10, 2);
		HotPipe0.setRotationPoint(8F, 21F, -8F);
		HotPipe0.setTextureSize(128, 64);
		HotPipe0.mirror = true;
		setRotation(HotPipe0, 0F, 0F, 0F);
		HotPipe1 = new LegacyModelPart(this, 72, 20);
		HotPipe1.addBox(-2F, -10F, 0F, 2, 10, 2);
		HotPipe1.setRotationPoint(-6F, 21F, -8F);
		HotPipe1.setTextureSize(128, 64);
		HotPipe1.mirror = true;
		setRotation(HotPipe1, 0F, 0F, 0F);
		HotPipe2 = new LegacyModelPart(this, 80, 20);
		HotPipe2.addBox(-2F, -10F, 0F, 2, 10, 2);
		HotPipe2.setRotationPoint(8F, 21F, 6F);
		HotPipe2.setTextureSize(128, 64);
		HotPipe2.mirror = true;
		setRotation(HotPipe2, 0F, 0F, 0F);
		HotPipe3 = new LegacyModelPart(this, 88, 20);
		HotPipe3.addBox(-2F, -10F, 0F, 2, 10, 2);
		HotPipe3.setRotationPoint(-6F, 21F, 6F);
		HotPipe3.setTextureSize(128, 64);
		HotPipe3.mirror = true;
		setRotation(HotPipe3, 0F, 0F, 0F);
		Ring = new LegacyModelPart(this, 0, 20);
		Ring.addBox(-16F, -2F, 0F, 16, 2, 16);
		Ring.setRotationPoint(8F, 11F, -8F);
		Ring.setTextureSize(128, 64);
		Ring.mirror = true;
		setRotation(Ring, 0F, 0F, 0F);
		FanA = new LegacyModelPart(this, 46, 38);
		FanA.addBox(-5F, -0.5F, -0.5F, 10, 1, 1);
		FanA.setRotationPoint(0F, 9F, 0F);
		FanA.setTextureSize(128, 64);
		FanA.mirror = true;
		setRotation(FanA, 0F, 0F, 0F);
		FanB = new LegacyModelPart(this, 96, 20);
		FanB.addBox(-0.5F, -0.5F, -5F, 1, 1, 10);
		FanB.setRotationPoint(0F, 9F, 0F);
		FanB.setTextureSize(128, 64);
		FanB.mirror = true;
		setRotation(FanB, 0F, 0F, 0F);
		FanHub = new LegacyModelPart(this, 38, 38);
		FanHub.addBox(-1F, -1F, -1F, 2, 1, 2);
		FanHub.setRotationPoint(0F, 9F, 0F);
		FanHub.setTextureSize(128, 64);
		FanHub.mirror = true;
		setRotation(FanHub, 0F, 0F, 0F);
		Gauge = new LegacyModelPart(this, 14, 38);
		Gauge.addBox(-3F, -3F, 0F, 3, 3, 1);
		Gauge.setRotationPoint(-3F, 15F, -6F);
		Gauge.setTextureSize(128, 64);
		Gauge.mirror = true;
		setRotation(Gauge, 0F, 0F, 0F);
		ValveWheel = new LegacyModelPart(this, 22, 38);
		ValveWheel.addBox(-3F, -3F, 0F, 3, 3, 1);
		ValveWheel.setRotationPoint(6F, 17F, -6F);
		ValveWheel.setTextureSize(128, 64);
		ValveWheel.mirror = true;
		setRotation(ValveWheel, 0F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3,
			float f4, float f5, float progress) {
		Base.render(f5);
		Core.render(f5);
		HeatWindow.render(f5);
		if (active) { HeatGlow.render(f5); }
		HotPipe0.render(f5);
		HotPipe1.render(f5);
		HotPipe2.render(f5);
		HotPipe3.render(f5);
		Ring.render(f5);
		FanA.rotateAngleY = 0F + spin * -1.5F; FanA.render(f5);
		FanB.rotateAngleY = 0F + spin * -1.5F; FanB.render(f5);
		FanHub.rotateAngleY = 0F + spin * -1.5F; FanHub.render(f5);
		Gauge.render(f5);
		ValveWheel.render(f5);
	}

}
