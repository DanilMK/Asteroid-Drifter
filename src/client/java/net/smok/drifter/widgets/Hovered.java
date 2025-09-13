package net.smok.drifter.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface Hovered {

    boolean isHover(int mouseX, int mouseY);

    List<Component> content();

    default boolean appendContent(List<Component> contents, int mouseX, int mouseY) {
        if (isHover(mouseX, mouseY)) {
            contents.addAll(content());
            return true;
        }
        return false;
    }

    static void renderHover(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, boolean addSpace, Hovered... hovers) {
        List<Component> contents = new ArrayList<>();
        if (addSpace) {
            for (Hovered hover : hovers)
                if (hover.appendContent(contents, mouseX, mouseY)) contents.add(Component.empty());

            if (contents.size() > 1) contents.remove(contents.size() - 1);
        } else {
            for (Hovered hover : hovers) hover.appendContent(contents, mouseX, mouseY);
        }
        if (!contents.isEmpty()) guiGraphics.renderTooltip(font, contents, Optional.empty(), mouseX, mouseY);
    }

    static void renderHover(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, boolean addSpace, List<Hovered> hovers) {
        List<Component> contents = new ArrayList<>();
        if (addSpace) {
            for (Hovered hover : hovers)
                if (hover.appendContent(contents, mouseX, mouseY)) contents.add(Component.empty());

            if (contents.size() > 1) contents.remove(contents.size() - 1);
        } else {
            for (Hovered hover : hovers) hover.appendContent(contents, mouseX, mouseY);
        }
        if (!contents.isEmpty()) guiGraphics.renderTooltip(font, contents, Optional.empty(), mouseX, mouseY);
    }

    static boolean isHover(int minX, int minY, int maxX, int maxY, int mouseX, int mouseY) {
        return  minX < mouseX & minY < mouseY &
                maxX > mouseX & maxY > mouseY;
    }

    static boolean isInCircle(double circleX, double circleY, double radius, int mouseX, int mouseY) {
        double dx = mouseX - circleX;
        double dy = mouseY - circleY;
        double dist = dx * dx + dy * dy;
        return dist < radius * radius;
    }
}
