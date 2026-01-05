package net.smok.drifter.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.smok.drifter.blocks.alert.AlertSound;
import net.smok.drifter.blocks.alert.Detector;
import net.smok.drifter.blocks.alert.Icon;
import net.smok.drifter.registries.Values;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class NetworkHandler {
    public static void init() {
        CONTROLLER_LAUNCH.register();
        CONTROLLER_MOVE_SHIP.register();
        CREATIVE_SHIP_CONTROL.register();
    }

    public static final ServerBoundPackets.@NotNull ServerBoundPacket4<BlockPos, BlockPos, Boolean, Boolean> SHIP_STRUCTURE_COMMIT =
            ServerBoundPackets.of(new ResourceLocation(Values.MOD_ID, "ship_structure_commit"),
                    ServerBoundPackets.BLOCK_POS_CODEC, ServerBoundPackets.BLOCK_POS_CODEC, ServerBoundPackets.BOOLEAN_CODEC, ServerBoundPackets.BOOLEAN_CODEC,
                        (server, player, level, value1, value2, value3, value4) -> level.getBlockEntity(value1, DrifterBlocks.SHIP_STRUCTURE_BLOCK_ENTITY.get())
                                .ifPresent(block -> {
                                    if (Container.stillValidBlockEntity(block, player)) block.setFromMenu(player, value2, value3, value4);
                                })
            ).register();

    public static final ServerBoundPackets.ServerBoundPacket3<BlockPos, Integer, String> DETECTOR_NAME =
            ServerBoundPackets.of(new ResourceLocation(Values.MOD_ID, "detector_name"), ServerBoundPackets.BLOCK_POS_CODEC,
                    ServerBoundPackets.INTEGER_CODEC, ServerBoundPackets.STRING_CODEC,
                    (server, player, level, value1, value2, value3) -> {
                        BlockEntity blockEntity = level.getBlockEntity(value1);
                        if (blockEntity instanceof Detector detector && Container.stillValidBlockEntity(blockEntity, player))
                            detector.setName(value2, value3);
                    }).register();

    public static final ServerBoundPackets.ServerBoundPacket3<BlockPos, Integer, Icon> DETECTOR_ICON =
            ServerBoundPackets.of(new ResourceLocation(Values.MOD_ID, "detector_icon"), ServerBoundPackets.BLOCK_POS_CODEC,
                    ServerBoundPackets.INTEGER_CODEC, Icon.BYTE_CODEC,
                    (server, player, level, value1, value2, value3) -> {
                        BlockEntity blockEntity = level.getBlockEntity(value1);
                        if (blockEntity instanceof Detector detector && Container.stillValidBlockEntity(blockEntity, player))
                            detector.setIcon(value2, value3);
                    }).register();

    public static final ServerBoundPackets.ServerBoundPacket3<BlockPos, Integer, AlertSound> DETECTOR_SOUND =
            ServerBoundPackets.of(new ResourceLocation(Values.MOD_ID, "detector_sound"), ServerBoundPackets.BLOCK_POS_CODEC,
                    ServerBoundPackets.INTEGER_CODEC, AlertSound.BYTE_CODEC,
                    (server, player, level, value1, value2, value3) -> {
                        BlockEntity blockEntity = level.getBlockEntity(value1);
                        if (blockEntity instanceof Detector detector && Container.stillValidBlockEntity(blockEntity, player))
                            detector.setSound(value2, value3);
                    }).register();

    public static final ServerBoundPackets.ServerBoundPacket3<BlockPos, Integer, Integer> DETECTOR_MOVE =
            ServerBoundPackets.of(new ResourceLocation(Values.MOD_ID, "detector_priority"), ServerBoundPackets.BLOCK_POS_CODEC,
                    ServerBoundPackets.INTEGER_CODEC, ServerBoundPackets.INTEGER_CODEC,
                    (server, player, level, value1, value2, value3) -> {
                        BlockEntity blockEntity = level.getBlockEntity(value1);
                        if (blockEntity instanceof Detector detector && Container.stillValidBlockEntity(blockEntity, player))
                            detector.moveAlert(value2, value3);
                    }).register();

    public static final ServerBoundPackets.ServerBoundPacket3<BlockPos, Integer, Boolean> DETECTOR_TEST =
            ServerBoundPackets.of(new ResourceLocation(Values.MOD_ID, "detector_test"), ServerBoundPackets.BLOCK_POS_CODEC,
                    ServerBoundPackets.INTEGER_CODEC, ServerBoundPackets.BOOLEAN_CODEC,
                    (server, player, level, value1, value2, value3) -> {
                        BlockEntity blockEntity = level.getBlockEntity(value1);
                        if (blockEntity instanceof Detector detector && Container.stillValidBlockEntity(blockEntity, player))
                            detector.setTest(value2, value3);
                    }).register();

    public static final ServerBoundPackets.ServerBoundPacket2<BlockPos, Integer> DETECTOR_MIN_SET =
            ServerBoundPackets.of(new ResourceLocation(Values.MOD_ID, "detector_min_set"), ServerBoundPackets.BLOCK_POS_CODEC, ServerBoundPackets.INTEGER_CODEC,
                    (server, player, level, value1, value2) -> level.getBlockEntity(value1, DrifterBlocks.DETECTOR_BLOCK_ENTITY.get()).ifPresent(
                            detector -> {
                                if (Container.stillValidBlockEntity(detector, player)) {
                                    detector.setMinSignal(value2);
                                }
                            }
                    )).register();

    public static final ServerBoundPackets.ServerBoundPacket2<BlockPos, Integer> DETECTOR_MAX_SET =
            ServerBoundPackets.of(new ResourceLocation(Values.MOD_ID, "detector_max_set"), ServerBoundPackets.BLOCK_POS_CODEC, ServerBoundPackets.INTEGER_CODEC,
                    (server, player, level, value1, value2) -> {
                        level.getBlockEntity(value1, DrifterBlocks.DETECTOR_BLOCK_ENTITY.get()).ifPresent(
                                detector -> {
                                    if (Container.stillValidBlockEntity(detector, player)) {
                                        detector.setMaxSignal(value2);
                                    }
                                }
                        );
                    }).register();


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



}
