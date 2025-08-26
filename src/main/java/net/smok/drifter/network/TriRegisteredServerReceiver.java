package net.smok.drifter.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;

public abstract class TriRegisteredServerReceiver<T, V, E> implements ServerPlayNetworking.PlayChannelHandler {

    private final ResourceLocation id;

    protected TriRegisteredServerReceiver(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public TriRegisteredServerReceiver<T, V, E> register() {
        ServerPlayNetworking.registerGlobalReceiver(id, this);
        return this;
    }


    public abstract T decode(FriendlyByteBuf buf);
    public abstract V decodeSecond(FriendlyByteBuf buf);
    public abstract E decodeThird(FriendlyByteBuf buf);


    public abstract FriendlyByteBuf encode(T value, V value2, E value3);

    public abstract void receive(MinecraftServer server, ServerPlayer player, Level level, ServerGamePacketListenerImpl handler, T value, V value2, E value3);

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        T decode = decode(buf);
        V decodeSecond = decodeSecond(buf);
        E decodeThird = decodeThird(buf);

        server.execute(() -> receive(server, player, player.level(), handler, decode, decodeSecond, decodeThird));
    }
}
