package co.pyragon.jamoss.content.coupler;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class ResonanceReceiverRenderer extends KineticBlockEntityRenderer<ResonanceReceiverBlockEntity> {

	public ResonanceReceiverRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	/** Drawn here rather than via {@code super}: the kinetic base skips rendering under Flywheel, and this block has no visual. */
	@Override
	protected void renderSafe(ResonanceReceiverBlockEntity be, float partialTicks, com.mojang.blaze3d.vertex.PoseStack ms,
		net.minecraft.client.renderer.MultiBufferSource buffer, int light, int overlay) {
		SuperByteBuffer shaft = getRotatedModel(be, be.getBlockState());
		standardKineticRotationTransform(shaft, be, light).renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.RenderType.solid()));

		// model is authored with the shaft facing south; blockstate rotations: horizontal y = toYRot, up x = 270, down x = 90
		net.minecraft.core.Direction facing = be.getBlockState().getValue(com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING);
		java.util.function.Consumer<com.mojang.blaze3d.vertex.PoseStack> rotation = switch (facing) {
			case UP -> pose -> pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-270));
			case DOWN -> pose -> pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));
			default -> pose -> pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-facing.toYRot()));
		};
		CouplerCrystalRenderer.drawSides(be.crystal.getStackInSlot(0), be.getLevel(), ms, buffer, light, overlay, rotation, 8, 9);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(ResonanceReceiverBlockEntity be, BlockState state) {
		return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state);
	}
}
