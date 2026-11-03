package techguns;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

@EventBusSubscriber(modid = Techguns.MODID)
public final class TGPermissions {
    public static final PermissionNode<Boolean> ALLOW_UNSAFE_MODE = new PermissionNode<>(
            Techguns.MODID, "allowunsafemode", PermissionTypes.BOOLEAN, (player, uuid, contexts) -> true);

    @SubscribeEvent
    public static void registerNodes(PermissionGatherEvent.Nodes event) { event.addNodes(ALLOW_UNSAFE_MODE); }

    public static boolean canUseUnsafeMode(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        if (TGConfig.limitUnsafeModeToOP) return serverPlayer.server.getPlayerList().isOp(serverPlayer.getGameProfile());
        return PermissionAPI.getPermission(serverPlayer, ALLOW_UNSAFE_MODE);
    }
    private TGPermissions() {}
}
