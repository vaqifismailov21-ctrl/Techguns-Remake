package techguns.network;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

/** Forge NetworkHooks.openScreen replacements for NeoForge 1.21 (ServerPlayer#openMenu with extra data). */
public final class NetworkHooks {
    public static void openScreen(ServerPlayer player, MenuProvider provider) { player.openMenu(provider); }
    public static void openScreen(ServerPlayer player, MenuProvider provider, BlockPos pos) { player.openMenu(provider, pos); }
    public static void openScreen(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData) { player.openMenu(provider, extraData); }
    private NetworkHooks() {}
}
