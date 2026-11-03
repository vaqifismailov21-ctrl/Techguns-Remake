package techguns.api.tginventory;

import net.minecraft.network.chat.Component;

/** Original slot types and original translation keys, including the historical spelling. */
public enum TGSlotType {
    NORMAL("normal_item"),
    FACESLOT("face"),
    BACKSLOT("back"),
    HANDSLOT("hands"),
    AMMOSLOT("ammo"),
    FOODSLOT("food"),
    HEALSLOT("healing"),
    TURRETARMOR("turret_armor"),
    REACTION_CHAMBER_FOCUS("reaction_focus"),
    DRILL_SMALL("small_drill"),
    DRILL_MEDIUM("medium_drill"),
    DRILL_LARGE("large_drill"),
    ARMOR_UPGRADE("armor_ugpgrade");

    private final String translationKey;

    TGSlotType(String key) {
        translationKey = "techguns.tooltip.tgslottype." + key;
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }
}
