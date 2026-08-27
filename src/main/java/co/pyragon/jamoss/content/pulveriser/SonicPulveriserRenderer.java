package co.pyragon.jamoss.content.pulveriser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;

import co.pyragon.jamoss.registry.COPartialModels;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

/** Half shaft at the back, and the internal fork shivering while a layer cracks. */
public class SonicPulveriserRenderer extends KineticBlockEntityRenderer<SonicPulveriserBlockEntity> {

	public SonicPulveriserRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(SonicPulveriserBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
		FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);
		float dx = 0, dz = 0;
		if (be.isWorking()) {
			float time = AnimationTickHolder.getRenderTime(be.getLevel());
			dx = Mth.sin(time * 2.6f) * (1 / 48f);
			dz = Mth.cos(time * 1.9f) * (1 / 48f);
		}
		SuperByteBuffer fork = CachedBuffers.partialFacing(COPartialModels.PULVERISER_FORK, be.getBlockState());
		fork.translate(dx, 0, dz).light(light).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		ItemStack crystal = be.getDisplayedCrystal();
		if (crystal.isEmpty())
			return;
		float time = AnimationTickHolder.getRenderTime(be.getLevel());
		ms.pushPose();
		// centred between the tines, under the roof window, bobbing gently
		ms.translate(0.5, 0.5 + Mth.sin(time / 12f) * 0.02, 0.5);
		ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(time * (be.isWorking() ? 6f : 1.5f)));
		ms.scale(0.4f, 0.4f, 0.4f);
		Minecraft.getInstance().getItemRenderer().renderStatic(crystal, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(), 0);
		ms.popPose();
	}

	@Override
	protected SuperByteBuffer getRotatedModel(SonicPulveriserBlockEntity be, BlockState state) {
		Direction back = state.getValue(DirectionalKineticBlock.FACING).getOpposite();
		return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, back);
	}
}
