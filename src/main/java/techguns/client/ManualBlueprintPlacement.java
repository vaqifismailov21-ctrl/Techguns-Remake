package techguns.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;

/**
 * Manual blueprints (Patchouli "Visualize") can also be set down by right-clicking into the air:
 * the blueprint is anchored on the ground a few blocks in front of the player, facing the way the player looks.
 */
@Mod.EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
public final class ManualBlueprintPlacement {
    private ManualBlueprintPlacement(){}
    private static boolean resolved;private static Field has;private static Method anchored,anchor,rotation;
    private static boolean resolve(){
        if(resolved)return has!=null;resolved=true;
        if(!ModList.get().isLoaded("patchouli"))return false;
        try{
            var handler=Class.forName("vazkii.patchouli.client.handler.MultiblockVisualizationHandler");
            has=handler.getField("hasMultiblock");anchored=handler.getMethod("isAnchored");anchor=handler.getMethod("anchorTo",BlockPos.class,Rotation.class);
            rotation=Class.forName("vazkii.patchouli.common.util.RotationUtil").getMethod("rotationFromFacing",Direction.class);
            return true;
        }catch(ReflectiveOperationException|LinkageError e){has=null;Techguns.LOGGER.warn("Patchouli blueprint placement unavailable: {}",e.toString());return false;}
    }
    @SubscribeEvent
    public static void use(InputEvent.InteractionKeyMappingTriggered event){
        var mc=Minecraft.getInstance();var player=mc.player;
        if(!event.isUseItem()||player==null||mc.level==null||!resolve())return;
        if(mc.hitResult!=null&&mc.hitResult.getType()!=HitResult.Type.MISS)return; // blocks are handled by Patchouli itself
        try{
            if(!has.getBoolean(null)||(boolean)anchored.invoke(null))return;
            var facing=player.getDirection();
            var front=BlockPos.containing(player.getX(),player.getY(),player.getZ()).relative(facing,3);
            // the block the player would have clicked: the ground under that spot
            BlockPos ground=front.below();
            for(int i=0;i<6&&mc.level.getBlockState(ground).isAir();i++)ground=ground.below();
            if(mc.level.getBlockState(ground).isAir())ground=front.below();
            anchor.invoke(null,ground,rotation.invoke(null,facing));
            event.setCanceled(true);event.setSwingHand(false);
        }catch(ReflectiveOperationException e){Techguns.LOGGER.warn("Patchouli blueprint placement failed: {}",e.toString());}
    }
}
