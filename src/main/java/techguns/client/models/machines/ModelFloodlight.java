package techguns.client.models.machines;

import techguns.client.models.LegacyModelPart;

import net.minecraft.world.entity.Entity;

/** Floodlight. Texture: textures/blocks/floodlight.png (128x64). */
public class ModelFloodlight extends PowerModel {

	//fields
	LegacyModelPart Foot;
	LegacyModelPart Swivel;
	LegacyModelPart YokeBar;
	LegacyModelPart YokeL;
	LegacyModelPart YokeR;
	LegacyModelPart Head;
	LegacyModelPart Hood;
	LegacyModelPart Rim;
	LegacyModelPart Lens;
	LegacyModelPart LensLit;

	public ModelFloodlight()
	{
		textureWidth = 128;
		textureHeight = 64;

		Foot = new LegacyModelPart(this, 36, 0);
		Foot.addBox(-8F, -2F, 0F, 8, 2, 8);
		Foot.setRotationPoint(4F, 24F, -4F);
		Foot.setTextureSize(128, 64);
		Foot.mirror = true;
		setRotation(Foot, 0F, 0F, 0F);
		Swivel = new LegacyModelPart(this, 98, 0);
		Swivel.addBox(-4F, -2F, 0F, 4, 2, 4);
		Swivel.setRotationPoint(2F, 22F, -2F);
		Swivel.setTextureSize(128, 64);
		Swivel.mirror = true;
		setRotation(Swivel, 0F, 0F, 0F);
		YokeBar = new LegacyModelPart(this, 36, 18);
		YokeBar.addBox(-10F, -1F, 0F, 10, 1, 2);
		YokeBar.setRotationPoint(5F, 21F, -1F);
		YokeBar.setTextureSize(128, 64);
		YokeBar.mirror = true;
		setRotation(YokeBar, 0F, 0F, 0F);
		YokeL = new LegacyModelPart(this, 86, 0);
		YokeL.addBox(-1F, -7F, 0F, 1, 7, 2);
		YokeL.setRotationPoint(5F, 20F, -1F);
		YokeL.setTextureSize(128, 64);
		YokeL.mirror = true;
		setRotation(YokeL, 0F, 0F, 0F);
		YokeR = new LegacyModelPart(this, 92, 0);
		YokeR.addBox(-1F, -7F, 0F, 1, 7, 2);
		YokeR.setRotationPoint(-4F, 20F, -1F);
		YokeR.setTextureSize(128, 64);
		YokeR.mirror = true;
		setRotation(YokeR, 0F, 0F, 0F);
		Head = new LegacyModelPart(this, 0, 0);
		Head.addBox(-4F, -4F, -5F, 8, 8, 10);
		Head.setRotationPoint(0F, 15F, 0F);
		Head.setTextureSize(128, 64);
		Head.mirror = true;
		setRotation(Head, -0.3927F, 0F, 0F);
		Hood = new LegacyModelPart(this, 12, 18);
		Hood.addBox(-4F, -5F, -7F, 8, 1, 4);
		Hood.setRotationPoint(0F, 15F, 0F);
		Hood.setTextureSize(128, 64);
		Hood.mirror = true;
		setRotation(Hood, -0.3927F, 0F, 0F);
		Rim = new LegacyModelPart(this, 68, 0);
		Rim.addBox(-4F, -4F, -6F, 8, 8, 1);
		Rim.setRotationPoint(0F, 15F, 0F);
		Rim.setTextureSize(128, 64);
		Rim.mirror = true;
		setRotation(Rim, -0.3927F, 0F, 0F);
		Lens = new LegacyModelPart(this, 114, 0);
		Lens.addBox(-3F, -3F, -6.1F, 6, 6, 0);
		Lens.setRotationPoint(0F, 15F, 0F);
		Lens.setTextureSize(128, 64);
		Lens.mirror = true;
		setRotation(Lens, -0.3927F, 0F, 0F);
		LensLit = new LegacyModelPart(this, 0, 18);
		LensLit.addBox(-3F, -3F, -6.1F, 6, 6, 0);
		LensLit.setRotationPoint(0F, 15F, 0F);
		LensLit.setTextureSize(128, 64);
		LensLit.mirror = true;
		setRotation(LensLit, -0.3927F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3,
			float f4, float f5, float progress) {
		Foot.render(f5);
		Swivel.render(f5);
		YokeBar.render(f5);
		YokeL.render(f5);
		YokeR.render(f5);
		Head.render(f5);
		Hood.render(f5);
		Rim.render(f5);
		if (!active) { Lens.render(f5); }
		if (active) { LensLit.render(f5); }
	}

}
