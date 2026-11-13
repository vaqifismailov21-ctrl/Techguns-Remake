package techguns.client;
import java.util.*;
import java.nio.file.*;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Matrix4f;
import techguns.*;
import techguns.machines.*;
/** Opt-in, isolated flat QA world. Uses normal server menus and records only this test client's framebuffer. */
@Mod.EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
public final class WorldVisualVerifier {
    private record Fixture(String name,BlockPos pos){}
    private static final List<Fixture> fixtures=new ArrayList<>();
    private static int stage,ticks,menuIndex,screenFrames;private static volatile boolean ready;private static boolean suitsDone;
    public static void begin(){
        if(stage!=0)return;stage=1;var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;mc.options.guiScale().set(2);mc.options.renderDistance().set(4);mc.resizeDisplay();var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);rules.getRule(GameRules.RULE_RANDOMTICKING).set(0,null);
        mc.createWorldOpenFlows().createFreshLevel("techguns-qa-"+System.currentTimeMillis(),new LevelSettings("Techguns isolated interface verification",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(1729L,false,false),access->access.registryOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).createWorldDimensions());
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!Boolean.getBoolean("techguns.verifyWorldInterfaces")||stage==0)return;
        if(++ticks>5000)throw new IllegalStateException("World interface verification timed out at stage "+stage+", menu "+menuIndex);
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());
        if(stage==1){stage=2;mc.getSingleplayerServer().execute(()->{
            var server=mc.getSingleplayerServer();var level=server.overworld();var player=server.getPlayerList().getPlayers().get(0);level.setDayTime(6000);player.teleportTo(level,6.5,80,6.5,0,0);
            for(int x=0;x<15;x++)for(int z=0;z<15;z++)level.setBlockAndUpdate(new BlockPos(x,79,z),Blocks.STONE.defaultBlockState());
            for(var kind:MachineKind.values()){var pos=position(fixtures.size());level.setBlockAndUpdate(pos,TGMachines.BLOCKS.get(kind).get().defaultBlockState());var machine=(ProcessingMachine)level.getBlockEntity(pos);machine.access.place(player);machine.setEnergy(machine.energy.getMaxEnergyStored());
                for(var recipe:MachineRecipes.all())if(recipe.kind()==kind){for(int input=0;input<recipe.ingredients().length;input++){var options=recipe.ingredients()[input].getItems();if(options.length>0)machine.inventory.setStackInSlot(input,options[0].copyWithCount(Math.min(64,recipe.amounts()[input]*8)));}break;}
                if(kind==MachineKind.CHEM_LAB||kind==MachineKind.REACTION){machine.inputTank.fill(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,4000),net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);if(kind==MachineKind.CHEM_LAB)machine.outputTank.fill(new net.minecraftforge.fluids.FluidStack(TGFluids.ACID.get(),8000),net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);machine.changed();}
                fixtures.add(new Fixture(kind.name().toLowerCase(Locale.ROOT),pos));}
            for(var kind:BenchKind.values()){var pos=position(fixtures.size());level.setBlockAndUpdate(pos,TGBenches.BLOCKS.get(kind).get().defaultBlockState());var bench=(BenchEntity)level.getBlockEntity(pos);bench.access.place(player);if(kind==BenchKind.CAMO)bench.inventory.setStackInSlot(0,TGuns.m4.get().getDefaultInstance());else if(kind==BenchKind.UPGRADE){bench.inventory.setStackInSlot(0,TGEquipment.ARMOR.get("t1_scout_helmet").get().getDefaultInstance());bench.inventory.setStackInSlot(1,TGItems.UPGRADE_PROTECTION_1.get().getDefaultInstance());}fixtures.add(new Fixture("bench-"+kind.name().toLowerCase(Locale.ROOT),pos));}
            var doorPos=position(fixtures.size());TGDoors.BLOCKS.get(0).get().place(level,doorPos,false,player);fixtures.add(new Fixture("door-settings",doorPos.above()));
            for(boolean advanced:new boolean[]{false,true}){var pos=position(fixtures.size());level.setBlockAndUpdate(pos,(advanced?TGCharges.ADVANCED:TGCharges.TNT).get().defaultBlockState());((techguns.tileentities.MiningChargeEntity)level.getBlockEntity(pos)).access.place(player);fixtures.add(new Fixture(advanced?"advanced-charge":"tnt-charge",pos));}
            {var pos=position(fixtures.size());level.setBlockAndUpdate(pos,TGTurrets.BLOCK.get().defaultBlockState());var turret=(techguns.tileentities.TurretTileEnt)level.getBlockEntity(pos);turret.setOwner(player);turret.energy.receiveEnergy(3000,false);turret.inventory.setStackInSlot(techguns.tileentities.TurretTileEnt.SLOT_WEAPON,TGuns.m4.get().getDefaultInstance());turret.inventory.setStackInSlot(techguns.tileentities.TurretTileEnt.SLOT_ARMOR,new ItemStack(TGItems.TURRET_ARMOR_STEEL.get()));turret.inventory.setStackInSlot(0,TGuns.m4.get().getAmmoType().getAmmo(0)[0].copyWithCount(8));turret.attackAnimals=true;turret.spawnTurret(level,pos);turret.needUpdate();fixtures.add(new Fixture("turret",pos));}
            {var pos=position(fixtures.size());level.setBlockAndUpdate(pos.below(2),TGWorld.CLUSTERS.get(techguns.world.ClusterKind.COAL).get().defaultBlockState());level.setBlockAndUpdate(pos.below(),TGOreDrill.BLOCKS.get(techguns.oredrill.OreDrillType.ROD).get().defaultBlockState());level.setBlockAndUpdate(pos,TGOreDrill.CONTROLLER.get().defaultBlockState());
                var drill=(techguns.oredrill.OreDrillTileEntMaster)level.getBlockEntity(pos);drill.setOwner(player);techguns.oredrill.OreDrillDefinition.form(level,pos);drill.inventory.setStackInSlot(0,new ItemStack(TGItems.OREDRILLHEAD_STEEL.get()));drill.inventory.setStackInSlot(1,new ItemStack(net.minecraft.world.item.Items.COAL,16));
                drill.inputTank.fill(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.LAVA,6000),net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);drill.outputTank.fill(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,12000),net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);drill.tick();drill.needUpdate();fixtures.add(new Fixture("oredrill",pos));}
            {var master=new BlockPos(24,90,6);var down=net.minecraft.core.Direction.DOWN;var part=(java.util.function.Function<techguns.oredrill.OreDrillType,net.minecraft.world.level.block.state.BlockState>)t->TGOreDrill.BLOCKS.get(t).get().defaultBlockState();
                level.setBlockAndUpdate(master,part.apply(techguns.oredrill.OreDrillType.CONTROLLER));for(var p:techguns.oredrill.OreDrillDefinition.getEngineBlocks(master,down,1,2))level.setBlockAndUpdate(p,part.apply(techguns.oredrill.OreDrillType.ENGINE));for(int i=1;i<=3;i++)level.setBlockAndUpdate(master.relative(down,2+i),part.apply(techguns.oredrill.OreDrillType.ROD));
                for(var p:techguns.oredrill.OreDrillDefinition.getFrameBlocks(master,down,2,5,true,true))level.setBlockAndUpdate(p,part.apply(techguns.oredrill.OreDrillType.FRAME));for(var p:techguns.oredrill.OreDrillDefinition.getFrameBlocks(master,down,2,5,false,false))level.setBlockAndUpdate(p,part.apply(techguns.oredrill.OreDrillType.SCAFFOLD));
                level.setBlockAndUpdate(master.relative(down,6),TGWorld.CLUSTERS.get(techguns.world.ClusterKind.COMMON_METAL).get().defaultBlockState());if(!techguns.oredrill.OreDrillDefinition.checkForm(level,player,master))throw new IllegalStateException("QA 3x3 ore drill did not validate");techguns.oredrill.OreDrillDefinition.form(level,master);
                var large=(techguns.oredrill.OreDrillTileEntMaster)level.getBlockEntity(master);large.inventory.setStackInSlot(0,new ItemStack(TGItems.OREDRILLHEAD_MEDIUM_OBSIDIANSTEEL.get()));large.energy.receiveEnergy(500000,false);large.tick();large.needUpdate();}
            fixtures.add(new Fixture("player-inventory",null)); ready=true;
        });}
        else if(stage==2&&ready&&fixtures.stream().allMatch(f->f.pos==null||mc.level.getBlockEntity(f.pos)!=null)){stage=3;ticks=0;mc.setScreen(null);}
        else if(stage==3&&ticks>20){
            if(!suitsDone){if(!Boolean.getBoolean("techguns.skipScopeInput"))ClientScopeVerifier.verify(mc);renderSuits(mc);ArmorHudVisualVerifier.verify(mc);ProjectileFxVisualVerifier.verify(mc);NpcVisualVerifier.verify(mc);DeathEffectVisualVerifier.verify(mc);StructureVisualVerifier.verify(mc);MachineVisualVerifier.verify(mc);suitsDone=true;}
            if(menuIndex>=fixtures.size()){
                Techguns.LOGGER.info("TECHGUNS_WORLD_INTERFACES_PASSED: {} real server menus opened and rendered in an isolated flat QA world; language {}; original-mod visual comparison still pending",fixtures.size(),mc.options.languageCode);
                if(!languagesDone){languagesDone=true;stage=7;String next=mc.options.languageCode.equals("ru_ru")?"en_us":"ru_ru";mc.options.languageCode=next;mc.getLanguageManager().setSelected(next);mc.reloadResourcePacks().thenRun(()->mc.tell(()->{menuIndex=0;ticks=20;stage=3;}));return;}
                Runnable complete=()->{if(!ManualVisualVerifier.begin(mc,mc::stop))mc.stop();};
                if(Boolean.getBoolean("techguns.verifyJei")&&JeiVisualVerifier.begin(mc,complete)){stage=8;return;}
                if(ManualVisualVerifier.begin(mc,mc::stop)){stage=8;return;}
                stage=6;mc.stop();return;
            }
            stage=4;screenFrames=0;var fixture=fixtures.get(menuIndex);mc.getSingleplayerServer().execute(()->{var player=mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);if(fixture.pos==null){for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}){String suffix=switch(slot){case HEAD->"helmet";case CHEST->"chestplate";case LEGS->"leggings";default->"boots";};player.setItemSlot(slot,TGEquipment.ARMOR.get("steam_"+suffix).get().getDefaultInstance());}NetworkHooks.openScreen(player,new SimpleMenuProvider((id,inv,p)->new techguns.gui.player.TGPlayerInventoryContainer(id,inv),net.minecraft.network.chat.Component.translatable("techguns.extendedinventory")));return;}var entity=player.serverLevel().getBlockEntity(fixture.pos);if(!(entity instanceof net.minecraft.world.MenuProvider provider))throw new IllegalStateException("Missing fixture menu "+fixture.name);NetworkHooks.openScreen(player,provider,fixture.pos);});
        }
        else if(stage==5&&ticks>10){stage=3;ticks=20;}
    }
    private static BlockPos position(int index){return new BlockPos(2+(index%4)*3,80,2+(index/4)*3);}
    @SubscribeEvent(priority=EventPriority.LOW) public static void screen(ScreenEvent.Render.Post event){
        if(stage!=4||!(event.getScreen() instanceof AbstractContainerScreen)||++screenFrames<3)return;
        var mc=Minecraft.getInstance();var fixture=fixtures.get(menuIndex);try{Files.createDirectories(output(mc));event.getGuiGraphics().flush();try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(output(mc).resolve("interface-"+fixture.name+".png"));image.writeToFile(output(mc).resolve("interface-"+mc.options.languageCode+"-"+fixture.name+".png"));}}catch(Exception error){throw new IllegalStateException("Interface GPU test failed: "+fixture.name,error);}
        Techguns.LOGGER.info("TECHGUNS_INTERFACE_RENDERED: {}",fixture.name);menuIndex++;stage=5;ticks=0;mc.player.closeContainer();
    }
    private static boolean languagesDone;
    private static Path output(Minecraft mc){return mc.gameDirectory.toPath().resolve("verification");}
    private static void renderSuits(Minecraft mc){
        var target=new TextureTarget(640,540,true,Minecraft.ON_OSX);var view=RenderSystem.getModelViewStack();RenderSystem.backupProjectionMatrix();view.pushPose();view.setIdentity();RenderSystem.applyModelViewMatrix();var previous=new EnumMap<EquipmentSlot,ItemStack>(EquipmentSlot.class);for(var slot:EquipmentSlot.values()){previous.put(slot,mc.player.getItemBySlot(slot).copy());mc.player.setItemSlot(slot,ItemStack.EMPTY);}var data=techguns.capabilities.TGExtendedPlayer.get(mc.player);var extra=new ItemStack[3];boolean gliding=data.isGliding;for(int i=0;i<3;i++){extra[i]=data.tg_inventory.getItem(i).copy();data.tg_inventory.setItem(i,ItemStack.EMPTY);}
        try{Files.createDirectories(output(mc));var suits=new LinkedHashMap<String,EnumMap<EquipmentSlot,ItemStack>>();for(var entry:TGEquipment.ARMOR.entrySet()){var key=entry.getKey().replaceAll("_(helmet|chestplate|leggings|boots)$","");suits.computeIfAbsent(key,k->new EnumMap<>(EquipmentSlot.class)).put(entry.getValue().get().slot,entry.getValue().get().getDefaultInstance());}
            for(var entry:suits.entrySet()){for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})mc.player.setItemSlot(slot,entry.getValue().getOrDefault(slot,ItemStack.EMPTY));target.setClearColor(.12f,.15f,.18f,1);target.clear(Minecraft.ON_OSX);target.bindWrite(true);RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,640,540,0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());g.pose().translate(0,0,-11000);InventoryScreen.renderEntityInInventoryFollowsMouse(g,320,465,160,-35,-15,mc.player);g.flush();try(var pixels=Screenshot.takeScreenshot(target)){pixels.writeToFile(output(mc).resolve("armor-"+entry.getKey()+".png"));}}
            Techguns.LOGGER.info("TECHGUNS_WORN_ARMOR_RENDER_PASSED: {} original suit groups rendered on the actual client player",suits.size());
            for(var slot:EquipmentSlot.values())mc.player.setItemSlot(slot,ItemStack.EMPTY);for(var registered:TGAccessories.ITEMS.values()){var item=registered.get();var stack=item.getDefaultInstance();int slot=item.getSlot(stack)==techguns.api.tginventory.TGSlotType.FACESLOT?0:1;data.tg_inventory.setItem(slot,stack);data.isGliding=item.kind==techguns.items.armors.AccessoryItem.Kind.GLIDER;playerFrame(mc,target,"accessory-"+net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item).getPath());data.tg_inventory.setItem(slot,ItemStack.EMPTY);}Techguns.LOGGER.info("TECHGUNS_WORN_ACCESSORY_RENDER_PASSED: eight original face/back devices rendered on the actual client player");
        }catch(Exception error){throw new IllegalStateException("Worn armor GPU test failed",error);}finally{for(var entry:previous.entrySet())mc.player.setItemSlot(entry.getKey(),entry.getValue());for(int i=0;i<3;i++)data.tg_inventory.setItem(i,extra[i]);data.isGliding=gliding;target.destroyBuffers();mc.getMainRenderTarget().bindWrite(true);view.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.restoreProjectionMatrix();RenderSystem.disableDepthTest();}
    }
    private static void playerFrame(Minecraft mc,TextureTarget target,String name)throws java.io.IOException{target.setClearColor(.12f,.15f,.18f,1);target.clear(Minecraft.ON_OSX);target.bindWrite(true);RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,640,540,0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());g.pose().translate(0,0,-11000);InventoryScreen.renderEntityInInventoryFollowsMouse(g,320,465,160,-200,-15,mc.player);g.flush();try(var pixels=Screenshot.takeScreenshot(target)){pixels.writeToFile(output(mc).resolve(name+".png"));}}
    private WorldVisualVerifier(){}
}
