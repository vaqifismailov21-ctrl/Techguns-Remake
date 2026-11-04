package techguns.client.models.machines;

import techguns.client.models.LegacyModelPart;

import net.minecraft.world.entity.Entity;

/** Nuclear generator. Texture: textures/blocks/nuclear_generator.png (128x64). */
public class ModelNuclearGenerator extends PowerModel {

	//fields
	LegacyModelPart Base;
	LegacyModelPart Vessel;
	LegacyModelPart CoreGlow;
	LegacyModelPart CoreBar1;
	LegacyModelPart CoreBar2;
	LegacyModelPart Coolant0;
	LegacyModelPart CoolantTop0;
	LegacyModelPart CoolantIn0;
	LegacyModelPart Coolant1;
	LegacyModelPart CoolantTop1;
	LegacyModelPart CoolantIn1;
	LegacyModelPart RodDrive0;
	LegacyModelPart RodCap0;
	LegacyModelPart RodDrive1;
	LegacyModelPart RodCap1;
	LegacyModelPart RodDrive2;
	LegacyModelPart RodCap2;
	LegacyModelPart RodDrive3;
	LegacyModelPart RodCap3;
	LegacyModelPart CentreRod;
	LegacyModelPart WarnLampOn;
	LegacyModelPart WarnLampOff;

