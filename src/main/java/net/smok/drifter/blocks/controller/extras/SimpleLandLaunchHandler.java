package net.smok.drifter.blocks.controller.extras;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.smok.drifter.blocks.controller.LandLaunchHandler;
import net.smok.drifter.recipies.AsteroidRecipe;

public class SimpleLandLaunchHandler implements LandLaunchHandler {

    public final int blockOffset;
    private final BlockPos controllerPos;
    private final int max = 4;
    private final int chunkSize = 16;

    private int cx;
    private int cy;
    private int cz;

    public SimpleLandLaunchHandler(BlockPos controllerPos, int blockOffset) {
        this.controllerPos = controllerPos;
        this.blockOffset = blockOffset;
        cx = max + 1;
        cy = max + 1;
        cz = max + 1;
    }

    @Override
    public void placeOnLand(ServerLevel serverLevel, AsteroidRecipe recipe) {
        recipe.feature().ifPresent(s ->
                serverLevel.registryAccess().registry(Registries.CONFIGURED_FEATURE).ifPresent(configuredFeatures -> {
                    ConfiguredFeature<?, ?> configuredFeature = configuredFeatures.get(s);
                    if (configuredFeature != null) {
                        BlockPos place = this.controllerPos.below(blockOffset);
                        //serverLevel.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(place), 1, place);
                        configuredFeature.place(serverLevel, serverLevel.getChunkSource().getGenerator(), serverLevel.random, place);
                    }
                })
        );

        recipe.structure().ifPresent(s -> {
            StructureTemplate template = serverLevel.getStructureManager().getOrCreate(s);
            Vec3i size = template.getSize();
            Vec3i offset = new Vec3i(-size.getX() / 2, -size.getY() - blockOffset, -size.getZ() / 2);
            BlockPos place = controllerPos.offset(offset);
            //serverLevel.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(place), 1, place);
            template.placeInWorld(serverLevel, place, place, new StructurePlaceSettings(), serverLevel.random, 2);
        });
    }

    @Override
    public void destroyOnLaunch(ServerLevel serverLevel) {
        if (cx > max || cy > max || cz > max) return;
        BlockPos point = controllerPos.offset(cx * chunkSize, 0, cz * chunkSize);
        ChunkAccess chunk = serverLevel.getChunk(point);
        ChunkPos chunkPos = chunk.getPos();
        BlockState air = Blocks.AIR.defaultBlockState();
        int dy = controllerPos.getY() + (cy - max) * chunkSize - blockOffset - chunkSize;

        for (int x = 0; x < chunkSize; x++) {
            for (int y = 0; y < chunkSize; y++) {
                for (int z = 0; z < chunkSize; z++) {
                    BlockPos pos = chunkPos.getBlockAt(x, y + dy, z);
                    BlockEntity blockEntity = chunk.getBlockEntity(pos);
                    if (blockEntity instanceof Container container) {
                        container.clearContent();
                    }
                    serverLevel.setBlock(pos, air, 3);
                }
            }
        }

        cx++;
        if (cx > max) {
            cx = -max;
            cz++;
        }
        if (cz > max) {
            cz = -max;
            cy++;
        }

    }

    @Override
    public void startDestroy() {
        cx = -max;
        cy = -max;
        cz = -max;
    }
}
