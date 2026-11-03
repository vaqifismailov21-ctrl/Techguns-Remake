package techguns.client.models.guns;

import techguns.client.models.LegacyModelPart;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.entity.Entity;
import techguns.client.models.ModelMultipart;

public class ModelBlasterRifle extends ModelMultipart {
    public LegacyModelPart Grip1;
    public LegacyModelPart Trigger01;
    public LegacyModelPart Trigger02;
    public LegacyModelPart BarrelGuard;
    public LegacyModelPart Barrel;
    public LegacyModelPart Receiver03;
    public LegacyModelPart BarrelRails04;
    public LegacyModelPart MagSocket;
    public LegacyModelPart Receiver01;
    public LegacyModelPart BarrelCooling1;
    public LegacyModelPart BarrelCooling2;
    public LegacyModelPart MagSocket_1;
    public LegacyModelPart Magazine;
    public LegacyModelPart MagRoundThing1;
    public LegacyModelPart MagRoundThing2;
    public LegacyModelPart MagSocket2;
    public LegacyModelPart Receiver01_1;
    public LegacyModelPart ReceiverTop;
    public LegacyModelPart SideBox01;
    public LegacyModelPart scope02;
    public LegacyModelPart scope01;
    public LegacyModelPart scope03;
    public LegacyModelPart FrontPart;

