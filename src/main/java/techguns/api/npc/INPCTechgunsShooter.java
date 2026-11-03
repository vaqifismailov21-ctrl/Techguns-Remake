package techguns.api.npc;

public interface INPCTechgunsShooter {
    float getWeaponPosX();
    float getWeaponPosY();
    float getWeaponPosZ();
    default boolean hasWeaponArmPose(){return true;}
    default float getGunScale(){return 1;}
    default float getBulletOffsetSide(){return 0;}
    default float getBulletOffsetHeight(){return 0;}
}
