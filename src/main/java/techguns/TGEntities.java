package techguns;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import techguns.entities.special.EntityRadiation;
import techguns.entities.projectiles.GenericProjectile;
import techguns.entities.projectiles.GenericProjectileIncendiary;

/** Only fully implemented entity classes are registered here. */
public final class TGEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Techguns.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.AdvancedBulletProjectile>> ADVANCED_PROJECTILE=ENTITIES.register("advancedbulletprojectile",()->EntityType.Builder.<techguns.entities.projectiles.AdvancedBulletProjectile>of(techguns.entities.projectiles.AdvancedBulletProjectile::new,MobCategory.MISC).sized(.25F,.25F).fireImmune().clientTrackingRange(128).updateInterval(1).build("techguns:advancedbulletprojectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.EnergyProjectile>> ENERGY_PROJECTILE=ENTITIES.register("energyprojectile",()->EntityType.Builder.<techguns.entities.projectiles.EnergyProjectile>of(techguns.entities.projectiles.EnergyProjectile::new,MobCategory.MISC).sized(0.25F,0.25F).eyeHeight(0.25F).fireImmune().clientTrackingRange(128).updateInterval(1).build("techguns:energyprojectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<GenericProjectile>> GENERIC_PROJECTILE = ENTITIES.register("genericprojectile", () ->
            EntityType.Builder.<GenericProjectile>of(GenericProjectile::new, MobCategory.MISC).sized(0.25F, 0.25F).eyeHeight(0.25F).fireImmune()
                    .clientTrackingRange(128).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("techguns:genericprojectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<GenericProjectileIncendiary>> INCENDIARY_PROJECTILE = ENTITIES.register("genericprojectileincendiary", () ->
            EntityType.Builder.<GenericProjectileIncendiary>of(GenericProjectileIncendiary::new, MobCategory.MISC).sized(0.25F, 0.25F).eyeHeight(0.25F).fireImmune()
                    .clientTrackingRange(128).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("techguns:genericprojectileincendiary"));
    public static final DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.StoneBulletProjectile>> STONE_BULLET = ENTITIES.register("stonebulletprojectile", () ->
            EntityType.Builder.<techguns.entities.projectiles.StoneBulletProjectile>of(techguns.entities.projectiles.StoneBulletProjectile::new,MobCategory.MISC).sized(0.25F,0.25F).eyeHeight(0.25F).fireImmune()
                    .clientTrackingRange(128).updateInterval(1).build("techguns:stonebulletprojectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.GenericProjectileExplosive>> EXPLOSIVE_BULLET = ENTITIES.register("genericprojectileexplosive", () ->
            EntityType.Builder.<techguns.entities.projectiles.GenericProjectileExplosive>of(techguns.entities.projectiles.GenericProjectileExplosive::new,MobCategory.MISC).sized(0.25F,0.25F).eyeHeight(0.25F).fireImmune()
                    .clientTrackingRange(128).updateInterval(1).build("techguns:genericprojectileexplosive"));
    public static final DeferredHolder<EntityType<?>, EntityType<EntityRadiation>> RADIATION = ENTITIES.register("radiationentity", () ->
            EntityType.Builder.<EntityRadiation>of(EntityRadiation::new, MobCategory.MISC).sized(0.6F, 1.8F)
                    .clientTrackingRange(128).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("techguns:radiationentity"));
    private TGEntities() {}
    public static final DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.RocketProjectile>> ROCKET=rocket("rocketprojectile");
    public static final DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.ToolProjectile>> HAMMER=tool("powerhammerprojectile"),CHAINSAW=tool("chainsawprojectile");
    private static DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.ToolProjectile>> tool(String name){return ENTITIES.register(name,()->EntityType.Builder.<techguns.entities.projectiles.ToolProjectile>of(techguns.entities.projectiles.ToolProjectile::new,MobCategory.MISC).sized(0.25F,0.25F).eyeHeight(0.25F).fireImmune().clientTrackingRange(128).updateInterval(1).build("techguns:"+name));}
    public static final DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.BioGunProjectile>> BIO=ENTITIES.register("biogunprojectile",()->EntityType.Builder.<techguns.entities.projectiles.BioGunProjectile>of(techguns.entities.projectiles.BioGunProjectile::new,MobCategory.MISC).sized(0.25F,0.25F).eyeHeight(0.25F).fireImmune().clientTrackingRange(128).updateInterval(1).build("techguns:biogunprojectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.GuidedMissileProjectile>> GUIDED_MISSILE=ENTITIES.register("guidedmissileprojectile",()->EntityType.Builder.<techguns.entities.projectiles.GuidedMissileProjectile>of(techguns.entities.projectiles.GuidedMissileProjectile::new,MobCategory.MISC).sized(0.25F,0.25F).eyeHeight(0.25F).fireImmune().clientTrackingRange(128).updateInterval(1).build("techguns:guidedmissileprojectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.FlamethrowerProjectile>> FLAME=ENTITIES.register("flamethrowerprojectile",()->EntityType.Builder.<techguns.entities.projectiles.FlamethrowerProjectile>of(techguns.entities.projectiles.FlamethrowerProjectile::new,MobCategory.MISC).sized(0.25F,0.25F).eyeHeight(0.25F).fireImmune().clientTrackingRange(128).updateInterval(1).build("techguns:flamethrowerprojectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.RocketProjectile>> ROCKET_HV=rocket("rocketprojectilehv");
    public static final DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.RocketProjectile>> ROCKET_NUKE=rocket("rocketprojectilenuke");
    public static final DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.GrenadeProjectile>> GRENADE=grenade("grenadeprojectile");
    public static final DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.GrenadeProjectile>> FRAG_GRENADE=grenade("fraggrenadeprojectile");
    public static final DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.GrenadeProjectile>> GRENADE_40MM=grenade("grenade40mmprojectile");
    private static DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.RocketProjectile>> rocket(String name) {
        return ENTITIES.register(name,()->EntityType.Builder.<techguns.entities.projectiles.RocketProjectile>of(techguns.entities.projectiles.RocketProjectile::new,MobCategory.MISC).sized(0.25F,0.25F).eyeHeight(0.25F).fireImmune().clientTrackingRange(128).updateInterval(1).build("techguns:"+name));
    }
    private static DeferredHolder<EntityType<?>, EntityType<techguns.entities.projectiles.GrenadeProjectile>> grenade(String name) {
        return ENTITIES.register(name,()->EntityType.Builder.<techguns.entities.projectiles.GrenadeProjectile>of(techguns.entities.projectiles.GrenadeProjectile::new,MobCategory.MISC).sized(0.25F,0.25F).eyeHeight(0.25F).fireImmune().clientTrackingRange(128).updateInterval(1).build("techguns:"+name));
    }
}
