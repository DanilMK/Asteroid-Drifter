package net.smok.drifter;

import net.fabricmc.api.ModInitializer;

import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.registries.DrifterItems;
import net.smok.drifter.network.NetworkHandler;
import net.smok.drifter.registries.Values;

public class AsteroidDrifter implements ModInitializer {


	@Override
	public void onInitialize() {
		Values.init();
		NetworkHandler.init();
		DrifterBlocks.init();
		DrifterItems.init();
		DrifterCreativeTab.TABS.init();

	}
}