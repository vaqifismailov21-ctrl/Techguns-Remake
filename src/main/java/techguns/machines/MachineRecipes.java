package techguns.machines;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import techguns.TGItems;
/** Source recipes shared by the machine, automation validation and recipe display. */
public final class MachineRecipes {
    private static List<ProcessingRecipe> recipes;
    public static Ingredient tag(String path){return Ingredient.of(TagKey.create(Registries.ITEM,ResourceLocation.parse(path)));}
    public static Ingredient items(Item... items){return Ingredient.of(items);}
    private static Ingredient alternatives(String... tags){return Ingredient.fromValues(Arrays.stream(tags).map(t->new Ingredient.TagValue(TagKey.create(Registries.ITEM,ResourceLocation.parse(t)))));}
    public static List<ProcessingRecipe> all(){if(recipes==null){recipes=new ArrayList<>();GeneratedMachineRecipes.add(recipes);GeneratedFabricatorRecipes.add(recipes);CommunityRecipes.fabricator(recipes);}return Collections.unmodifiableList(recipes);}
    public static ProcessingRecipe ammo(int plan){
        Item[] outputs={TGItems.PISTOL_ROUNDS.get(),TGItems.SHOTGUN_ROUNDS.get(),TGItems.RIFLE_ROUNDS.get(),TGItems.SNIPER_ROUNDS.get()};int[] counts={12,16,8,4};
        return new ProcessingRecipe(MachineKind.AMMO_PRESS,new Ingredient[]{alternatives("forge:ingots/steel","forge:ingots/lead"),alternatives("forge:ingots/copper","forge:ingots/tin","forge:ingots/iron","forge:ingots/bronze"),items(Items.GUNPOWDER)},new int[]{1,2,1},new ItemStack(outputs[Math.floorMod(plan,4)],counts[Math.floorMod(plan,4)]),100,5,false);
    }
    public static ProcessingRecipe find(ProcessingMachine machine){if(machine.kind==MachineKind.AMMO_PRESS){var recipe=ammo(machine.plan);return recipe.match(machine.inventory)!=null?recipe:null;}return all().stream().filter(r->r.kind()==machine.kind&&r.match(machine.inventory)!=null&&allowed(machine,r)).findFirst().orElse(null);}
    /** Cybernetic parts need the machine owner to have studied the blueprints. */
    public static boolean allowed(ProcessingMachine machine,ProcessingRecipe recipe){
        if(recipe.kind()==MachineKind.FABRICATOR&&recipe.result().is(TGItems.CYBERNETIC_PARTS.get())&&!techguns.items.blueprint.BlueprintKnowledge.knows(machine.getLevel(),machine.access.ownerId()))return false;
        var steam=steam(recipe);return steam==null||machine.steamUpgrade()&&machine.pressure>=steam[1]&&machine.inputTank.getFluidAmount()>=steam[0];}
    /** Metal press recipes worked with steam: {steam in mB, pressure level}; null for ordinary recipes. */
    public static int[] steam(ProcessingRecipe recipe){return recipe.kind()==MachineKind.METAL_PRESS&&recipe.result().is(TGItems.STEAMARMOR_PLATE.get())?new int[]{4000,7}:null;}
    public static boolean accepts(MachineKind kind,int slot,ItemStack stack){if(kind==MachineKind.AMMO_PRESS)return slot<3&&ammo(0).ingredients()[slot].test(stack);for(var r:all())if(r.kind()==kind){if(slot<r.ingredients().length&&r.ingredients()[slot].test(stack))return true;if(r.swap()&&slot<2&&r.ingredients()[1-slot].test(stack))return true;}return false;}
    private MachineRecipes(){}
}
