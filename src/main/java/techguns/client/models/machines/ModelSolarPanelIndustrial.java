package techguns.client.models.machines;

import techguns.client.models.LegacyModelPart;

import net.minecraft.world.entity.Entity;

/** Industrial solar panel. Texture: textures/blocks/solar_panel_industrial.png (128x64). */
public class ModelSolarPanelIndustrial extends PowerModel {

	//fields
	LegacyModelPart Inverter;
	LegacyModelPart Motor;
	LegacyModelPart Strut1;
	LegacyModelPart Strut2;
	LegacyModelPart Rail;
	LegacyModelPart PanelFront;
	LegacyModelPart PanelBack;

	public ModelSolarPanelIndustrial()
	{
		textureWidth = 128;
		textureHeight = 64;

		Inverter = new LegacyModelPart(this, 0, 0);
		Inverter.addBox(-16F, -6F, 0F, 16, 6, 16);
		Inverter.setRotationPoint(8F, 24F, -8F);
		Inverter.setTextureSize(128, 64);
		Inverter.mirror = true;
		setRotation(Inverter, 0F, 0F, 0F);
		Motor = new LegacyModelPart(this, 88, 22);
		Motor.addBox(-2F, -2F, 0F, 2, 2, 2);
		Motor.setRotationPoint(1F, 18F, -5F);
		Motor.setTextureSize(128, 64);
		Motor.mirror = true;
		setRotation(Motor, 0F, 0F, 0F);
		Strut1 = new LegacyModelPart(this, 48, 22);
		Strut1.addBox(-1F, -4F, 0F, 1, 4, 1);
		Strut1.setRotationPoint(6F, 18F, 3F);
		Strut1.setTextureSize(128, 64);
		Strut1.mirror = true;
		setRotation(Strut1, 0F, 0F, 0F);
		Strut2 = new LegacyModelPart(this, 52, 22);
		Strut2.addBox(-1F, -4F, 0F, 1, 4, 1);
		Strut2.setRotationPoint(-5F, 18F, 3F);
		Strut2.setTextureSize(128, 64);
		Strut2.mirror = true;
		setRotation(Strut2, 0F, 0F, 0F);
		Rail = new LegacyModelPart(this, 56, 22);
		Rail.addBox(-14F, -2F, 0F, 14, 2, 2);
		Rail.setRotationPoint(7F, 18F, -1F);
		Rail.setTextureSize(128, 64);
		Rail.mirror = true;
		setRotation(Rail, 0F, 0F, 0F);
		PanelFront = new LegacyModelPart(this, 64, 0);
		PanelFront.addBox(-8F, -0.5F, -4F, 16, 1, 8);
		PanelFront.setRotationPoint(0F, 15.5F, -4F);
		PanelFront.setTextureSize(128, 64);
		PanelFront.mirror = true;
		setRotation(PanelFront, 0.3927F, 0F, 0F);
		PanelBack = new LegacyModelPart(this, 0, 22);
		PanelBack.addBox(-8F, -0.5F, -4F, 16, 1, 8);
		PanelBack.setRotationPoint(0F, 13.5F, 4F);
		PanelBack.setTextureSize(128, 64);
		PanelBack.mirror = true;
		setRotation(PanelBack, 0.3927F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3,
			float f4, float f5, float progress) {
		Inverter.render(f5);
		Motor.render(f5);
		Strut1.render(f5);
		Strut2.render(f5);
		Rail.render(f5);
		PanelFront.render(f5);
		PanelBack.render(f5);
	}

}
