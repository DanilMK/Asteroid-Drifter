package net.smok.drifter.blocks.alert;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.smok.drifter.registries.Values;

import java.awt.*;

public class AlertOverlay implements HudRenderCallback {
    private static final ResourceLocation NAUSEA_LOCATION = new ResourceLocation("textures/misc/nausea.png");
    public static final float intensityPreTick = 0.5f;
    public static final float period = 0.05f;

    private float effectDuration;

    private static final Color[] COLORS = new Color[] {Color.black, Color.darkGray, Color.gray, Color.white,
            new Color(0x5a381e), Color.red, Color.orange, Color.yellow, Color.green, new Color(0x455522),
            Color.cyan, new Color(0x217fb9), Color.blue, new Color(0x5d1d91), Color.magenta, Color.pink};


    @Override
    public void onHudRender(GuiGraphics drawContext, float tickDelta) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (!player.hasEffect(Values.ALERT_EFFECT.get())) {
            effectDuration = 0;
            return;
        }

        MobEffectInstance effect = player.getEffect(Values.ALERT_EFFECT.get());
        effectDuration += tickDelta;
        int amplifier = effect.getAmplifier();

        float intensity = (float) (Math.sin(effectDuration * period) * intensityPreTick);

        if (amplifier > 15) amplifier = 3;

        renderConfusionOverlay(drawContext, intensity, COLORS[amplifier]);

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
}
