package techguns.client.models.machines;

import techguns.client.models.LegacyModelPart;

import net.minecraft.world.entity.Entity;

/** Fuel generator. Texture: textures/blocks/fuel_generator.png (128x64). */
public class ModelFuelGenerator extends PowerModel {

	//fields
	LegacyModelPart Base;
	LegacyModelPart Body;
	LegacyModelPart Firebox;
	LegacyModelPart FireGlow;
	LegacyModelPart Gauge;
	LegacyModelPart LedOn;
	LegacyModelPart LedOff;
	LegacyModelPart Hopper;
	LegacyModelPart HopperLid;
	LegacyModelPart Exhaust;
	LegacyModelPart ExhaustCap;
	LegacyModelPart FeedPipe;
	LegacyModelPart Flywheel;
	LegacyModelPart FlywheelHub;
	LegacyModelPart Post0;
	LegacyModelPart Post1;
	LegacyModelPart PostBar;

	public ModelFuelGenerator()
	{
		textureWidth = 128;
		textureHeight = 64;

		Base = new LegacyModelPart(this, 54, 0);
		Base.addBox(-16F, -2F, 0F, 16, 2, 16);
		Base.setRotationPoint(8F, 24F, -8F);
		Base.setTextureSize(128, 64);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		Body = new LegacyModelPart(this, 0, 0);
		Body.addBox(-14F, -10F, 0F, 14, 10, 13);
		Body.setRotationPoint(7F, 22F, -6F);
		Body.setTextureSize(128, 64);
		Body.mirror = true;
		setRotation(Body, 0F, 0F, 0F);
		Firebox = new LegacyModelPart(this, 62, 23);
		Firebox.addBox(-8F, -7F, 0F, 8, 7, 1);
		Firebox.setRotationPoint(4F, 21F, -7F);
		Firebox.setTextureSize(128, 64);
		Firebox.mirror = true;
		setRotation(Firebox, 0F, 0F, 0F);
		FireGlow = new LegacyModelPart(this, 108, 23);
		FireGlow.addBox(-6F, -5F, 0F, 6, 5, 0);
		FireGlow.setRotationPoint(3F, 20F, -7.1F);
		FireGlow.setTextureSize(128, 64);
		FireGlow.mirror = true;
		setRotation(FireGlow, 0F, 0F, 0F);
		Gauge = new LegacyModelPart(this, 120, 23);
		Gauge.addBox(-3F, -4F, 0F, 3, 4, 1);
		Gauge.setRotationPoint(-4F, 17F, -7F);
		Gauge.setTextureSize(128, 64);
		Gauge.mirror = true;
		setRotation(Gauge, 0F, 0F, 0F);
		LedOn = new LegacyModelPart(this, 58, 37);
		LedOn.addBox(-1F, -1F, 0F, 1, 1, 1);
		LedOn.setRotationPoint(6F, 15F, -6.6F);
		LedOn.setTextureSize(128, 64);
		LedOn.mirror = true;
		setRotation(LedOn, 0F, 0F, 0F);
		LedOff = new LegacyModelPart(this, 62, 37);
		LedOff.addBox(-1F, -1F, 0F, 1, 1, 1);
		LedOff.setRotationPoint(6F, 15F, -6.5F);
		LedOff.setTextureSize(128, 64);
		LedOff.mirror = true;
		setRotation(LedOff, 0F, 0F, 0F);
		Hopper = new LegacyModelPart(this, 36, 23);
		Hopper.addBox(-6F, -3F, 0F, 6, 3, 7);
		Hopper.setRotationPoint(6F, 12F, -4F);
		Hopper.setTextureSize(128, 64);
		Hopper.mirror = true;
		setRotation(Hopper, 0F, 0F, 0F);
		HopperLid = new LegacyModelPart(this, 0, 37);
		HopperLid.addBox(-7F, -1F, 0F, 7, 1, 2);
		HopperLid.setRotationPoint(6.5F, 9F, -4.5F);
		HopperLid.setTextureSize(128, 64);
		HopperLid.mirror = true;
		setRotation(HopperLid, 0F, 0F, 0F);
		Exhaust = new LegacyModelPart(this, 16, 23);
		Exhaust.addBox(-3F, -8F, 0F, 3, 8, 3);
		Exhaust.setRotationPoint(-2F, 12F, 1F);
		Exhaust.setTextureSize(128, 64);
		Exhaust.mirror = true;
		setRotation(Exhaust, 0F, 0F, 0F);
		ExhaustCap = new LegacyModelPart(this, 80, 23);
		ExhaustCap.addBox(-5F, -1F, 0F, 5, 1, 5);
		ExhaustCap.setRotationPoint(-1F, 4F, 0F);
		ExhaustCap.setTextureSize(128, 64);
		ExhaustCap.mirror = true;
		setRotation(ExhaustCap, 0F, 0F, 0F);
		FeedPipe = new LegacyModelPart(this, 52, 37);
		FeedPipe.addBox(-2F, -1F, 0F, 2, 1, 1);
		FeedPipe.setRotationPoint(0F, 11F, 2F);
		FeedPipe.setTextureSize(128, 64);
		FeedPipe.mirror = true;
		setRotation(FeedPipe, 0F, 0F, 0F);
		Flywheel = new LegacyModelPart(this, 0, 23);
		Flywheel.addBox(-0.5F, -3.5F, -3.5F, 1, 7, 7);
		Flywheel.setRotationPoint(-7.5F, 17.5F, 0.5F);
		Flywheel.setTextureSize(128, 64);
		Flywheel.mirror = true;
		setRotation(Flywheel, 0F, 0F, 0F);
		FlywheelHub = new LegacyModelPart(this, 100, 23);
		FlywheelHub.addBox(-0.5F, -1.5F, -1.5F, 1, 3, 3);
		FlywheelHub.setRotationPoint(-8.5F, 17.5F, 0.5F);
		FlywheelHub.setTextureSize(128, 64);
		FlywheelHub.mirror = true;
		setRotation(FlywheelHub, 0F, 0F, 0F);
		Post0 = new LegacyModelPart(this, 28, 23);
		Post0.addBox(-1F, -10F, 0F, 1, 10, 1);
		Post0.setRotationPoint(8F, 22F, -8F);
		Post0.setTextureSize(128, 64);
		Post0.mirror = true;
		setRotation(Post0, 0F, 0F, 0F);
		Post1 = new LegacyModelPart(this, 32, 23);
		Post1.addBox(-1F, -10F, 0F, 1, 10, 1);
		Post1.setRotationPoint(-7F, 22F, -8F);
		Post1.setTextureSize(128, 64);
		Post1.mirror = true;
		setRotation(Post1, 0F, 0F, 0F);
		PostBar = new LegacyModelPart(this, 18, 37);
		PostBar.addBox(-16F, -1F, 0F, 16, 1, 1);
		PostBar.setRotationPoint(8F, 12F, -8F);
		PostBar.setTextureSize(128, 64);
		PostBar.mirror = true;
		setRotation(PostBar, 0F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3,
			float f4, float f5, float progress) {
		Base.render(f5);
		Body.render(f5);
		Firebox.render(f5);
		if (active) { FireGlow.render(f5); }
		Gauge.render(f5);
		if (active) { LedOn.render(f5); }
		if (!active) { LedOff.render(f5); }
		Hopper.render(f5);
		HopperLid.render(f5);
		Exhaust.render(f5);
		ExhaustCap.render(f5);
		FeedPipe.render(f5);
		Flywheel.rotateAngleX = 0F + spin * -1F; Flywheel.render(f5);
		FlywheelHub.rotateAngleX = 0F + spin * -1F; FlywheelHub.render(f5);
		Post0.render(f5);
		Post1.render(f5);
		PostBar.render(f5);
	}

}
