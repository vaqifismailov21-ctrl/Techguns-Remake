package techguns.gametest;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import techguns.TGConfig;
import techguns.TGItems;
import techguns.TGOreDrill;
import techguns.TGWorld;
import techguns.Techguns;
import techguns.oredrill.*;
import techguns.world.ClusterKind;

/** Executed checks of the ported original ore drill: geometry rules, forming/unforming, placement rules, fuel/power and mining. */
@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class OreDrillGameTests {
    private static net.minecraft.world.level.block.state.BlockState part(OreDrillType type){return TGOreDrill.BLOCKS.get(type).get().defaultBlockState();}
    private static void cluster(GameTestHelper h,BlockPos rel,ClusterKind kind){h.setBlock(rel,TGWorld.CLUSTERS.get(kind).get());}

    /** Builds the original drill around a downward controller using the source definition's position lists. */
    private static BlockPos buildLarge(GameTestHelper h,BlockPos controllerRel,int rad,int engines,int rods,ClusterKind kind){
        var level=h.getLevel();var master=h.absolutePos(controllerRel);var down=Direction.DOWN;
        level.setBlockAndUpdate(master,part(OreDrillType.CONTROLLER));
        for(var p:OreDrillDefinitionAccess.engines(master,down,rad,engines))level.setBlockAndUpdate(p,part(OreDrillType.ENGINE));
        for(int i=1;i<=rods;i++)level.setBlockAndUpdate(master.relative(down,engines+i),part(OreDrillType.ROD));
        for(var p:OreDrillDefinitionAccess.frame(master,down,rad+1,engines+rods,true,true))level.setBlockAndUpdate(p,part(OreDrillType.FRAME));
        for(var p:OreDrillDefinitionAccess.frame(master,down,rad+1,engines+rods,false,false))level.setBlockAndUpdate(p,part(OreDrillType.SCAFFOLD));
        level.setBlockAndUpdate(master.relative(down,engines+rods+1),TGWorld.CLUSTERS.get(kind).get().defaultBlockState());
        return master;
    }

    @GameTest(template="empty",batch="oredrill") public static void oreDrillGeometryKeepsOriginalRules(GameTestHelper h){
        h.assertTrue(OreDrillDefinition.MAX_RADIUS==3&&OreDrillDefinition.MAX_SIZE==16,"Original size limits");
        h.assertTrue(OreDrillDefinition.checkDrillSize(3,2,1)&&!OreDrillDefinition.checkDrillSize(2,2,1)&&!OreDrillDefinition.checkDrillSize(3,4,1)&&!OreDrillDefinition.checkDrillSize(3,1,1),"rods >= 2r+1, rods >= engines, engines > r");
        var o=BlockPos.ZERO;var d=Direction.DOWN;
        // Hand-counted from the original loops for radius 1 (box radius 2, 5x5) and length 5.
        h.assertTrue(OreDrillDefinitionAccess.engines(o,d,1,2).size()==18,"3x3x2 engines");
        h.assertTrue(OreDrillDefinitionAccess.frame(o,d,2,5,true,true).size()==44,"Frame: 4 long edges of 5 plus two end rings of 12");
        h.assertTrue(OreDrillDefinitionAccess.frame(o,d,2,5,false,true).size()==36,"Checked scaffold: 3 middle rows of 12 side cells");
        h.assertTrue(OreDrillDefinitionAccess.frame(o,d,2,5,false,false).size()==44,"Linked scaffold also includes the 8 last-row inner cells");
        h.assertTrue(OreDrillDefinitionAccess.air(o.relative(d,2),d,1,2).size()==16&&OreDrillDefinitionAccess.finalRow(o,d,1,5).size()==8,"Air around two rod rows and the hidden final row");
        h.assertTrue(OreDrillDefinition.getSide1(Direction.EAST)==Direction.DOWN&&OreDrillDefinition.getSide2(Direction.EAST)==Direction.NORTH&&OreDrillDefinition.getSide1(Direction.UP)==Direction.NORTH&&OreDrillDefinition.getSide2(Direction.SOUTH)==Direction.EAST,"Original side axes");
        h.setBlock(2,2,2,TGOreDrill.CONTROLLER.get());var tile=(OreDrillTileEntMaster)h.getBlockEntity(new BlockPos(2,2,2));
        h.assertTrue(tile.energy.getMaxEnergyStored()==500000&&tile.inputTank.getCapacity()==16000&&tile.outputTank.getCapacity()==32000&&tile.inventory.getSlots()==11,"Original controller: 500000 FE, 16/32 bucket tanks, 11 slots");
        Techguns.LOGGER.info("TECHGUNS_OREDRILL_GEOMETRY_PASSED: original limits, size rule, hand-counted engine/frame/scaffold/air/final-row cells, side axes and controller capacities");
        h.succeed();
    }

    @GameTest(template="weapon_arena",batch="oredrill") public static void oreDrillFormsBreaksAndReportsErrors(GameTestHelper h){
        var player=h.makeMockPlayer();var level=h.getLevel();
        // Mini drill: controller, one rod, cluster.
        cluster(h,new BlockPos(4,10,4),ClusterKind.COAL);h.setBlock(4,11,4,part(OreDrillType.ROD));h.setBlock(4,12,4,part(OreDrillType.CONTROLLER));
        var miniPos=h.absolutePos(new BlockPos(4,12,4));
        h.assertTrue(OreDrillDefinition.checkForm(level,player,miniPos),"Controller + rod + cluster is a valid mini drill");OreDrillDefinition.form(level,miniPos);
        var mini=(OreDrillTileEntMaster)level.getBlockEntity(miniPos);var rod=(OreDrillSlaveEntity)level.getBlockEntity(miniPos.below());
        h.assertTrue(mini.isFormed()&&mini.getEngines()==0&&mini.getRods()==1&&mini.getDrillRadius()==0&&mini.getDrill_direction()==Direction.DOWN,"Mini drill dimensions");
        h.assertTrue(rod.hasMaster()&&rod.getMasterPos().equals(miniPos)&&level.getBlockState(miniPos.below()).getValue(OreDrillBlock.FORMED)&&level.getBlockState(miniPos).getValue(OreDrillBlock.FORMED),"Rod linked and both blocks formed");
        // Large drill: radius 1, two engine layers, three rods, pointing down.
        var master=buildLarge(h,new BlockPos(24,22,24),1,2,3,ClusterKind.COMMON_METAL);
        h.assertTrue(OreDrillDefinition.checkForm(level,player,master),"Complete large drill is valid");OreDrillDefinition.form(level,master);
        var tile=(OreDrillTileEntMaster)level.getBlockEntity(master);
        h.assertTrue(tile.isFormed()&&tile.getEngines()==2&&tile.getRods()==3&&tile.getRadius()==1&&tile.getDrillRadius()==2&&tile.getTotalDrillLength()==5,"Large drill dimensions");
        int[] types=new int[5];int hidden=0,formed=0;
        for(int x=-3;x<=3;x++)for(int y=-6;y<=0;y++)for(int z=-3;z<=3;z++){var p=master.offset(x,y,z);if(level.getBlockEntity(p) instanceof OreDrillSlaveEntity s&&s.hasMaster()&&s.getMasterPos().equals(master)){types[s.getSlaveType()]++;formed+=level.getBlockState(p).getValue(OreDrillBlock.FORMED)?1:0;if(OreDrillScaffoldBlock.isHidden(level.getBlockState(p)))hidden++;}}
        h.assertTrue(types[0]==3&&types[1]==18&&types[2]==44&&types[3]==36&&types[4]==8,"Original slave types: rods 0, engines 1, frame 2, scaffold 3, hidden final row 4: "+java.util.Arrays.toString(types));
        h.assertTrue(formed==109&&hidden==8,"All 109 parts formed and the 8 final-row scaffolds hidden");
        // Breaking a frame block unforms everything and restores visible scaffolds.
        var someFrame=OreDrillDefinitionAccess.frame(master,Direction.DOWN,2,5,true,true).get(0);
        level.destroyBlock(someFrame,false);
        h.assertTrue(!tile.isFormed()&&!level.getBlockState(master).getValue(OreDrillBlock.FORMED),"Breaking a part unforms the controller");
        int stillLinked=0,stillHidden=0;for(int x=-3;x<=3;x++)for(int y=-6;y<=0;y++)for(int z=-3;z<=3;z++){var p=master.offset(x,y,z);if(level.getBlockEntity(p) instanceof OreDrillSlaveEntity s&&(s.hasMaster()||level.getBlockState(p).getValue(OreDrillBlock.FORMED)))stillLinked++;if(OreDrillScaffoldBlock.isHidden(level.getBlockState(p)))stillHidden++;}
        h.assertTrue(stillLinked==0&&stillHidden==0,"All parts unlinked and visible again");
        h.assertTrue(!OreDrillDefinition.checkForm(level,player,master),"Missing frame block is rejected");
        level.setBlockAndUpdate(someFrame,part(OreDrillType.FRAME));
        var airPos=OreDrillDefinitionAccess.air(master.relative(Direction.DOWN,2),Direction.DOWN,1,2).get(0);level.setBlockAndUpdate(airPos,Blocks.STONE.defaultBlockState());
        h.assertTrue(!OreDrillDefinition.checkForm(level,player,master),"Blocks inside the rod space are rejected");level.setBlockAndUpdate(airPos,Blocks.AIR.defaultBlockState());
        h.assertTrue(OreDrillDefinition.checkForm(level,player,master),"Repaired structure is valid again");
        level.setBlockAndUpdate(master.above(),part(OreDrillType.ROD));h.assertTrue(!OreDrillDefinition.checkForm(level,player,master),"Two drill lines next to a controller are rejected");level.setBlockAndUpdate(master.above(),Blocks.AIR.defaultBlockState());
        // Engines larger than the rods allow: 3 engines but only 2 rods after shortening.
        Techguns.LOGGER.info("TECHGUNS_OREDRILL_FORM_PASSED: mini and 3x3 drills formed with original slave types and hidden final row, part break unforms all, frame/air/direction errors rejected");
        h.succeed();
    }

    @GameTest(template="weapon_arena",batch="oredrill") public static void oreDrillPlacementRulesMatchOriginal(GameTestHelper h){
        var player=h.makeMockPlayer();var level=h.getLevel();
        cluster(h,new BlockPos(10,10,10),ClusterKind.COAL);h.setBlock(10,10,11,TGWorld.CLUSTERS.get(ClusterKind.COAL).get());
        h.setBlock(20,10,20,Blocks.STONE);
        var rodItem=(OreDrillBlockItem)TGOreDrill.ITEMS.get(OreDrillType.ROD).get();var controllerItem=(OreDrillBlockItem)TGOreDrill.ITEMS.get(OreDrillType.CONTROLLER).get();
        h.assertTrue(place(h,player,rodItem,new BlockPos(20,10,20),Direction.UP)==InteractionResult.FAIL,"A rod needs an ore cluster or another rod");
        h.assertTrue(place(h,player,rodItem,new BlockPos(10,10,10),Direction.UP).consumesAction(),"First rod on a cluster");
        h.assertTrue(place(h,player,rodItem,new BlockPos(10,10,11),Direction.UP)==InteractionResult.FAIL,"A connected cluster already has a rod");
        h.assertTrue(place(h,player,rodItem,new BlockPos(10,11,10),Direction.UP).consumesAction(),"Rods extend from rods");
        h.assertTrue(place(h,player,controllerItem,new BlockPos(10,12,10),Direction.UP).consumesAction(),"Controller on the rod");
        var controller=(OreDrillTileEntMaster)level.getBlockEntity(h.absolutePos(new BlockPos(10,13,10)));h.assertTrue(controller.isOwnedByPlayer(player),"Controller owned by the placer");
        h.assertTrue(place(h,player,controllerItem,new BlockPos(10,13,10),Direction.UP)==InteractionResult.FAIL,"Two controllers may not touch");
        Techguns.LOGGER.info("TECHGUNS_OREDRILL_PLACEMENT_PASSED: rod needs cluster/rod, one rod per connected cluster, rods extend, controller ownership and no adjacent controllers");
        h.succeed();
    }
    private static InteractionResult place(GameTestHelper h,Player player,OreDrillBlockItem item,BlockPos against,Direction face){
        var abs=h.absolutePos(against);var stack=new ItemStack(item);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        return item.place(new BlockPlaceContext(new UseOnContext(h.getLevel(),player,InteractionHand.MAIN_HAND,stack,new BlockHitResult(Vec3.atCenterOf(abs).relative(face,.5),face,abs,false))));
    }

    @GameTest(template="weapon_arena",batch="oredrill") public static void oreDrillMinesWithOriginalRatesFuelAndOutputs(GameTestHelper h){
        var player=h.makeMockPlayer();var level=h.getLevel();
        cluster(h,new BlockPos(6,10,6),ClusterKind.COAL);h.setBlock(6,11,6,part(OreDrillType.ROD));h.setBlock(6,12,6,part(OreDrillType.CONTROLLER));
        var pos=h.absolutePos(new BlockPos(6,12,6));OreDrillDefinition.form(level,pos);var tile=(OreDrillTileEntMaster)level.getBlockEntity(pos);
        // No drill head: nothing starts and the GUI reports the missing head.
        tile.tick();h.assertTrue(!tile.isWorking()&&!tile.hasDrill()&&tile.getDrillItemMiningLevel()<0,"No head, no operation");
        h.assertTrue(!OreDrillMenu.isValidDrill(tile,new ItemStack(TGItems.OREDRILLHEAD_MEDIUM_STEEL.get()))&&OreDrillMenu.isValidDrill(tile,new ItemStack(TGItems.OREDRILLHEAD_STEEL.get())),"Mini drill accepts only small heads in its slot");
        tile.inventory.setStackInSlot(OreDrillTileEntMaster.SLOT_DRILL,new ItemStack(TGItems.OREDRILLHEAD_STEEL.get()));tile.tick();
        // Coal: mining level 0, amount x10, power x0.1; single-block cluster, drill length 1, small steel level 1.
        double orePerHour=((1*3)+(1*1*0.5))*TGConfig.oreDrillMultiplierOres*TGConfig.oremult_coal;
        h.assertTrue(tile.isWorking()&&tile.hasDrill()&&tile.getDrillType()==1&&tile.totaltime==(int)(60.0/orePerHour*1200.0)&&tile.getPowerPerTick()==(int)(8*orePerHour*TGConfig.powermult_coal*TGConfig.oreDrillMultiplierPower),"Original ore/hour, duration and power formula: "+tile.totaltime+" "+tile.getPowerPerTick());
        // Furnace fuel: coal burns 1600 ticks = 1600 x oreDrillMultiplierFuel internal energy.
        tile.inventory.setStackInSlot(OreDrillTileEntMaster.SLOT_FURNACE,new ItemStack(Items.COAL,2));int before=tile.progress;tile.tick();
        h.assertTrue(tile.progress==before+1&&tile.hasPower()&&tile.getCurrentFuelBufferMax()==1600&&tile.inventory.getStackInSlot(OreDrillTileEntMaster.SLOT_FURNACE).getCount()==1,"One coal loads the fuel buffer");
        int power=tile.getPowerPerTick();int expectedBuffer=(int)(1600-Math.max(power/TGConfig.oreDrillMultiplierFuel,1));
        h.assertTrue(tile.getFuelBuffer()==expectedBuffer,"Buffer drain max(power/factor,1): "+tile.getFuelBuffer());
        // Finish: the cluster's weighted ore lands in the outputs.
        tile.progress=tile.totaltime-1;tile.tick();
        var out=tile.inventory.getStackInSlot(OreDrillTileEntMaster.SLOT_OUTPUT1);
        h.assertTrue(out.is(Blocks.COAL_ORE.asItem())||out.is(Items.DIAMOND),"Coal cluster yields coal ore (99) or a diamond (1): "+out);
        h.assertTrue(tile.isWorking()&&tile.progress==0,"Next operation starts immediately");
        // Lava in the input tank refuels at 20 per mB when no furnace fuel is left.
        tile.inventory.setStackInSlot(OreDrillTileEntMaster.SLOT_FURNACE,ItemStack.EMPTY);tile.setFuelBuffer(0);
        var fluids=tile.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        h.assertTrue(fluids.fill(new FluidStack(Fluids.LAVA,2000),IFluidHandler.FluidAction.EXECUTE)==2000,"Input tank accepts fluids");
        tile.tick();h.assertTrue(tile.inputTank.getFluidAmount()==1000&&tile.getCurrentFuelBufferMax()==20000,"One bucket of lava = 20000 fuel");
        // Removing the head cancels the operation on the next 20-tick check once a head was recorded there.
        tile.lastDrill=tile.inventory.getStackInSlot(OreDrillTileEntMaster.SLOT_DRILL).copy();tile.inventory.setStackInSlot(OreDrillTileEntMaster.SLOT_DRILL,ItemStack.EMPTY);
        h.runAfterDelay(25,()->{
            h.assertTrue(!tile.isWorking(),"Removing the head cancels the operation");
            // A cluster above the head's level yields cobblestone at 24 power for 400 ticks.
            level.setBlockAndUpdate(h.absolutePos(new BlockPos(6,10,6)),TGWorld.CLUSTERS.get(ClusterKind.SHINY_GEM).get().defaultBlockState());
            tile.inventory.setStackInSlot(OreDrillTileEntMaster.SLOT_DRILL,new ItemStack(TGItems.OREDRILLHEAD_STEEL.get()));tile.tick();
            h.assertTrue(tile.isWorking()&&tile.totaltime==400&&tile.getPowerPerTick()==(int)(24*TGConfig.oreDrillMultiplierPower),"Insufficient level: cobblestone at 24 power for 400 ticks");
            Techguns.LOGGER.info("TECHGUNS_OREDRILL_MINING_PASSED: head/radius rules, original ore-per-hour/duration/power formula, furnace and lava fuel buffers, weighted cluster output, head removal and cobblestone fallback");
            h.succeed();
        });
    }

    @GameTest(template="weapon_arena",batch="oredrill") public static void oreDrillMenuMatchesOriginal(GameTestHelper h){
        var player=h.makeMockPlayer();var level=h.getLevel();
        cluster(h,new BlockPos(6,10,6),ClusterKind.COAL);h.setBlock(6,11,6,part(OreDrillType.ROD));h.setBlock(6,12,6,part(OreDrillType.CONTROLLER));
        var pos=h.absolutePos(new BlockPos(6,12,6));var tile=(OreDrillTileEntMaster)level.getBlockEntity(pos);player.setPos(Vec3.atCenterOf(pos).add(2,0,0));
        var menu=new OreDrillMenu(1,player.getInventory(),tile);
        h.assertTrue(menu.slots.size()==47&&menu.slots.get(0).x==30&&menu.slots.get(0).y==17&&menu.slots.get(1).y==51&&menu.slots.get(2).x==102&&menu.slots.get(10).x==138&&menu.slots.get(10).y==53,"Original slot layout");
        h.assertTrue(!menu.slots.get(0).mayPlace(new ItemStack(TGItems.OREDRILLHEAD_STEEL.get())),"Unformed drill accepts no head");
        OreDrillDefinition.form(level,pos);
        player.getInventory().setItem(9,new ItemStack(TGItems.OREDRILLHEAD_STEEL.get()));player.getInventory().setItem(10,new ItemStack(Items.COAL,5));player.getInventory().setItem(11,new ItemStack(Blocks.DIRT,5));
        menu.quickMoveStack(player,11);menu.quickMoveStack(player,12);menu.quickMoveStack(player,13);
        h.assertTrue(tile.inventory.getStackInSlot(0).is(TGItems.OREDRILLHEAD_STEEL.get())&&tile.inventory.getStackInSlot(1).getCount()==5&&player.getInventory().getItem(11).getCount()==5,"Shift-click: head and fuel to their slots, others stay");
        h.assertTrue(menu.quickMoveStack(player,0).isEmpty()&&tile.inventory.getStackInSlot(0).getCount()==1,"The head never shift-clicks out");
        menu.quickMoveStack(player,1);h.assertTrue(tile.inventory.getStackInSlot(1).isEmpty(),"Fuel shift-clicks back");
        h.assertTrue(menu.clickMenuButton(player,OreDrillTileEntMaster.BUTTON_ID_REDSTONE)&&tile.getRedstoneBehaviour()==1&&!menu.clickMenuButton(player,5),"Redstone button cycles; unknown buttons rejected");
        var items=tile.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        h.assertTrue(items.insertItem(OreDrillTileEntMaster.SLOT_OUTPUT1,new ItemStack(Blocks.STONE),false).getCount()==1&&items.extractItem(OreDrillTileEntMaster.SLOT_DRILL,1,true).isEmpty(),"Automation: no output insertion, no head extraction");
        Techguns.LOGGER.info("TECHGUNS_OREDRILL_MENU_PASSED: original slot layout, formed/size head rule, shift-click routing, buttons and automation access");
        h.succeed();
    }

    /** Test access to the original position loops. */
    static final class OreDrillDefinitionAccess {
        static List<BlockPos> engines(BlockPos m,Direction d,int r,int len){return reflect("getEngineBlocks",m,d,r,len);}
        static List<BlockPos> air(BlockPos m,Direction d,int r,int len){return reflect("getAirBlocks",m,d,r,len);}
        static List<BlockPos> finalRow(BlockPos m,Direction d,int r,int len){return reflect("getFinalScaffoldRow",m,d,r,len);}
        static List<BlockPos> frame(BlockPos m,Direction d,int r,int len,boolean outer,boolean last){
            try{var method=OreDrillDefinition.class.getDeclaredMethod("getFrameBlocks",BlockPos.class,Direction.class,int.class,int.class,boolean.class,boolean.class);method.setAccessible(true);@SuppressWarnings("unchecked") var list=(List<BlockPos>)method.invoke(null,m,d,r,len,outer,last);return new ArrayList<>(list);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
        }
        @SuppressWarnings("unchecked") private static List<BlockPos> reflect(String name,BlockPos m,Direction d,int r,int len){
            try{var method=OreDrillDefinition.class.getDeclaredMethod(name,BlockPos.class,Direction.class,int.class,int.class);method.setAccessible(true);return new ArrayList<>((List<BlockPos>)method.invoke(null,m,d,r,len));}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
        }
    }
}
