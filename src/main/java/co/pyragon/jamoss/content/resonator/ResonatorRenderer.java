package co.pyragon.jamoss.content.resonator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import co.pyragon.jamoss.registry.COPartialModels;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Spins the rotor (or leaves that to Flywheel) and draws the fork (collar + tines), which
 * shivers while the resonator is working.
 */
public class ResonatorRenderer extends KineticBlockEntityRenderer<ResonatorBlockEntity> {

	public ResonatorRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(ResonatorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		float time = AnimationTickHolder.getRenderTime(be.getLevel());
		float dx = 0, dz = 0;
		if (be.isWorking()) {
			float rate = Mth.clamp(Math.abs(be.getSpeed()) / 64f, 0.5f, 2f);
			dx = Mth.sin(time * 2.3f * rate) * (1 / 64f);
			dz = Mth.cos(time * 1.7f * rate) * (1 / 64f);
		}
		SuperByteBuffer fork = CachedBuffers.partial(COPartialModels.RESONATOR_FORK, be.getBlockState());
		fork.translate(dx, 0, dz).light(light).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		// rotor: KineticBlockEntityRenderer skips this when Flywheel handles it
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(ResonatorBlockEntity be, BlockState state) {
		return CachedBuffers.partial(COPartialModels.RESONATOR_ROTOR, state);
	}
}
