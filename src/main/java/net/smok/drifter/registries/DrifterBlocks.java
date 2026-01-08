package net.smok.drifter.registries;

import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import earth.terrarium.adastra.common.blocks.lamps.IndustrialLampBlock;
import earth.terrarium.botarium.common.registry.RegistryHelpers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.smok.drifter.blocks.alert.*;
import net.smok.drifter.blocks.controller.ShipControllerBlock;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.blocks.engine.*;
import net.smok.drifter.blocks.garden.*;
import net.smok.drifter.blocks.structure.ShipStructureBlock;
import net.smok.drifter.blocks.structure.ShipStructureBlockEntity;
import net.smok.drifter.ShipConfig;
import org.jetbrains.annotations.NotNull;

public final class DrifterBlocks {


    /* Reminder
    1. Block
    2. Item
    3. Block Entity
    4. Lang's translation
    5. Texture
    6. Model
    7. BlockState
    8. Mineable Tag
    9. Loot Table
    10. Recipe
     */

    public static final ResourcefulRegistry<Block> BLOCKS = ResourcefulRegistries.create(BuiltInRegistries.BLOCK, Values.MOD_ID);
    public static final ResourcefulRegistry<BlockEntityType<?>> BLOCK_ENTITY_TYPES = ResourcefulRegistries.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Values.MOD_ID);


    @NotNull
    private static BlockBehaviour.Properties steelProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                .requiresCorrectToolForDrops()
                .strength(5f, 12f)
                .sound(SoundType.COPPER);
    }


    //Garden

    public static final BlockBehaviour.Properties CROP_PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .noCollission()
            .randomTicks()
            .instabreak()
            .sound(SoundType.CROP)
            .pushReaction(PushReaction.DESTROY);


    public static final RegistryEntry<FrostWheat> FROST_WHEAT = BLOCKS.register("frost_wheat",
            () -> new FrostWheat(CROP_PROPERTIES)
    );

    public static final RegistryEntry<MartianMandrakeBlock> MARTIAN_MANDRAKE = BLOCKS.register("martian_mandrake",
            () -> new MartianMandrakeBlock(CROP_PROPERTIES)
    );

    public static final RegistryEntry<CropBlock> CARROTS = BLOCKS.register("carrots",
            () -> new CropBlock(7, CropBlock.EIGHT_AGE_SHAPES, CROP_PROPERTIES));


    public static final RegistryEntry<CropBlock> POTATOES = BLOCKS.register("potatoes",
            () -> new CropBlock(7, CropBlock.EIGHT_AGE_SHAPES, CROP_PROPERTIES));


    public static final RegistryEntry<MoonFarmBlock> MOON_FARM = BLOCKS.register("moon_farm",
            () -> new MoonFarmBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.6F)
                    .sound(SoundType.COPPER)
                    .isSuffocating(MoonFarmBlock::hasSoil)
                    .isViewBlocking(MoonFarmBlock::hasSoil))
    );

    public static final RegistryEntry<BlockEntityType<MoonFarmBlockEntity>> MOON_FARM_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("moon_farm",
                    () -> RegistryHelpers.createBlockEntityType(MoonFarmBlockEntity::new, MOON_FARM.get()));

    // Structure Block
    public static final RegistryEntry<ShipStructureBlock> SHIP_STRUCTURE_BLOCK = BLOCKS.register("ship_structure_block",
            () -> new ShipStructureBlock(steelProperties()));

    public static final RegistryEntry<BlockEntityType<ShipStructureBlockEntity>> SHIP_STRUCTURE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("ship_structure_block",
                    () -> RegistryHelpers.createBlockEntityType(ShipStructureBlockEntity::new, SHIP_STRUCTURE_BLOCK.get()));


    // Fluid tanks
    public static final RegistryEntry<TankBlock> STEEL_TANK_BLOCK = BLOCKS.register("steel_tank",
            () -> new TankBlock(steelProperties(), 4000L));

    public static final RegistryEntry<TankBlock> OSTRUM_TANK_BLOCK = BLOCKS.register("ostrum_tank",
            () -> new TankBlock(steelProperties(), 12000L));

    public static final RegistryEntry<TankBlock> DESH_TANK_BLOCK = BLOCKS.register("desh_tank",
            () -> new TankBlock(steelProperties(), 18000L));

    public static final RegistryEntry<TankBlock> CALORITE_TANK_BLOCK = BLOCKS.register("calorite_tank",
            () -> new TankBlock(steelProperties(), 25000));


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
    public static final RegistryEntry<Block> ALERT_LAMP = BLOCKS.register("alert_lamp",
            () -> new AlertLampBlock(new AlertLampBlock.LampShape(
                    IndustrialLampBlock.NORTH_SHAPE, IndustrialLampBlock.EAST_SHAPE, IndustrialLampBlock.SOUTH_SHAPE,
                    IndustrialLampBlock.WEST_SHAPE, IndustrialLampBlock.UP_SHAPE_X, IndustrialLampBlock.UP_SHAPE_Z,
                    IndustrialLampBlock.DOWN_SHAPE_X, IndustrialLampBlock.DOWN_SHAPE_Z),
                    steelProperties().lightLevel(value -> value.getValue(AlertLampBlock.COLOR) > 0 ? 7 : 0)));


    // Sludge
    public static final RegistryEntry<Block> OIL_SLUDGE = BLOCKS.register("oil_sludge",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
            )
    );

    public static final RegistryEntry<Block> CRYO_SLUDGE = BLOCKS.register("cryo_sludge",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.ICE)
                    .instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
            )
    );

    public static final RegistryEntry<Block> BIO_SLUDGE = BLOCKS.register("bio_sludge",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL)
                    .instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
            )
    );


    // Alert Detector
    public static final RegistryEntry<DetectorBlock> DETECTOR_BLOCK = BLOCKS.register("detector",
            () -> new DetectorBlock(steelProperties()));

    public static final RegistryEntry<BlockEntityType<DetectorBlockEntity>> DETECTOR_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("detector",
                () -> RegistryHelpers.createBlockEntityType(DetectorBlockEntity::new, DETECTOR_BLOCK.get()));

}
