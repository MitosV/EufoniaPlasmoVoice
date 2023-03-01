package su.plo.voice.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.server.socket.SocketServerUDP;

public class VoiceShowConnections {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vrc")
                .requires(source ->
                        CommandManager.requiresPermission(source, "voice.showconnections")
                )
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(Component.literal(
                            "showconnections"
                    ), false);
                    VoiceShowConnections.showConnections(ctx.getSource());
                    return 1;
                }));
    }

    private static void showConnections(CommandSourceStack source) {
        String connectionsList = "Couldn't find any disconnected players from VoiceChat Server.";
        for (ServerPlayer serverPlayer : source.getLevel().players()) {
            if (SocketServerUDP.clients.containsKey(serverPlayer.getUUID())) {
                continue;
            }

            connectionsList = VoiceShowConnections.appendConnectionName(connectionsList, serverPlayer.getName().getString());
        }

        source.sendSuccess(Component.literal(connectionsList), true);
    }

    private static String appendConnectionName(String originalString, String connectionName) {
        if(!originalString.startsWith("[")){
            originalString = "";
        }

        originalString = originalString + "[" + connectionName + "] ";
        return originalString;
    }
}
