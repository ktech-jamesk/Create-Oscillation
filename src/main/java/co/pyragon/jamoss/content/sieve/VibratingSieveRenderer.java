package co.pyragon.jamoss.content.sieve;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import co.pyragon.jamoss.registry.COPartialModels;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

/** Draws the cage (small shiver) and the mesh (bigger shake) while the resonator above drives it. */
public class VibratingSieveRenderer extends SafeBlockEntityRenderer<VibratingSieveBlockEntity> {

	public VibratingSieveRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(VibratingSieveBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		float time = AnimationTickHolder.getRenderTime(be.getLevel());
		boolean shaking = be.isShaking();
		float rate = Mth.clamp(be.getDriveSpeed() / 64f, 0.5f, 2f);

		SuperByteBuffer cage = CachedBuffers.partial(COPartialModels.SIEVE_CAGE, be.getBlockState());
		if (shaking)
			cage.translate(Mth.sin(time * 2.3f * rate) * (1 / 96f), 0, Mth.cos(time * 1.7f * rate) * (1 / 96f));
		cage.light(light).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		SuperByteBuffer mesh = CachedBuffers.partial(COPartialModels.SIEVE_MESH, be.getBlockState());
		if (shaking)
			mesh.translate(Mth.cos(time * 1.1f * rate) * (1 / 64f), Mth.sin(time * 1.6f * rate) * (1 / 32f),
				-Mth.cos(time * 1.1f * rate) * (1 / 64f));
		mesh.light(light).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
	}
}
