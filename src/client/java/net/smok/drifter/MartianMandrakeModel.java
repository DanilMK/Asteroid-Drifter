package net.smok.drifter;
// Made with Blockbench 5.0.4

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.smok.drifter.entities.MartianMandrake;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class MartianMandrakeModel extends EntityModel<MartianMandrake> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Values.MOD_ID, "martian_mandrake"), "main");
	public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(Values.MOD_ID, "textures/entities/martian_mandrake.png");
	private final ModelPart main;
	private final ModelPart foot;

	private float jumpRotation;

	public MartianMandrakeModel(ModelPart root) {
		this.main = root.getChild("main");
		this.foot = main.getChild("foot");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition hair = body.addOrReplaceChild("hair", CubeListBuilder.create()
				.texOffs(16, 0).addBox(0.0F, -12.0F, -8.0F, 0.0F, 16.0F, 16.0F, Set.of(Direction.WEST))
				.texOffs(16, 16).addBox(-8.0F, -12.0F, 0.0F, 16.0F, 16.0F, 0.0F, Set.of(Direction.NORTH)),
				PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0f, 45f, 0.0f));

		PartDefinition foot = body.addOrReplaceChild("foot", CubeListBuilder.create().texOffs(0, 12).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	public static @NotNull MobRenderer<MartianMandrake, MartianMandrakeModel> getMobRenderer(EntityRendererProvider.Context context) {
		return new MobRenderer<>(context, new MartianMandrakeModel(context.bakeLayer(LAYER_LOCATION)), 0.3f) {
			@Override
			public @NotNull ResourceLocation getTextureLocation(MartianMandrake entity) {
				return TEXTURE_LOCATION;
			}
		};
	}

	@Override
	public void setupAnim(MartianMandrake entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		jumpRotation = Mth.sin((float) (entity.getJumpCompletion(ageInTicks - entity.tickCount) * Math.PI));
		foot.xRot = this.jumpRotation * 50.0F * (float) (Math.PI / 180.0);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public void prepareMobModel(MartianMandrake entity, float limbSwing, float limbSwingAmount, float partialTick) {
		super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
		jumpRotation = Mth.sin((float) (entity.getJumpCompletion(partialTick) * Math.PI));
	}
}