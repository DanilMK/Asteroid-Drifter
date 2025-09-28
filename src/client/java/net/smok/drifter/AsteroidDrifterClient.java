package net.smok.drifter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.smok.drifter.alert.AlertOverlay;
import net.smok.drifter.alert.AlertSystemScreen;
import net.smok.drifter.engine.TankRenderer;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.controller.ShipControllerScreen;
import net.smok.drifter.engine.EngineScreen;
import net.smok.drifter.registries.DrifterMenus;

public class AsteroidDrifterClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		MenuScreens.register(DrifterMenus.SHIP_CONTROLLER_MENU.get(), ShipControllerScreen::new);
		MenuScreens.register(DrifterMenus.ALERT_SYSTEM_MENU.get(), AlertSystemScreen::new);
		MenuScreens.register(DrifterMenus.ENGINE_MENU.get(), EngineScreen::new);
		HudRenderCallback.EVENT.register(new AlertOverlay());
		BlockRenderLayerMap.INSTANCE.putBlock(DrifterBlocks.ALERT_LUMP.get(), RenderType.cutout());
		BlockRenderLayerMap.INSTANCE.putBlock(DrifterBlocks.STEEL_TANK_BLOCK.get(), RenderType.cutout());
		BlockRenderLayerMap.INSTANCE.putBlock(DrifterBlocks.OSTRUM_TANK_BLOCK.get(), RenderType.cutout());
		BlockRenderLayerMap.INSTANCE.putBlock(DrifterBlocks.DESH_TANK_BLOCK.get(), RenderType.cutout());
		BlockRenderLayerMap.INSTANCE.putBlock(DrifterBlocks.CALORITE_TANK_BLOCK.get(), RenderType.cutout());
		EntityModelLayerRegistry.registerModelLayer(TankRenderer.FLUID_LOCATION, TankRenderer::createLayer);
		BlockEntityRenderers.register(DrifterBlocks.TANK_BLOCK_ENTITY.get(), TankRenderer::new);

	}

}