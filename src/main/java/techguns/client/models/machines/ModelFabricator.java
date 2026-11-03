package techguns.client.models.machines;

import techguns.client.models.LegacyModelPart;

import net.minecraft.world.entity.Entity;

public class ModelFabricator extends ModelMachine {

	  public LegacyModelPart Glass;
	    public LegacyModelPart basePlate;
	    public LegacyModelPart Corner1;
	    public LegacyModelPart Corner2;
	    public LegacyModelPart Corner3;
	    public LegacyModelPart Corner4;
	    public LegacyModelPart SidePlate1;
	    public LegacyModelPart SidePlate2;
	    public LegacyModelPart SidePlate3;
	    public LegacyModelPart SidePlate4;
	    public LegacyModelPart Controller;
	    public LegacyModelPart Connection1;
	    public LegacyModelPart Connection2;
	    public LegacyModelPart Connection3;
	    public LegacyModelPart Connection4;
	    public LegacyModelPart Connection5;
	    public LegacyModelPart Connection6;
	    public LegacyModelPart Connection7;
	    public LegacyModelPart SideBarTop1;
	    public LegacyModelPart SideBarTop2;
	    public LegacyModelPart SideBarTop3;
	    public LegacyModelPart SideBarTop4;
	    public LegacyModelPart innerPlate;
	    public LegacyModelPart MidPlate;
	    public LegacyModelPart Frame1;
	    public LegacyModelPart Frame2;
	    public LegacyModelPart Frame3;
	    public LegacyModelPart Frame4;
	    public LegacyModelPart FrameTop1;
	    public LegacyModelPart FrameTop2;
	    public LegacyModelPart FrameMove1;
	    public LegacyModelPart WorkingHead;
	    public LegacyModelPart WorkingDrill1;
	    public LegacyModelPart WorkingDrill2;
	    public LegacyModelPart WorkingLaser1;
	    public LegacyModelPart WorkingLaser2;
	    public LegacyModelPart WorkingTube1;
	    public LegacyModelPart WorkingTube2;
	    public LegacyModelPart WorkingLaserBeam;

