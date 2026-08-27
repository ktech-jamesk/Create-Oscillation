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

	@Override
	protected SuperByteBuffer getRotatedModel(ResonanceReceiverBlockEntity be, BlockState state) {
		return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state);
	}
}
