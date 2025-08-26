package net.smok.drifter.controller;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.registries.Values;
import net.smok.drifter.blocks.controller.AsteroidSlot;
import net.smok.drifter.utils.FlyUtils;
import net.smok.drifter.widgets.Hovered;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class AsteroidSlotWidget implements Renderable, GuiEventListener, NarratableEntry, Hovered {
    public static final ResourceLocation SELECTOR = new ResourceLocation(Values.MOD_ID, "textures/gui/controller/selector.png");
    private static final ChatFormatting TOOLTIP_COLOR = ChatFormatting.GRAY;

    private final ShipControllerBlockEntity controller;
    private final AsteroidSlot slot;
    private boolean focused;
    private boolean selected;

    private final ShipControllerScreen parent;

    public AsteroidSlotWidget(ShipControllerBlockEntity controller, AsteroidSlot slot, ShipControllerScreen parent) {
        this.controller = controller;
        this.slot = slot;
        this.parent = parent;
    }

    public int getX() {
        return parent.width / 2 + slot.getX();
    }

    public int getY() {
        return parent.height / 2 + slot.getY();
    }

    public @NotNull ItemStack getItem() {
        return slot.getItem();
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float timeDelta) {
        if (focused) {
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            guiGraphics.blit(SELECTOR, getX() - 1, getY() - 1, 0, 0, 18, 18, 18, 18);
            RenderSystem.disableBlend();
        }
        guiGraphics.renderItem(slot.getItem(), getX(), getY());
        if (isHover(mouseX, mouseY)) guiGraphics.fillGradient(RenderType.guiOverlay(), getX(), getY(), getX() + 16,
                getY() + 16, 0x80ffffff, 0x80ffffff, 0);
    }


    @Override
    public void setFocused(boolean bl) {
        focused = bl;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return !getItem().isEmpty() & isHover((int) d, (int) e);
    }

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public boolean isHover(int mouseX, int mouseY) {

        if (!getItem().isEmpty()) {
            if (controller.getRemainDistance() <= 0) {
                return getX() < mouseX & getY() < mouseY &
                        getX() + 16 > mouseX & getY() + 16 > mouseY;
            } else {
                return selected && Hovered.isHover(parent.width / 2 - 8, parent.height / 2 - 8,
                        parent.width / 2 + 8, parent.height / 2 + 8, mouseX, mouseY);
            }
        }
        return false;
    }

    public boolean isSelected() {
        return selected;
    }

    public AsteroidSlotWidget setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    @Override
    public List<Component> content() {
        List<Component> tooltip = parent.getMinecraft() != null ? Screen.getTooltipFromItem(parent.getMinecraft(), getItem()) : new ArrayList<>();

        MutableComponent distance = Component.translatable("tooltip.asteroid_drifter.full_distance", String.format("%,d", slot.getDist().get()));
        distance.withStyle(TOOLTIP_COLOR);

        Component fuel = controller.getRequired(slot.getDist().get());

        String totalTime = FlyUtils.timeToString(FlyUtils.totalTime(
                controller.maxSpeed(), slot.getDist().get()));
        MutableComponent time = Component.translatable("tooltip.asteroid_drifter.time_required", totalTime).withStyle(TOOLTIP_COLOR);

        tooltip.add(distance);
        if (fuel != null) tooltip.add(fuel);
        tooltip.add(time);

        return tooltip;
    }
}
