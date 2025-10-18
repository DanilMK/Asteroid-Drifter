package net.smok.drifter.structure;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.smok.drifter.blocks.structure.ShipStructureBlockEntity;
import net.smok.drifter.blocks.structure.ShipStructureBlockMenu;
import net.smok.drifter.network.NetworkHandler;

public class ShipStructureBlockScreen extends AbstractContainerScreen<ShipStructureBlockMenu> {

    private static final MutableComponent SHOW_BOX = Component.translatable("show");

    private final ShipStructureBlockEntity block;
    private EditBox sizeXEdit;
    private EditBox sizeYEdit;
    private EditBox sizeZEdit;
    private boolean boxVisible;
    private boolean blocksVisible;

    public ShipStructureBlockScreen(ShipStructureBlockMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        block = menu.getStructureBlock();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {}

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
        guiGraphics.drawString(font, Component.translatable("structure_block.size"), width / 2 - 152, 70, 0xFFFFFF, false);
        guiGraphics.drawString(font, SHOW_BOX, width / 2 + 154 - font.width(SHOW_BOX), 70, 0xFFFFFF, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void init() {
        super.init();

        boxVisible = block.visibleBox();
        blocksVisible = block.visibleBLocks();
        addRenderableWidget(
                CycleButton.onOffBuilder(block.visibleBox())
                        .displayOnlyValue()
                        .create(this.width / 2 + 104, 80, 50, 20,
                                SHOW_BOX, (cycleButton, b) -> boxVisible = b)
        );

        addRenderableWidget(
                CycleButton.onOffBuilder(block.visibleBLocks())
                        .displayOnlyValue()
                        .create(this.width / 2 + 104, 120, 50, 20,
                                SHOW_BOX, (cycleButton, b) -> blocksVisible = b)
        );

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onDone())
                .bounds(this.width / 2 - 75, 210, 150, 20).build());


        sizeXEdit = new EditBox(this.font, this.width / 2 - 152, 80, 80, 20, Component.translatable("structure_block.size.x"));
        sizeXEdit.setMaxLength(15);
        sizeXEdit.setValue(Integer.toString(block.sizeX()));
        addRenderableWidget(sizeXEdit);
        sizeYEdit = new EditBox(this.font, this.width / 2 - 72, 80, 80, 20, Component.translatable("structure_block.size.y"));
        sizeYEdit.setMaxLength(15);
        sizeYEdit.setValue(Integer.toString(block.sizeY()));
        addRenderableWidget(sizeYEdit);
        sizeZEdit = new EditBox(this.font, this.width / 2 + 8, 80, 80, 20, Component.translatable("structure_block.size.z"));
        sizeZEdit.setMaxLength(15);
        sizeZEdit.setValue(Integer.toString(block.sizeZ()));
        addRenderableWidget(sizeZEdit);


    }

    private void onDone() {
        if (minecraft != null && minecraft.getConnection() != null)
            minecraft.getConnection().send(NetworkHandler.SHIP_STRUCTURE_COMMIT.createPacket(block.getBlockPos(), parseSize(), boxVisible, blocksVisible));
        onClose();
    }

    private BlockPos parseSize() {
        return new BlockPos(
                parseInt(sizeXEdit.getValue(), block.sizeX()),
                parseInt(sizeYEdit.getValue(), block.sizeY()),
                parseInt(sizeZEdit.getValue(), block.sizeZ())
        );
    }

    private int parseInt(String asString, int orElse) {
        try {
            return Integer.parseInt(asString);
        } catch (NumberFormatException e) {
            return orElse;
        }
    }

    @Override
    protected void containerTick() {
        sizeXEdit.tick();
        sizeYEdit.tick();
        sizeZEdit.tick();
    }


    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        renderBackground(guiGraphics);
    }
}
