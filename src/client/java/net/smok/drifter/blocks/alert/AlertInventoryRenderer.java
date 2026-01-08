package net.smok.drifter.blocks.alert;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.smok.drifter.widgets.Hovered;
import net.smok.drifter.widgets.Sprite;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class AlertInventoryRenderer {

    private static final Sprite BIG_FRAME = Sprite.of(AbstractContainerScreen.INVENTORY_LOCATION, 0, 166, 120, 32, 256, 256);
    private static final Sprite SMALL_FRAME = Sprite.of(AbstractContainerScreen.INVENTORY_LOCATION, 0, 198, 32, 32, 256, 256);
    private static final Sprite OVER_FRAME = Sprite.of(AbstractContainerScreen.INVENTORY_LOCATION, 141, 166, 24, 24, 256, 256);

    public static void renderEffects(GuiGraphics guiGraphics, int imageRight, int imageTop, int freeSpace,
                                     int mouseX, int mouseY, boolean noEffects, Font font) {
        List<Alert> alerts = AlertPlayerHolder.INSTANCE.getActiveAlerts();
        if (alerts.isEmpty() || freeSpace < 32) return;

        int x = getX(imageRight, freeSpace, noEffects);

        boolean bigSize = noEffects ? freeSpace >= 120 : freeSpace >= 242;

        int height = alerts.size() > 5 ? 132 / (alerts.size() - 1) : 33;

        int y = imageTop;

        if (bigSize) {
            for (Alert alert : alerts) {
                renderBigAlert(guiGraphics, font, x, y, alert);
                y += height;
            }
        } else {
            for (Alert alert : alerts) {
                renderSmallAlert(guiGraphics, x, y, alert);
                y += height;
            }
            y = imageTop;
            for (Alert alert : alerts) {
                if (renderHoverText(guiGraphics, font, x, y, mouseX, mouseY, alert)) break;
                y += height;
            }
        }
    }

    private static int getX(int imageRight, int freeSpace, boolean noEffects) {
        int effectsSize;
        if (noEffects) effectsSize = 0;
        else if (isEffectBig(freeSpace, false)) effectsSize = 121;
        else effectsSize = 33;
        return imageRight + effectsSize;
    }

    public static boolean isEffectBig(int freeSpace, boolean noAlerts) {
        if (noAlerts) return freeSpace >= 120;
        return freeSpace >= 152;
    }


    public static void renderBigAlert(@NotNull GuiGraphics guiGraphics, @NotNull Font font, int x, int y, @NotNull Alert alert) {
        BIG_FRAME.draw(guiGraphics, x, y);
        AlertDisplay.renderIcon(guiGraphics, alert.getIcon(), x + 7, y + 7);
        guiGraphics.drawString(font, alert.text(), x + 28, y + 6, 16777215);
        guiGraphics.drawString(font, alert.subText(), x + 28, y + 16, 16777215);
    }

    public static void renderSmallAlert(@NotNull GuiGraphics guiGraphics, int x, int y, @NotNull Alert alert) {
        SMALL_FRAME.draw(guiGraphics, x, y);
        AlertDisplay.renderIcon(guiGraphics, alert.getIcon(), x + 7, y + 7);
    }

    public static void renderOverlayAlert(@NotNull GuiGraphics guiGraphics, int x, int y, @NotNull Alert alert) {
        OVER_FRAME.draw(guiGraphics, x, y);
        AlertDisplay.renderIcon(guiGraphics, alert.getIcon(), x + 3, y + 3);
    }

    public static boolean renderHoverText(@NotNull GuiGraphics guiGraphics, Font font, int x, int y, int mouseX, int mouseY, @NotNull Alert alert) {
        if (Hovered.isHover(x, y, x + 32, y + 32, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, List.of(alert.text(), alert.subText()), Optional.empty(), mouseX, mouseY);
            return true;
        }
        return false;
    }
}
