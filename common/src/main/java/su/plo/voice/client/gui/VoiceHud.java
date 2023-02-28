package su.plo.voice.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import su.plo.voice.client.Icons;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.MicrophoneIconPosition;

public class VoiceHud {
    private final Minecraft client = Minecraft.getInstance();

    public void render() {
        if (!VoiceClient.isConnected()) return;

        final Player player = client.player;
        if (player == null) return;

        if (VoiceClient.socketUDP.isTimedOut()) {
            renderConnectionError();
            return;
        }

        if (VoiceClient.getClientConfig().speakerMuted.get()) {
            renderSpeakerMuted();
        } else if (VoiceClient.getClientConfig().microphoneMuted.get()
                || !VoiceClient.recorder.isAvailable()
                || VoiceClient.getServerConfig().getMuted().containsKey(player.getUUID())
        ) {
            renderMicrophoneMuted();
        } else if (VoiceClient.isSpeaking()) {
            if (ClientConfig.isPrincipal()) {
                renderPrioritySpeaking();
            } else {
                renderSpeaking();
            }
        }
    }

    private void renderConnectionError() {
        renderIcon(Icons.DISCONECTED);
    }

    private void renderSpeakerMuted() {
        renderIcon(Icons.SPEAKER_OFF);
    }

    private void renderMicrophoneMuted() {
        renderIcon(Icons.MICROPHONE_OFF);
    }

    private void renderSpeaking() {
        renderIcon(Icons.MICROPHONE);
    }

    private void renderPrioritySpeaking() {
        renderIcon(Icons.MICROPHONE_GOLD);
    }

    private void renderIcon(ResourceLocation location) {
        final Gui inGameHud = client.gui;
        final PoseStack matrixStack = new PoseStack();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, location);

        int p = 40;

        inGameHud.blit(
                matrixStack,
                MicrophoneIconPosition.BOTTOM_LEFT.getX(client),
                MicrophoneIconPosition.BOTTOM_LEFT.getY(client),
                0,0, p, p, p, p);

/*
        inGameHud.blit(
                matrixStack,
                VoiceClient.getClientConfig().micIconPosition.get().getX(client),
                VoiceClient.getClientConfig().micIconPosition.get().getY(client),
                x,
                z,
                16,
                16
        );*/
    }
}
