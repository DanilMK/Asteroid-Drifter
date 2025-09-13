package net.smok.drifter.controller;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.smok.drifter.Debug;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.recipies.AsteroidRecipe;
import net.smok.drifter.recipies.PlacedAsteroidRecipe;
import net.smok.drifter.registries.Values;
import net.smok.drifter.utils.FlyUtils;
import net.smok.drifter.widgets.Hovered;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AsteroidSlotWidget implements Renderable, GuiEventListener, NarratableEntry, Hovered {
    public static final ResourceLocation SELECTOR = new ResourceLocation(Values.MOD_ID, "textures/gui/controller/selector.png");
    private static final ChatFormatting TOOLTIP_COLOR = ChatFormatting.GRAY;

    private final int slot;
    private final ShipControllerBlockEntity controller;
    private boolean focused;
    private boolean selected;

    private final ShipControllerScreen parent;

    public AsteroidSlotWidget(int slot, ShipControllerBlockEntity controller, ShipControllerScreen parent) {
        this.slot = slot;
        this.controller = controller;
        this.parent = parent;
    }

    public int getX() {
        return parent.width / 2 + getPlacedAsteroidRecipe().x() - 8;
    }

    public int getY() {
        return parent.height / 2 + getPlacedAsteroidRecipe().y() - 8;
    }

    public int getXMid() {
        return parent.width / 2 + getPlacedAsteroidRecipe().x();
    }

    public int getYMid() {
        return parent.height / 2 + getPlacedAsteroidRecipe().y();
    }

    public Optional<AsteroidRecipe> getRecipe() {
        return getPlacedAsteroidRecipe().recipe();
    }

    private @NotNull PlacedAsteroidRecipe getPlacedAsteroidRecipe() {
        return controller.getAllRecipes().get(slot);
    }

    public @NotNull ItemStack getItem() {
        return getRecipe().map(AsteroidRecipe::icon).orElse(ItemStack.EMPTY);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float timeDelta) {
       // Debug.log("Slot " + slot + " placed " + getPlacedAsteroidRecipe() + " recipe " + getRecipe().orElse(null) );
        if (focused) {
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            guiGraphics.blit(SELECTOR, getX() - 1, getY() - 1, 0, 0, 18, 18, 18, 18);
            RenderSystem.disableBlend();
        }
        guiGraphics.renderItem(getItem(), getX(), getY());
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
        List<Component> tooltip = getRecipe().map(r -> new ArrayList<>(r.tooltips().stream().map(key -> (Component) Component.translatable(key)).toList())).orElse(new ArrayList<>());

        MutableComponent distance = Component.translatable("tooltip.asteroid_drifter.full_distance", String.format("%,d", getPlacedAsteroidRecipe().distance()));
        distance.withStyle(TOOLTIP_COLOR);

        Component fuel = controller.getRequired(getPlacedAsteroidRecipe().distance());

        String totalTime = FlyUtils.timeToString(FlyUtils.totalTime(
                controller.maxSpeed(), getPlacedAsteroidRecipe().distance()));
        MutableComponent time = Component.translatable("tooltip.asteroid_drifter.time_required", totalTime).withStyle(TOOLTIP_COLOR);

        tooltip.add(distance);
        if (fuel != null) tooltip.add(fuel);
        tooltip.add(time);

        return tooltip;
    }
}
