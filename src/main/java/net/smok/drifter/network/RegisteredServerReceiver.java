package net.smok.drifter.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.smok.drifter.Debug;

public abstract class RegisteredServerReceiver<T> implements ServerPlayNetworking.PlayChannelHandler {

    private final ResourceLocation id;

    protected RegisteredServerReceiver(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public RegisteredServerReceiver<T> register() {
        ServerPlayNetworking.registerGlobalReceiver(id, this);
        return this;
    }


    public abstract T decode(FriendlyByteBuf buf);
    public abstract FriendlyByteBuf encode(T value);

    public abstract void receive(MinecraftServer server, ServerPlayer player, Level level, ServerGamePacketListenerImpl handler, T value);

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        T apply = decode(buf);
        server.execute(() -> receive(server, player, player.level(), handler, apply));
    }
}
