package net.smok.drifter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.smok.drifter.alert.AlertOverlay;
import net.smok.drifter.alert.AlertSystemScreen;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.controller.ShipControllerScreen;
import net.smok.drifter.engine.EngineScreen;
import net.smok.drifter.registries.DrifterMenus;
import net.smok.drifter.menus.ExtendedMenu;
import net.smok.drifter.utils.CustomDataSlot;

public class AsteroidDrifterClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		MenuScreens.register(DrifterMenus.SHIP_CONTROLLER_MENU.get(), ShipControllerScreen::new);
		MenuScreens.register(DrifterMenus.ALERT_SYSTEM_MENU.get(), AlertSystemScreen::new);
		MenuScreens.register(DrifterMenus.ENGINE_MENU.get(), EngineScreen::new);
		HudRenderCallback.EVENT.register(new AlertOverlay());
		BlockRenderLayerMap.INSTANCE.putBlock(DrifterBlocks.ALERT_LUMP.get(), RenderType.cutout());


		ClientPlayNetworking.registerGlobalReceiver(CustomDataSlot.ID, AsteroidDrifterClient::customDataReceiver);
	}



	private static void customDataReceiver(Minecraft minecraft, ClientPacketListener clientPacketListener, FriendlyByteBuf friendlyByteBuf, PacketSender packetSender) {
		LocalPlayer player = minecraft.player;
		if (player != null) {
			AbstractContainerMenu containerMenu = player.containerMenu;
			if (containerMenu instanceof ExtendedMenu extendedMenu) {
				int index = friendlyByteBuf.readInt();
				CompoundTag tag = friendlyByteBuf.readNbt();
				extendedMenu.receiveData(index, tag);
			}
		}
	}
}