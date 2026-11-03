package techguns.blocks;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.shapes.*;
import techguns.world.MilitaryCrateKind;

/** Original player-harvest loot, including Fortune-as-luck and Silk Touch pickup. */
public final class MilitaryCrateBlock extends Block {
    public final MilitaryCrateKind kind;
    private static final VoxelShape SHAPE=Block.box(.5,0,.5,15.5,16,15.5);
    public MilitaryCrateBlock(MilitaryCrateKind kind){super(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(4,4).sound(SoundType.STONE).noOcclusion());this.kind=kind;}
    @Override public VoxelShape getShape(BlockState state,BlockGetter world,BlockPos pos,CollisionContext context){return SHAPE;}
    @Override public List<ItemStack> getDrops(BlockState state,LootParams.Builder builder){
        var entity=builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
        var tool=builder.getOptionalParameter(LootContextParams.TOOL);if(tool==null)tool=ItemStack.EMPTY;
        if(!(entity instanceof Player)||EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH,tool)>0){
            Float radius=builder.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);
            // The old explosion used dropBlockAsItemWithChance(1 / explosionSize).
            if(radius!=null&&builder.getLevel().random.nextFloat()>1/radius)return List.of();
            return List.of(new ItemStack(this));
        }
        int fortune=EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,tool);
        var params=new LootParams.Builder(builder.getLevel()).withParameter(LootContextParams.ORIGIN,builder.getParameter(LootContextParams.ORIGIN)).withParameter(LootContextParams.THIS_ENTITY,entity).withLuck(fortune).create(LootContextParamSets.CHEST);
        return builder.getLevel().getServer().getLootData().getLootTable(ResourceLocation.fromNamespaceAndPath("techguns","blocks/military_crate_"+kind.loot)).getRandomItems(params);
    }
}
