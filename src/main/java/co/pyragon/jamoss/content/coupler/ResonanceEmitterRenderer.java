package co.pyragon.jamoss.content.coupler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

/** Shows the seated crystal on both sides of the emitter body. */
public class ResonanceEmitterRenderer extends SafeBlockEntityRenderer<ResonanceEmitterBlockEntity> {

	public ResonanceEmitterRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(ResonanceEmitterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		Direction facing = be.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
		// model is authored facing north; blockstate y = toYRot + 180
		float y = (facing.toYRot() + 180) % 360;
		CouplerCrystalRenderer.drawSides(be.crystal.getStackInSlot(0), be.getLevel(), ms, buffer, light, overlay,
			pose -> pose.mulPose(Axis.YP.rotationDegrees(-y)), 8, 10);
	}
}
