package techguns.api.guns;

import techguns.items.guns.GenericGun;

/** Per-client scope state, independent of frame/tick ordering. */
public final class WeaponAimState {
    private GenericGun weapon;
    private int slot=-1;
    private boolean enabled;
    public void reset(){weapon=null;slot=-1;enabled=false;}
    private void select(GenericGun gun,int selected){if(weapon!=gun||slot!=selected){weapon=gun;slot=selected;enabled=false;}}
    public void press(GenericGun gun,int selected,boolean reloading){
        if(gun==null||!gun.canZoom()||reloading)return;
        select(gun,selected);if(!gun.isHoldZoom())enabled=!enabled;
    }
    public void tick(GenericGun gun,int selected,boolean alive,boolean reloading,boolean sneak){
        if(!alive||gun==null||!gun.canZoom()){reset();return;}
        select(gun,selected);
        if(reloading){enabled=false;return;}
        if(gun.isHoldZoom())enabled=sneak;
    }
    public boolean active(GenericGun gun,int selected){return enabled&&weapon==gun&&slot==selected;}
}
