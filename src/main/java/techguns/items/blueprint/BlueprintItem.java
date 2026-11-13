package techguns.items.blueprint;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import techguns.TGSounds;

/** A blueprint found in Techguns chests: reading it lets the player craft the blocks it describes. */
public final class BlueprintItem extends Item {
    public final String blueprint;
    public BlueprintItem(String blueprint, Rarity rarity) { super(new Item.Properties().stacksTo(16).rarity(rarity)); this.blueprint = blueprint; }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide && level.getServer() != null) {
            boolean fresh = BlueprintKnowledge.get(level.getServer()).learn(player.getUUID(), blueprint);
            player.displayClientMessage(Component.translatable(fresh ? "techguns.blueprint." + blueprint + ".learned" : "techguns.blueprints.known")
                    .withStyle(fresh ? ChatFormatting.GREEN : ChatFormatting.GRAY), true);
            if (fresh) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), TGSounds.BLUEPRINT_UNLOCK.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) held.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("techguns.blueprint." + blueprint + ".tooltip").withStyle(ChatFormatting.GRAY));
        for (var id : Blueprints.unlocks(blueprint)) tooltip.add(Component.literal(" - ").append(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(id).getDescription()).withStyle(ChatFormatting.DARK_GRAY));
    }
}