    public ModelBlasterRifle() {
        this.textureWidth = 128;
        this.textureHeight = 64;
        this.Magazine = new LegacyModelPart(this, 92, 18);
        this.Magazine.setRotationPoint(5.0F, -3.5F, -3.5F);
        this.Magazine.addBox(0.0F, 0.0F, 0.0F, 5, 2, 5, 0.0F);
        this.Receiver01_1 = new LegacyModelPart(this, 0, 31);
        this.Receiver01_1.setRotationPoint(-2.5F, -5.0F, -6.0F);
        this.Receiver01_1.addBox(0.0F, 0.0F, 0.0F, 4, 5, 28, 0.0F);
        this.SideBox01 = new LegacyModelPart(this, 77, 18);
        this.SideBox01.setRotationPoint(1.5F, -8.0F, 6.0F);
        this.SideBox01.addBox(0.0F, 0.0F, 0.0F, 3, 5, 8, 0.0F);
        this.scope01 = new LegacyModelPart(this, 0, 13);
        this.scope01.setRotationPoint(-0.5F, -7.5F, 3.0F);
        this.scope01.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 5, 0.0F);
        this.setRotation(scope01, 0.0F, 0.0F, 0.7853981633974483F);
        this.Trigger02 = new LegacyModelPart(this, 83, 5);
        this.Trigger02.setRotationPoint(-1.0F, 1.5F, 5.0F);
        this.Trigger02.addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
        this.setRotation(Trigger02, -0.2617993877991494F, -0.0F, 0.0F);
        this.Receiver03 = new LegacyModelPart(this, 92, 0);
        this.Receiver03.setRotationPoint(-2.0F, 0.0F, -5.0F);
        this.Receiver03.addBox(0.0F, 0.0F, 0.0F, 3, 2, 15, 0.0F);
        this.BarrelRails04 = new LegacyModelPart(this, 37, 36);
        this.BarrelRails04.setRotationPoint(-1.5F, -0.5F, -20.0F);
        this.BarrelRails04.addBox(0.0F, 0.0F, 0.0F, 2, 2, 15, 0.0F);
        this.MagRoundThing1 = new LegacyModelPart(this, 97, 26);
        this.MagRoundThing1.setRotationPoint(2.75F, -4.25F, -4.0F);
        this.MagRoundThing1.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 6, 0.0F);
        this.setRotation(MagRoundThing1, 0.0F, 0.0F, 0.7853981633974483F);
        this.BarrelGuard = new LegacyModelPart(this, 82, 41);
        this.BarrelGuard.setRotationPoint(-0.5F, -2.5F, -25.0F);
        this.BarrelGuard.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 19, 0.0F);
        this.setRotation(BarrelGuard, 0.0F, 0.0F, 0.7853981633974483F);
        this.MagSocket2 = new LegacyModelPart(this, 92, 0);
        this.MagSocket2.setRotationPoint(2.0F, -5.0F, 2.0F);
        this.MagSocket2.addBox(0.0F, 0.0F, 0.0F, 3, 1, 1, 0.0F);
        this.FrontPart = new LegacyModelPart(this, 57, 41);
        this.FrontPart.setRotationPoint(-2.0F, -1.25F, -24.0F);
        this.FrontPart.addBox(0.0F, 0.0F, 0.0F, 3, 2, 7, 0.0F);
        this.setRotation(FrontPart, -0.22759093446006054F, 0.0F, 0.0F);
        this.BarrelCooling2 = new LegacyModelPart(this, 66, 38);
        this.BarrelCooling2.setRotationPoint(-0.5F, -2.5F, -22.0F);
        this.BarrelCooling2.addBox(-0.5F, -2.5F, 0.0F, 1, 5, 16, 0.0F);
        this.setRotation(BarrelCooling2, 0.0F, 0.0F, -0.7853981633974483F);
        this.ReceiverTop = new LegacyModelPart(this, 0, 0);
        this.ReceiverTop.setRotationPoint(-1.5F, -6.0F, -5.0F);
        this.ReceiverTop.addBox(0.0F, 0.0F, 0.0F, 2, 1, 27, 0.0F);
        this.Grip1 = new LegacyModelPart(this, 68, 0);
        this.Grip1.setRotationPoint(-2.0F, 2.0F, 6.0F);
        this.Grip1.addBox(0.0F, 0.0F, 0.0F, 3, 9, 4, 0.0F);
        this.setRotation(Grip1, 0.4461433291435241F, 0.0F, 0.0F);
        this.scope02 = new LegacyModelPart(this, 34, 14);
        this.scope02.setRotationPoint(-0.5F, -8.0F, 8.0F);
        this.scope02.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 8, 0.0F);
        this.setRotation(scope02, 0.0F, 0.0F, 0.7853981633974483F);
        this.BarrelCooling1 = new LegacyModelPart(this, 66, 38);
        this.BarrelCooling1.setRotationPoint(-0.5F, -2.5F, -22.0F);
        this.BarrelCooling1.addBox(-0.5F, -2.5F, 0.0F, 1, 5, 16, 0.0F);
        this.setRotation(BarrelCooling1, 0.0F, 0.0F, 0.7853981633974483F);
        this.MagSocket = new LegacyModelPart(this, 83, 0);
        this.MagSocket.setRotationPoint(-1.5F, 2.0F, 2.0F);
        this.MagSocket.addBox(0.0F, 0.0F, 0.0F, 2, 3, 1, 0.0F);
        this.MagSocket_1 = new LegacyModelPart(this, 106, 19);
        this.MagSocket_1.setRotationPoint(1.0F, -4.0F, -4.0F);
        this.MagSocket_1.addBox(0.0F, 0.0F, 0.0F, 4, 3, 7, 0.0F);
        this.Barrel = new LegacyModelPart(this, 0, 54);
        this.Barrel.setRotationPoint(-0.5F, -2.5F, -26.0F);
        this.Barrel.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 1, 0.0F);
        this.setRotation(Barrel, 0.0F, 0.0F, 0.7853981633974483F);
        this.Trigger01 = new LegacyModelPart(this, 78, 9);
        this.Trigger01.setRotationPoint(-1.5F, 4.0F, 3.0F);
        this.Trigger01.addBox(0.0F, 0.0F, 0.0F, 2, 1, 5, 0.0F);
        this.MagRoundThing2 = new LegacyModelPart(this, 97, 26);
        this.MagRoundThing2.setRotationPoint(4.25F, -4.25F, -4.0F);
        this.MagRoundThing2.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 6, 0.0F);
        this.setRotation(MagRoundThing2, 0.0F, 0.0F, 0.7853981633974483F);
        this.Receiver01 = new LegacyModelPart(this, 0, 0);
        this.Receiver01.setRotationPoint(-0.5F, -3.0F, 22.0F);
        this.Receiver01.addBox(-2.5F, -2.5F, 0.0F, 5, 5, 2, 0.0F);
        this.setRotation(Receiver01, 0.0F, -0.0F, 0.7853981633974483F);
        this.scope03 = new LegacyModelPart(this, 1, 8);
        this.scope03.setRotationPoint(-0.5F, -7.0F, 1.0F);
        this.scope03.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 2, 0.0F);
        this.setRotation(scope03, 0.0F, 0.0F, 0.7853981633974483F);
    }

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, int ammoLeft,
			float reloadProgress, ItemTransforms.TransformType transformType, int part, float fireProgress, float chargeProgress) {

        this.Magazine.render(scale);
        this.Receiver01_1.render(scale);
        this.SideBox01.render(scale);
        this.scope01.render(scale);
        this.Trigger02.render(scale);
        this.Receiver03.render(scale);
        this.BarrelRails04.render(scale);
        this.MagRoundThing1.render(scale);
        this.BarrelGuard.render(scale);
        this.MagSocket2.render(scale);
        this.FrontPart.render(scale);
        this.BarrelCooling2.render(scale);
        this.ReceiverTop.render(scale);
        this.Grip1.render(scale);
        this.scope02.render(scale);
        this.BarrelCooling1.render(scale);
        this.MagSocket.render(scale);
        this.MagSocket_1.render(scale);
        this.Barrel.render(scale);
        this.Trigger01.render(scale);
        this.MagRoundThing2.render(scale);
        this.Receiver01.render(scale);
        this.scope03.render(scale);
    }

}