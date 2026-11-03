package techguns.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Small compatibility layer with the old Forge SimpleChannel API on top of NeoForge 1.21 payloads.
 * Every message class is wrapped into its own {@link CustomPacketPayload} type ("techguns:m&lt;id&gt;").
 * Handlers run on the main thread, like consumerMainThread in Forge.
 */
public final class SimpleChannel {
    public record Wrapped<T>(CustomPacketPayload.Type<Wrapped<T>> type, T message) implements CustomPacketPayload {}

    /** Mirrors Forge's NetworkEvent.Context: only the members Techguns uses. */
    public static final class Context {
        private final IPayloadContext ctx;
        Context(IPayloadContext ctx) { this.ctx = ctx; }
        public ServerPlayer getSender() { return ctx.player() instanceof ServerPlayer sp ? sp : null; }
        public void setPacketHandled(boolean handled) {}
        public void enqueueWork(Runnable work) { work.run(); }
        public IPayloadContext context() { return ctx; }
    }

    private final class Entry<T> {
        final Class<T> type; final NetworkDirection direction; final CustomPacketPayload.Type<Wrapped<T>> payloadType;
        BiConsumer<T, FriendlyByteBuf> encoder = (m, b) -> {}; Function<FriendlyByteBuf, T> decoder;
        BiConsumer<T, Supplier<Context>> handler = (m, c) -> {};
        Entry(Class<T> type, int id, NetworkDirection direction) {
            this.type = type; this.direction = direction;
            this.payloadType = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(namespace, "m" + id));
        }
        StreamCodec<RegistryFriendlyByteBuf, Wrapped<T>> codec() {
            return StreamCodec.of((buf, w) -> encoder.accept(w.message(), buf), buf -> new Wrapped<>(payloadType, decoder.apply(buf)));
        }
        void handle(Wrapped<T> w, IPayloadContext ctx) { Context c = new Context(ctx); handler.accept(w.message(), () -> c); }
        void register(PayloadRegistrar r) {
            if (direction == NetworkDirection.PLAY_TO_SERVER) r.playToServer(payloadType, codec(), this::handle);
            else r.playToClient(payloadType, codec(), this::handle);
        }
    }

    public final class MessageBuilder<T> {
        private final Entry<T> entry;
        MessageBuilder(Entry<T> entry) { this.entry = entry; }
        public MessageBuilder<T> encoder(BiConsumer<T, FriendlyByteBuf> encoder) { entry.encoder = encoder; return this; }
        public MessageBuilder<T> decoder(Function<FriendlyByteBuf, T> decoder) { entry.decoder = decoder; return this; }
        public MessageBuilder<T> consumerMainThread(BiConsumer<T, Supplier<Context>> handler) { entry.handler = handler; return this; }
        public MessageBuilder<T> consumerNetworkThread(BiConsumer<T, Supplier<Context>> handler) { entry.handler = handler; return this; }
        public void add() { entries.add(entry); byClass.put(entry.type, entry); }
    }

    private final String namespace; private final String version;
    private final List<Entry<?>> entries = new ArrayList<>();
    private final Map<Class<?>, Entry<?>> byClass = new HashMap<>();

    public SimpleChannel(String namespace, String version) { this.namespace = namespace; this.version = version; }

    /** Must be called from the mod constructor with the mod event bus. */
    public void registerOn(IEventBus modBus) { modBus.addListener(this::onRegister); }
    private void onRegister(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(version);
        for (Entry<?> e : entries) e.register(registrar);
    }

    public <T> MessageBuilder<T> messageBuilder(Class<T> type, int id, NetworkDirection direction) {
        return new MessageBuilder<>(new Entry<>(type, id, direction));
    }

    @SuppressWarnings("unchecked")
    public <T> CustomPacketPayload wrap(T message) {
        Entry<T> e = (Entry<T>) byClass.get(message.getClass());
        if (e == null) throw new IllegalArgumentException("Unregistered Techguns message " + message.getClass());
        return new Wrapped<>(e.payloadType, message);
    }

    public <T> void sendToServer(T message) { net.neoforged.neoforge.network.PacketDistributor.sendToServer(wrap(message)); }
    public <T> void send(PacketDistributor.PacketTarget target, T message) { target.send(wrap(message)); }
}
