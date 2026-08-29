package co.pyragon.jamoss.content.amplifier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import co.pyragon.jamoss.registry.COPartialModels;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the brass body (shivers like the Resonator's fork while the block below is using the
 * vibration) and the seated crystals, one at each corner between the plates.
 */
public class ResonanceAmplifierRenderer extends SafeBlockEntityRenderer<ResonanceAmplifierBlockEntity> {

	/** Corner (x, z) for each ladder rung: NW, NE, SE, SW. Sprites are static, turned to face out of their corner. */
	private static final float[][] CORNERS = { { 3 / 16f, 3 / 16f }, { 13 / 16f, 3 / 16f }, { 13 / 16f, 13 / 16f }, { 3 / 16f, 13 / 16f } };

	public ResonanceAmplifierRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(ResonanceAmplifierBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		float time = AnimationTickHolder.getRenderTime(be.getLevel());
		boolean working = be.isWorking();
		float dx = 0, dz = 0;
		if (working) {
			float rate = Mth.clamp(Math.abs(be.getVibrationSpeed()) / 64f, 0.5f, 2f);
			dx = Mth.sin(time * 2.3f * rate) * (1 / 64f);
			dz = Mth.cos(time * 1.7f * rate) * (1 / 64f);
		}
		SuperByteBuffer body = CachedBuffers.partial(COPartialModels.AMPLIFIER_BODY, be.getBlockState());
		body.translate(dx, 0, dz).light(light).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		boolean active = be.isAmplifying();
		for (int i = 0; i < CrystalLadder.RUNGS.length; i++) {
			ItemStack crystal = be.crystals.getStackInSlot(i);
			if (crystal.isEmpty())
				continue;
			ms.pushPose();
			ms.translate(CORNERS[i][0] + dx, 0.5 + (active ? Mth.sin(time / 6f + i) * 0.01 : 0), CORNERS[i][1] + dz);
			// static; each corner's sprite faces outward diagonally
			ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(45 + i * 90));
			ms.scale(0.3f, 0.3f, 0.3f);
			Minecraft.getInstance().getItemRenderer().renderStatic(crystal, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(), 0);
			ms.popPose();
		}
	}
}
