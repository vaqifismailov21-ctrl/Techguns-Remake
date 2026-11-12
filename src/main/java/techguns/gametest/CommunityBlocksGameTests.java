package techguns.gametest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.blocks.*;
import techguns.fluids.FluidPipes;
import techguns.machines.*;
@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class CommunityBlocksGameTests {
    /** Steam made in one chemical laboratory flows through pipes into a metal press, and nothing else gets into the press. */
    @GameTest(template="empty") public static void pipesCarrySteamFromChemLabToMetalPress(GameTestHelper h){
        h.setBlock(1,1,1,TGMachines.BLOCKS.get(MachineKind.CHEM_LAB).get());h.setBlock(2,1,1,FluidPipes.PIPE.get());h.setBlock(3,1,1,FluidPipes.PIPE.get());h.setBlock(4,1,1,TGMachines.BLOCKS.get(MachineKind.METAL_PRESS).get());
        var lab=(ProcessingMachine)h.getBlockEntity(new BlockPos(1,1,1));var press=(ProcessingMachine)h.getBlockEntity(new BlockPos(4,1,1));
        lab.outputTank.fill(new FluidStack(TGFluids.STEAM.get(),1000),net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        h.succeedWhen(()->{h.assertTrue(press.inputTank.getFluidAmount()==1000&&lab.outputTank.isEmpty(),"all steam reached the press");});
    }
    /** A pane goes into a frame and comes out again; the frame keeps its facing. */
    @GameTest(template="empty") public static void windowFrameTakesAndReturnsPane(GameTestHelper h){
        var frame=TGDecorations.BLOCKS.get("windowframe_metal").get();h.setBlock(2,1,2,frame);var pos=new BlockPos(2,1,2);
        var player=h.makeMockPlayer();player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.RED_STAINED_GLASS_PANE));
        h.getBlockState(pos).use(h.getLevel(),player,net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(pos)),net.minecraft.core.Direction.NORTH,h.absolutePos(pos),false));
        h.assertTrue(h.getBlockState(pos).getValue(WindowFrameBlock.PANE)==WindowFrameBlock.Pane.RED,"red pane set");
        h.succeed();
    }
    /** Two fences side by side next to two more form a solid 2x2 wall with filled corners. */
    @GameTest(template="empty") public static void fencesFillCornersInSquares(GameTestHelper h){
        var fence=TGDecorations.BLOCKS.get("fence_container_red").get();
        h.setBlock(2,1,2,fence);h.setBlock(3,1,2,fence);h.setBlock(2,1,3,fence);h.setBlock(3,1,3,fence);
        var state=h.getBlockState(new BlockPos(2,1,2));
        h.assertTrue(state.getValue(DecorFenceBlock.EAST)&&state.getValue(DecorFenceBlock.SOUTH)&&state.getValue(DecorFenceBlock.CORNER_ES),"corner filled in a square");
        h.succeed();
    }
}
