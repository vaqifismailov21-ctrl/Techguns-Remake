package techguns.items.blueprint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/** The blueprints every player has studied (cybernetics, generators, power plants, reactors). Stored once per world in the overworld data. */
public final class BlueprintKnowledge extends SavedData {
    private static final String NAME = "techguns_blueprints";
    public static final String CYBERNETICS = "cybernetics";
    private final Map<UUID, Set<String>> learned = new HashMap<>();

    public static BlueprintKnowledge get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(BlueprintKnowledge::load, BlueprintKnowledge::new, NAME);
    }

    /** Whether the player has studied the cybernetics blueprints. */
    public static boolean knows(@Nullable Level level, @Nullable UUID player) { return knows(level, player, CYBERNETICS); }

    public static boolean knows(@Nullable Level level, @Nullable UUID player, String blueprint) {
        if (player == null || !(level instanceof ServerLevel server)) return false;
        return get(server.getServer()).has(player, blueprint);
    }

    public boolean has(UUID player, String blueprint) { var set = learned.get(player); return set != null && set.contains(blueprint); }

    /** Learns the cybernetics blueprints; @return true if the player did not know them yet. */
    public boolean learn(UUID player) { return learn(player, CYBERNETICS); }

    /** @return true if the player did not know this blueprint yet. */
    public boolean learn(UUID player, String blueprint) {
        if (!learned.computeIfAbsent(player, id -> new HashSet<>()).add(blueprint)) return false;
        setDirty();
        return true;
    }

    private static BlueprintKnowledge load(CompoundTag tag) {
        BlueprintKnowledge data = new BlueprintKnowledge();
        // worlds saved before there were several blueprints only list the players who know cybernetics
        for (Tag entry : tag.getList("players", Tag.TAG_INT_ARRAY)) data.learned.computeIfAbsent(NbtUtils.loadUUID(entry), id -> new HashSet<>()).add(CYBERNETICS);
        for (Tag entry : tag.getList("knowledge", Tag.TAG_COMPOUND)) {
            var compound = (CompoundTag) entry;
            var set = data.learned.computeIfAbsent(compound.getUUID("player"), id -> new HashSet<>());
            for (Tag name : compound.getList("blueprints", Tag.TAG_STRING)) set.add(name.getAsString());
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (var entry : learned.entrySet()) {
            var compound = new CompoundTag();
            compound.putUUID("player", entry.getKey());
            var names = new ListTag();
            for (String name : entry.getValue()) names.add(StringTag.valueOf(name));
            compound.put("blueprints", names);
            list.add(compound);
        }
        tag.put("knowledge", list);
        return tag;
    }
}
