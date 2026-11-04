package techguns.client;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import techguns.Techguns;

/**
 * -Dtechguns.verifyCompare=true: frames to compare with the original mod — the Techguns creative tab page by page,
 * 3D items in the hand, placed ladders and doors with their break particles, multiblocks (formed, unformed, a failed
 * attempt) and the lock-on reticle of the guided missile launcher. Saved to verification/compare-*.png, then quits.
 */
@Mod.EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
public final class CompareVisualVerifier {
    private static final int Y=100;
    private static int stage,ticks,step,titleFrames;
    private static final List<Runnable> steps=new ArrayList<>();
    private static final List<String> saved=new ArrayList<>();

    private static boolean enabled(){return Boolean.getBoolean("techguns.verifyCompare");}
    @SubscribeEvent public static void title(ScreenEvent.Render.Post event){
        if(!enabled()||!(event.getScreen() instanceof net.minecraft.client.gui.screens.TitleScreen)||++titleFrames!=3)return;
        stage=1;var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;mc.options.guiScale().set(3);
        var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);
        mc.createWorldOpenFlows().createFreshLevel("techguns-compare-"+System.currentTimeMillis(),new LevelSettings("Techguns comparison",GameType.CREATIVE,false,Difficulty.EASY,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(1729L,false,false),access->access.registryOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).createWorldDimensions());
    }
    private static ItemStack item(String id){var item=ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("techguns",id));return item==null?ItemStack.EMPTY:new ItemStack(item);}
    private static net.minecraft.world.level.block.Block block(String id){return ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("techguns",id));}
    private static void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> task){var server=Minecraft.getInstance().getSingleplayerServer();server.execute(()->task.accept(server.getPlayerList().getPlayers().get(0)));}
    private static void view(double x,double y,double z,float yaw,float pitch){server(p->p.teleportTo(p.serverLevel(),x,y,z,yaw,pitch));}
    private static void hold(ItemStack stack){server(p->p.setItemInHand(InteractionHand.MAIN_HAND,stack.copy()));}
    private static void shot(String name){steps.add(()->save(name));}
    private static void wait(int n){for(int i=0;i<n;i++)steps.add(()->{});}

    private static void build(net.minecraft.server.level.ServerLevel level){
        level.setDayTime(6000);
        for(int x=-6;x<=30;x++)for(int z=-6;z<=30;z++)level.setBlockAndUpdate(new BlockPos(x,Y-1,z),Blocks.SMOOTH_STONE.defaultBlockState());
        // ladders on a wall
        for(int i=0;i<4;i++){var wall=new BlockPos(i*2,Y,0);for(int h=0;h<3;h++){level.setBlockAndUpdate(wall.above(h),Blocks.STONE_BRICKS.defaultBlockState());level.setBlockAndUpdate(wall.south().above(h),block(new String[]{"ladder_metal","ladder_shiny","ladder_rusty","ladder_carbon"}[i]).defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,Direction.SOUTH));}}
        // formed fabricator, an unformed one and a reaction chamber
        placeMultiblock(level,new BlockPos(12,Y,4),techguns.machines.MachineKind.FABRICATOR,true);
        placeMultiblock(level,new BlockPos(16,Y,4),techguns.machines.MachineKind.FABRICATOR,false);
        placeMultiblock(level,new BlockPos(22,Y,4),techguns.machines.MachineKind.REACTION,true);
    }
    private static void placeMultiblock(net.minecraft.server.level.ServerLevel level,BlockPos pos,techguns.machines.MachineKind kind,boolean form){
        level.setBlockAndUpdate(pos,techguns.TGMachines.BLOCKS.get(kind).get().defaultBlockState().setValue(techguns.machines.MachineBlock.FACING,Direction.SOUTH));
        var machine=(techguns.machines.ProcessingMachine)level.getBlockEntity(pos);
        var facing=kind==techguns.machines.MachineKind.REACTION?Direction.NORTH:Direction.SOUTH;
        var parts=machine.structureParts(kind==techguns.machines.MachineKind.REACTION?facing.getOpposite():facing);
        boolean reactor=kind==techguns.machines.MachineKind.REACTION;
        for(var e:parts.entrySet())level.setBlockAndUpdate(e.getKey(),(reactor?(e.getValue()?techguns.TGFabricator.REACTION_GLASS:techguns.TGFabricator.REACTION_HOUSING):(e.getValue()?techguns.TGFabricator.GLASS:techguns.TGFabricator.HOUSING)).get().defaultBlockState());
        if(form)machine.formStructure(facing);
    }
    private static void plan(){
        // creative tab, five rows per frame
        steps.add(()->{var mc=Minecraft.getInstance();var screen=new CreativeModeInventoryScreen(mc.player,mc.player.connection.enabledFeatures(),true);mc.setScreen(screen);});
        wait(4);
        steps.add(()->{try{var m=net.minecraftforge.fml.util.ObfuscationReflectionHelper.findMethod(CreativeModeInventoryScreen.class,"m_98560_",net.minecraft.world.item.CreativeModeTab.class);m.invoke(Minecraft.getInstance().screen,Techguns.TAB.get());}catch(Exception e){throw new IllegalStateException(e);}});
        wait(4);
        for(int page=0;page<12;page++){int p=page;shot("creative-"+p);steps.add(()->{var s=Minecraft.getInstance().screen;if(s!=null)for(int i=0;i<5;i++)s.mouseScrolled(s.width/2.0,s.height/2.0,-1);});wait(3);}
        steps.add(()->Minecraft.getInstance().setScreen(null));
        // 3D items in the first person view
        String[] held={"t3_power_helmet","t3_power_chestplate","t3_power_leggings","t3_power_boots","steam_helmet","steam_chestplate","steam_leggings","steam_boots",
                "t4_power_helmet","t4_power_chestplate","t4_power_leggings","t4_power_boots","jetpack","antigravpack","tacticalmask","gasmask","glider","jumppack","scubatanks","nightvisiongoggles",
                "ladder_metal","metal_door_3x3","hangar_door_up","nether_door_3x3","bunker_door","guidedmissilelauncher","rocketlauncher"};
        steps.add(()->view(10.5,Y,20.5,180,0));wait(10);
        for(var id:held){steps.add(()->hold(item(id)));wait(6);steps.add(()->{if(!Minecraft.getInstance().player.getMainHandItem().isEmpty())save("hand-"+id);});}
        steps.add(()->hold(ItemStack.EMPTY));
        // ladders, multiblocks
        steps.add(()->view(3.5,Y+1.6,6.5,180,15));wait(15);shot("ladders");
        steps.add(()->view(18,Y+3.5,13,180,25));wait(15);shot("multiblocks");
        // failed fabricator: remove one glass block, then try to form it like a player
        steps.add(()->server(p->{var level=p.serverLevel();var pos=new BlockPos(16,Y,4);var m=(techguns.machines.ProcessingMachine)level.getBlockEntity(pos);
            var glass=m.structureParts(Direction.SOUTH).entrySet().stream().filter(java.util.Map.Entry::getValue).findFirst().orElseThrow().getKey();level.setBlockAndUpdate(glass,Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(m.structureParts(Direction.SOUTH).entrySet().stream().filter(e->!e.getValue()).findFirst().orElseThrow().getKey(),Blocks.STONE.defaultBlockState());
            p.teleportTo(level,16.5,Y,8.5,180,30);
            level.getBlockState(pos).use(level,p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.SOUTH,pos,false));}));
        wait(8);steps.add(()->Minecraft.getInstance().options.hideGui=false);wait(2);shot("multiblock-fail");
        // doors and their break particles
        steps.add(()->server(p->{var level=p.serverLevel();int x=0;for(var id:new String[]{"metal_door_3x3","nether_door_3x3","hangar_door_up"}){var pos=new BlockPos(x,Y,14);var stack=item(id);
            p.setItemInHand(InteractionHand.MAIN_HAND,stack);stack.useOn(new net.minecraft.world.item.context.UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos.below()),Direction.UP,pos.below(),false)));x+=5;}
            p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.teleportTo(level,6.5,Y+1.6,22.5,180,5);}));
        wait(15);shot("doors");
        steps.add(()->{var mc=Minecraft.getInstance();for(int x:new int[]{1,6,11})for(int h=0;h<3;h++){var pos=new BlockPos(x,Y+h,14);var state=mc.level.getBlockState(pos);if(!state.isAir())mc.particleEngine.destroy(pos,state);}});
        wait(2);shot("door-particles");
        // lock-on reticle on a zombie
        steps.add(()->server(p->{var level=p.serverLevel();var zombie=net.minecraft.world.entity.EntityType.ZOMBIE.create(level);zombie.moveTo(10.5,Y,4.5,0,0);zombie.setNoAi(true);zombie.setPersistenceRequired();level.addFreshEntity(zombie);
            p.teleportTo(level,10.5,Y,16.5,180,0);p.setItemInHand(InteractionHand.MAIN_HAND,item("guidedmissilelauncher"));}));
        wait(20);
        steps.add(()->{var mc=Minecraft.getInstance();var zombies=mc.level.getEntitiesOfClass(net.minecraft.world.entity.monster.Zombie.class,mc.player.getBoundingBox().inflate(30));
            if(!zombies.isEmpty()){var state=techguns.capabilities.TGExtendedPlayer.get(mc.player);state.lockOnEntity=zombies.get(0);state.lockOnTicks=8;}});
        wait(1);shot("lockon-locking");
        steps.add(()->{var state=techguns.capabilities.TGExtendedPlayer.get(Minecraft.getInstance().player);if(state.lockOnEntity!=null)state.lockOnTicks=100;});
        wait(1);shot("lockon-locked");
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!enabled()||stage==0)return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        if(++ticks>8000)throw new IllegalStateException("Comparison timed out at step "+step);
        if(stage==1){stage=2;ticks=0;server(p->{p.getAbilities().flying=true;p.onUpdateAbilities();build(p.serverLevel());p.teleportTo(p.serverLevel(),10.5,Y,20.5,180,0);});plan();return;}
        if(stage==2&&ticks>100){stage=3;ticks=0;}
        if(stage==3){
            // keep lock-on state while a lock-on frame is pending (the launcher logic clears it when not used)
            if(step<steps.size()){steps.get(step++).run();return;}
            Techguns.LOGGER.info("TECHGUNS_COMPARE_VISUALS_PASSED: {} frames",saved.size());stage=4;mc.stop();
        }
    }
    private static void save(String name){
        var mc=Minecraft.getInstance();
        try{var out=mc.gameDirectory.toPath().resolve("verification");Files.createDirectories(out);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve("compare-"+name+".png"));}
            saved.add(name);Techguns.LOGGER.info("TECHGUNS_COMPARE_FRAME: {}",name);
        }catch(Exception error){throw new IllegalStateException("Could not save "+name,error);}
    }
    private CompareVisualVerifier(){}
}
