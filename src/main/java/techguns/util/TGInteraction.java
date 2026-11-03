package techguns.util;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;

/** Maps the 1.20.1 Block#use result onto 1.21 Block#useItemOn (PASS keeps the old "let the item act" behaviour). */
public final class TGInteraction {
    public static ItemInteractionResult item(InteractionResult result) {
        return switch (result) {
            case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
            case CONSUME -> ItemInteractionResult.CONSUME;
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            case FAIL -> ItemInteractionResult.FAIL;
            case PASS -> ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        };
    }
    private TGInteraction() {}
}
