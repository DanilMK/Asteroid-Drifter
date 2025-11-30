package net.smok.drifter.garden;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.smok.drifter.blocks.garden.MoonFarmBlockEntity;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.NotNull;

public class MoonFarmRenderer implements BlockEntityRenderer<MoonFarmBlockEntity> {

    public static final ResourceLocation MODEL_ID = new ResourceLocation(Values.MOD_ID, "block/moon_farmland");
    public static final ModelLayerLocation MODEL_LOCATION = new ModelLayerLocation(MODEL_ID, "main");
    public static final Material CONTROLLER_OVERLAY = new Material(TextureAtlas.LOCATION_BLOCKS, MODEL_ID);

    private final ItemRenderer itemRenderer;


    public MoonFarmRenderer(BlockEntityRendererProvider.@NotNull Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(MoonFarmBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack soil = blockEntity.soil();

        if (soil.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.625f, 0.5f);
        poseStack.scale(1.5F, 1.5F, 1.5F);

        this.itemRenderer.renderStatic(soil, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, blockEntity.getLevel(), blockEntity.hashCode());
        poseStack.popPose();
    }
}
