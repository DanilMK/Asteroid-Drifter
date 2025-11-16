package net.smok.drifter;

import com.mojang.authlib.GameProfile;
import earth.terrarium.adastra.api.systems.OxygenApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public final class DrifterRespawnLogic {


    /**
     * Return valid spawn block with oxygen and in room
     */
    public static @NotNull BlockPos fudgeSpawnLocationInjection(@NotNull ServerLevel level, @NotNull ServerPlayer player) {
        BlockPos spawnPos = level.getSharedSpawnPos();

        int i = Math.max(0, level.getServer().getSpawnRadius(level));
        int j = Mth.floor(level.getWorldBorder().getDistanceToBorder(spawnPos.getX(), spawnPos.getZ()));
        if (j < i) {
            i = j;
        }

        if (j <= 1) {
            i = 1;
        }

        long l = i * 2L + 1;
        long m = l * l;
        int k = m > 2147483647L ? Integer.MAX_VALUE : (int)m;
        int n = k <= 16 ? k - 1 : 17;
        int o = RandomSource.create().nextInt(k);

        for (int p = 0; p < k; p++) {
            int q = (o + n * p) % k;
            int r = q % (i * 2 + 1);
            int s = q / (i * 2 + 1);

            BlockPos blockPos2 = getOxygenPos(level, spawnPos.getX() + r - i, spawnPos.getZ() + s - i, player);
            if (blockPos2 != null) return blockPos2;
        }
        return spawnPos;
    }


    @Nullable
    private static BlockPos getOxygenPos(ServerLevel level, int x, int z, ServerPlayer player) {
        LevelChunk levelChunk = level.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        int x1 = x & 15;
        int z1 = z & 15;

        int spawnHeight = levelChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x1, z1);
        // Test on void
        if (spawnHeight <= level.getMinBuildHeight()) return null;
        int nonAirHeight = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, x1, z1);

        // Test on water
        if (nonAirHeight <= spawnHeight && nonAirHeight > levelChunk.getHeight(Heightmap.Types.OCEAN_FLOOR, x1, z1)) return null;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        boolean hasCelling = false, hasOxygen = false;

        for (int k = spawnHeight + 1; k >= level.getMinBuildHeight(); k--) {
            mutableBlockPos.set(x, k, z);
            BlockState blockState = level.getBlockState(mutableBlockPos);


            if (hasOxygen && hasCelling && Block.isFaceFull(blockState.getCollisionShape(level, mutableBlockPos), Direction.UP)
                    && level.noCollision(player.getBoundingBox().move(mutableBlockPos)))
                return mutableBlockPos;


            hasOxygen = OxygenApi.API.hasOxygen(level, mutableBlockPos);
            if (!hasOxygen) hasCelling =  Block.isFaceFull(blockState.getCollisionShape(level, mutableBlockPos), Direction.DOWN);
        }

        return null;
    }



    // Change spawn world
    // The code belongs to Commoble : https://github.com/Commoble/respawn

    public static ResourceKey<Level> redirectPlayerListPlaceNewPlayerGetOverworld() {
        return DrifterConfig.startOnShip ? Values.ASTEROID_LEVEL : ServerLevel.OVERWORLD;
    }

    public static void onPlayerListGetPlayerForLogin(PlayerList playerList, GameProfile profile, CallbackInfoReturnable<ServerPlayer> cir)
    {
        if (DrifterConfig.startOnShip)
        {
            MinecraftServer server = playerList.getServer();
            ServerLevel serverLevel = server.getLevel(Values.ASTEROID_LEVEL);

            if (serverLevel == null) errorOnBrokenLevel();
            else cir.setReturnValue(new ServerPlayer(server, serverLevel, profile));
        }
    }

    public static ServerLevel redirectPlayerListRespawnGetServerOverworld(MinecraftServer server) {

        if (DrifterConfig.startOnShip)
        {
            ServerLevel serverLevel = server.getLevel(Values.ASTEROID_LEVEL);

            if (serverLevel == null) errorOnBrokenLevel();
            else return serverLevel;
        }
        return server.overworld(); // if we don't want to redirect, fall back to vanilla
    }

    private static void errorOnBrokenLevel() {
        Debug.err("Cannot respawn in " + Values.ASTEROID_LEVEL + ", this dimension was broken.");
    }
}
