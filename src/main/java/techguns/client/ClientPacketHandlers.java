package techguns.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import techguns.TGPackets;
import techguns.capabilities.TGExtendedPlayer;

public final class ClientPacketHandlers {
    public static void apply(TGPackets.PlayerSync message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        var entity = minecraft.level.getEntity(message.entityId());
        if (entity instanceof Player player && player.getUUID().equals(message.uuid())) {
            TGExtendedPlayer.find(player).ifPresent(state -> state.applySynchronization(message.data(), message.full()));
        }
    }
    private ClientPacketHandlers() {}
}
