package techguns.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;

/** 1.20.1 GUI helpers on top of the 1.21.1 API. */
public final class TGGuiCompat {
    /**
     * Old InventoryScreen.renderEntityInInventoryFollowsMouse(g, x, y, scale, relX, relY, entity): (x, y) is the feet
     * position, relX/relY the look offsets. 1.21 takes a box and absolute mouse coordinates; this keeps the old framing.
     */
    public static void renderEntity(GuiGraphics g, int x, int y, int scale, float relX, float relY, LivingEntity entity) {
        // 1.21 places the feet (bbHeight/2 + 0.0625) * scale below the box centre; choose the box so the feet stay at y.
        int x1 = x - 25, x2 = x + 25, y1 = y - 67, y2 = y + 9;
        float cx = (x1 + x2) / 2F, cy = (y1 + y2) / 2F;
        InventoryScreen.renderEntityInInventoryFollowsMouse(g, x1, y1, x2, y2, scale, 0.0625F, cx - relX, cy - relY, entity);
    }
    private TGGuiCompat() {}
}
