package su.plo.voice.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.common.packets.tcp.ServerConnectPacket;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.network.ServerNetworkHandler;

import java.io.IOException;
import java.util.UUID;

public class VoiceConnect {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vconnect")
                .requires(source ->
                        source.hasPermission(3)
                )
                .then(Commands.argument("target", EntityArgument.players()).executes(ctx -> {
                    var players = EntityArgument.getPlayers(ctx,"target");
                    players.forEach(player -> {
                        connect(player, ctx);
                    });
                    return 1;
                })));
    }


    public static void connect(ServerPlayer player, CommandContext<CommandSourceStack> ctx){
        ServerNetworkHandler.sendConnectPacket.forEach(playerUUID -> {
            var player1 = ctx.getSource().getServer().getPlayerList().getPlayer(playerUUID);
            assert player1 != null;
            ctx.getSource().sendSuccess(Component.literal(player1.getName().getString()), false);
        } );
        ServerNetworkHandler.execute(()->{
            UUID token = UUID.randomUUID();
            ServerNetworkHandler.playerToken.put(player.getUUID(), token);
            try {
                ServerNetworkHandler.sendTo(new ServerConnectPacket(token.toString(),
                                VoiceServer.getServerConfig().getProxyIp() != null && !VoiceServer.getServerConfig().getProxyIp().isEmpty()
                                        ? VoiceServer.getServerConfig().getProxyIp()
                                        : VoiceServer.getServerConfig().getIp(),
                                VoiceServer.getServerConfig().getProxyPort() != 0
                                        ? VoiceServer.getServerConfig().getProxyPort()
                                        : VoiceServer.getServerConfig().getPort(),
                                VoiceServer.getPlayerManager().hasPermission(player.getUUID(), "voice.priority")),
                        player);
                ctx.getSource().sendSuccess(Component.literal("Trying to connect "+player.getName().getString())
                ,false);
            } catch (IOException e) {
                ctx.getSource().sendFailure(Component.literal("Cant connect "+player.getName().getString()));
            }
        });
    }


}
