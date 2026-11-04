package techguns.client.models.machines;

import techguns.client.models.LegacyModelPart;

import net.minecraft.world.entity.Entity;

/** Wind turbine head with rotor. Texture: textures/blocks/wind_turbine_head.png (128x64). */
public class ModelWindTurbineHead extends PowerModel {

	//fields
	LegacyModelPart Mast;
	LegacyModelPart Bearing;
	LegacyModelPart Nacelle;
	LegacyModelPart Tail;
	LegacyModelPart Hub;
	LegacyModelPart Nose;
	LegacyModelPart Blade0;
	LegacyModelPart Blade1;
	LegacyModelPart Blade2;

	public ModelWindTurbineHead()
	{
		textureWidth = 128;
		textureHeight = 64;

		Mast = new LegacyModelPart(this, 98, 0);
		Mast.addBox(-6F, -3F, 0F, 6, 3, 6);
		Mast.setRotationPoint(3F, 24F, -3F);
		Mast.setTextureSize(128, 64);
		Mast.mirror = true;
		setRotation(Mast, 0F, 0F, 0F);
		Bearing = new LegacyModelPart(this, 66, 0);
		Bearing.addBox(-8F, -1F, 0F, 8, 1, 8);
		Bearing.setRotationPoint(4F, 21F, -4F);
		Bearing.setTextureSize(128, 64);
		Bearing.mirror = true;
		setRotation(Bearing, 0F, 0F, 0F);
		Nacelle = new LegacyModelPart(this, 24, 0);
		Nacelle.addBox(-8F, -7F, 0F, 8, 7, 13);
		Nacelle.setRotationPoint(4F, 20F, -5F);
		Nacelle.setTextureSize(128, 64);
		Nacelle.mirror = true;
		setRotation(Nacelle, 0F, 0F, 0F);
		Tail = new LegacyModelPart(this, 14, 21);
		Tail.addBox(-2F, -3F, 0F, 2, 3, 3);
		Tail.setRotationPoint(1F, 18F, 8F);
		Tail.setTextureSize(128, 64);
		Tail.mirror = true;
		setRotation(Tail, 0F, 0F, 0F);
		Hub = new LegacyModelPart(this, 0, 21);
		Hub.addBox(-2F, -2F, -1.5F, 4, 4, 3);
		Hub.setRotationPoint(0F, 17F, -6.5F);
		Hub.setTextureSize(128, 64);
		Hub.mirror = true;
		setRotation(Hub, 0F, 0F, 0F);
		Nose = new LegacyModelPart(this, 24, 21);
		Nose.addBox(-1F, -1F, -2.5F, 2, 2, 1);
		Nose.setRotationPoint(0F, 17F, -6.5F);
		Nose.setTextureSize(128, 64);
		Nose.mirror = true;
		setRotation(Nose, 0F, 0F, 0F);
		Blade0 = new LegacyModelPart(this, 0, 0);
		Blade0.addBox(-1.5F, -22F, -0.5F, 3, 20, 1);
		Blade0.setRotationPoint(0F, 17F, -6.5F);
		Blade0.setTextureSize(128, 64);
		Blade0.mirror = true;
		setRotation(Blade0, 0F, 0F, 0F);
		Blade1 = new LegacyModelPart(this, 8, 0);
		Blade1.addBox(-1.5F, -22F, -0.5F, 3, 20, 1);
		Blade1.setRotationPoint(0F, 17F, -6.5F);
		Blade1.setTextureSize(128, 64);
		Blade1.mirror = true;
		setRotation(Blade1, 0F, 0F, 2.0944F);
		Blade2 = new LegacyModelPart(this, 16, 0);
		Blade2.addBox(-1.5F, -22F, -0.5F, 3, 20, 1);
		Blade2.setRotationPoint(0F, 17F, -6.5F);
		Blade2.setTextureSize(128, 64);
		Blade2.mirror = true;
		setRotation(Blade2, 0F, 0F, 4.1888F);
	}

	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3,
			float f4, float f5, float progress) {
		Mast.render(f5);
		Bearing.render(f5);
		Nacelle.render(f5);
		Tail.render(f5);
		Hub.rotateAngleZ = 0F + spin * 1F; Hub.render(f5);
		Nose.rotateAngleZ = 0F + spin * 1F; Nose.render(f5);
		Blade0.rotateAngleZ = 0F + spin * 1F; Blade0.render(f5);
		Blade1.rotateAngleZ = 2.0944F + spin * 1F; Blade1.render(f5);
		Blade2.rotateAngleZ = 4.1888F + spin * 1F; Blade2.render(f5);
	}

}
