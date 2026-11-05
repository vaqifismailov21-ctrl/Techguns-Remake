package techguns.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;
import techguns.damagesystem.ThermonuclearBlast;

/**
 * /tgnuke [radius] [position] - thermonuclear blast of any size up to 1000 blocks crater radius (cheats required).
 * /tgnuke stop - stops the craters still being dug.
 */
@Mod.EventBusSubscriber(modid=Techguns.MODID)
public final class NukeCommand {
    private NukeCommand(){}
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event){register(event.getDispatcher());}
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("tgnuke").requires(s->s.hasPermission(2))
            .executes(c->nuke(c,ThermonuclearBlast.CRATER,c.getSource().getPosition()))
            .then(Commands.literal("stop").executes(c->{int n=ThermonuclearBlast.stopAll();c.getSource().sendSuccess(()->Component.translatable("techguns.command.tgnuke.stopped",n),true);return n;}))
            .then(Commands.argument("radius",IntegerArgumentType.integer(1,ThermonuclearBlast.MAX_CRATER))
                .executes(c->nuke(c,IntegerArgumentType.getInteger(c,"radius"),c.getSource().getPosition()))
                .then(Commands.argument("position",Vec3Argument.vec3())
                    .executes(c->nuke(c,IntegerArgumentType.getInteger(c,"radius"),Vec3Argument.getVec3(c,"position"))))));
    }
    private static int nuke(CommandContext<CommandSourceStack> context,int radius,Vec3 at){
        var source=context.getSource();var level=source.getLevel();
        var damage=level.damageSources().explosion(null,source.getEntity());
        ThermonuclearBlast.detonate(level,at,damage,null,radius,1e30,true);
        int kill=ThermonuclearBlast.killRadius(radius),blast=ThermonuclearBlast.blastRadius(radius);
        source.sendSuccess(()->Component.translatable("techguns.command.tgnuke.done",radius,kill,blast),true);
        return radius;
    }
}
