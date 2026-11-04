package techguns.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import techguns.TGFluids;
import techguns.TGItems;
import techguns.Techguns;
import techguns.power.*;

/**
 * -Dtechguns.verifyPower=true: builds every power block in a fresh flat world, waits until they run, then saves game
 * frames (day, night, close-ups), the generator windows and the hotbar icons to verification/power-*.png and quits.
 */
@Mod.EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
public final class PowerVisualVerifier {
    private record Shot(String name,double x,double y,double z,float yaw,float pitch,long time,boolean gui){}
    private static final int Y=100;
    private static int stage,ticks,shot,frames,menu;
    private static final List<Shot> shots=new ArrayList<>();
    private static final List<String> saved=new ArrayList<>();
    private static BlockPos nuclear,fuel,liquid;
    private static String expected="";

    public static boolean enabled(){return Boolean.getBoolean("techguns.verifyPower");}
    public static void begin(){
        if(stage!=0)return;stage=1;var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;mc.options.guiScale().set(2);mc.options.renderDistance().set(6);
        var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);
        mc.createWorldOpenFlows().createFreshLevel("techguns-power-qa-"+System.currentTimeMillis(),new LevelSettings("Techguns power block verification",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(1729L,false,false),access->access.registryOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).createWorldDimensions());
    }
    private static BlockPos at(int x,int z){return new BlockPos(x,Y,z);}
    private static void build(net.minecraft.server.level.ServerLevel level,ServerPlayer player){
        level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);
        for(int x=-4;x<=26;x++)for(int z=0;z<=24;z++){level.setBlockAndUpdate(new BlockPos(x,Y-2,z),Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(new BlockPos(x,Y-1,z),Blocks.SMOOTH_STONE.defaultBlockState());}
        var south=Direction.SOUTH;
        fuel=at(2,10);level.setBlockAndUpdate(fuel,PowerBlocks.FUEL_GENERATOR.get().defaultBlockState().setValue(GeneratorBlock.FACING,south));
        ((FuelGeneratorEntity)level.getBlockEntity(fuel)).fuel.setStackInSlot(0,new ItemStack(Items.COAL_BLOCK,8));
        liquid=at(4,10);level.setBlockAndUpdate(liquid,PowerBlocks.LIQUID_GENERATOR.get().defaultBlockState().setValue(GeneratorBlock.FACING,south));
        ((LiquidGeneratorEntity)level.getBlockEntity(liquid)).tank.fill(new FluidStack(TGFluids.OIL.get(),5500),IFluidHandler.FluidAction.EXECUTE);
        level.setBlockAndUpdate(at(6,10).below(),Blocks.LAVA.defaultBlockState());
        level.setBlockAndUpdate(at(6,10),PowerBlocks.GEOTHERMAL_GENERATOR.get().defaultBlockState().setValue(FacingMachineBlock.FACING,south));
        level.setBlockAndUpdate(at(8,10).below(),Blocks.WATER.defaultBlockState());
        nuclear=at(8,10);level.setBlockAndUpdate(nuclear,PowerBlocks.NUCLEAR_GENERATOR.get().defaultBlockState().setValue(FacingMachineBlock.FACING,south));
        ((NuclearGeneratorEntity)level.getBlockEntity(nuclear)).slots.setStackInSlot(0,new ItemStack(TGItems.NUCLEAR_POWERCELL.get(),4));
        int x=10;for(var panel:List.of(PowerBlocks.SOLAR_PANEL,PowerBlocks.SOLAR_PANEL_ADVANCED,PowerBlocks.SOLAR_PANEL_INDUSTRIAL)){level.setBlockAndUpdate(at(x,10),panel.get().defaultBlockState().setValue(SolarPanelBlock.FACING,south));x+=2;}
        level.setBlockAndUpdate(at(16,10),PowerBlocks.ENERGY_CELL.get().defaultBlockState().setValue(EnergyCellBlock.FACING,south));
        level.setBlockAndUpdate(at(18,10),PowerBlocks.ENERGY_BANK.get().defaultBlockState().setValue(EnergyCellBlock.FACING,south));
        var bank=level.getBlockEntity(at(18,10)).getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY,Direction.UP).resolve().orElseThrow();for(int i=0;i<1000;i++)bank.receiveEnergy(PowerBlocks.BANK_TRANSFER,false);
        // cable behind the generators into the back of the cell
        for(int cx=2;cx<=16;cx++)level.setBlockAndUpdate(at(cx,9),PowerBlocks.ENERGY_CABLE.get().defaultBlockState());
        level.setBlockAndUpdate(at(16,10).above(),PowerBlocks.ENERGY_CABLE.get().defaultBlockState());
        level.setBlockAndUpdate(at(20,14),PowerBlocks.FLOODLIGHT.get().defaultBlockState().setValue(FacingMachineBlock.FACING,Direction.WEST));
        level.setBlockAndUpdate(at(21,14),PowerBlocks.ENERGY_CELL.get().defaultBlockState().setValue(EnergyCellBlock.FACING,Direction.WEST));
        var cell=level.getBlockEntity(at(21,14)).getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY,Direction.UP).resolve().orElseThrow();for(int i=0;i<1000;i++)cell.receiveEnergy(PowerBlocks.CELL_TRANSFER,false);
        for(int y=0;y<5;y++)level.setBlockAndUpdate(at(12,16).above(y),PowerBlocks.WIND_TURBINE.get().defaultBlockState().setValue(WindTurbineBlock.FACING,south));
        player.getInventory().clearContent();var items=PowerBlocks.tabItems();for(int i=0;i<9&&i<items.size();i++)player.getInventory().setItem(i,new ItemStack(items.get(i).get()));
        player.getInventory().setItem(9,new ItemStack(items.get(9).get()));
    }
    private static void plan(){
        shots.add(new Shot("day-overview",10.5,Y+5.5,24.5,180,22,6000,false));
        shots.add(new Shot("night-overview",10.5,Y+5.5,24.5,180,22,18000,false));
        String[] names={"fuel_generator","liquid_generator","geothermal_generator","nuclear_generator","solar_panel","solar_panel_advanced","solar_panel_industrial","energy_cell","energy_bank"};
        for(int i=0;i<names.length;i++){double bx=2+i*2+.5;shots.add(new Shot("close-"+names[i],bx-1.6,Y+1.7,13.4,209,22,6000,false));}
        shots.add(new Shot("close-wind_turbine",14.5,Y+3.5,22.5,165,8,6000,false));
        shots.add(new Shot("close-floodlight-night",17.5,Y+2.2,16.5,240,20,18000,false));
        shots.add(new Shot("hotbar",10.5,Y+1.6,20.5,180,10,6000,true));
    }
    private static int titleFrames;
    @SubscribeEvent public static void title(ScreenEvent.Render.Post event){if(enabled()&&event.getScreen() instanceof net.minecraft.client.gui.screens.TitleScreen&&++titleFrames==3)begin();}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!enabled()||stage==0)return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        if(++ticks>6000)throw new IllegalStateException("Power verification timed out at stage "+stage);
        var server=mc.getSingleplayerServer();
        if(stage==1){stage=2;server.execute(()->{var player=server.getPlayerList().getPlayers().get(0);player.getAbilities().flying=true;player.onUpdateAbilities();build(server.overworld(),player);});plan();ticks=0;return;}
        if(stage==2&&ticks>140){stage=3;ticks=0;check(server);}
        if(stage==3&&ticks>5){
            if(shot>=shots.size()){stage=5;ticks=0;openMenu(server,nuclear);return;}
            var s=shots.get(shot);mc.options.hideGui=!s.gui;
            server.execute(()->{var player=server.getPlayerList().getPlayers().get(0);server.overworld().setDayTime(s.time);player.teleportTo(server.overworld(),s.x,s.y,s.z,s.yaw,s.pitch);player.getInventory().selected=0;});
            stage=4;ticks=0;frames=0;return;
        }
        if(stage==4&&ticks>30){
            if(mc.player.getYRot()!=shots.get(shot).yaw){mc.player.setYRot(shots.get(shot).yaw);mc.player.setXRot(shots.get(shot).pitch);}
            save(mc,"power-"+shots.get(shot).name);shot++;stage=3;ticks=0;
        }
        if(stage==7&&ticks>10){
            mc.options.hideGui=false;
            Techguns.LOGGER.info("TECHGUNS_POWER_VISUALS_PASSED: {} frames: {}",saved.size(),saved);
            mc.stop();
        }
    }
    /** Server-side state after the blocks have run for a while. */
    private static void check(net.minecraft.server.MinecraftServer server){
        server.execute(()->{
            var level=server.overworld();var lines=new ArrayList<String>();
            for(var pos:List.of(fuel,liquid,at(6,10),nuclear)){var state=level.getBlockState(pos);lines.add(state.getBlock().getDescriptionId()+" lit="+state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)+" energy="+((EnergySource)level.getBlockEntity(pos)).energyStored());}
            var base=(WindTurbineEntity)level.getBlockEntity(at(12,16));lines.add("wind rate="+base.rate+" masts="+base.masts+" problem="+base.problem);
            lines.add("cell charge="+level.getBlockState(at(16,10)).getValue(EnergyCellBlock.CHARGE)+" energy="+((EnergySource)level.getBlockEntity(at(16,10))).energyStored());
            lines.add("floodlight lit="+level.getBlockState(at(20,14)).getValue(FacingMachineBlock.LIT)+" beam="+level.getBlockState(at(18,14)).is(PowerBlocks.FLOODLIGHT_LIGHT.get()));
            Techguns.LOGGER.info("TECHGUNS_POWER_STATE: {}",lines);
        });
    }
    private static void openMenu(net.minecraft.server.MinecraftServer server,BlockPos pos){
        server.execute(()->{var player=server.getPlayerList().getPlayers().get(0);player.closeContainer();player.teleportTo(server.overworld(),pos.getX()+.5,pos.getY(),pos.getZ()+2.5,180,30);
            NetworkHooks.openScreen(player,new GeneratorMenu.Provider((EnergySource)server.overworld().getBlockEntity(pos)),buf->buf.writeBlockPos(pos));});
        expected=Minecraft.getInstance().level.getBlockState(pos).getBlock().getName().getString();
        stage=6;frames=0;
    }
    @SubscribeEvent public static void screen(ScreenEvent.Render.Post event){
        if(!enabled()||stage!=6||!event.getScreen().getTitle().getString().equals(expected)||++frames<8)return;
        var mc=Minecraft.getInstance();mc.options.hideGui=false;event.getGuiGraphics().flush();
        save(mc,"power-gui-"+List.of("nuclear","fuel","liquid").get(menu));menu++;mc.player.closeContainer();
        var server=mc.getSingleplayerServer();
        if(menu<3){openMenu(server,menu==1?fuel:liquid);}
        else{stage=7;ticks=0;}
    }
    private static void save(Minecraft mc,String name){
        try{
            Path out=mc.gameDirectory.toPath().resolve("verification");Files.createDirectories(out);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(name+".png"));}
            saved.add(name);Techguns.LOGGER.info("TECHGUNS_POWER_FRAME: {}",name);
        }catch(Exception error){throw new IllegalStateException("Could not save "+name,error);}
    }
    private PowerVisualVerifier(){}
}
