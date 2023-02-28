package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import su.plo.voice.client.Icons;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.PlayerVolumeHandler;
import su.plo.voice.client.socket.SocketClientUDPQueue;
import su.plo.voice.common.entities.MutedEntity;

public class EntityIconRenderer {
    private final Minecraft client = Minecraft.getInstance();

    @Getter
    private static final EntityIconRenderer instance = new EntityIconRenderer();

    private final Minecraft minecraft;
    private EntityIconRenderer() {
        minecraft = Minecraft.getInstance();
    }

    public void entityRender(Player player, double distance, PoseStack matrices, boolean hasLabel, MultiBufferSource vertexConsumers, int light) {
        //if (isIconHidden(player)) return;

        if (VoiceClient.getServerConfig().getClients().contains(player.getUUID())) {
            if (VoiceClient.getClientConfig().isMuted(player.getUUID())) {
                //renderIcon(80, 0, player, distance, matrices, hasLabel, vertexConsumers, light);
                renderPlayerSmallIcon(player, Icons.SPEAKER_OFF,matrices,vertexConsumers,light);
            } else if (VoiceClient.getServerConfig().getMuted().containsKey(player.getUUID())) {
                renderPlayerIcon(player, Icons.SPEAKER_OFF,matrices,vertexConsumers,light);
            } else {
                Boolean isTalking = SocketClientUDPQueue.talking.get(player.getUUID());
                if (isTalking != null) {
                    if (isTalking)
                        renderPlayerSmallIcon(player, Icons.RED_SPEAKER,matrices,vertexConsumers,light);
                    else
                        renderPlayerSmallIcon(player, Icons.RED_SPEAKER,matrices,vertexConsumers,light);
                } else if (PlayerVolumeHandler.isShow(player)) {
                    renderPercent(player, distance, matrices, hasLabel, vertexConsumers, light);
                }
            }
        } else {
            renderPlayerSmallIcon(player, Icons.DISCONECTED,matrices,vertexConsumers,light);
            //renderIcon(112, 0, player, distance, matrices, hasLabel, vertexConsumers, light);
        }
    }

    private boolean isIconHidden(Player player) {
        if (VoiceClient.getClientConfig().showIcons.get() == 2) {
            return true;
        }

        if (player.getUUID().equals(client.player.getUUID())) {
            return true;
        }

        if (!client.player.connection.getOnlinePlayerIds().contains(player.getUUID())) {
            return true;
        }

        if (player.isInvisibleTo(client.player) ||
                (client.options.hideGui && VoiceClient.getClientConfig().showIcons.get() == 0)) {
            return true;
        }

        return false;
    }

    private void renderPercent(Player player, double distance, PoseStack matrices, boolean hasLabel,
                               MultiBufferSource vertexConsumers, int light) {
        double yOffset = 0.5D;
        if (hasLabel) {
            yOffset += 0.3D;

            Scoreboard scoreboard = player.getScoreboard();
            Objective scoreboardObjective = scoreboard.getDisplayObjective(2);
            if (scoreboardObjective != null && distance < 100.0D) {
                yOffset += 0.3D;
            }
        }

        matrices.pushPose();
        matrices.translate(0D, player.getBbHeight() + yOffset, 0D);
        matrices.mulPose(client.getEntityRenderDispatcher().cameraOrientation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        matrices.translate(0D, -1D, 0D);

        Matrix4f matrix4f = matrices.last().pose();
        boolean bl = !player.isDescending();
        float g = client.options.getBackgroundOpacity(0.25F);
        int j = (int) (g * 255.0F) << 24;
        Font textRenderer = client.font;

        Component text = Component.literal((int) Math.round(VoiceClient.getClientConfig().getPlayerVolumes().getOrDefault(player.getUUID(), 1.0D) * 100.0D) + "%");

        float h = (float) (-textRenderer.width(text) / 2);
        textRenderer.drawInBatch(text, h, 0, 553648127, false, matrix4f, vertexConsumers, bl, j, light);
        if (bl) {
            textRenderer.drawInBatch(text, h, 0, -1, false, matrix4f, vertexConsumers, false, 0, light);
        }

        matrices.popPose();
    }

    private void renderPlayerSmallIcon(Player player, ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource buffer, int light) {
        matrixStackIn.pushPose();

        float font = minecraft.font.width(player.getDisplayName());

        matrixStackIn.translate(0D, player.getBbHeight() + 0.5D, 0D);
        matrixStackIn.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
        matrixStackIn.scale(2, 2,1);
        matrixStackIn.translate(-(font/3.5), -3D, 0D);

        float offset = (float) (font / 2 );

        VertexConsumer builder = buffer.getBuffer(RenderType.text(texture));
        int alpha = 32;

        if (player.isDiscrete()) {
            vertex(builder, matrixStackIn, offset, 10F, 0F, 0F, 1F, alpha, light);
            vertex(builder, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, alpha, light);
            vertex(builder, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, alpha, light);
            vertex(builder, matrixStackIn, offset, 0F, 0F, 0F, 0F, alpha, light);
        } else {
            vertex(builder, matrixStackIn, offset, 10F, 0F, 0F, 1F, light);
            vertex(builder, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, light);
            vertex(builder, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, light);
            vertex(builder, matrixStackIn, offset, 0F, 0F, 0F, 0F, light);

            VertexConsumer builderSeeThrough = buffer.getBuffer(RenderType.textSeeThrough(texture));
            vertex(builderSeeThrough, matrixStackIn, offset, 10F, 0F, 0F, 1F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset, 0F, 0F, 0F, 0F, alpha, light);
        }
        matrixStackIn.popPose();
    }

    private void renderPlayerIcon(Player player, ResourceLocation texture, PoseStack matrixStackIn,
                                  MultiBufferSource buffer, int light) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0D, player.getBbHeight() + 0.5D, 0D);
        matrixStackIn.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
        matrixStackIn.translate(0D, -1D, 0D);

