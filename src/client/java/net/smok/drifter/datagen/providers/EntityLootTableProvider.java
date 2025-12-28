package net.smok.drifter.datagen.providers;

import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.smok.drifter.registries.DrifterEntities;
import net.smok.drifter.registries.DrifterItems;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class EntityLootTableProvider extends SimpleFabricLootTableProvider {



    private final Map<RegistryEntry<?>, LootTable.Builder> map = new HashMap<>();

    public EntityLootTableProvider(FabricDataOutput output) {
        super(output, LootContextParamSets.ENTITY);
    }

    public void generate() {
        add(DrifterEntities.MARTIAN_MANDRAKE,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1f))
                                .add(LootItem.lootTableItem(DrifterItems.MARTIAN_MANDRAKE.get()))
                        ).withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(1f, 3f))
                                .add(LootItem.lootTableItem(DrifterItems.MARTIAN_MANDRAKE_SEEDS.get()).apply(
                                        LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(1f, 3f))))
                        )
        );
    }

    private void add(RegistryEntry<?> entity, LootTable.Builder builder) {
        map.put(entity, builder);
    }


    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
        generate();

        for (Map.Entry<RegistryEntry<?>, LootTable.Builder> entry : map.entrySet()) {
            ResourceLocation id = new ResourceLocation(entry.getKey().getId().getNamespace(),
                    "entities/" + entry.getKey().getId().getPath());
            LootTable.Builder builder = entry.getValue();
            biConsumer.accept(id, builder);
        }
    }
}