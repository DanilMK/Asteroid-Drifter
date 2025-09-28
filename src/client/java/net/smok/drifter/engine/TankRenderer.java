package net.smok.drifter.engine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.utils.ClientFluidHooks;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.smok.drifter.blocks.engine.TankBlockEntity;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class TankRenderer implements BlockEntityRenderer<TankBlockEntity> {


    public static final ResourceLocation MODEL_ID = new ResourceLocation(Values.MOD_ID, "block/tank_fluid");
    public static final ModelLayerLocation FLUID_LOCATION = new ModelLayerLocation(MODEL_ID, "main");
    private final ModelPart[] sides;
    private final ModelPart top;


    public TankRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart fluid = context.bakeLayer(FLUID_LOCATION);
        sides = new ModelPart[] {
                fluid.getChild("back"), fluid.getChild("left"),
                fluid.getChild("right"), fluid.getChild("front")
        };
        top = fluid.getChild("top");

    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();
        CubeListBuilder cubes = CubeListBuilder.create()
                .texOffs(1, 0)
                .addBox(0.0F, 0.0F, 0.0F, 13.8F, 13.8F, 0, EnumSet.of(Direction.NORTH));

        root.addOrReplaceChild("back", cubes, PartPose.offsetAndRotation(1.1F, 1.1F, 1.1F, 0, 0, 0));
        root.addOrReplaceChild("left", cubes, PartPose.offsetAndRotation(1.1F, 1.1F, 1.1F, 0, (float) (-Math.PI / 2), 0));
        root.addOrReplaceChild("right", cubes, PartPose.offsetAndRotation(14.9F, 1.1F, 14.9F, 0, (float) (Math.PI / 2), 0));
        root.addOrReplaceChild("front", cubes, PartPose.offsetAndRotation(14.9F, 1.1F, 14.9F, 0, (float) Math.PI, 0));


        root.addOrReplaceChild("top", cubes, PartPose.offsetAndRotation(1.1F, 1.1F, 1.1F, (float) (Math.PI / 2), 0, 0));


        return LayerDefinition.create(meshDefinition, 16, 16);
    }

    @Override
    public void render(TankBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.getFluidContainer().isEmpty()) return;

        FluidHolder holder = blockEntity.getFluidContainer().getFirstFluid();
        float amount = (float) holder.getFluidAmount() / blockEntity.getFluidContainer().getTankCapacity(0);
        VertexConsumer consumer = getConsumer(buffer, holder);
        float[] colors = getColors(holder);

        for (ModelPart side : sides) {
            side.yScale = amount;
            side.render(poseStack, consumer, packedLight, packedOverlay, colors[0], colors[1], colors[2], 1);
        }

        top.y = 14f * amount + 1;
        top.render(poseStack, consumer, packedLight, packedOverlay, colors[0], colors[1], colors[2], 1);
    }

    private static @NotNull VertexConsumer getConsumer(MultiBufferSource buffer, FluidHolder holder) {
        TextureAtlasSprite sprite = ClientFluidHooks.getFluidSprite(holder);
        Material material = new Material(sprite.atlasLocation(), sprite.contents().name());
        return material.buffer(buffer, RenderType::entityTranslucent);
    }

    private static float[] getColors(FluidHolder holder) {
        int color = ClientFluidHooks.getFluidColor(holder);

        float r = (float) FastColor.ARGB32.red(color) / 255.0F;
        float g = (float) FastColor.ARGB32.green(color) / 255.0F;
        float b = (float) FastColor.ARGB32.blue(color) / 255.0F;

        return new float[] {r, g, b};
    }
}
