package techguns.client.models.machines;

import techguns.client.models.LegacyModelPart;

import net.minecraft.world.entity.Entity;

/** Advanced solar panel. Texture: textures/blocks/solar_panel_advanced.png (128x64). */
public class ModelSolarPanelAdvanced extends PowerModel {

	//fields
	LegacyModelPart Housing;
	LegacyModelPart PostL;
	LegacyModelPart PostR;
	LegacyModelPart Hinge;
	LegacyModelPart Panel;
	LegacyModelPart LedOn;

	public ModelSolarPanelAdvanced()
	{
		textureWidth = 128;
		textureHeight = 64;

		Housing = new LegacyModelPart(this, 0, 0);
		Housing.addBox(-14F, -4F, 0F, 14, 4, 14);
		Housing.setRotationPoint(7F, 24F, -7F);
		Housing.setTextureSize(128, 64);
		Housing.mirror = true;
		setRotation(Housing, 0F, 0F, 0F);
		PostL = new LegacyModelPart(this, 120, 0);
		PostL.addBox(-2F, -5F, 0F, 2, 5, 2);
		PostL.setRotationPoint(5F, 20F, -1F);
		PostL.setTextureSize(128, 64);
		PostL.mirror = true;
		setRotation(PostL, 0F, 0F, 0F);
		PostR = new LegacyModelPart(this, 0, 18);
		PostR.addBox(-2F, -5F, 0F, 2, 5, 2);
		PostR.setRotationPoint(-3F, 20F, -1F);
		PostR.setTextureSize(128, 64);
		PostR.mirror = true;
		setRotation(PostR, 0F, 0F, 0F);
		Hinge = new LegacyModelPart(this, 8, 18);
		Hinge.addBox(-12F, -1F, 0F, 12, 1, 2);
		Hinge.setRotationPoint(6F, 16F, -1F);
		Hinge.setTextureSize(128, 64);
		Hinge.mirror = true;
		setRotation(Hinge, 0F, 0F, 0F);
		Panel = new LegacyModelPart(this, 56, 0);
		Panel.addBox(-8F, -1F, -8F, 16, 1, 16);
		Panel.setRotationPoint(0F, 15F, 0F);
		Panel.setTextureSize(128, 64);
		Panel.mirror = true;
		setRotation(Panel, 0.3927F, 0F, 0F);
		LedOn = new LegacyModelPart(this, 36, 18);
		LedOn.addBox(-1F, -1F, 0F, 1, 1, 1);
		LedOn.setRotationPoint(-4F, 23F, -7.5F);
		LedOn.setTextureSize(128, 64);
		LedOn.mirror = true;
		setRotation(LedOn, 0F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3,
			float f4, float f5, float progress) {
		Housing.render(f5);
		PostL.render(f5);
		PostR.render(f5);
		Hinge.render(f5);
		Panel.render(f5);
		LedOn.render(f5);
	}

}