	public ModelNuclearGenerator()
	{
		textureWidth = 128;
		textureHeight = 64;

		Base = new LegacyModelPart(this, 48, 0);
		Base.addBox(-16F, -2F, 0F, 16, 2, 16);
		Base.setRotationPoint(8F, 24F, -8F);
		Base.setTextureSize(128, 64);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		Vessel = new LegacyModelPart(this, 0, 0);
		Vessel.addBox(-12F, -12F, 0F, 12, 12, 12);
		Vessel.setRotationPoint(6F, 22F, -6F);
		Vessel.setTextureSize(128, 64);
		Vessel.mirror = true;
		setRotation(Vessel, 0F, 0F, 0F);
		CoreGlow = new LegacyModelPart(this, 8, 24);
		CoreGlow.addBox(-6F, -5F, 0F, 6, 5, 0);
		CoreGlow.setRotationPoint(3F, 18F, -6.1F);
		CoreGlow.setTextureSize(128, 64);
		CoreGlow.mirror = true;
		setRotation(CoreGlow, 0F, 0F, 0F);
		CoreBar1 = new LegacyModelPart(this, 0, 24);
		CoreBar1.addBox(-1F, -5F, 0F, 1, 5, 1);
		CoreBar1.setRotationPoint(1F, 18F, -6.5F);
		CoreBar1.setTextureSize(128, 64);
		CoreBar1.mirror = true;
		setRotation(CoreBar1, 0F, 0F, 0F);
		CoreBar2 = new LegacyModelPart(this, 4, 24);
		CoreBar2.addBox(-1F, -5F, 0F, 1, 5, 1);
		CoreBar2.setRotationPoint(-0.5F, 18F, -6.5F);
		CoreBar2.setTextureSize(128, 64);
		CoreBar2.mirror = true;
		setRotation(CoreBar2, 0F, 0F, 0F);
		Coolant0 = new LegacyModelPart(this, 112, 0);
		Coolant0.addBox(-2F, -10F, 0F, 2, 10, 2);
		Coolant0.setRotationPoint(8F, 22F, 2F);
		Coolant0.setTextureSize(128, 64);
		Coolant0.mirror = true;
		setRotation(Coolant0, 0F, 0F, 0F);
		CoolantTop0 = new LegacyModelPart(this, 52, 24);
		CoolantTop0.addBox(-2F, -1F, 0F, 2, 1, 3);
		CoolantTop0.setRotationPoint(8F, 12F, 2F);
		CoolantTop0.setTextureSize(128, 64);
		CoolantTop0.mirror = true;
		setRotation(CoolantTop0, 0F, 0F, 0F);
		CoolantIn0 = new LegacyModelPart(this, 72, 24);
		CoolantIn0.addBox(-2F, -2F, 0F, 2, 2, 2);
		CoolantIn0.setRotationPoint(8F, 20F, -4F);
		CoolantIn0.setTextureSize(128, 64);
		CoolantIn0.mirror = true;
		setRotation(CoolantIn0, 0F, 0F, 0F);
		Coolant1 = new LegacyModelPart(this, 120, 0);
		Coolant1.addBox(-2F, -10F, 0F, 2, 10, 2);
		Coolant1.setRotationPoint(-6F, 22F, 2F);
		Coolant1.setTextureSize(128, 64);
		Coolant1.mirror = true;
		setRotation(Coolant1, 0F, 0F, 0F);
		CoolantTop1 = new LegacyModelPart(this, 62, 24);
		CoolantTop1.addBox(-2F, -1F, 0F, 2, 1, 3);
		CoolantTop1.setRotationPoint(-6F, 12F, 2F);
		CoolantTop1.setTextureSize(128, 64);
		CoolantTop1.mirror = true;
		setRotation(CoolantTop1, 0F, 0F, 0F);
		CoolantIn1 = new LegacyModelPart(this, 80, 24);
		CoolantIn1.addBox(-2F, -2F, 0F, 2, 2, 2);
		CoolantIn1.setRotationPoint(-6F, 20F, -4F);
		CoolantIn1.setTextureSize(128, 64);
		CoolantIn1.mirror = true;
		setRotation(CoolantIn1, 0F, 0F, 0F);
		RodDrive0 = new LegacyModelPart(this, 20, 24);
		RodDrive0.addBox(-2F, -3F, 0F, 2, 3, 2);
		RodDrive0.setRotationPoint(4F, 10F, -4F);
		RodDrive0.setTextureSize(128, 64);
		RodDrive0.mirror = true;
		setRotation(RodDrive0, 0F, 0F, 0F);
		RodCap0 = new LegacyModelPart(this, 96, 24);
		RodCap0.addBox(-2F, -1F, 0F, 2, 1, 2);
		RodCap0.setRotationPoint(4F, 7F, -4F);
		RodCap0.setTextureSize(128, 64);
		RodCap0.mirror = true;
		setRotation(RodCap0, 0F, 0F, 0F);
		RodDrive1 = new LegacyModelPart(this, 28, 24);
		RodDrive1.addBox(-2F, -3F, 0F, 2, 3, 2);
		RodDrive1.setRotationPoint(-2F, 10F, -4F);
		RodDrive1.setTextureSize(128, 64);
		RodDrive1.mirror = true;
		setRotation(RodDrive1, 0F, 0F, 0F);
		RodCap1 = new LegacyModelPart(this, 104, 24);
		RodCap1.addBox(-2F, -1F, 0F, 2, 1, 2);
		RodCap1.setRotationPoint(-2F, 7F, -4F);
		RodCap1.setTextureSize(128, 64);
		RodCap1.mirror = true;
		setRotation(RodCap1, 0F, 0F, 0F);
		RodDrive2 = new LegacyModelPart(this, 36, 24);
		RodDrive2.addBox(-2F, -3F, 0F, 2, 3, 2);
		RodDrive2.setRotationPoint(4F, 10F, 2F);
		RodDrive2.setTextureSize(128, 64);
		RodDrive2.mirror = true;
		setRotation(RodDrive2, 0F, 0F, 0F);
		RodCap2 = new LegacyModelPart(this, 112, 24);
		RodCap2.addBox(-2F, -1F, 0F, 2, 1, 2);
		RodCap2.setRotationPoint(4F, 7F, 2F);
		RodCap2.setTextureSize(128, 64);
		RodCap2.mirror = true;
		setRotation(RodCap2, 0F, 0F, 0F);
		RodDrive3 = new LegacyModelPart(this, 44, 24);
		RodDrive3.addBox(-2F, -3F, 0F, 2, 3, 2);
		RodDrive3.setRotationPoint(-2F, 10F, 2F);
		RodDrive3.setTextureSize(128, 64);
		RodDrive3.mirror = true;
		setRotation(RodDrive3, 0F, 0F, 0F);
		RodCap3 = new LegacyModelPart(this, 120, 24);
		RodCap3.addBox(-2F, -1F, 0F, 2, 1, 2);
		RodCap3.setRotationPoint(-2F, 7F, 2F);
		RodCap3.setTextureSize(128, 64);
		RodCap3.mirror = true;
		setRotation(RodCap3, 0F, 0F, 0F);
		CentreRod = new LegacyModelPart(this, 88, 24);
		CentreRod.addBox(-2F, -2F, 0F, 2, 2, 2);
		CentreRod.setRotationPoint(1F, 10F, -1F);
		CentreRod.setTextureSize(128, 64);
		CentreRod.mirror = true;
		setRotation(CentreRod, 0F, 0F, 0F);
		WarnLampOn = new LegacyModelPart(this, 0, 30);
		WarnLampOn.addBox(-2F, -1F, 0F, 2, 1, 1);
		WarnLampOn.setRotationPoint(1F, 10F, -6F);
		WarnLampOn.setTextureSize(128, 64);
		WarnLampOn.mirror = true;
		setRotation(WarnLampOn, 0F, 0F, 0F);
		WarnLampOff = new LegacyModelPart(this, 6, 30);
		WarnLampOff.addBox(-2F, -1F, 0F, 2, 1, 1);
		WarnLampOff.setRotationPoint(1F, 10F, -6F);
		WarnLampOff.setTextureSize(128, 64);
		WarnLampOff.mirror = true;
		setRotation(WarnLampOff, 0F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3,
			float f4, float f5, float progress) {
		Base.render(f5);
		Vessel.render(f5);
		if (active) { CoreGlow.render(f5); }
		CoreBar1.render(f5);
		CoreBar2.render(f5);
		Coolant0.render(f5);
		CoolantTop0.render(f5);
		CoolantIn0.render(f5);
		Coolant1.render(f5);
		CoolantTop1.render(f5);
		CoolantIn1.render(f5);
		RodDrive0.render(f5);
		RodCap0.render(f5);
		RodDrive1.render(f5);
		RodCap1.render(f5);
		RodDrive2.render(f5);
		RodCap2.render(f5);
		RodDrive3.render(f5);
		RodCap3.render(f5);
		CentreRod.render(f5);
		if (active) { WarnLampOn.render(f5); }
		if (!active) { WarnLampOff.render(f5); }
	}

}
