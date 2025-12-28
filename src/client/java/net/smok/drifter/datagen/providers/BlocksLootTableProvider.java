package net.smok.drifter.datagen.providers;

import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.smok.drifter.blocks.garden.CropBlock;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.registries.DrifterItems;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlocksLootTableProvider extends FabricBlockLootTableProvider {

    private static final Set<Block> SELF_DROP = Stream.of(
            DrifterBlocks.STEEL_TANK_BLOCK,
            DrifterBlocks.DESH_TANK_BLOCK,
            DrifterBlocks.OSTRUM_TANK_BLOCK,
            DrifterBlocks.CALORITE_TANK_BLOCK,
            DrifterBlocks.STEEL_NUZZLE_BLOCK,
            DrifterBlocks.DESH_NUZZLE_BLOCK,
            DrifterBlocks.OSTRUM_NUZZLE_BLOCK,
            DrifterBlocks.CALORITE_NUZZLE_BLOCK,
            DrifterBlocks.SHIP_CONTROLLER,
            DrifterBlocks.ENGINE_PANEL_BLOCK,
            DrifterBlocks.SHIP_STRUCTURE_BLOCK,
            DrifterBlocks.MOON_FARM,
            DrifterBlocks.ALERT_PANEL_BLOCK,
            DrifterBlocks.ALERT_LAMP,
            DrifterBlocks.OIL_SLUDGE,
            DrifterBlocks.CRYO_SLUDGE,
            DrifterBlocks.BIO_SLUDGE
    ).map(RegistryEntry::get).collect(Collectors.toSet());

    public BlocksLootTableProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate() {
        SELF_DROP.forEach(this::dropSelf);
        dropAsPotato(DrifterBlocks.POTATOES.get(), Items.POTATO);
        dropAsPotato(DrifterBlocks.CARROTS.get(), Items.CARROT);
        dropAsWheat(DrifterBlocks.FROST_WHEAT.get(), Items.WHEAT, DrifterItems.FROST_WHEAT_SEEDS.get());
        dropAsWheat(DrifterBlocks.MARTIAN_MANDRAKE.get(), DrifterItems.MARTIAN_MANDRAKE.get(), DrifterItems.MARTIAN_MANDRAKE_SEEDS.get());
    }
    

    public void dropAsPotato(CropBlock cropBlock, Item fruit) {
        LootItemBlockStatePropertyCondition.Builder builder = LootItemBlockStatePropertyCondition.hasBlockStateProperties(cropBlock);
        builder.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(cropBlock.getAgeProperty(), cropBlock.getMaxAge()));
        LootTable.Builder builder1 = LootTable.lootTable().withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(fruit)))
                .withPool(LootPool.lootPool().when(builder)
                        .add(LootItem.lootTableItem(fruit).apply(ApplyBonusCount
                                .addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286F, 3))));
        add(cropBlock, builder1);
    }

    public void dropAsWheat(CropBlock cropBlock, Item fruit, Item seeds) {
        LootItemBlockStatePropertyCondition.Builder builder = LootItemBlockStatePropertyCondition.hasBlockStateProperties(cropBlock);
        builder.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(cropBlock.getAgeProperty(), cropBlock.getMaxAge()));
        add(cropBlock, createCropDrops(cropBlock, fruit, seeds, builder));
    }

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
        this.generate();

        for (Map.Entry<ResourceLocation, LootTable.Builder> entry : map.entrySet()) {
            ResourceLocation id = entry.getKey();
            LootTable.Builder builder = entry.getValue();
            biConsumer.accept(id, builder);
        }
    }
}
