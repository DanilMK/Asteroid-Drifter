package net.smok.drifter.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.smok.drifter.registries.Values;
import net.smok.drifter.blocks.alert.AlertPanelBlockEntity;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class NetworkHandler {
    public static void init() {
        CONTROLLER_LAUNCH.register();
        CONTROLLER_MOVE_SHIP.register();
        ALERT_TEST_DANGER.register();
        ALERT_CHANGE_DANGER_COLOR.register();
        CREATIVE_SHIP_CONTROL.register();
    }

    public static final ServerBoundPackets.@NotNull ServerBoundPacket4<BlockPos, BlockPos, Boolean, Boolean> SHIP_STRUCTURE_COMMIT =
            ServerBoundPackets.of(new ResourceLocation(Values.MOD_ID, "ship_structure_commit"),
                    ServerBoundPackets.BLOCK_POS_CODEC, ServerBoundPackets.BLOCK_POS_CODEC, ServerBoundPackets.BOOLEAN_CODEC, ServerBoundPackets.BOOLEAN_CODEC,
                        (server, player, level, value1, value2, value3, value4) -> level.getBlockEntity(value1, DrifterBlocks.SHIP_STRUCTURE_BLOCK_ENTITY.get())
                                .ifPresent(block -> block.setFromMenu(player, value2, value3, value4))
            ).register();



    public static final BiRegisteredServerReceiver<BlockPos, Integer> CREATIVE_SHIP_CONTROL =
            new BiRegisteredServerReceiver<>(new ResourceLocation(Values.MOD_ID, "creative_ship_control")) {
        @Override
        public BlockPos decode(FriendlyByteBuf buf) {
            return buf.readBlockPos();
        }

        @Override
        public Integer decodeSecond(FriendlyByteBuf buf) {
            return buf.readInt();
        }

        @Override
        public FriendlyByteBuf encode(BlockPos value, Integer value2) {
            FriendlyByteBuf friendlyByteBuf = PacketByteBufs.create().writeBlockPos(value);
            friendlyByteBuf.writeInt(value2);
            return friendlyByteBuf;
        }

        @Override
        public void receive(MinecraftServer server, ServerPlayer player, Level level, ServerGamePacketListenerImpl handler, BlockPos blockPos, Integer value2) {
            if (player.isCreative()) {

                Optional<ShipControllerBlockEntity> blockEntity = level.getChunkAt(blockPos).getBlockEntity(blockPos, DrifterBlocks.SHIP_CONTROLLER_BLOCK_ENTITY.get());
                blockEntity.ifPresent(controller -> controller.creativeControl(value2));
            }
        }
    };

    public static final BiRegisteredServerReceiver<BlockPos, Integer> CONTROLLER_LAUNCH = new BiRegisteredServerReceiver<>(new ResourceLocation(Values.MOD_ID, "controller_launch")) {
        @Override
        public BlockPos decode(FriendlyByteBuf buf) {
            return buf.readBlockPos();
        }

        @Override
        public Integer decodeSecond(FriendlyByteBuf buf) {
            return buf.readInt();
        }

        @Override
        public FriendlyByteBuf encode(BlockPos value, Integer value2) {
            FriendlyByteBuf friendlyByteBuf = PacketByteBufs.create().writeBlockPos(value);
            friendlyByteBuf.writeInt(value2);
            return friendlyByteBuf;
        }

        @Override
        public void receive(MinecraftServer server, ServerPlayer player, Level level, ServerGamePacketListenerImpl handler, BlockPos value, Integer value2) {
            Optional<ShipControllerBlockEntity> blockEntity = level.getChunkAt(value).getBlockEntity(value, DrifterBlocks.SHIP_CONTROLLER_BLOCK_ENTITY.get());
            blockEntity.ifPresent(controllerBlock -> controllerBlock.launch(value2));
        }
    };

    public static final BiRegisteredServerReceiver<BlockPos, Integer> CONTROLLER_MOVE_SHIP =
            new BiRegisteredServerReceiver<>(new ResourceLocation(Values.MOD_ID, "controller_move_ship_launch")) {

        @Override
        public BlockPos decode(FriendlyByteBuf buf) {
            return buf.readBlockPos();
        }

        @Override
        public Integer decodeSecond(FriendlyByteBuf buf) {
            return buf.readInt();
        }

        @Override
        public FriendlyByteBuf encode(BlockPos value, Integer value2) {
            FriendlyByteBuf friendlyByteBuf = PacketByteBufs.create().writeBlockPos(value);
            friendlyByteBuf.writeInt(value2);
            return friendlyByteBuf;
        }

        @Override
        public void receive(MinecraftServer server, ServerPlayer player, Level level, ServerGamePacketListenerImpl handler, BlockPos value, Integer value2) {
            Optional<ShipControllerBlockEntity> blockEntity = level.getChunkAt(value).getBlockEntity(value, DrifterBlocks.SHIP_CONTROLLER_BLOCK_ENTITY.get());
            blockEntity.ifPresent(controllerBlock -> controllerBlock.moveShip(value2));
        }
    };

    public static final BiRegisteredServerReceiver<BlockPos, Integer> ALERT_TEST_DANGER =
            new BiRegisteredServerReceiver<>(new ResourceLocation(Values.MOD_ID, "alert_system_test_danger")) {

        @Override
        public BlockPos decode(FriendlyByteBuf buf) {
            return buf.readBlockPos();
        }

        @Override
        public Integer decodeSecond(FriendlyByteBuf buf) {
            return buf.readInt();
        }

        @Override
        public FriendlyByteBuf encode(BlockPos value, Integer value2) {
            FriendlyByteBuf friendlyByteBuf = PacketByteBufs.create().writeBlockPos(value);
            friendlyByteBuf.writeInt(value2);
            return friendlyByteBuf;
        }

        @Override
        public void receive(MinecraftServer server, ServerPlayer player, Level level, ServerGamePacketListenerImpl handler, BlockPos value, Integer value2) {
            Optional<AlertPanelBlockEntity> blockEntity = level.getChunkAt(value).getBlockEntity(value, DrifterBlocks.ALERT_PANEL_BLOCK_ENTITY.get());
            blockEntity.ifPresent(alertSystemBlock -> alertSystemBlock.testDanger(value2));
        }
    };

    public static final TriRegisteredServerReceiver<BlockPos, Integer, Integer> ALERT_CHANGE_DANGER_COLOR =
            new TriRegisteredServerReceiver<>(new ResourceLocation(Values.MOD_ID, "alert_system_change_danger_color")) {

        @Override
        public BlockPos decode(FriendlyByteBuf buf) {
            return buf.readBlockPos();
        }

        @Override
        public Integer decodeSecond(FriendlyByteBuf buf) {
            return buf.readInt();
        }

        @Override
        public Integer decodeThird(FriendlyByteBuf buf) {
            return buf.readInt();
        }

                @Override
        public FriendlyByteBuf encode(BlockPos value, Integer danger, Integer color) {
            FriendlyByteBuf friendlyByteBuf = PacketByteBufs.create().writeBlockPos(value);
            friendlyByteBuf.writeInt(danger);
            friendlyByteBuf.writeInt(color);
            return friendlyByteBuf;
        }

        @Override
        public void receive(MinecraftServer server, ServerPlayer player, Level level, ServerGamePacketListenerImpl handler, BlockPos value, Integer value2, Integer value3) {
            Optional<AlertPanelBlockEntity> blockEntity = level.getChunkAt(value).getBlockEntity(value, DrifterBlocks.ALERT_PANEL_BLOCK_ENTITY.get());
            blockEntity.ifPresent(alertSystemBlock -> alertSystemBlock.changeColor(value2, value3));
        }
    };




}
