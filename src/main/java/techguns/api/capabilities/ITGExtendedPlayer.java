package techguns.api.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public interface ITGExtendedPlayer extends ITGShooterValues {
    Player getEntity();
    int getFireDelay(InteractionHand hand);
    void setFireDelay(InteractionHand hand, int delay);
    Container getTGInventory();
    void saveToNBT(CompoundTag tags);
    void loadFromNBT(CompoundTag tags);
}
