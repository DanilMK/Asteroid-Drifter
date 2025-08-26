package net.smok.drifter.widgets;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface Hovered {

    boolean isHover(int mouseX, int mouseY);

    List<Component> content();

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
