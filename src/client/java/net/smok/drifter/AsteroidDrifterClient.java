package net.smok.drifter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.level.block.Block;
import net.smok.drifter.blocks.alert.AlertOverlay;
import net.smok.drifter.blocks.alert.AlertSystemScreen;
import net.smok.drifter.blocks.controller.ControllerBlockRenderer;
import net.smok.drifter.blocks.engine.TankRenderer;
import net.smok.drifter.blocks.garden.MoonFarmRenderer;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.blocks.controller.ShipControllerScreen;
import net.smok.drifter.blocks.engine.EngineScreen;
import net.smok.drifter.registries.DrifterEntities;
import net.smok.drifter.registries.DrifterMenus;
import net.smok.drifter.blocks.structure.ShipStructureBlockRenderer;
import net.smok.drifter.blocks.structure.ShipStructureBlockScreen;

public class AsteroidDrifterClient implements ClientModInitializer {

	private static final Block[] TRANSPARENT_BLOCKS = new Block[] {
			DrifterBlocks.ALERT_LUMP.get(),
			DrifterBlocks.CARROTS.get(),
			DrifterBlocks.POTATOES.get(),
			DrifterBlocks.FROST_WHEAT.get(),
			DrifterBlocks.STEEL_TANK_BLOCK.get(),
			DrifterBlocks.OSTRUM_TANK_BLOCK.get(),
			DrifterBlocks.DESH_TANK_BLOCK.get(),
			DrifterBlocks.CALORITE_TANK_BLOCK.get(),
			DrifterBlocks.MARTIAN_MANDRAKE.get()
	};

	@Override
	public void onInitializeClient() {
		MenuScreens.register(DrifterMenus.SHIP_CONTROLLER_MENU.get(), ShipControllerScreen::new);
		MenuScreens.register(DrifterMenus.ALERT_SYSTEM_MENU.get(), AlertSystemScreen::new);
		MenuScreens.register(DrifterMenus.ENGINE_MENU.get(), EngineScreen::new);
		MenuScreens.register(DrifterMenus.SHIP_STRUCTURE_MENU.get(), ShipStructureBlockScreen::new);

		HudRenderCallback.EVENT.register(new AlertOverlay());

		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), TRANSPARENT_BLOCKS);

		EntityModelLayerRegistry.registerModelLayer(TankRenderer.FLUID_LOCATION, TankRenderer::createLayer);
		EntityModelLayerRegistry.registerModelLayer(ControllerBlockRenderer.MODEL_LOCATION, ControllerBlockRenderer::createLayerDefinition);
		EntityModelLayerRegistry.registerModelLayer(MartianMandrakeModel.LAYER_LOCATION, MartianMandrakeModel::createBodyLayer);

		BlockEntityRenderers.register(DrifterBlocks.TANK_BLOCK_ENTITY.get(), TankRenderer::new);
		BlockEntityRenderers.register(DrifterBlocks.SHIP_CONTROLLER_BLOCK_ENTITY.get(), ControllerBlockRenderer::new);
		BlockEntityRenderers.register(DrifterBlocks.SHIP_STRUCTURE_BLOCK_ENTITY.get(), context -> new ShipStructureBlockRenderer());
		BlockEntityRenderers.register(DrifterBlocks.MOON_FARM_BLOCK_ENTITY.get(), MoonFarmRenderer::new);
		EntityRendererRegistry.register(DrifterEntities.MARTIAN_MANDRAKE.get(), MartianMandrakeModel::getMobRenderer);

		EntityRendererRegistry.register(DrifterEntities.COLLIDED_ASTEROID.get(), context -> new ThrownItemRenderer<>(context, 3.0F, true));
		//EntityRendererRegistry.register(DrifterEntities.MAGNETIC_FIELD.get(), MagneticFieldRenderer::new);

	}

}