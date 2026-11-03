package techguns;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

@Mod.EventBusSubscriber(modid = Techguns.MODID)
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
