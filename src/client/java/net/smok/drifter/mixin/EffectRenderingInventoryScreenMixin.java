package net.smok.drifter.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.smok.drifter.blocks.alert.AlertInventoryRenderer;
import net.smok.drifter.blocks.alert.AlertPlayerHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectRenderingInventoryScreenMixin<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {


    public EffectRenderingInventoryScreenMixin(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "renderEffects", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;"))
    private void effectSizeRedirect(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci, @Local LocalBooleanRef bl,
                                    @Local(ordinal = 2) int i, @Local(ordinal = 3) int j) {
        bl.set(AlertInventoryRenderer.isEffectBig(j, AlertPlayerHolder.INSTANCE.getActiveAlerts().isEmpty()));
    }

    @Inject(method = "renderEffects", at = @At(value = "RETURN"))
    private void hoverRenderInjection(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo callbackInfo,
                                      @Local(ordinal = 2) int i, @Local(ordinal = 3) int j,
                                      @Local(ordinal = 0) Collection<MobEffectInstance> collection) {
        AlertInventoryRenderer.renderEffects(guiGraphics, i, topPos, j, mouseX, mouseY, collection.isEmpty(), font);
    }
}
