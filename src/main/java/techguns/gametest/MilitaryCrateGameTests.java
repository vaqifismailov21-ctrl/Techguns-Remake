package techguns.gametest;

import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.world.MilitaryCrateKind;

@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class MilitaryCrateGameTests {
    @GameTest(template="empty",batch="world-crates") public static void nineSourceCrateVariantsYieldRealCategoryLootAndSilkPickup(GameTestHelper h){
        var player=h.makeMockSurvivalPlayer();var tool=new ItemStack(Items.IRON_AXE);int total=0;
        for(var kind:MilitaryCrateKind.values()){
            var block=TGWorld.CRATES.get(kind).get();var state=block.defaultBlockState();
            h.assertTrue(state.getDestroySpeed(h.getLevel(),h.absolutePos(net.minecraft.core.BlockPos.ZERO))==4,"Original crate hardness");
            h.assertTrue(state.getSoundType()==SoundType.STONE,"Original GenericBlockMetaEnum stone sound");
            var shape=state.getShape(h.getLevel(),h.absolutePos(net.minecraft.core.BlockPos.ZERO)).bounds();h.assertTrue(shape.minX==.03125&&shape.maxX==.96875&&shape.maxY==1,"Original full-height inset crate collision shape");
            int generated=0;for(int sample=0;sample<24;sample++){
                var drops=block.getDrops(state,new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.ORIGIN,Vec3.ZERO).withParameter(LootContextParams.THIS_ENTITY,player).withParameter(LootContextParams.TOOL,tool));
                for(var drop:drops){h.assertTrue(!drop.isEmpty()&&!drop.is(block.asItem()),"Player harvest must use the original category loot, not a placeholder crate drop: "+kind);generated+=drop.getCount();}
            }
            h.assertTrue(generated>0,"Original category loot must actually generate: "+kind);total+=generated;
            var silk=new ItemStack(Items.IRON_AXE);silk.enchant(Enchantments.SILK_TOUCH,1);
            var pickup=block.getDrops(state,new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.ORIGIN,Vec3.ZERO).withParameter(LootContextParams.THIS_ENTITY,player).withParameter(LootContextParams.TOOL,silk));
            h.assertTrue(pickup.size()==1&&pickup.get(0).is(block.asItem()),"Silk Touch must retain each original crate variant");
            var noPlayer=block.getDrops(state,new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.ORIGIN,Vec3.ZERO).withParameter(LootContextParams.TOOL,tool));
            h.assertTrue(noPlayer.size()==1&&noPlayer.get(0).is(block.asItem()),"No player harvester must not unlock crate contents");
            int survivors=0;for(int sample=0;sample<128;sample++)survivors+=block.getDrops(state,new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.ORIGIN,Vec3.ZERO).withParameter(LootContextParams.TOOL,tool).withParameter(LootContextParams.EXPLOSION_RADIUS,4f)).size();
            h.assertTrue(survivors>0&&survivors<128,"Source explosion decay must drop some intact crates without always preserving every crate: "+kind);
        }
        Techguns.LOGGER.info("TECHGUNS_MILITARY_CRATES_PASSED: nine source variants, real category loot ({} items), Silk Touch pickup, non-player drops and original shape/sound",total);h.succeed();
    }
    private MilitaryCrateGameTests(){}
}
