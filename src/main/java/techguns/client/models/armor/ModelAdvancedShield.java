package techguns.client.models.armor;

import techguns.client.models.LegacyModelBase;
import techguns.client.models.LegacyModelPart;
import net.minecraft.world.entity.Entity;

public class ModelAdvancedShield extends LegacyModelBase {
    public LegacyModelPart Plate;
    public LegacyModelPart Handle;
    public LegacyModelPart TopBorder;
    public LegacyModelPart BottomBorder;
    public LegacyModelPart SideBorder0;
    public LegacyModelPart SideBorder1;
    public LegacyModelPart Glass;
    public LegacyModelPart SideBorder2;
    public LegacyModelPart SideBorder3;

    public ModelAdvancedShield() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.SideBorder0 = new LegacyModelPart(this, 20, 0);
        this.SideBorder0.setRotationPoint(4.0F, -4.0F, -2.0F);
        this.SideBorder0.addBox(0.0F, 0.0F, 0.0F, 3, 15, 1, 0.0F);
        this.setRotateAngle(SideBorder0, 0.0F, -0.39269908169872414F, 0.0F);
        this.BottomBorder = new LegacyModelPart(this, 0, 16);
        this.BottomBorder.setRotationPoint(-4.0F, 10.0F, -2.0F);
        this.BottomBorder.addBox(0.0F, 0.0F, 0.0F, 8, 2, 1, 0.0F);
        this.setRotateAngle(BottomBorder, 0.39269908169872414F, 0.0F, 0.0F);
        this.Glass = new LegacyModelPart(this, 26, 17);
        this.Glass.setRotationPoint(-4.0F, -10.5F, -1.8F);
        this.Glass.addBox(0.0F, 0.0F, 0.0F, 8, 7, 1, 0.0F);
        this.SideBorder3 = new LegacyModelPart(this, 29, 0);
        this.SideBorder3.setRotationPoint(4.0F, -11.0F, -2.0F);
        this.SideBorder3.addBox(0.0F, 0.0F, 0.0F, 2, 7, 1, 0.0F);
        this.setRotateAngle(SideBorder3, 0.0F, -0.39269908169872414F, 0.0F);
        this.Handle = new LegacyModelPart(this, 14, 20);
        this.Handle.setRotationPoint(-1.0F, -3.0F, -1.0F);
        this.Handle.addBox(0.0F, 0.0F, 0.0F, 2, 6, 6, 0.0F);
        this.SideBorder1 = new LegacyModelPart(this, 20, 0);
        this.SideBorder1.mirror = true;
        this.SideBorder1.setRotationPoint(-4.0F, -4.0F, -2.0F);
        this.SideBorder1.addBox(-3.0F, 0.0F, 0.0F, 3, 15, 1, 0.0F);
        this.setRotateAngle(SideBorder1, 0.0F, 0.39269908169872414F, 0.0F);
        this.Plate = new LegacyModelPart(this, 0, 0);
        this.Plate.setRotationPoint(-4.0F, -4.0F, -2.0F);
        this.Plate.addBox(0.0F, 0.0F, 0.0F, 8, 14, 1, 0.0F);
        this.TopBorder = new LegacyModelPart(this, 0, 20);
        this.TopBorder.setRotationPoint(-4.0F, -10.5F, -2.1F);
        this.TopBorder.addBox(0.0F, -1.0F, 0.0F, 8, 1, 1, 0.0F);
        this.setRotateAngle(TopBorder, -0.39269908169872414F, 0.0F, 0.0F);
        this.SideBorder2 = new LegacyModelPart(this, 29, 0);
        this.SideBorder2.mirror = true;
        this.SideBorder2.setRotationPoint(-4.0F, -11.0F, -2.0F);
        this.SideBorder2.addBox(-2.0F, 0.0F, 0.0F, 2, 7, 1, 0.0F);
        this.setRotateAngle(SideBorder2, 0.0F, 0.39269908169872414F, 0.0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { 
        this.SideBorder0.render(f5);
        this.BottomBorder.render(f5);
        this.Glass.render(f5);
        this.SideBorder3.render(f5);
        this.Handle.render(f5);
        this.SideBorder1.render(f5);
        this.Plate.render(f5);
        this.TopBorder.render(f5);
        this.SideBorder2.render(f5);
    }

    public void setRotateAngle(LegacyModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}