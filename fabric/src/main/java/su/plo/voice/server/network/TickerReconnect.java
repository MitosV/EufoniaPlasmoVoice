package su.plo.voice.server.network;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.common.packets.tcp.ServerConnectPacket;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.socket.SocketServerUDP;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TickerReconnect {

    private int tickControl;


    public TickerReconnect(){
        tickControl = 0;
        ServerTickEvents.END_SERVER_TICK.register(this::reconnectTicker);
    }


    public void reconnectTicker(MinecraftServer server){
        if (tickControl < 101){
            tickControl++;
            return;
        }


        for (ServerPlayer player : server.getPlayerList().getPlayers()){
            if (ServerNetworkHandler.sendConnectPacket.contains(player.getUUID()))
                continue;
            reconnect(player);
        }


        tickControl = 0;
    }


    public static void reconnect(ServerPlayer player){


        ServerNetworkHandler.execute(()->{
            UUID token = UUID.randomUUID();
            ServerNetworkHandler.playerToken.put(player.getUUID(), token);
            ServerNetworkHandler.sendConnectPacket.add(player.getUUID());
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
                System.out.println("Trying to reconnect "+player.getName().getString());
            } catch (IOException e) {
                ServerNetworkHandler.sendConnectPacket.remove(player.getUUID());
            }
        });
    }


}
