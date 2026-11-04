package techguns.client.models.machines;

import techguns.client.models.LegacyModelPart;

import net.minecraft.world.entity.Entity;

/** Wind turbine base. Texture: textures/blocks/wind_turbine_base.png (128x64). */
public class ModelWindTurbineBase extends PowerModel {

	//fields
	LegacyModelPart Base;
	LegacyModelPart Housing;
	LegacyModelPart Door;
	LegacyModelPart Mast;
	LegacyModelPart LedOn;

	public ModelWindTurbineBase()
	{
		textureWidth = 128;
		textureHeight = 64;

		Base = new LegacyModelPart(this, 48, 0);
		Base.addBox(-16F, -3F, 0F, 16, 3, 16);
		Base.setRotationPoint(8F, 24F, -8F);
		Base.setTextureSize(128, 64);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		Housing = new LegacyModelPart(this, 0, 0);
		Housing.addBox(-12F, -8F, 0F, 12, 8, 12);
		Housing.setRotationPoint(6F, 21F, -6F);
		Housing.setTextureSize(128, 64);
		Housing.mirror = true;
		setRotation(Housing, 0F, 0F, 0F);
		Door = new LegacyModelPart(this, 24, 20);
		Door.addBox(-4F, -6F, 0F, 4, 6, 1);
		Door.setRotationPoint(2F, 21F, -7F);
		Door.setTextureSize(128, 64);
		Door.mirror = true;
		setRotation(Door, 0F, 0F, 0F);
		Mast = new LegacyModelPart(this, 0, 20);
		Mast.addBox(-6F, -5F, 0F, 6, 5, 6);
		Mast.setRotationPoint(3F, 13F, -3F);
		Mast.setTextureSize(128, 64);
		Mast.mirror = true;
		setRotation(Mast, 0F, 0F, 0F);
		LedOn = new LegacyModelPart(this, 34, 20);
		LedOn.addBox(-1F, -1F, 0F, 1, 1, 1);
		LedOn.setRotationPoint(-3F, 16F, -6.5F);
		LedOn.setTextureSize(128, 64);
		LedOn.mirror = true;
		setRotation(LedOn, 0F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3,
			float f4, float f5, float progress) {
		Base.render(f5);
		Housing.render(f5);
		Door.render(f5);
		Mast.render(f5);
		LedOn.render(f5);
	}

}
