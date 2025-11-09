package net.smok.drifter.controller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.smok.drifter.blocks.controller.ShipControllerBlock;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ControllerBlockRenderer implements BlockEntityRenderer<ShipControllerBlockEntity> {

    public static final ResourceLocation MODEL_ID = new ResourceLocation(Values.MOD_ID, "block/controller_overlay");
    public static final ModelLayerLocation MODEL_LOCATION = new ModelLayerLocation(MODEL_ID, "main");
    public static final Material CONTROLLER_OVERLAY = new Material(TextureAtlas.LOCATION_BLOCKS, MODEL_ID);

    private final ModelPart lever;

    public ControllerBlockRenderer(BlockEntityRendererProvider.@NotNull Context context) {
        ModelPart modelPart = context.bakeLayer(MODEL_LOCATION);
        this.lever = modelPart.getChild("lever_stick");
    }


    public static @NotNull LayerDefinition createLayerDefinition() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition leverStick = root.addOrReplaceChild("lever_stick", CubeListBuilder.create().texOffs(0, 1)
                        .addBox(0f, 0, -0.5f, 1, 3, 1,
                                Set.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)),
                PartPose.offset(4f, 13.5f, 5f));

        leverStick.addOrReplaceChild("lever_top", CubeListBuilder.create().texOffs(0, 0)
                .addBox(0, 0,0, 3, 1, 1),
                PartPose.offset(-1, 3, -0.5f));
        return LayerDefinition.create(meshDefinition, 16, 16);
    }

    @Override
    public void render(@NotNull ShipControllerBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // Rotate model by facing
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - blockEntity.getBlockState().getValue(ShipControllerBlock.FACING).toYRot()));
        poseStack.translate(-0.5f, -0.5f, -0.5f);

        // Rotate lever
        float time = Mth.clamp((blockEntity.getClientTick() + partialTick) / 10f - 1, -1, 1);
        lever.xRot = blockEntity.isStand() ? -time : time;


        //todo add indicators


        VertexConsumer vertexConsumer = CONTROLLER_OVERLAY.buffer(buffer, RenderType::entitySolid);
        lever.render(poseStack, vertexConsumer, packedLight, packedOverlay);

        poseStack.popPose();
    }
}
