package co.pyragon.jamoss.content.condenser;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.fluids.FluidStack;

/** Like Create's tank renderer, but draws the gas at the top and the condensed liquid at the bottom. */
public class CondenserRenderer extends SafeBlockEntityRenderer<CondenserBlockEntity> {

	public CondenserRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(CondenserBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		if (!be.isController() || !be.hasWindow())
			return;

		float capHeight = 1 / 4f;
		float hull = 1 / 16f + 1 / 128f;
		float minPuddle = 1 / 16f;
		float totalHeight = be.getHeight() - 2 * capHeight - minPuddle;
		float bottom = capHeight + minPuddle;

		LerpedFloat gasLevel = be.getFluidLevel();
		float gasFrac = gasLevel == null ? 0 : Mth.clamp(gasLevel.getValue(partialTicks), 0, 1);
		float liquidFrac = be.getOutputTank().getCapacity() == 0 ? 0
			: Mth.clamp((float) be.getOutputTank().getFluidAmount() / be.getOutputTank().getCapacity(), 0, 1);

		// Both tanks share the visible column; shrink proportionally if they would overlap.
		float sum = gasFrac + liquidFrac;
		if (sum > 1) {
			gasFrac /= sum;
			liquidFrac /= sum;
		}

		float xMin = hull;
		float xMax = xMin + be.getWidth() - 2 * hull;
		float zMin = hull;
		float zMax = zMin + be.getWidth() - 2 * hull;

		FluidStack liquid = be.getOutputTank().getFluid();
		float liquidHeight = liquidFrac * totalHeight;
		if (!liquid.isEmpty() && liquidHeight > 1 / 512f)
			NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(liquid, xMin, bottom, zMin, xMax, bottom + liquidHeight, zMax,
				buffer, ms, light, false, true);

		FluidStack gas = be.getTankInventory().getFluid();
		float gasHeight = gasFrac * totalHeight;
		if (!gas.isEmpty() && gasHeight > 1 / 512f) {
			float top = bottom + totalHeight;
			NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(gas, xMin, top - gasHeight, zMin, xMax, top, zMax,
				buffer, ms, light, false, true);
		}
	}

	@Override
	public boolean shouldRenderOffScreen(CondenserBlockEntity be) {
		return be.isController();
	}
}
