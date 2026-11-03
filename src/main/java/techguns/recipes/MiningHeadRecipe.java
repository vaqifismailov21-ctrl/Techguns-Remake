package techguns.recipes;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import techguns.TGRecipeSerializers;
import techguns.items.guns.MiningGun;
/** Original head upgrade recipe, copying all weapon NBT before replacing the mining head. */
public final class MiningHeadRecipe extends ShapelessRecipe {
    private MiningHeadRecipe(ShapelessRecipe source){super(source.getId(),source.getGroup(),source.getResultItem(),source.getIngredients());}
    @Override public ItemStack assemble(CraftingContainer input) {
        ItemStack weapon=ItemStack.EMPTY,head=ItemStack.EMPTY;
        for(int i=0;i<input.getContainerSize();i++){var stack=input.getItem(i);if(stack.getItem() instanceof MiningGun)weapon=stack;else if(!stack.isEmpty())head=stack;}
        if(!(weapon.getItem() instanceof MiningGun gun)||gun.getMiningHeadLevelForHead(head)==0)return ItemStack.EMPTY;
        var result=super.assemble(input);gun.getCurrentAmmo(weapon);result.setTag(weapon.getTag().copy());result.getOrCreateTag().putInt("miningHead",gun.getMiningHeadLevelForHead(head));return result;
    }
    @Override public RecipeSerializer<?> getSerializer(){return TGRecipeSerializers.MINING_HEAD.get();}
    public static final class Serializer implements RecipeSerializer<MiningHeadRecipe> {
        private final ShapelessRecipe.Serializer delegate=new ShapelessRecipe.Serializer();
        @Override public MiningHeadRecipe fromJson(ResourceLocation id,JsonObject json){return new MiningHeadRecipe(delegate.fromJson(id,json));}
        @Override public MiningHeadRecipe fromNetwork(ResourceLocation id,FriendlyByteBuf b){return new MiningHeadRecipe(delegate.fromNetwork(id,b));}
        @Override public void toNetwork(FriendlyByteBuf b,MiningHeadRecipe recipe){delegate.toNetwork(b,recipe);}
    }
}
