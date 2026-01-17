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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlertPlayerHolder implements ClientTickEvents.EndWorldTick, ClientPlayNetworking.PlayChannelHandler, HudRenderCallback {

    private static final ResourceLocation NAUSEA_LOCATION = new ResourceLocation("textures/misc/nausea.png");
    public static final ResourceLocation tickId = new ResourceLocation(Values.MOD_ID, "alert_player_holder");
    public static final AlertPlayerHolder INSTANCE = new AlertPlayerHolder();

    public static final float intensityPreTick = 0.5f;
    public static final float period = 0.1f;

    private long effectDuration;
    private final List<AlertHolder> holders = new ArrayList<>();

    @Override
    public void onEndTick(ClientLevel clientLevel) {
        if (clientLevel.getGameTime() % 20L == 0) holders.removeIf(AlertHolder::tickRemove);
        if (holders.isEmpty()) effectDuration = 0;
        else {
            effectDuration++;
        }
    }

    public void addAlerts(@NotNull List<AlertHolder> holders, Player player, @NotNull ClientLevel clientLevel) {
        holders.forEach(a -> {
            boolean bool = true;
            for (AlertHolder b : this.holders) {
                if (a.equals(b)) {
                    b.tickAdd();
                    b.setSubText(a.getSubText());
                    bool = false;
                    break;
                }
            }
            if (bool) {
                this.holders.add(a);
                AlertSound sound = a.getSound();
                clientLevel.playLocalSound(player.getX(), player.getY(), player.getZ(), sound.getSoundEvent(),
                        SoundSource.RECORDS, 1, sound.getPitch(), false);
            }
        });
    }

    @Override
    public void receive(Minecraft minecraft, ClientPacketListener clientPacketListener, FriendlyByteBuf friendlyByteBuf, PacketSender packetSender) {
        ArrayList<AlertHolder> alerts = new ArrayList<>();
        int size = friendlyByteBuf.readInt();
        for (int i = 0; i < size; i++) {
            Component text = friendlyByteBuf.readComponent();
            Component subText = friendlyByteBuf.readComponent();
            Icon icon = Icon.BYTE_CODEC.decode(friendlyByteBuf);
            AlertSound sound = AlertSound.BYTE_CODEC.decode(friendlyByteBuf);
            alerts.add(new AlertHolder(text, icon, sound, subText));
        }
        addAlerts(alerts, minecraft.player, clientPacketListener.getLevel());
    }

    @Override
    public void onHudRender(GuiGraphics guiGraphics, float v) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || holders.isEmpty()) return;
        float duration = effectDuration + v;
        float intensity = (float) ((Math.sin(duration * period) + 1) / 2 * intensityPreTick);
        Color color = holders.get(0).getIcon().getColor();
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

    public List<AlertHolder> getActiveAlerts() {
        return holders;
    }


    public static class AlertHolder {
        private final Component text;
        private final Icon icon;
        private final AlertSound sound;
        private Component subText;
        private byte lifeTime;

        public AlertHolder(Component text, Icon icon, AlertSound sound, @Nullable Component subText) {
            this.text = text;
            this.icon = icon;
            this.sound = sound;
            this.subText = subText;
            this.lifeTime = 4;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            AlertHolder that = (AlertHolder) object;
            return Objects.equals(text, that.text) && Objects.equals(icon, that.icon) && Objects.equals(sound, that.sound);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, icon, sound);
        }

        public Component getSubText() {
            return subText;
        }

        public Component getText() {
            return text;
        }

        public Icon getIcon() {
            return icon;
        }

        public AlertSound getSound() {
            return sound;
        }

        public void setSubText(Component subText) {
            this.subText = subText;
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
