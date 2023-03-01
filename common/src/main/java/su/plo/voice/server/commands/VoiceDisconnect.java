package su.plo.voice.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.network.ServerNetworkHandler;

public class VoiceDisconnect {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vdisconnect")
                .requires(source ->
                        source.hasPermission(3)
                )
                        .then(Commands.argument("target", EntityArgument.players()).executes(ctx -> {

                            var players = EntityArgument.getPlayers(ctx,"target");
                            players.forEach(player -> {
                                ServerNetworkHandler.disconnectClient(player.getUUID());
                                ctx.getSource().sendSuccess(Component.literal(
                                        "Disconnect " +player.getName()
                                ), false);
                            });

                            return 1;
                        })));
    }

}
