package net.smok.drifter.blocks.structure;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class ShipStructureBlockRenderer implements BlockEntityRenderer<ShipStructureBlockEntity> {


    @Override
    public void render(ShipStructureBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (blockEntity.visibleBox()) renderBox(blockEntity, poseStack, buffer);
        if (blockEntity.visibleBLocks()) renderBlocks(blockEntity, poseStack, buffer);

    }

    private void renderBlocks(ShipStructureBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());
        blockEntity.getShipBlocks().keySet().forEach(pos ->
                renderBlock(poseStack, vertexConsumer, pos.subtract(blockEntity.getBlockPos()), 0.5F, 0.5F, 1.0F));

        renderBlock(poseStack, vertexConsumer, BlockPos.ZERO, 1.0F, 0.75F, 0.75F);
    }

    private void renderBlock(PoseStack poseStack, VertexConsumer vertexConsumer, BlockPos pos, float r, float g, float b) {

        Vec3 min = pos.getCenter().subtract(0.55, 0.55, 0.55);
        Vec3 max = pos.getCenter().add(0.55, 0.55, 0.55);

        LevelRenderer.renderLineBox(poseStack, vertexConsumer,
                min.x, min.y, min.z, max.x, max.y, max.z,
                r, g, b, 1.0F, r, g, b);

    }

    private void renderBox(ShipStructureBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource buffer) {
        int sizeX = blockEntity.sizeX();
        int sizeY = blockEntity.sizeY();
        int sizeZ = blockEntity.sizeZ();

        double minX = -sizeX;
        double minY = -sizeY;
        double minZ = -sizeZ;

        double maxX = 1 + sizeX;
        double maxY = 1 + sizeY;
        double maxZ = 1 + sizeZ;

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, vertexConsumer,
                minX, minY, minZ, maxX, maxY, maxZ,
                0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
    }


    @Override
    public int getViewDistance() {
        return BlockEntityRenderer.super.getViewDistance();
    }
}
