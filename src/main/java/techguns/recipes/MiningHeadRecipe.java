package techguns.recipes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import techguns.TGRecipeSerializers;
import techguns.items.guns.MiningGun;
import techguns.util.TGItemNbt;

/** Original head upgrade recipe, copying all weapon data before replacing the mining head. */
public final class MiningHeadRecipe extends ShapelessRecipe {
    private MiningHeadRecipe(ShapelessRecipe source){super(source.getGroup(),source.category(),source.getResultItem(RegistryAccess.EMPTY),source.getIngredients());}
    @Override public ItemStack assemble(CraftingInput input,HolderLookup.Provider registries) {
        ItemStack weapon=ItemStack.EMPTY,head=ItemStack.EMPTY;
        for(int i=0;i<input.size();i++){var stack=input.getItem(i);if(stack.getItem() instanceof MiningGun)weapon=stack;else if(!stack.isEmpty())head=stack;}
        if(!(weapon.getItem() instanceof MiningGun gun)||gun.getMiningHeadLevelForHead(head)==0)return ItemStack.EMPTY;
        var result=super.assemble(input,registries);gun.getCurrentAmmo(weapon);result.applyComponents(weapon.getComponentsPatch());
        int level=gun.getMiningHeadLevelForHead(head);TGItemNbt.update(result,tag->tag.putInt("miningHead",level));return result;
    }
    @Override public RecipeSerializer<?> getSerializer(){return TGRecipeSerializers.MINING_HEAD.get();}
    public static final class Serializer implements RecipeSerializer<MiningHeadRecipe> {
        private final ShapelessRecipe.Serializer delegate=new ShapelessRecipe.Serializer();
        private final MapCodec<MiningHeadRecipe> codec=delegate.codec().xmap(MiningHeadRecipe::new,r->r);
        private final StreamCodec<RegistryFriendlyByteBuf,MiningHeadRecipe> stream=delegate.streamCodec().map(MiningHeadRecipe::new,r->r);
        @Override public MapCodec<MiningHeadRecipe> codec(){return codec;}
        @Override public StreamCodec<RegistryFriendlyByteBuf,MiningHeadRecipe> streamCodec(){return stream;}
    }
}
