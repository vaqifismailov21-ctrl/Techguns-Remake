package techguns.deatheffects;

import net.minecraft.core.registries.BuiltInRegistries;
/** Original wire values and the exact entity classes eligible for gore effects. */
public final class EntityDeathUtils {
    private static final java.util.Set<String> GORE_NPCS=java.util.Set.of("zombiesoldier","armysoldier","cyberdemon","zombiepigmansoldier","zombiefarmer","zombieminer","bandit","psychosteve","dictatordave","skeletonsoldier","alienbug","stormtrooper","supermutantelite","supermutantheavy","supermutantbasic","outcast","commando","ghastling");
    private static final java.util.Set<String> GORE_ENTITIES=java.util.Set.of("player","zombie","skeleton","enderman","creeper","cow","sheep","pig","chicken","zombified_piglin","spider","cave_spider","witch","slime","horse","mooshroom","wolf","squid","villager","llama","evoker","husk","polar_bear","magma_cube","parrot","rabbit","stray","silverfish","vindicator","vex","shulker","wither_skeleton","ghast","zombie_villager","donkey","mule");
    public static boolean hasSpecialDeathAnim(net.minecraft.world.entity.LivingEntity entity,DeathType type){
        if(type==DeathType.BIO||type==DeathType.LASER)return true;
        var id=BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return type==DeathType.GORE&&id!=null&&(id.getNamespace().equals("minecraft")&&GORE_ENTITIES.contains(id.getPath())||id.getNamespace().equals("techguns")&&GORE_NPCS.contains(id.getPath()));
    }
    public enum DeathType {
        DEFAULT(0), GORE(1), BIO(2), LASER(3), DISMEMBER(4);
        private final int value;
        DeathType(int value) { this.value = value; }
        public int getValue() { return value; }
    }
    private EntityDeathUtils() {}
}
