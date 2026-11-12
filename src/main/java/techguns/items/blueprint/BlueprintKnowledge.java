package techguns.items.blueprint;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/** Players who have studied the cybernetics blueprints. Stored once per world in the overworld data. */
public final class BlueprintKnowledge extends SavedData {
    private static final String NAME = "techguns_blueprints";
    private final Set<UUID> learned = new HashSet<>();

    public static BlueprintKnowledge get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(BlueprintKnowledge::load, BlueprintKnowledge::new, NAME);
    }

    public static boolean knows(@Nullable Level level, @Nullable UUID player) {
        if (player == null || !(level instanceof ServerLevel server)) return false;
        return get(server.getServer()).learned.contains(player);
    }

    /** @return true if the player did not know the blueprints yet. */
    public boolean learn(UUID player) {
        if (!learned.add(player)) return false;
        setDirty();
        return true;
    }

    private static BlueprintKnowledge load(CompoundTag tag) {
        BlueprintKnowledge data = new BlueprintKnowledge();
        for (Tag entry : tag.getList("players", Tag.TAG_INT_ARRAY)) data.learned.add(NbtUtils.loadUUID(entry));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (UUID id : learned) list.add(NbtUtils.createUUID(id));
        tag.put("players", list);
        return tag;
    }
}
