package techguns.client.models.machines;

import techguns.client.models.LegacyModelPart;

import net.minecraft.world.entity.Entity;

/** Wind turbine mast. Texture: textures/blocks/wind_turbine_mast.png (128x64). */
public class ModelWindTurbineMast extends PowerModel {

	//fields
	LegacyModelPart Mast;
	LegacyModelPart RailL;
	LegacyModelPart RailR;
	LegacyModelPart Rung0;
	LegacyModelPart Rung1;
	LegacyModelPart Rung2;
	LegacyModelPart Rung3;

	public ModelWindTurbineMast()
	{
		textureWidth = 128;
		textureHeight = 64;

		Mast = new LegacyModelPart(this, 0, 0);
		Mast.addBox(-6F, -16F, 0F, 6, 16, 6);
		Mast.setRotationPoint(3F, 24F, -3F);
		Mast.setTextureSize(128, 64);
		Mast.mirror = true;
		setRotation(Mast, 0F, 0F, 0F);
		RailL = new LegacyModelPart(this, 24, 0);
		RailL.addBox(-1F, -16F, 0F, 1, 16, 1);
		RailL.setRotationPoint(2F, 24F, 3F);
		RailL.setTextureSize(128, 64);
		RailL.mirror = true;
		setRotation(RailL, 0F, 0F, 0F);
		RailR = new LegacyModelPart(this, 28, 0);
		RailR.addBox(-1F, -16F, 0F, 1, 16, 1);
		RailR.setRotationPoint(-1F, 24F, 3F);
		RailR.setTextureSize(128, 64);
		RailR.mirror = true;
		setRotation(RailR, 0F, 0F, 0F);
		Rung0 = new LegacyModelPart(this, 32, 0);
		Rung0.addBox(-2F, -1F, 0F, 2, 1, 1);
		Rung0.setRotationPoint(1F, 22F, 3F);
		Rung0.setTextureSize(128, 64);
		Rung0.mirror = true;
		setRotation(Rung0, 0F, 0F, 0F);
		Rung1 = new LegacyModelPart(this, 38, 0);
		Rung1.addBox(-2F, -1F, 0F, 2, 1, 1);
		Rung1.setRotationPoint(1F, 18F, 3F);
		Rung1.setTextureSize(128, 64);
		Rung1.mirror = true;
		setRotation(Rung1, 0F, 0F, 0F);
		Rung2 = new LegacyModelPart(this, 44, 0);
		Rung2.addBox(-2F, -1F, 0F, 2, 1, 1);
		Rung2.setRotationPoint(1F, 14F, 3F);
		Rung2.setTextureSize(128, 64);
		Rung2.mirror = true;
		setRotation(Rung2, 0F, 0F, 0F);
		Rung3 = new LegacyModelPart(this, 50, 0);
		Rung3.addBox(-2F, -1F, 0F, 2, 1, 1);
		Rung3.setRotationPoint(1F, 10F, 3F);
		Rung3.setTextureSize(128, 64);
		Rung3.mirror = true;
		setRotation(Rung3, 0F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3,
			float f4, float f5, float progress) {
		Mast.render(f5);
		RailL.render(f5);
		RailR.render(f5);
		Rung0.render(f5);
		Rung1.render(f5);
		Rung2.render(f5);
		Rung3.render(f5);
	}

}
