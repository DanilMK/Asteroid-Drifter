package net.smok.drifter.blocks.alert;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AlertPlayerHolder implements ClientTickEvents.EndWorldTick, ClientPlayNetworking.PlayChannelHandler, HudRenderCallback {

    private static final ResourceLocation NAUSEA_LOCATION = new ResourceLocation("textures/misc/nausea.png");
    public static final ResourceLocation tickId = new ResourceLocation(Values.MOD_ID, "alert_player_holder");
    public static final AlertPlayerHolder INSTANCE = new AlertPlayerHolder();

    public static final float intensityPreTick = 0.5f;
    public static final float period = 0.1f;

    private long effectDuration;
    private final List<AlertContainer> activeAlerts = new ArrayList<>();

    @Override
    public void onEndTick(ClientLevel clientLevel) {
        if (clientLevel.getGameTime() % 20L == 0) activeAlerts.removeIf(AlertContainer::tickRemove);
        if (activeAlerts.isEmpty()) effectDuration = 0;
        else {
            effectDuration++;
            AlertSound sound = activeAlerts.get(0).alert.getSound();
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && clientLevel.getGameTime() % sound.getPeriod() == 0 && sound.getVolume() >= 0.01f) {
                clientLevel.playLocalSound(player.getX(), player.getY(), player.getZ(), sound.getSoundEvent(),
                        SoundSource.RECORDS, 1, sound.getPitch(), false);
            }
        }
    }

    public void addAlerts(@NotNull Stream<Alert> alerts) {
        alerts.forEach(alert -> {
            boolean b;
            b = true;
            for (AlertContainer active : activeAlerts) {
                if (active.alert.equals(alert)) {
                    active.tickAdd();
                    b = false;
                }
            }
            if (b) activeAlerts.add(new AlertContainer(alert));
        });
    }

    @Override
    public void receive(Minecraft minecraft, ClientPacketListener clientPacketListener, FriendlyByteBuf friendlyByteBuf, PacketSender packetSender) {
        BlockPos pos = friendlyByteBuf.readBlockPos();
        clientPacketListener.getLevel().getBlockEntity(pos, DrifterBlocks.ALERT_PANEL_BLOCK_ENTITY.get()).ifPresent(block ->
                addAlerts(block.getAllAlerts().stream().filter(Alert::isActiveOrTested)));
    }

    @Override
    public void onHudRender(GuiGraphics guiGraphics, float v) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || activeAlerts.isEmpty()) return;
        float duration = effectDuration + v;
        float intensity = (float) ((Math.sin(duration * period) + 1) / 2 * intensityPreTick);
        Color color = activeAlerts.get(0).alert.getIcon().getColor();
        renderConfusionOverlay(guiGraphics, intensity, color);
    }

    private void renderConfusionOverlay(GuiGraphics guiGraphics, float f, Color color) {
        int i = guiGraphics.guiWidth();
        int j = guiGraphics.guiHeight();
        guiGraphics.pose().pushPose();
        float g = Mth.lerp(f, 2.0F, 1.0F);
        guiGraphics.pose().translate(i / 2.0F, j / 2.0F, 0.0F);
        guiGraphics.pose().scale(g, g, g);
        guiGraphics.pose().translate(-i / 2.0F, -j / 2.0F, 0.0F);
        float h = color.getRed() / 255f * f;
        float k = color.getGreen() / 255f * f;
        float l = color.getBlue() / 255f * f;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        guiGraphics.setColor(h, k, l, 1.0F);
        guiGraphics.blit(NAUSEA_LOCATION, 0, 0, -90, 0.0F, 0.0F, i, j, i, j);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        guiGraphics.pose().popPose();
    }

    public List<Alert> getActiveAlerts() {
        return activeAlerts.stream().map(alertContainer -> alertContainer.alert).toList();
    }



    private static final class AlertContainer {
        private final Alert alert;
        private byte lifeTime;

        private AlertContainer(Alert alert) {
            this.alert = alert;
            this.lifeTime = 4;
        }

        public boolean tickRemove() {
            lifeTime--;
            return lifeTime <= 0;
        }

        public void tickAdd() {
            lifeTime = 4;
        }
    }
}
