package techguns.machines;
import java.util.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import techguns.items.armors.TGSuitArmor;
public record GrindingRecipe(Item input,ItemStack[] outputs,double[] chances){
    private static List<GrindingRecipe> recipes;
    public static List<GrindingRecipe> all(){if(recipes==null){recipes=new ArrayList<>();GeneratedGrindingRecipes.add(recipes);}return Collections.unmodifiableList(recipes);}
    public static GrindingRecipe find(ItemStack input){if(input.getItem() instanceof TGSuitArmor armor){var inverted=input.copy();inverted.setDamageValue(input.getMaxDamage()-input.getDamageValue());return new GrindingRecipe(armor,armor.repairMaterials(inverted).toArray(ItemStack[]::new),null);}return all().stream().filter(r->input.is(r.input)).findFirst().orElse(null);}
    public List<ItemStack> maximum(int multiplier){var result=new ArrayList<ItemStack>();for(int i=0;i<outputs.length;i++){int count=(int)Math.ceil(outputs[i].getCount()*(chances==null?1:chances[i])*multiplier);if(count>0)result.add(outputs[i].copyWithCount(count));}return result;}
    public List<ItemStack> roll(RandomSource random,int multiplier){if(chances==null)return maximum(multiplier);var result=new ArrayList<ItemStack>();for(int i=0;i<outputs.length;i++){double amount=outputs[i].getCount()*chances[i]*multiplier;int fixed=(int)Math.floor(amount);if(random.nextDouble()<=amount-fixed)fixed++;if(fixed>0)result.add(outputs[i].copyWithCount(fixed));}return result;}
}
