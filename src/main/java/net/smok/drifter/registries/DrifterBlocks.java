package net.smok.drifter.registries;

import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import earth.terrarium.adastra.common.blocks.lamps.IndustrialLampBlock;
import earth.terrarium.adastra.common.config.MachineConfig;
import earth.terrarium.botarium.common.registry.RegistryHelpers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.smok.drifter.blocks.alert.AlertLampBlock;
import net.smok.drifter.blocks.alert.AlertLampBlockEntity;
import net.smok.drifter.blocks.alert.AlertPanelBlock;
import net.smok.drifter.blocks.alert.AlertPanelBlockEntity;
import net.smok.drifter.blocks.controller.ShipControllerBlock;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.blocks.engine.*;
import net.smok.drifter.blocks.structure.ShipStructureBlock;
import net.smok.drifter.blocks.structure.ShipStructureBlockEntity;
import net.smok.drifter.ShipConfig;
import org.jetbrains.annotations.NotNull;

public final class DrifterBlocks {
    public static void init() {
    }


    public static final ResourcefulRegistry<Block> BLOCKS = ResourcefulRegistries.create(BuiltInRegistries.BLOCK, Values.MOD_ID);
    public static final ResourcefulRegistry<BlockEntityType<?>> BLOCK_ENTITY_TYPES = ResourcefulRegistries.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Values.MOD_ID);


    @NotNull
    private static BlockBehaviour.Properties steelProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                .requiresCorrectToolForDrops().strength(5f, 12f).sound(SoundType.COPPER);
    }

    // Structure Block
    public static final RegistryEntry<ShipStructureBlock> SHIP_STRUCTURE_BLOCK = BLOCKS.register("ship_structure_block",
            () -> new ShipStructureBlock(steelProperties()));

    public static final RegistryEntry<BlockEntityType<ShipStructureBlockEntity>> SHIP_STRUCTURE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("ship_structure_block",
                    () -> RegistryHelpers.createBlockEntityType(ShipStructureBlockEntity::new, SHIP_STRUCTURE_BLOCK.get()));


    // Fluid tanks
    public static final RegistryEntry<TankBlock> STEEL_TANK_BLOCK = BLOCKS.register("steel_tank",
            () -> new TankBlock(steelProperties(), MachineConfig.steelTierFluidCapacity));

    public static final RegistryEntry<TankBlock> OSTRUM_TANK_BLOCK = BLOCKS.register("ostrum_tank",
            () -> new TankBlock(steelProperties(), MachineConfig.ostrumTierFluidCapacity));

    public static final RegistryEntry<TankBlock> DESH_TANK_BLOCK = BLOCKS.register("desh_tank",
            () -> new TankBlock(steelProperties(), MachineConfig.deshTierFluidCapacity));

    public static final RegistryEntry<TankBlock> CALORITE_TANK_BLOCK = BLOCKS.register("calorite_tank",
            () -> new TankBlock(steelProperties(), MachineConfig.steelTierFluidCapacity * 5));


    public static final RegistryEntry<BlockEntityType<TankBlockEntity>> TANK_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("tank_block_entity",
                    () -> RegistryHelpers.createBlockEntityType(TankBlockEntity::new,
                            STEEL_TANK_BLOCK.get(), OSTRUM_TANK_BLOCK.get(), DESH_TANK_BLOCK.get(), CALORITE_TANK_BLOCK.get()));

    // Engine Nuzzles
    public static final RegistryEntry<EngineNozzleBlock> STEEL_NUZZLE_BLOCK = BLOCKS.register("steel_engine_nuzzle",
            () -> new EngineNozzleBlock(steelProperties(), ShipConfig.startSpeed() / 16));

    public static final RegistryEntry<EngineNozzleBlock> DESH_NUZZLE_BLOCK = BLOCKS.register("desh_engine_nuzzle",
            () -> new EngineNozzleBlock(steelProperties(), ShipConfig.startSpeed() / 8));

    public static final RegistryEntry<EngineNozzleBlock> OSTRUM_NUZZLE_BLOCK = BLOCKS.register("ostrum_engine_nuzzle",
            () -> new EngineNozzleBlock(steelProperties(), ShipConfig.startSpeed() / 4));

    public static final RegistryEntry<EngineNozzleBlock> CALORITE_NUZZLE_BLOCK = BLOCKS.register("calorite_engine_nuzzle",
            () -> new EngineNozzleBlock(steelProperties(), ShipConfig.startSpeed() / 2));


    // Engine block
    public static final RegistryEntry<EnginePanelBlock> ENGINE_PANEL_BLOCK = BLOCKS.register("engine_panel", () ->
            new EnginePanelBlock(steelProperties()));

    public static final RegistryEntry<BlockEntityType<EnginePanelBlockEntity>> ENGINE_PANEL_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("engine_panel",
                    () -> RegistryHelpers.createBlockEntityType(EnginePanelBlockEntity::new, ENGINE_PANEL_BLOCK.get()));


    // Alert System Block
    public static final RegistryEntry<AlertPanelBlock> ALERT_PANEL_BLOCK = BLOCKS.register("alert_panel",
            () -> new AlertPanelBlock(steelProperties()));

    public static final RegistryEntry<BlockEntityType<AlertPanelBlockEntity>> ALERT_PANEL_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("alert_panel",
                    () -> RegistryHelpers.createBlockEntityType(AlertPanelBlockEntity::new, ALERT_PANEL_BLOCK.get()));


    // Controller
    public static final RegistryEntry<Block> SHIP_CONTROLLER = BLOCKS.register("ship_controller",
            () -> new ShipControllerBlock(steelProperties()));

    public static final RegistryEntry<BlockEntityType<ShipControllerBlockEntity>> SHIP_CONTROLLER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("ship_controller", () ->
                    RegistryHelpers.createBlockEntityType(ShipControllerBlockEntity::new, SHIP_CONTROLLER.get()));


    // Alert lamp
    public static final RegistryEntry<Block> ALERT_LUMP = BLOCKS.register("alert_lamp",
            () -> new AlertLampBlock(new AlertLampBlock.LampShape(
                    IndustrialLampBlock.NORTH_SHAPE, IndustrialLampBlock.EAST_SHAPE, IndustrialLampBlock.SOUTH_SHAPE,
                    IndustrialLampBlock.WEST_SHAPE, IndustrialLampBlock.UP_SHAPE_X, IndustrialLampBlock.UP_SHAPE_Z,
                    IndustrialLampBlock.DOWN_SHAPE_X, IndustrialLampBlock.DOWN_SHAPE_Z),
                    steelProperties().lightLevel(value -> value.getValue(AlertLampBlock.COLOR) > 0 ? 7 : 0)));

    public static final RegistryEntry<BlockEntityType<AlertLampBlockEntity>> ALERT_LAMP_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("alert_lamp", () -> RegistryHelpers.createBlockEntityType(AlertLampBlockEntity::new, ALERT_LUMP.get()));


}
