package net.smok.drifter.network;

import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ServerBoundPackets {

    @Contract("_, _, _ -> new")
    public static <V> @NotNull ServerBoundPacket1<V> of(ResourceLocation id, ByteCodec<V> codec1, ServerBoundPacket1.Action<V> action) {
        return new ServerBoundPacket1<>(id, codec1, action);
    } 
    
    @Contract("_, _, _, _ -> new")
    public static <V1, V2> @NotNull ServerBoundPacket2<V1, V2>
    of(ResourceLocation id,
       ByteCodec<V1> codec1,
       ByteCodec<V2> codec2,
       ServerBoundPacket2.Action<V1, V2> action) {
        return new ServerBoundPacket2<>(id, codec1, codec2, action);
    }

    @Contract("_, _, _, _, _ -> new")
    public static <V1, V2, V3> @NotNull ServerBoundPacket3<V1, V2, V3>
    of(ResourceLocation id,
       ByteCodec<V1> codec1,
       ByteCodec<V2> codec2,
       ByteCodec<V3> codec3,
       ServerBoundPacket3.Action<V1, V2, V3> action) {
        return new ServerBoundPacket3<>(id, codec1, codec2, codec3, action);
    }

    @Contract("_, _, _, _, _, _ -> new")
    public static <V1, V2, V3, V4> @NotNull ServerBoundPacket4<V1, V2, V3, V4>
    of(ResourceLocation id,
       ByteCodec<V1> codec1,
       ByteCodec<V2> codec2,
       ByteCodec<V3> codec3,
       ByteCodec<V4> codec4,
       ServerBoundPacket4.Action<V1, V2, V3, V4> action) {
        return new ServerBoundPacket4<>(id, codec1, codec2, codec3, codec4, action);
    }

    @Contract("_, _, _, _, _, _, _ -> new")
    public static <V1, V2, V3, V4, V5> @NotNull ServerBoundPacket5<V1, V2, V3, V4, V5>
    of(ResourceLocation id,
       ByteCodec<V1> codec1,
       ByteCodec<V2> codec2,
       ByteCodec<V3> codec3,
       ByteCodec<V4> codec4,
       ByteCodec<V5> codec5,
       ServerBoundPacket5.Action<V1, V2, V3, V4, V5> action) {
        return new ServerBoundPacket5<>(id, codec1, codec2, codec3, codec4, codec5, action);
    }

    public abstract static class ServerBoundPacket implements ServerPlayNetworking.PlayChannelHandler {
        protected final ResourceLocation id;

        protected ServerBoundPacket(ResourceLocation id) {
            this.id = id;
        }

        public ServerBoundPacket register() {
            ServerPlayNetworking.registerGlobalReceiver(id, this);
            return this;
        }

        public ResourceLocation getId() {
            return id;
        }
    }


    public static class ServerBoundPacket1<V> extends ServerBoundPacket {

        private final ByteCodec<V> codec1;
        private final Action<V> action;

        protected ServerBoundPacket1(ResourceLocation id, ByteCodec<V> codec1, Action<V> action) {
            super(id);
            this.codec1 = codec1;
            this.action = action;
        }

        @Override
        public ServerBoundPacket1<V> register() {
            super.register();
            return this;
        }

        @Override
        public void receive(@NotNull MinecraftServer minecraftServer, ServerPlayer serverPlayer, ServerGamePacketListenerImpl serverGamePacketListener, FriendlyByteBuf friendlyByteBuf, PacketSender packetSender) {
            V value1 = codec1.decode(friendlyByteBuf);
            minecraftServer.execute(() -> action.accept(minecraftServer, serverPlayer, serverPlayer.level(), value1));
        }

        public interface Action<V> {
            void accept(@NotNull MinecraftServer server, @NotNull ServerPlayer player, @NotNull Level level, @NotNull V value);
        }

        public ServerboundCustomPayloadPacket createPacket(@NotNull V value) {
            FriendlyByteBuf friendlyByteBuf = PacketByteBufs.create();
            codec1.encode(friendlyByteBuf, value);
            return new ServerboundCustomPayloadPacket(id, friendlyByteBuf);
        }
    }

    public static class ServerBoundPacket2<V1, V2> extends ServerBoundPacket {

        private final ByteCodec<V1> codec1;
        private final ByteCodec<V2> codec2;
        private final Action<V1, V2> action;

        protected ServerBoundPacket2(ResourceLocation id, ByteCodec<V1> codec1, ByteCodec<V2> codec2, Action<V1, V2> action) {
            super(id);
            this.codec1 = codec1;
            this.codec2 = codec2;
            this.action = action;
        }

        @Override
        public ServerBoundPacket2<V1, V2> register() {
            super.register();
            return this;
        }

        @Override
        public void receive(@NotNull MinecraftServer minecraftServer, ServerPlayer serverPlayer, ServerGamePacketListenerImpl serverGamePacketListener, FriendlyByteBuf friendlyByteBuf, PacketSender packetSender) {
            V1 value1 = codec1.decode(friendlyByteBuf);
            V2 value2 = codec2.decode(friendlyByteBuf);
            minecraftServer.execute(() -> action.accept(minecraftServer, serverPlayer, serverPlayer.level(), value1, value2));
        }

        public interface Action<V1, V2> {
            void accept(@NotNull MinecraftServer server, @NotNull ServerPlayer player, @NotNull Level level,
                        @NotNull V1 value1, @NotNull V2 value2);
        }

        public ServerboundCustomPayloadPacket createPacket(@NotNull V1 value1, @NotNull V2 value2) {
            FriendlyByteBuf friendlyByteBuf = PacketByteBufs.create();
            codec1.encode(friendlyByteBuf, value1);
            codec2.encode(friendlyByteBuf, value2);
            return new ServerboundCustomPayloadPacket(id, friendlyByteBuf);
        }
    }

    public static class ServerBoundPacket3<V1, V2, V3> extends ServerBoundPacket {

        private final ByteCodec<V1> codec1;
        private final ByteCodec<V2> codec2;
        private final ByteCodec<V3> codec3;
        private final Action<V1, V2, V3> action;

        protected ServerBoundPacket3(ResourceLocation id, ByteCodec<V1> codec1, ByteCodec<V2> codec2, ByteCodec<V3> codec3,
                                     Action<V1, V2, V3> action) {
            super(id);
            this.codec1 = codec1;
            this.codec2 = codec2;
            this.codec3 = codec3;
            this.action = action;
        }

        @Override
        public ServerBoundPacket3<V1, V2, V3> register() {
            super.register();
            return this;
        }

        @Override
        public void receive(@NotNull MinecraftServer minecraftServer, ServerPlayer serverPlayer,
                            ServerGamePacketListenerImpl serverGamePacketListener, FriendlyByteBuf friendlyByteBuf,
                            PacketSender packetSender) {
            V1 value1 = codec1.decode(friendlyByteBuf);
            V2 value2 = codec2.decode(friendlyByteBuf);
            V3 value3 = codec3.decode(friendlyByteBuf);

            minecraftServer.execute(() -> action.accept(minecraftServer, serverPlayer, serverPlayer.level(), value1, value2, value3));
        }

        public interface Action<V1, V2, V3> {
            void accept(@NotNull MinecraftServer server, @NotNull ServerPlayer player, @NotNull Level level,
                        @NotNull V1 value1, @NotNull V2 value2, @NotNull V3 value3);
        }

        public ServerboundCustomPayloadPacket createPacket(@NotNull V1 value1, @NotNull V2 value2, @NotNull V3 value3) {
            FriendlyByteBuf friendlyByteBuf = PacketByteBufs.create();
            codec1.encode(friendlyByteBuf, value1);
            codec2.encode(friendlyByteBuf, value2);
            codec3.encode(friendlyByteBuf, value3);
            return new ServerboundCustomPayloadPacket(id, friendlyByteBuf);
        }
    }

    public static class ServerBoundPacket4<V1, V2, V3, V4> extends ServerBoundPacket {

        private final ByteCodec<V1> codec1;
        private final ByteCodec<V2> codec2;
        private final ByteCodec<V3> codec3;
        private final ByteCodec<V4> codec4;
        private final Action<V1, V2, V3, V4> action;

        protected ServerBoundPacket4(ResourceLocation id, ByteCodec<V1> codec1, ByteCodec<V2> codec2,
                                     ByteCodec<V3> codec3, ByteCodec<V4> codec4, Action<V1, V2, V3, V4> action) {
            super(id);
            this.codec1 = codec1;
            this.codec2 = codec2;
            this.codec3 = codec3;
            this.codec4 = codec4;
            this.action = action;
        }

        @Override
        public ServerBoundPacket4<V1, V2, V3, V4> register() {
            super.register();
            return this;
        }

        @Override
        public void receive(@NotNull MinecraftServer minecraftServer, ServerPlayer serverPlayer,
                            ServerGamePacketListenerImpl serverGamePacketListener, FriendlyByteBuf friendlyByteBuf,
                            PacketSender packetSender) {
            V1 value1 = codec1.decode(friendlyByteBuf);
            V2 value2 = codec2.decode(friendlyByteBuf);
            V3 value3 = codec3.decode(friendlyByteBuf);
            V4 value4 = codec4.decode(friendlyByteBuf);

            minecraftServer.execute(() -> action.accept(minecraftServer, serverPlayer, serverPlayer.level(), value1, value2, value3, value4));
        }

        public interface Action<V1, V2, V3, V4> {
            void accept(@NotNull MinecraftServer server, @NotNull ServerPlayer player, @NotNull Level level,
                        @NotNull V1 value1, @NotNull V2 value2, @NotNull V3 value3, @NotNull V4 value4);
        }

        public ServerboundCustomPayloadPacket createPacket(@NotNull V1 value1, @NotNull V2 value2, @NotNull V3 value3, @NotNull V4 value4) {
            FriendlyByteBuf friendlyByteBuf = PacketByteBufs.create();
            codec1.encode(friendlyByteBuf, value1);
            codec2.encode(friendlyByteBuf, value2);
            codec3.encode(friendlyByteBuf, value3);
            codec4.encode(friendlyByteBuf, value4);
            return new ServerboundCustomPayloadPacket(id, friendlyByteBuf);
        }
    }

    public static class ServerBoundPacket5<V1, V2, V3, V4, V5> extends ServerBoundPacket {

        private final ByteCodec<V1> codec1;
        private final ByteCodec<V2> codec2;
        private final ByteCodec<V3> codec3;
        private final ByteCodec<V4> codec4;
        private final ByteCodec<V5> codec5;
        private final Action<V1, V2, V3, V4, V5> action;

        protected ServerBoundPacket5(ResourceLocation id, ByteCodec<V1> codec1, ByteCodec<V2> codec2, ByteCodec<V3> codec3,
                                     ByteCodec<V4> codec4, ByteCodec<V5> codec5, Action<V1, V2, V3, V4, V5> action) {
            super(id);
            this.codec1 = codec1;
            this.codec2 = codec2;
            this.codec3 = codec3;
            this.codec4 = codec4;
            this.codec5 = codec5;
            this.action = action;
        }

        @Override
        public ServerBoundPacket5<V1, V2, V3, V4, V5> register() {
            super.register();
            return this;
        }

        @Override
        public void receive(@NotNull MinecraftServer minecraftServer, ServerPlayer serverPlayer,
                            ServerGamePacketListenerImpl serverGamePacketListener, FriendlyByteBuf friendlyByteBuf,
                            PacketSender packetSender) {
            V1 value1 = codec1.decode(friendlyByteBuf);
            V2 value2 = codec2.decode(friendlyByteBuf);
            V3 value3 = codec3.decode(friendlyByteBuf);
            V4 value4 = codec4.decode(friendlyByteBuf);
            V5 value5 = codec5.decode(friendlyByteBuf);

            minecraftServer.execute(() -> action.accept(minecraftServer, serverPlayer, serverPlayer.level(), value1, value2, value3, value4, value5));
        }

        public interface Action<V1, V2, V3, V4, V5> {
            void accept(@NotNull MinecraftServer server, @NotNull ServerPlayer player, @NotNull Level level,
                        @NotNull V1 value1, @NotNull V2 value2, @NotNull V3 value3, @NotNull V4 value4, @NotNull V5 value5);
        }

        public ServerboundCustomPayloadPacket createPacket(@NotNull V1 value1, @NotNull V2 value2, @NotNull V3 value3, @NotNull V4 value4, @NotNull V5 value5) {
            FriendlyByteBuf friendlyByteBuf = PacketByteBufs.create();
            codec1.encode(friendlyByteBuf, value1);
            codec2.encode(friendlyByteBuf, value2);
            codec3.encode(friendlyByteBuf, value3);
            codec4.encode(friendlyByteBuf, value4);
            codec5.encode(friendlyByteBuf, value5);
            return new ServerboundCustomPayloadPacket(id, friendlyByteBuf);
        }
    }

    /**
     * Code and decode value in ByteBuf
     * @param <V> type of target value
     */
    public interface ByteCodec<V> {

        void encode(FriendlyByteBuf byteBuf, V value);
        V decode(FriendlyByteBuf byteBuf);
    }

    /**
     * Create ByteCodec interface from two methods.
     * @param coder encode value to ByteBuf
     * @param decoder decode value from ByteBuf
     * @return ByteCodec of type V
     * @param <V> type of target value
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static <V> @NotNull ByteCodec<V> codec(BiConsumer<FriendlyByteBuf, V> coder, Function<FriendlyByteBuf, V> decoder) {
        return new ByteCodec<>() {
            @Override
            public void encode(FriendlyByteBuf byteBuf, V value) {
                coder.accept(byteBuf, value);
            }

            @Override
            public V decode(FriendlyByteBuf byteBuf) {
                return decoder.apply(byteBuf);
            }
        };
    }
    
    public static final ByteCodec<Boolean> BOOLEAN_CODEC = codec(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean);

    public static final ByteCodec<Float> FLOAT_CODEC = codec(FriendlyByteBuf::writeFloat, FriendlyByteBuf::readFloat);

    public static final ByteCodec<Integer> INTEGER_CODEC = codec(FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt);

    public static final ByteCodec<Long> LONG_CODEC = codec(FriendlyByteBuf::writeLong, FriendlyByteBuf::readLong);

    public static final ByteCodec<BlockPos> BLOCK_POS_CODEC = codec(FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos);

    public static final ByteCodec<String> STRING_CODEC = codec(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf);
    
}
