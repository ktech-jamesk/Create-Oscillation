package co.pyragon.jamoss.content.tuningfork;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Two half shafts: the top one turns at the input speed, the bottom one at the speed handed to
 * the block below. The drum in the middle hides where they meet.
 */
public class TuningForkRenderer extends KineticBlockEntityRenderer<TuningForkBlockEntity> {

	public TuningForkRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(TuningForkBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		BlockState state = be.getBlockState();
		var vb = buffer.getBuffer(RenderType.solid());

		SuperByteBuffer top = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, Direction.UP);
		standardKineticRotationTransform(top, be, light).renderInto(ms, vb);

		float ratio = TuningForkBlockEntity.ratioFor(be.getBand(), be.getSpeed());
		float outputSpeed = ratio == 0 ? be.getSpeed() : be.getSpeed() * ratio;
		float time = AnimationTickHolder.getRenderTime(be.getLevel());
		float offset = getRotationOffsetForPosition(be, be.getBlockPos(), Axis.Y);
		float angle = ((time * outputSpeed * 3f / 10 + offset) % 360) / 180 * (float) Math.PI;
		SuperByteBuffer bottom = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, Direction.DOWN);
		kineticRotationTransform(bottom, be, Axis.Y, angle, light).renderInto(ms, vb);
	}
}
