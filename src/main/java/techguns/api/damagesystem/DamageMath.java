package techguns.api.damagesystem;

/** Numerical rules from DamageSystem/ShieldStats, independent of the game lifecycle. */
public final class DamageMath {
    private DamageMath() {}

    public static double afterArmor(float damage, float totalArmor, float toughness, float penetration) {
        float effectivePenetration = Math.max(penetration - toughness, 0);
        double armor = Math.max(0.0, Math.min(24.0, totalArmor - effectivePenetration));
        return damage * (1.0 - armor / 25.0);
    }

    public static float afterShield(float damage, double reduction, double threshold, float penetration) {
        double reduced = damage * (1.0 - reduction);
        double effectiveThreshold = Math.max(threshold - penetration * 2.0, 0.0);
        return (float) Math.max(reduced - effectiveThreshold, 0.0);
    }
}
