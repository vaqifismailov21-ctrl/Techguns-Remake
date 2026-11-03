package techguns.network;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/** Forge-style packet targets mapped onto NeoForge 1.21 {@code PacketDistributor} static methods. */
public final class PacketDistributor<T> {
    public static final PacketDistributor<ServerPlayer> PLAYER = new PacketDistributor<>(p -> payload -> net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p, payload));
    public static final PacketDistributor<Entity> TRACKING_ENTITY = new PacketDistributor<>(e -> payload -> net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(e, payload));
    public static final PacketDistributor<Entity> TRACKING_ENTITY_AND_SELF = new PacketDistributor<>(e -> payload -> net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(e, payload));
    public static final PacketDistributor<Void> ALL = new PacketDistributor<>(v -> payload -> net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers(payload));
    public static final PacketDistributor<TargetPoint> NEAR = new PacketDistributor<>(t -> payload -> {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        ServerLevel level = server.getLevel(t.dim());
        if (level != null) net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(level, t.excluded(), t.x(), t.y(), t.z(), t.r(), payload);
    });

    public record TargetPoint(ServerPlayer excluded, double x, double y, double z, double r, ResourceKey<Level> dim) {
        public TargetPoint(double x, double y, double z, double r, ResourceKey<Level> dim) { this(null, x, y, z, r, dim); }
    }

    /** A resolved target. */
    public interface PacketTarget { void send(CustomPacketPayload payload); }

    private final Function<T, Consumer<CustomPacketPayload>> sender;
    private PacketDistributor(Function<T, Consumer<CustomPacketPayload>> sender) { this.sender = sender; }

    public PacketTarget with(Supplier<? extends T> input) { Consumer<CustomPacketPayload> c = sender.apply(input.get()); return c::accept; }
    public PacketTarget noArg() { Consumer<CustomPacketPayload> c = sender.apply(null); return c::accept; }
}
