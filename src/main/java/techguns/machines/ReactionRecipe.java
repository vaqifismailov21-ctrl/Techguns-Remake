package techguns.machines;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.*;
import net.minecraftforge.registries.ForgeRegistries;
public record ReactionRecipe(String id,Ingredient input,Item focus,Fluid fluid,ItemStack[] outputs,int ticks,int requiredCompletion,int preferredIntensity,int margin,int liquidLevel,int liquidConsumption,float instability,Risk risk,int energy){
    public enum Risk {NONE,BREAK_ITEM,EXPLOSION_LOW,EXPLOSION_MEDIUM}
    private static List<ReactionRecipe> recipes;
    public static List<ReactionRecipe> all(){if(recipes==null){recipes=new ArrayList<>();GeneratedReactionRecipes.add(recipes);CommunityRecipes.reaction(recipes);}return Collections.unmodifiableList(recipes);}
    public static ReactionRecipe byId(String id){return all().stream().filter(r->r.id.equals(id)).findFirst().orElse(null);}
    public static Fluid externalOrLava(String name){return ForgeRegistries.FLUIDS.getValues().stream().filter(f->ForgeRegistries.FLUIDS.getKey(f).getPath().equals(name)).findFirst().orElse(Fluids.LAVA);}
    public boolean matches(ProcessingMachine machine,int intensity){return input.test(machine.inventory.getStackInSlot(0))&&machine.inventory.getStackInSlot(1).is(focus)&&machine.inputTank.getFluid().getFluid()==fluid&&machine.inputTank.getFluidAmount()>=this.liquidLevel*1000&&preferredIntensity==intensity;}
}
