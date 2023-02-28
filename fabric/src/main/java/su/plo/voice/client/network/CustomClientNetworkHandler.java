package su.plo.voice.client.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.ClientConfig;

public class CustomClientNetworkHandler {


    enum CustomPackets{
        VOLUME,
        VOICE,
        BLOCK_VOLUME,
        BLOCK_VOICE,
        SPEAKER,
        ALL,
        NOTHING;


        static CustomPackets valueOf(int i){
            CustomPackets value = null;
            switch (i){
                case 1 -> value = VOLUME;
                case 2 -> value = VOICE;
                case 3 -> value = BLOCK_VOLUME;
                case 4 -> value = BLOCK_VOICE;
                case 5 -> value = SPEAKER;
                case 6 -> value = ALL;
                default -> value = NOTHING;
            }
            return value;
        }
    }



    public static void receive(Minecraft client, ClientPacketListener handler,
                               FriendlyByteBuf buf, PacketSender responseSender){

        int i = buf.readInt();

        CustomPackets packet = CustomPackets.valueOf(i);

        switch (packet){
            case VOLUME -> volume(buf);
            case VOICE -> voice(buf);
            case BLOCK_VOLUME -> blockVolume(buf);
            case BLOCK_VOICE -> blockVoice(buf);
            case SPEAKER -> speaker(buf);
            case ALL -> all(buf);
        }

    }


    private static void volume(FriendlyByteBuf buf){
        ClientConfig config = VoiceClient.getClientConfig();
        var volume = buf.readInt();
        double finalVolume = (volume / 200D) * 2.0D;
        config.voiceVolume.set(finalVolume);
        config.save();
    }

    private static void voice(FriendlyByteBuf buf){
        ClientConfig config = VoiceClient.getClientConfig();
        boolean active = buf.readBoolean();
        config.speakerMuted.set(!active);
        config.save();
    }

    private static void blockVolume(FriendlyByteBuf buf){
        ClientConfig.setBlockVolume(buf.readBoolean());
    }

    private static void blockVoice(FriendlyByteBuf buf){
        ClientConfig.setBlockVoice(buf.readBoolean());
    }

    private static void speaker(FriendlyByteBuf buf){
        ClientConfig.setPrincipal(buf.readBoolean());
    }


    private static void all(FriendlyByteBuf buf){
        boolean blockVolume = buf.readBoolean();
        boolean blockVoice = buf.readBoolean();
        boolean speaker = buf.readBoolean();

        ClientConfig.setBlockVolume(blockVolume);
        ClientConfig.setBlockVoice(blockVoice);
        ClientConfig.setPrincipal(speaker);
    }


}
