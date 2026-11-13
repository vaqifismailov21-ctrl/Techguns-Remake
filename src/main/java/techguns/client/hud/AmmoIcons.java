package techguns.client.hud;

import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import techguns.TGItems;

/** Techguns CE EnumAmmoRenderTypes + EnumAmmoMagConversion: ammo pictures of the ammo HUD, magazines shown as their rounds. */
public enum AmmoIcons {
    PISTOL(2,1,29,8,TGItems.PISTOL_ROUNDS),PISTOL_I(34,1,29,8,TGItems.PISTOL_ROUNDS_INCENDIARY),
    RIFLE(2,12,28,5,TGItems.RIFLE_ROUNDS),RIFLE_I(32,12,28,5,TGItems.RIFLE_ROUNDS_INCENDIARY),
    SNIPER(2,20,31,7,TGItems.SNIPER_ROUNDS),SNIPER_I(36,20,31,7,TGItems.SNIPER_ROUNDS_INCENDIARY),SNIPER_E(70,20,31,7,TGItems.SNIPER_ROUNDS_EXPLOSIVE),
    SHOTGUN(2,86,31,24,TGItems.SHOTGUN_ROUNDS),SHOTGUN_I(36,86,31,24,TGItems.SHOTGUN_ROUNDS_INCENDIARY),
    ADV_BULLET(2,30,35,7,TGItems.ADVANCED_ROUNDS),STONE_BULLET(2,40,30,28,TGItems.STONE_BULLETS),GAUSS(2,70,32,13,TGItems.GAUSSRIFLE_SLUGS),
    NETHER_CHARGE(2,113,30,31,TGItems.NETHER_CHARGE),BIOFUEL(2,147,32,19,TGItems.BIO_TANK),ENERGY(33,209,14,25,TGItems.ENERGY_CELL),
    RADIATION(3,210,26,24,TGItems.NUCLEAR_POWERCELL),FIRE(2,183,20,26,TGItems.FUEL_TANK),
    ROCKET_REGULAR(217,1,37,10,TGItems.ROCKET),ROCKET_HV(217,13,37,10,TGItems.ROCKET_HIGH_VELOCITY),ROCKET_NUCLEAR(217,25,37,10,TGItems.ROCKET_NUKE),
    GRENADE(2,169,33,12,TGItems.GRENADE_40MM);
    public static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/ammo_types.png");
    public final int u,v,width,height;private final Supplier<? extends Item> item;
    AmmoIcons(int u,int v,int width,int height,Supplier<? extends Item> item){this.u=u;this.v=v;this.width=width;this.height=height;this.item=item;}

    @SuppressWarnings("unchecked")
    private static final Supplier<? extends Item>[][] MAGAZINES=new Supplier[][]{
        {TGItems.PISTOL_ROUNDS,TGItems.PISTOL_MAGAZINE,TGItems.SMG_MAGAZINE},
        {TGItems.PISTOL_ROUNDS_INCENDIARY,TGItems.PISTOL_MAGAZINE_INCENDIARY,TGItems.SMG_MAGAZINE_INCENDIARY},
        {TGItems.RIFLE_ROUNDS,TGItems.ASSAULTRIFLE_MAGAZINE,TGItems.LMG_MAGAZINE,TGItems.MINIGUN_DRUM},
        {TGItems.RIFLE_ROUNDS_INCENDIARY,TGItems.ASSAULTRIFLE_MAGAZINE_INCENDIARY,TGItems.LMG_MAGAZINE_INCENDIARY,TGItems.MINIGUN_DRUM_INCENDIARY},
        {TGItems.SNIPER_ROUNDS,TGItems.AS50_MAGAZINE},{TGItems.SNIPER_ROUNDS_INCENDIARY,TGItems.AS50_MAGAZINE_INCENDIARY},{TGItems.SNIPER_ROUNDS_EXPLOSIVE,TGItems.AS50_MAGAZINE_EXPLOSIVE},
        {TGItems.ADVANCED_ROUNDS,TGItems.ADVANCED_MAGAZINE},{TGItems.ENERGY_CELL,TGItems.REDSTONE_BATTERY}};

    /** EnumAmmoMagConversion.getAmmo: the rounds a magazine holds, or the stack itself. */
    public static Item rounds(ItemStack magazine){
        if(magazine.isEmpty())return null;
        for(var row:MAGAZINES)for(int i=1;i<row.length;i++)if(magazine.is(row[i].get()))return row[0].get();
        return magazine.getItem();
    }
    /** EnumAmmoRenderTypes.getEnum, or null when the ammo has no picture. */
    public static AmmoIcons of(ItemStack ammo){
        var rounds=rounds(ammo);if(rounds==null)return null;
        for(var type:values())if(type.item.get()==rounds)return type;
        return null;
    }
    public void draw(GuiGraphics graphics,int x,int y){graphics.blit(TEXTURE,x,y,u,v,width,height);}
}
