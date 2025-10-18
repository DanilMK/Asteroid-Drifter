package net.smok.drifter.registries;

import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import earth.terrarium.adastra.common.config.MachineConfig;
import earth.terrarium.botarium.common.registry.fluid.FluidBucketItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.smok.drifter.Debug;
import net.smok.drifter.items.ConfigureTool;
import net.smok.drifter.items.FuelTank;

@SuppressWarnings("unused")
public final class DrifterItems {
    public static void init() {
        Debug.log("Asteroid Drifter Items Loaded!");
    }

    public static final ResourcefulRegistry<Item> ITEMS = ResourcefulRegistries.create(BuiltInRegistries.ITEM, Values.MOD_ID);

    public static final RegistryEntry<Item> ALERT_PANEL = ITEMS.register("alert_panel", () ->
            new BlockItem(DrifterBlocks.ALERT_PANEL_BLOCK.get(), new Item.Properties()));

    public static final RegistryEntry<Item> SHIP_FUEL_BUCKET = ITEMS.register("ship_fuel_bucket", () ->
            new FluidBucketItem(Values.SHIP_FUEL, (new Item.Properties()).craftRemainder(net.minecraft.world.item.Items.BUCKET)
                    .stacksTo(1)));

    public static final RegistryEntry<Item> ALERT_LAMP = ITEMS.register("alert_lamp", () ->
            new BlockItem(DrifterBlocks.ALERT_LUMP.get(), new Item.Properties()));

    public static final RegistryEntry<Item> SHIP_CONTROLLER = ITEMS.register("ship_controller", () ->
            new BlockItem(DrifterBlocks.SHIP_CONTROLLER.get(), new Item.Properties()));

    public static final RegistryEntry<Item> ENGINE_PANEL = ITEMS.register("engine_panel", () ->
            new BlockItem(DrifterBlocks.ENGINE_PANEL_BLOCK.get(), new Item.Properties()));

    public static final RegistryEntry<Item> SHIP_STRUCTURE = ITEMS.register("ship_structure_block", () ->
            new BlockItem(DrifterBlocks.SHIP_STRUCTURE_BLOCK.get(), new Item.Properties()));



    public static final RegistryEntry<Item> SMALL_ASTEROID = ITEMS.register("small_asteroid", () ->
            new Item(new Item.Properties()));

    public static final RegistryEntry<Item> MEDIUM_ASTEROID = ITEMS.register("medium_asteroid", () ->
            new Item(new Item.Properties()));

    public static final RegistryEntry<Item> LARGE_ASTEROID = ITEMS.register("large_asteroid", () ->
            new Item(new Item.Properties()));

    public static final RegistryEntry<Item> SMALL_ORE_ASTEROID = ITEMS.register("small_ore_asteroid", () ->
            new Item(new Item.Properties()));

    public static final RegistryEntry<Item> MEDIUM_ORE_ASTEROID = ITEMS.register("medium_ore_asteroid", () ->
            new Item(new Item.Properties()));

    public static final RegistryEntry<Item> LARGE_ORE_ASTEROID = ITEMS.register("large_ore_asteroid", () ->
            new Item(new Item.Properties()));


    public static final RegistryEntry<ConfigureTool> CONFIGURE_TOOL = ITEMS.register("configure_tool", () ->
            new ConfigureTool(new Item.Properties()));


    public static final RegistryEntry<FuelTank> FUEL_TANK = ITEMS.register("fuel_tank", () ->
            new FuelTank(new Item.Properties(), MachineConfig.steelTierFluidCapacity));


    public static final RegistryEntry<BlockItem> STEEL_TANK = ITEMS.register("steel_tank", () -> 
            new BlockItem(DrifterBlocks.STEEL_TANK_BLOCK.get(), new Item.Properties()));

    public static final RegistryEntry<BlockItem> DESH_TANK = ITEMS.register("desh_tank", () ->
            new BlockItem(DrifterBlocks.DESH_TANK_BLOCK.get(), new Item.Properties()));

    public static final RegistryEntry<BlockItem> OSTRUM_TANK = ITEMS.register("ostrum_tank", () ->
            new BlockItem(DrifterBlocks.OSTRUM_TANK_BLOCK.get(), new Item.Properties()));

    public static final RegistryEntry<BlockItem> CALORITE_TANK = ITEMS.register("calorite_tank", () -> 
            new BlockItem(DrifterBlocks.CALORITE_TANK_BLOCK.get(), new Item.Properties()));


    public static final RegistryEntry<BlockItem> STEEL_ENGINE_NUZZLE = ITEMS.register("steel_engine_nuzzle", () ->
            new BlockItem(DrifterBlocks.STEEL_NUZZLE_BLOCK.get(), new Item.Properties()));

    public static final RegistryEntry<BlockItem> DESH_ENGINE_NUZZLE = ITEMS.register("desh_engine_nuzzle", () ->
            new BlockItem(DrifterBlocks.DESH_NUZZLE_BLOCK.get(), new Item.Properties()));

    public static final RegistryEntry<BlockItem> OSTRUM_ENGINE_NUZZLE = ITEMS.register("ostrum_engine_nuzzle", () ->
            new BlockItem(DrifterBlocks.OSTRUM_NUZZLE_BLOCK.get(), new Item.Properties()));

    public static final RegistryEntry<BlockItem> CALORITE_ENGINE_NUZZLE = ITEMS.register("calorite_engine_nuzzle", () ->
            new BlockItem(DrifterBlocks.CALORITE_NUZZLE_BLOCK.get(), new Item.Properties()));
}