        float offset = (float) (minecraft.font.width(player.getDisplayName()) / 2 );

        VertexConsumer builder = buffer.getBuffer(RenderType.text(texture));
        int alpha = 32;

        if (player.isDiscrete()) {
            vertex(builder, matrixStackIn, offset, 10F, 0F, 0F, 1F, alpha, light);
            vertex(builder, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, alpha, light);
            vertex(builder, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, alpha, light);
            vertex(builder, matrixStackIn, offset, 0F, 0F, 0F, 0F, alpha, light);
        } else {
            vertex(builder, matrixStackIn, offset, 10F, 0F, 0F, 1F, light);
            vertex(builder, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, light);
            vertex(builder, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, light);
            vertex(builder, matrixStackIn, offset, 0F, 0F, 0F, 0F, light);

            VertexConsumer builderSeeThrough = buffer.getBuffer(RenderType.textSeeThrough(texture));
            vertex(builderSeeThrough, matrixStackIn, offset, 10F, 0F, 0F, 1F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset, 0F, 0F, 0F, 0F, alpha, light);
        }

        matrixStackIn.popPose();
    }

    private void renderIcon(float u, float v, Player player, double distance, PoseStack matrices,
                            boolean hasLabel, MultiBufferSource vertexConsumers, int light) {
        double yOffset = 0.5D;
        if (PlayerVolumeHandler.isShow(player)) {
            renderPercent(player, distance, matrices, hasLabel, vertexConsumers, light);
            yOffset += 0.3D;
        }

        if (hasLabel) {
            yOffset += 0.3D;

            Scoreboard scoreboard = player.getScoreboard();
            Objective scoreboardObjective = scoreboard.getDisplayObjective(2);
            if (scoreboardObjective != null && distance < 100.0D) {
                yOffset += 0.3D;
            }
        }

        matrices.pushPose();
        matrices.translate(0D, player.getBbHeight() + yOffset, 0D);
        matrices.mulPose(client.getEntityRenderDispatcher().cameraOrientation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        matrices.translate(0D, -1D, 0D);

        float offset = -5F;

        VertexConsumer builder = vertexConsumers.getBuffer(RenderType.text(VoiceClient.ICONS));

        float u0 = u / (float) 256;
        float u1 = (u + (float) 16) / (float) 256;
        float v0 = v / (float) 256;
        float v1 = (v + (float) 16) / (float) 256;

        if (player.isDescending()) {
            vertex(builder, matrices, offset, 10F, 0F, u0, v1, 40, light);
            vertex(builder, matrices, offset + 10F, 10F, 0F, u1, v1, 40, light);
            vertex(builder, matrices, offset + 10F, 0F, 0F, u1, v0, 40, light);
            vertex(builder, matrices, offset, 0F, 0F, u0, v0, 40, light);
        } else {
            vertex(builder, matrices, offset, 10F, 0F, u0, v1, 255, light);
            vertex(builder, matrices, offset + 10F, 10F, 0F, u1, v1, 255, light);
            vertex(builder, matrices, offset + 10F, 0F, 0F, u1, v0, 255, light);
            vertex(builder, matrices, offset, 0F, 0F, u0, v0, 255, light);

            VertexConsumer builderSeeThrough = vertexConsumers.getBuffer(RenderType.textSeeThrough(VoiceClient.ICONS));
            vertex(builderSeeThrough, matrices, offset, 10F, 0F, u0, v1, 40, light);
            vertex(builderSeeThrough, matrices, offset + 10F, 10F, 0F, u1, v1, 40, light);
            vertex(builderSeeThrough, matrices, offset + 10F, 0F, 0F, u1, v0, 40, light);
            vertex(builderSeeThrough, matrices, offset, 0F, 0F, u0, v0, 40, light);
        }

        matrices.popPose();
    }

    private void vertex(VertexConsumer builder, PoseStack matrices, float x, float y, float z, float u, float v, int alpha, int light) {
        PoseStack.Pose entry = matrices.last();
        Matrix4f modelViewMatrix = entry.pose();

        builder.vertex(modelViewMatrix, x, y, z);
        builder.color(255, 255, 255, alpha);
        builder.uv(u, v);
        builder.overlayCoords(OverlayTexture.NO_OVERLAY);
        builder.uv2(light);
        builder.normal(0F, 0F, -1F);
        builder.endVertex();
    }

    private void vertex(VertexConsumer builder, PoseStack matrixStack, float x, float y, float z, float u, float v, int light) {
        vertex(builder, matrixStack, x, y, z, u, v, 255, light);
    }
}
