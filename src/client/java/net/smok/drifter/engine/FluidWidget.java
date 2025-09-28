package net.smok.drifter.engine;

import com.teamresourceful.resourcefullib.client.scissor.ClosingScissorBox;
import com.teamresourceful.resourcefullib.client.utils.RenderUtils;
import earth.terrarium.adastra.client.utils.GuiUtils;
import earth.terrarium.adastra.common.utils.TooltipUtils;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.utils.ClientFluidHooks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.smok.drifter.blocks.engine.EnginePanelBlockEntity;
import net.smok.drifter.widgets.Hovered;
import oshi.util.tuples.Pair;

import java.util.List;

public class FluidWidget extends AbstractWidget implements Hovered {

    private final EnginePanelBlockEntity engine;

    protected long lastFluidAmount;

    public FluidWidget(int x, int y, EnginePanelBlockEntity engine) {
        super(x, y, 12, 46, Component.empty());
        this.engine = engine;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Pair<FluidHolder, Long> holderCapacityPair = engine.getFluidHolder();
        FluidHolder holder = holderCapacityPair.getA();
        long max = holderCapacityPair.getB();
        long fluidAmount = holder.getFluidAmount();

        if (holder.isEmpty()) return;

        float ratio = (float)fluidAmount / (float)max;
        lastFluidAmount = fluidAmount;

        TextureAtlasSprite sprite = ClientFluidHooks.getFluidSprite(holder);
        int color = ClientFluidHooks.getFluidColor(holder);

        float r = (float) FastColor.ARGB32.red(color) / 255.0F;
        float g = (float) FastColor.ARGB32.green(color) / 255.0F;
        float b = (float) FastColor.ARGB32.blue(color) / 255.0F;

        ClosingScissorBox ignored = RenderUtils.createScissorBox(Minecraft.getInstance(), guiGraphics.pose(),
                getX(), getY() + 46 - (int)(46.0F * ratio), 12, 46);

        for(int i = 1; i < 5; ++i) guiGraphics.blit(getX(), getY() + 46 - i * 16, 0,
                16, 16, sprite, r, g, b, 1.0F);

        ignored.close();
        guiGraphics.blit(GuiUtils.FLUID_BAR, getX(), getY(), 0.0F, 0.0F, width, height, 12, 46);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public boolean isHover(int mouseX, int mouseY) {
        return isHoveredOrFocused();
    }

    @Override
    public List<Component> content() {
        Pair<FluidHolder, Long> holderCapacityPair = engine.getFluidHolder();
        FluidHolder holder = holderCapacityPair.getA();
        long max = holderCapacityPair.getB();

        return List.of(TooltipUtils.getFluidComponent(holder, max).copy().withStyle(ChatFormatting.WHITE), engine.fuelConsumption());
    }
}
