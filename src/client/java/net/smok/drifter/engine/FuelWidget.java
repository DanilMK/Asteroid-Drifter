package net.smok.drifter.engine;

import com.mojang.blaze3d.systems.RenderSystem;
import com.teamresourceful.resourcefullib.client.scissor.ClosingScissorBox;
import com.teamresourceful.resourcefullib.client.utils.RenderUtils;
import earth.terrarium.adastra.client.utils.GuiUtils;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.utils.ClientFluidHooks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.material.Fluid;
import net.smok.drifter.blocks.engine.EnginePanelBlockEntity;
import net.smok.drifter.registries.Values;
import net.smok.drifter.widgets.Hovered;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

public record FuelWidget(EnginePanelBlockEntity enginePanelBlock,
                         int posX, int posY, int width, int height, Font font) implements Renderable, Hovered {


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTime) {
        float amount = 1f - (float) enginePanelBlock.getFuel() / enginePanelBlock.getFuelCapacity();
        int screenAmount = (int) (amount * height);


        FluidHolder holder = FluidHolder.of(Values.SHIP_FUEL.getStillFluid().get());
        TextureAtlasSprite sprite = ClientFluidHooks.getFluidSprite(holder);
        int color = ClientFluidHooks.getFluidColor(holder);

        float r = (float) FastColor.ARGB32.red(color) / 255.0F;
        float g = (float) FastColor.ARGB32.green(color) / 255.0F;
        float b = (float) FastColor.ARGB32.blue(color) / 255.0F;

        ClosingScissorBox scissorBox = RenderUtils.createScissorBox(Minecraft.getInstance(), guiGraphics.pose(),
                posX(), posY() + 46 - screenAmount, 12, 46);
        for(int i = 1; i < 5; ++i) guiGraphics.blit(posX(), posY() + 46 - i * 16, 0,
                16, 16, sprite, r, g, b, 1.0F);

        scissorBox.close();

        guiGraphics.blit(GuiUtils.FLUID_BAR, posX, posY, 0.0F, 0.0F, width, height, 12, 46);


    }

    @Override
    public boolean isHover(int mouseX, int mouseY) {
        return Hovered.isHover(posX(), posY(), posX() + width(), posY() + height(), mouseX, mouseY);
    }

    @Contract(" -> new")
    @Override
    public @NotNull @Unmodifiable List<Component> content() {
        return List.of(enginePanelBlock.fuelConsumption());
    }

}
