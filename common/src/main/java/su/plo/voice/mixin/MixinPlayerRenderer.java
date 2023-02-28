package su.plo.voice.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.Icons;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.gui.PlayerVolumeHandler;
import su.plo.voice.client.render.EntityIconRenderer;
import su.plo.voice.client.socket.SocketClientUDPQueue;

@Mixin(value = PlayerRenderer.class, priority =900)
public abstract class MixinPlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public MixinPlayerRenderer(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void render(AbstractClientPlayer player, float f, float g, PoseStack matrices,
                       MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        if(!VoiceClient.isConnected()) {
            return;
        }

        double d = this.entityRenderDispatcher.distanceToSqr(player);
        if (d > 4096.0D) {
            return;
        }


        EntityIconRenderer.getInstance().entityRender(
                player,
                d,
                matrices,
                this.shouldShowName(player),
                vertexConsumers,
                light
        );


    }


}
