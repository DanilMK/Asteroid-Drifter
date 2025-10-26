package net.smok.drifter;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import net.smok.drifter.registries.*;
import net.smok.drifter.network.NetworkHandler;

public class AsteroidDrifter implements ModInitializer {


	@Override
	public void onInitialize() {
		Values.init();
		NetworkHandler.init();
		ShipEventRegistries.init();
		ModifierRegistries.init();
		DrifterEntities.init();
		DrifterBlocks.init();
		DrifterItems.init();
		DrifterCreativeTab.TABS.init();

		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ShipEventRegistries.CollisionRegistration());

	}
}