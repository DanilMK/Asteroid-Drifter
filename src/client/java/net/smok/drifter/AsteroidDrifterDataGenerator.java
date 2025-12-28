package net.smok.drifter;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.smok.drifter.datagen.providers.*;

public class AsteroidDrifterDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider((fabricDataOutput, completableFuture) -> new RecipeDataProvider(fabricDataOutput));
		pack.addProvider((fabricDataOutput, completableFuture) -> new BlocksLootTableProvider(fabricDataOutput));
		pack.addProvider((fabricDataOutput, completableFuture) -> new EntityLootTableProvider(fabricDataOutput));
		pack.addProvider(BlockTagsProvider::new);
		pack.addProvider(ItemTagsProvider::new);
	}
}
