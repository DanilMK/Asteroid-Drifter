package net.smok.drifter.alert;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.smok.drifter.registries.Values;
import net.smok.drifter.blocks.alert.AlertPanelBlockEntity;
import net.smok.drifter.network.NetworkHandler;
import net.smok.drifter.widgets.Hovered;

import java.util.List;
import java.util.function.Supplier;

public class AlertDangerWidget extends AbstractWidget {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Values.MOD_ID, "textures/gui/alert/alert_buttons.png");

    public static final ItemStack[] COLORS_DYE = new ItemStack[] {                new ItemStack(Items.BLACK_DYE),
            new ItemStack(Items.GRAY_DYE),   new ItemStack(Items.LIGHT_GRAY_DYE), new ItemStack(Items.WHITE_DYE),
            new ItemStack(Items.BROWN_DYE),  new ItemStack(Items.RED_DYE),        new ItemStack(Items.ORANGE_DYE),
            new ItemStack(Items.YELLOW_DYE), new ItemStack(Items.GREEN_DYE),      new ItemStack(Items.LIME_DYE),
            new ItemStack(Items.CYAN_DYE),   new ItemStack(Items.BLUE_DYE),       new ItemStack(Items.LIGHT_BLUE_DYE),
            new ItemStack(Items.PURPLE_DYE), new ItemStack(Items.MAGENTA_DYE),    new ItemStack(Items.PINK_DYE)
    };

    public static final ItemStack[] COLORS_WOOL = new ItemStack[] {                 new ItemStack(Items.BLACK_WOOL),
            new ItemStack(Items.GRAY_WOOL),   new ItemStack(Items.LIGHT_GRAY_WOOL), new ItemStack(Items.WHITE_WOOL),
            new ItemStack(Items.BROWN_WOOL),  new ItemStack(Items.RED_WOOL),        new ItemStack(Items.ORANGE_WOOL),
            new ItemStack(Items.YELLOW_WOOL), new ItemStack(Items.GREEN_WOOL),      new ItemStack(Items.LIME_WOOL),
            new ItemStack(Items.CYAN_WOOL),   new ItemStack(Items.BLUE_WOOL),       new ItemStack(Items.LIGHT_BLUE_WOOL),
            new ItemStack(Items.PURPLE_WOOL), new ItemStack(Items.MAGENTA_WOOL),    new ItemStack(Items.PINK_WOOL)
    };


    private static final int BUTTON_SIZE = 20;
    private static final int TEXT_SIZE = 100;
    public static final int WIDTH = TEXT_SIZE + BUTTON_SIZE * 3, HEIGHT = 20;

    private final Font font;
    private final Sub text, alert, test, slot;
    private final AlertPanelBlockEntity.Danger danger;
    private final BlockPos alertSystemBlock;
    private final int index;


    private final List<Sub> subs;

    public AlertDangerWidget(int x, int y, AlertPanelBlockEntity.Danger danger, Font font, BlockPos alertSystemBlock, int index) {
        super(x, y, WIDTH, HEIGHT, Component.empty());
        this.font = font;
        this.danger = danger;
        this.alertSystemBlock = alertSystemBlock;
        this.index = index;

        text = new Sub(x, y, TEXT_SIZE, () -> null);

        int x1 = x + text.width;

        alert = new Sub(x1, y, BUTTON_SIZE, () -> {
                ChatFormatting format = danger.active().getValue() ? ChatFormatting.RED : ChatFormatting.GRAY;
                return Component.translatable("tooltip.asteroid_drifter.danger.alert_active").withStyle(format);
        });

        x1 += alert.width;

        test = new Sub(x1, y, BUTTON_SIZE,
                () -> Component.translatable("tooltip.asteroid_drifter.danger.alert_test").withStyle(ChatFormatting.GRAY));

        x1 += test.width;

        slot = new Sub(x1, y, BUTTON_SIZE,
                () -> Component.translatable("tooltip.asteroid_drifter.danger.alert_color").withStyle(ChatFormatting.GRAY));

        subs = List.of(test, alert, test, slot);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float timeDelta) {

        RenderSystem.enableBlend();

        renderTexture(guiGraphics, TEXTURE, text.x, text.y, 0, 0, 0, text.width, HEIGHT, 160, 40);
        renderTexture(guiGraphics, TEXTURE, alert.x, alert.y, TEXT_SIZE, danger.active().getValue() ? 0 : BUTTON_SIZE, 0, alert.width, HEIGHT, 160, 40);
        renderTexture(guiGraphics, TEXTURE, test.x, test.y, TEXT_SIZE + BUTTON_SIZE, danger.tested().getValue() ? 0 : BUTTON_SIZE, 0,  BUTTON_SIZE, HEIGHT,160, 40);
        renderTexture(guiGraphics, TEXTURE, slot.x, slot.y, TEXT_SIZE + BUTTON_SIZE * 2, 0, 0,  BUTTON_SIZE, HEIGHT,160, 40);

        RenderSystem.disableBlend();
        guiGraphics.renderItem(COLORS_WOOL[danger.color().getValue()], slot.x + 2, slot.y + 2);
        renderScrollingString(guiGraphics, font, danger.text(), text.x, text.y, text.x + text.width, text.y + HEIGHT, 0xFFFFFFFF);


        for (Sub sub : subs) {
            if (sub.isHovered(mouseX, mouseY)) {
                Component component = sub.getComponent();
                if (component != null) {
                    guiGraphics.renderTooltip(font, component, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        int mx = (int) mouseX, my = (int) mouseY;
        if (!Hovered.isHover(getX(), getY(), getX() + width, getY() + height, mx, my)) return false;
        if (test.isHovered(mx, my)) {
            testButton();
            return true;
        }
        if (slot.isHovered(mx, my)) {
            slotButton(mouseButton);
            return true;
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private void slotButton(int button) {
        int i;
        if (button > 0) {
            i = danger.color().getValue() - 1;
            if (i < 0) i = 15;
        } else {
            i = danger.color().getValue() + 1;
            if (i > 15) i = 0;
        }

        danger.color().setValue(i);
        ClientPlayNetworking.send(NetworkHandler.ALERT_CHANGE_DANGER_COLOR.getId(), NetworkHandler.ALERT_CHANGE_DANGER_COLOR.encode(alertSystemBlock, index, i));
    }

    private void testButton() {
        danger.tested().setValue(!danger.tested().getValue());
        ClientPlayNetworking.send(NetworkHandler.ALERT_TEST_DANGER.getId(), NetworkHandler.ALERT_TEST_DANGER.encode(alertSystemBlock, index));
    }

    private record Sub(int x, int y, int width, Supplier<Component> componentSupplier) {

        public boolean isHovered(int mouseX, int mouseY) {
            return Hovered.isHover(x, y, x + width, y + HEIGHT, mouseX, mouseY);
        }

        public Component getComponent() {
            return componentSupplier.get();
        }

    }
}
