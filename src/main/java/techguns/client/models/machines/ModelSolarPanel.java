package techguns.client.models.machines;

import techguns.client.models.LegacyModelPart;

import net.minecraft.world.entity.Entity;

/** Solar panel. Texture: textures/blocks/solar_panel.png (128x64). */
public class ModelSolarPanel extends PowerModel {

	//fields
	LegacyModelPart Foot;
	LegacyModelPart Post;
	LegacyModelPart Bracket;
	LegacyModelPart Panel;
	LegacyModelPart JunctionBox;

	public ModelSolarPanel()
	{
		textureWidth = 128;
		textureHeight = 64;

		Foot = new LegacyModelPart(this, 56, 0);
		Foot.addBox(-10F, -2F, 0F, 10, 2, 10);
		Foot.setRotationPoint(5F, 24F, -5F);
		Foot.setTextureSize(128, 64);
		Foot.mirror = true;
		setRotation(Foot, 0F, 0F, 0F);
		Post = new LegacyModelPart(this, 96, 0);
		Post.addBox(-2F, -6F, 0F, 2, 6, 2);
		Post.setRotationPoint(1F, 22F, -1F);
		Post.setTextureSize(128, 64);
		Post.mirror = true;
		setRotation(Post, 0F, 0F, 0F);
		Bracket = new LegacyModelPart(this, 104, 0);
		Bracket.addBox(-4F, -1F, 0F, 4, 1, 4);
		Bracket.setRotationPoint(2F, 17F, -2F);
		Bracket.setTextureSize(128, 64);
		Bracket.mirror = true;
		setRotation(Bracket, 0F, 0F, 0F);
		Panel = new LegacyModelPart(this, 0, 0);
		Panel.addBox(-7F, -1F, -7F, 14, 1, 14);
		Panel.setRotationPoint(0F, 16F, 0F);
		Panel.setTextureSize(128, 64);
		Panel.mirror = true;
		setRotation(Panel, 0.3927F, 0F, 0F);
		JunctionBox = new LegacyModelPart(this, 0, 15);
		JunctionBox.addBox(-2F, 1F, 2F, 4, 1, 3);
		JunctionBox.setRotationPoint(0F, 16F, 0F);
		JunctionBox.setTextureSize(128, 64);
		JunctionBox.mirror = true;
		setRotation(JunctionBox, 0.3927F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3,
			float f4, float f5, float progress) {
		Foot.render(f5);
		Post.render(f5);
		Bracket.render(f5);
		Panel.render(f5);
		JunctionBox.render(f5);
	}

}
