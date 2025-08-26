package net.smok.drifter.menus;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.smok.drifter.utils.CustomDataSlot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class ExtendedMenu extends AbstractContainerMenu {
    protected List<CustomDataSlot> customDataSlots = new ArrayList<>();
    protected final Player player;

    protected ExtendedMenu(@Nullable MenuType<?> menuType, int containerId, Player player) {
        super(menuType, containerId);
        this.player = player;
    }

    protected void addCustomDataSlot(CustomDataSlot slot) {
        customDataSlots.add(slot);
    }

    @Override
    public void broadcastFullState() {
        super.broadcastFullState();
        for (int i = 0; i < customDataSlots.size(); i++) {
            CustomDataSlot slot = customDataSlots.get(i);
            CompoundTag data = new CompoundTag();
            slot.sendData(data);
            if (player instanceof ServerPlayer serverPlayer) {
                FriendlyByteBuf friendlyByteBuf = PacketByteBufs.create();
                friendlyByteBuf.writeInt(i);
                friendlyByteBuf.writeNbt(data);
                ServerPlayNetworking.send(serverPlayer, CustomDataSlot.ID, friendlyByteBuf);
            }
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        for (int i = 0; i < customDataSlots.size(); i++) {
            CustomDataSlot slot = customDataSlots.get(i);
            if (slot.changed()) {
                CompoundTag data = new CompoundTag();
                slot.sendData(data);
                if (player instanceof ServerPlayer serverPlayer) {
                    FriendlyByteBuf friendlyByteBuf = PacketByteBufs.create();
                    friendlyByteBuf.writeInt(i);
                    friendlyByteBuf.writeNbt(data);
                    ServerPlayNetworking.send(serverPlayer, CustomDataSlot.ID, friendlyByteBuf);
                }
            }
        }
    }

    public void receiveData(int index, CompoundTag data) {

        customDataSlots.get(index).receiveData(data);
    }


    /**
     * Add slots for player inventory
     * @param playerInventory Player inventory
     * @param xOffset X offset from default inventory (default - 8)
     * @param yOffset Y offset from default inventory (default - 84)
     */
    public void addInventorySlots(Inventory playerInventory, int xOffset, int yOffset) {

        xOffset += 8;
        yOffset += 84;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, j * 18 + xOffset, i * 18 + yOffset));
            }
        }

        yOffset += 58;

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, i * 18 + xOffset, yOffset));
        }
    }

    /**
     * Move items from container to inventory and reverse
     * @param index click slot
     * @param lastInput last input slot + 1
     * @param inventoryStart first inventory slot
     * @return return empty or remainder item in clicked slot
     */
    public ItemStack quickMoveStack(int index, int lastInput, int inventoryStart) {

        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        int inventoryEnd = inventoryStart + 36;

        if (!slot.hasItem()) return itemStack;


        ItemStack itemStack2 = slot.getItem();
        itemStack = itemStack2.copy();

        if (index < inventoryStart) {
            if (!moveItemStackTo(itemStack2, inventoryStart, inventoryEnd, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(itemStack2, itemStack);
        } else {
            if (!moveItemStackTo(itemStack2, 0, lastInput, false)) return ItemStack.EMPTY;
            if (index < inventoryEnd - 9) {
                if (!this.moveItemStackTo(itemStack2, inventoryEnd - 9, inventoryEnd, false)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(itemStack2, inventoryStart, inventoryEnd - 9, false)) return ItemStack.EMPTY;
            }
        }
        if (itemStack2.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (itemStack2.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, itemStack2);

        return itemStack;
    }
}
