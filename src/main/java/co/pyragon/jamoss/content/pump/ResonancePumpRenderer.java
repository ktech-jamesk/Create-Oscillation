package co.pyragon.jamoss.content.pump;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import co.pyragon.jamoss.registry.COPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

/** Spins the pump's impeller ring, oriented along the pump's facing like Create's pump cog. */
public class ResonancePumpRenderer extends KineticBlockEntityRenderer<ResonancePumpBlockEntity> {

	public ResonancePumpRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(ResonancePumpBlockEntity be, BlockState state) {
		return CachedBuffers.partialFacing(COPartialModels.RESONANCE_PUMP_ROTOR, state);
	}
}