	    public ModelFabricator() {
	        this.textureWidth = 128;
	        this.textureHeight = 128;
	        this.WorkingLaser1 = new LegacyModelPart(this, 14, 102);
	        this.WorkingLaser1.setRotationPoint(-6.7F, 2.5F, 6.5F);
	        this.WorkingLaser1.addBox(-0.5F, 0.0F, -0.5F, 1, 7, 1, 0.0F);
	        this.setRotation(WorkingLaser1, 0.0F, 0.7853981633974483F, 0.0F);
	        this.Corner3 = new LegacyModelPart(this, 0, 0);
	        this.Corner3.setRotationPoint(-24.0F, 8.0F, 21.0F);
	        this.Corner3.addBox(0.0F, 0.0F, 0.0F, 3, 14, 3, 0.0F);
	        this.Connection2 = new LegacyModelPart(this, 0, 21);
	        this.Connection2.setRotationPoint(-24.0F, 16.0F, 0.0F);
	        this.Connection2.addBox(-4.0F, -4.0F, 0.0F, 8, 8, 1, 0.0F);
	        this.setRotation(Connection2, 0.0F, 1.5707963267948966F, 0.0F);
	        this.Corner4 = new LegacyModelPart(this, 0, 0);
	        this.Corner4.setRotationPoint(5.0F, 8.0F, 21.0F);
	        this.Corner4.addBox(0.0F, 0.0F, 0.0F, 3, 14, 3, 0.0F);
	        this.Glass = new LegacyModelPart(this, 8, 86);
	        this.Glass.setRotationPoint(-8.0F, -6.0F, 8.0F);
	        this.Glass.addBox(-15.0F, 0.0F, -15.0F, 30, 12, 30, 0.0F);
	        this.Corner1 = new LegacyModelPart(this, 0, 0);
	        this.Corner1.setRotationPoint(5.0F, 8.0F, -8.0F);
	        this.Corner1.addBox(0.0F, 0.0F, 0.0F, 3, 14, 3, 0.0F);
	        this.Connection7 = new LegacyModelPart(this, 0, 21);
	        this.Connection7.setRotationPoint(8.0F, 16.0F, 16.0F);
	        this.Connection7.addBox(-4.0F, -4.0F, 0.0F, 8, 8, 1, 0.0F);
	        this.setRotation(Connection7, 0.0F, -1.5707963267948966F, 0.0F);
	        this.FrameMove1 = new LegacyModelPart(this, 0, 71);
	        this.FrameMove1.setRotationPoint(-8.0F, -5.0F, 8.0F);
	        this.FrameMove1.addBox(-10.0F, 0.0F, -2.0F, 20, 2, 4, 0.0F);
	        this.Frame4 = new LegacyModelPart(this, 18, 0);
	        this.Frame4.setRotationPoint(3.0F, -5.0F, 18.0F);
	        this.Frame4.addBox(-1.0F, 0.0F, -2.0F, 2, 16, 4, 0.0F);
	        this.setRotation(Frame4, 0.0F, 3.141592653589793F, 0.0F);
	        this.Frame2 = new LegacyModelPart(this, 18, 0);
	        this.Frame2.setRotationPoint(-19.0F, -5.0F, 18.0F);
	        this.Frame2.addBox(-1.0F, 0.0F, -2.0F, 2, 16, 4, 0.0F);
	        this.SidePlate2 = new LegacyModelPart(this, 0, 35);
	        this.SidePlate2.setRotationPoint(-8.0F, 8.0F, 23.0F);
	        this.SidePlate2.addBox(-13.0F, 0.0F, 0.0F, 26, 14, 3, 0.0F);
	        this.setRotation(SidePlate2, 0.0F, 3.141592653589793F, 0.0F);
	        this.SidePlate1 = new LegacyModelPart(this, 0, 35);
	        this.SidePlate1.setRotationPoint(-8.0F, 8.0F, -7.0F);
	        this.SidePlate1.addBox(-13.0F, 0.0F, 0.0F, 26, 14, 3, 0.0F);
	        this.Connection3 = new LegacyModelPart(this, 0, 21);
	        this.Connection3.setRotationPoint(-24.0F, 16.0F, 16.0F);
	        this.Connection3.addBox(-4.0F, -4.0F, 0.0F, 8, 8, 1, 0.0F);
	        this.setRotation(Connection3, 0.0F, 1.5707963267948966F, 0.0F);
	        this.SideBarTop4 = new LegacyModelPart(this, 0, 62);
	        this.SideBarTop4.setRotationPoint(8.0F, 6.0F, 8.0F);
	        this.SideBarTop4.addBox(-12.0F, 0.0F, 0.0F, 24, 2, 4, 0.0F);
	        this.setRotation(SideBarTop4, 0.0F, -1.5707963267948966F, 0.0F);
	        this.WorkingLaser2 = new LegacyModelPart(this, 0, 102);
	        this.WorkingLaser2.setRotationPoint(-6.7F, -2.5F, 6.5F);
	        this.WorkingLaser2.addBox(-1.0F, 0.0F, -1.0F, 2, 3, 2, 0.0F);
	        this.setRotation(WorkingLaser2, 0.0F, 0.7853981633974483F, 0.0F);
	        this.SideBarTop1 = new LegacyModelPart(this, 0, 53);
	        this.SideBarTop1.setRotationPoint(-8.0F, 6.0F, -8.0F);
	        this.SideBarTop1.addBox(-16.0F, 0.0F, 0.0F, 32, 2, 4, 0.0F);
	        this.SideBarTop2 = new LegacyModelPart(this, 0, 53);
	        this.SideBarTop2.setRotationPoint(-8.0F, 6.0F, 24.0F);
	        this.SideBarTop2.addBox(-16.0F, 0.0F, 0.0F, 32, 2, 4, 0.0F);
	        this.setRotation(SideBarTop2, 0.0F, 3.141592653589793F, 0.0F);
	        this.SidePlate3 = new LegacyModelPart(this, 0, 35);
	        this.SidePlate3.setRotationPoint(-23.0F, 8.0F, 8.0F);
	        this.SidePlate3.addBox(-13.0F, 0.0F, 0.0F, 26, 14, 3, 0.0F);
	        this.setRotation(SidePlate3, 0.0F, 1.5707963267948966F, 0.0F);
	        this.FrameTop2 = new LegacyModelPart(this, 92, 35);
	        this.FrameTop2.setRotationPoint(3.0F, -4.0F, 8.0F);
	        this.FrameTop2.addBox(-1.0F, -1.0F, -8.0F, 2, 2, 16, 0.0F);
	        this.setRotation(FrameTop2, 0.0F, 3.141592653589793F, 0.0F);
	        this.WorkingTube1 = new LegacyModelPart(this, 9, 77);
	        this.WorkingTube1.setRotationPoint(-6.7F, -2.5F, 9.5F);
	        this.WorkingTube1.addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2, 0.0F);
	        this.setRotation(WorkingTube1, 0.0F, 0.7853981633974483F, 0.0F);
	        this.SidePlate4 = new LegacyModelPart(this, 0, 35);
	        this.SidePlate4.setRotationPoint(7.0F, 8.0F, 8.0F);
	        this.SidePlate4.addBox(-13.0F, 0.0F, 0.0F, 26, 14, 3, 0.0F);
	        this.setRotation(SidePlate4, 0.0F, -1.5707963267948966F, 0.0F);
	        this.SideBarTop3 = new LegacyModelPart(this, 0, 62);
	        this.SideBarTop3.setRotationPoint(-24.0F, 6.0F, 8.0F);
	        this.SideBarTop3.addBox(-12.0F, 0.0F, 0.0F, 24, 2, 4, 0.0F);
	        this.setRotation(SideBarTop3, 0.0F, 1.5707963267948966F, 0.0F);
	        this.Connection1 = new LegacyModelPart(this, 0, 21);
	        this.Connection1.setRotationPoint(-16.0F, 16.0F, -8.0F);
	        this.Connection1.addBox(-4.0F, -4.0F, 0.0F, 8, 8, 1, 0.0F);
	        this.WorkingDrill2 = new LegacyModelPart(this, 0, 85);
	        this.WorkingDrill2.setRotationPoint(-9.3F, -3.5F, 8.0F);
	        this.WorkingDrill2.addBox(-0.5F, 0.0F, -0.5F, 1, 8, 1, 0.0F);
	        this.setRotation(WorkingDrill2, 0.0F, 0.7853981633974483F, 0.0F);
	        this.WorkingTube2 = new LegacyModelPart(this, 18, 78);
	        this.WorkingTube2.setRotationPoint(-6.7F, -3.5F, 9.5F);
	        this.WorkingTube2.addBox(-0.5F, 0.0F, -0.5F, 1, 9, 1, 0.0F);
	        this.setRotation(WorkingTube2, 0.0F, 0.7853981633974483F, 0.0F);
	        this.WorkingDrill1 = new LegacyModelPart(this, 0, 77);
	        this.WorkingDrill1.setRotationPoint(-9.3F, -2.5F, 8.0F);
	        this.WorkingDrill1.addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2, 0.0F);
	        this.setRotation(WorkingDrill1, 0.0F, 0.7853981633974483F, 0.0F);
	        this.innerPlate = new LegacyModelPart(this, 32, 59);
	        this.innerPlate.setRotationPoint(-8.0F, 11.0F, 8.0F);
	        this.innerPlate.addBox(-12.0F, 0.0F, -12.0F, 24, 2, 24, 0.0F);
	        this.FrameTop1 = new LegacyModelPart(this, 92, 35);
	        this.FrameTop1.setRotationPoint(-19.0F, -4.0F, 8.0F);
	        this.FrameTop1.addBox(-1.0F, -1.0F, -8.0F, 2, 2, 16, 0.0F);
	        this.Connection5 = new LegacyModelPart(this, 0, 21);
	        this.Connection5.setRotationPoint(0.0F, 16.0F, 24.0F);
	        this.Connection5.addBox(-4.0F, -4.0F, 0.0F, 8, 8, 1, 0.0F);
	        this.setRotation(Connection5, 0.0F, 3.141592653589793F, 0.0F);
	        this.Connection6 = new LegacyModelPart(this, 0, 21);
	        this.Connection6.setRotationPoint(8.0F, 17.0F, 0.0F);
	        this.Connection6.addBox(-4.0F, -5.0F, 0.0F, 8, 8, 1, 0.0F);
	        this.setRotation(Connection6, 0.0F, -1.5707963267948966F, 0.0F);
	        this.WorkingLaserBeam = new LegacyModelPart(this, 9, 102);
	        this.WorkingLaserBeam.setRotationPoint(-6.7F, 0.5F, 6.5F);
	        this.WorkingLaserBeam.addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1, 0.0F);
	        this.setRotation(WorkingLaserBeam, 0.0F, 0.7853981633974483F, 0.0F);
	        this.WorkingHead = new LegacyModelPart(this, 0, 94);
	        this.WorkingHead.setRotationPoint(-8.0F, -4.5F, 8.0F);
	        this.WorkingHead.addBox(-3.0F, 0.0F, -3.0F, 6, 2, 6, 0.0F);
	        this.basePlate = new LegacyModelPart(this, 0, 0);
	        this.basePlate.setRotationPoint(-8.0F, 22.0F, 8.0F);
	        this.basePlate.addBox(-16.0F, 0.0F, -16.0F, 32, 2, 32, 0.0F);
	        this.Connection4 = new LegacyModelPart(this, 0, 21);
	        this.Connection4.setRotationPoint(-16.0F, 16.0F, 24.0F);
	        this.Connection4.addBox(-4.0F, -4.0F, 0.0F, 8, 8, 1, 0.0F);
	        this.setRotation(Connection4, 0.0F, 3.141592653589793F, 0.0F);
	        this.Frame3 = new LegacyModelPart(this, 18, 0);
	        this.Frame3.setRotationPoint(3.0F, -5.0F, -2.0F);
	        this.Frame3.addBox(-1.0F, 0.0F, -2.0F, 2, 16, 4, 0.0F);
	        this.setRotation(Frame3, 0.0F, 3.141592653589793F, 0.0F);
	        this.Controller = new LegacyModelPart(this, 103, 0);
	        this.Controller.setRotationPoint(-4.0F, 12.0F, -7.0F);
	        this.Controller.addBox(0.0F, 0.0F, 0.0F, 8, 8, 3, 0.0F);
	        this.setRotation(Controller, -0.1308996938995747F, 0.0F, 0.0F);
	        this.MidPlate = new LegacyModelPart(this, 58, 36);
	        this.MidPlate.setRotationPoint(-8.0F, 10.0F, 8.0F);
	        this.MidPlate.addBox(-6.0F, 0.0F, -6.0F, 12, 1, 12, 0.0F);
	        this.Corner2 = new LegacyModelPart(this, 0, 0);
	        this.Corner2.setRotationPoint(-24.0F, 8.0F, -8.0F);
	        this.Corner2.addBox(0.0F, 0.0F, 0.0F, 3, 14, 3, 0.0F);
	        this.Frame1 = new LegacyModelPart(this, 18, 0);
	        this.Frame1.setRotationPoint(-19.0F, -5.0F, -2.0F);
	        this.Frame1.addBox(-1.0F, 0.0F, -2.0F, 2, 16, 4, 0.0F);
	    }

	    
	    
	    @Override
		public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
				float headPitch, float scale, float progress) {
	    	
			//unanimated
		        this.innerPlate.render(scale);
		        this.FrameTop1.render(scale);
		        this.Connection7.render(scale);
		        this.Controller.render(scale);
		        this.basePlate.render(scale);
		        this.Frame2.render(scale);
		        this.Frame4.render(scale);
		        this.Connection1.render(scale);
		        this.FrameTop2.render(scale);
		        this.SideBarTop1.render(scale);
		        this.Frame3.render(scale);
		        this.SidePlate4.render(scale);
		        this.Connection5.render(scale);
		        this.Corner3.render(scale);
		        this.SidePlate3.render(scale);
		        this.Corner4.render(scale);
		        this.Connection6.render(scale);
		        this.Corner1.render(scale);
		        this.Connection2.render(scale);
		        this.Connection3.render(scale);
		        this.Frame1.render(scale);
		        this.MidPlate.render(scale);
		        this.Connection4.render(scale);
		        this.SidePlate2.render(scale);
		        this.Corner2.render(scale);
		        this.SidePlate1.render(scale);
		        this.SideBarTop2.render(scale);
		        this.SideBarTop4.render(scale);
		        this.SideBarTop3.render(scale);
		        
			//animate frame, part 1	GL11.glPushMatrix();
	        	pushMatrix();
	        	translate(0f, 0f, 0.25f*Math.sin(progress*Math.PI*4));
        	
		        this.FrameMove1.render(scale);
		        
			//animate head, part 2
		        translate(0.125f*Math.sin(progress*Math.PI*8),0f,0f);
        		
		        this.WorkingHead.render(scale);
		        this.WorkingDrill1.render(scale);
		        this.WorkingTube1.render(scale);
		        this.WorkingLaser2.render(scale);
		        this.WorkingLaserBeam.render(scale);
		        
		        if (progress<0.1f || progress >0.9f) {
		        	this.WorkingDrill2.render(scale); 
		        	this.WorkingTube2.render(scale);
		        } else if(progress>=0.1f && progress<0.3f) {
		        	float progScaled = (progress - 0.1f)*5.0f;
        			pushMatrix();
        				float offsetX=-0.575f;
        				float offsetZ=0.5f;
        			
        				translate(offsetX,0,offsetZ);
        				rotate(progScaled*360*8, 0, 1, 0);
        				translate(-offsetX,0,-offsetZ);
        				double sin = Math.sin(progScaled*Math.PI);
        				translate(0, 0.3d*sin*sin, 0);
        				this.WorkingDrill2.render(scale); 
        			popMatrix();
        			this.WorkingTube2.render(scale);
        			
		        } else if (progress>=0.3f && progress <0.6f){
		        	 this.WorkingDrill2.render(scale); 
		        	 
    				float progScaled = (progress - 0.3f)* (1.0f/0.3f);
    				pushMatrix();
    					double sin = Math.sin(progScaled*Math.PI*4);
    					translate(0, 0.3d*sin*sin, 0);
    					this.WorkingTube2.render(scale);
					popMatrix();
		        } else {
			        this.WorkingDrill2.render(scale); 
			        this.WorkingLaser1.render(scale);
			        this.WorkingTube2.render(scale);
    			}
		        popMatrix();
		        
	        //render glass last
			useAlphaLayer(true);this.Glass.render(scale);useAlphaLayer(false);
			
	    }
}
