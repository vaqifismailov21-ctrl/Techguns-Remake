package techguns.network;

import java.util.function.Supplier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/** Replacement for Forge's removed DistExecutor: the inner supplier is only touched on the requested side. */
public final class DistExecutor {
    public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) { if (FMLEnvironment.dist == dist) toRun.get().run(); }
    public static void safeRunWhenOn(Dist dist, Supplier<Runnable> toRun) { unsafeRunWhenOn(dist, toRun); }
    private DistExecutor() {}
}
