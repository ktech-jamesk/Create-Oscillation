package co.pyragon.jamoss.content.pulveriser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import co.pyragon.jamoss.content.amplifier.CrystalLadder;
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

/** The internal fork shivering while a layer cracks, and the seated ladder crystals. */
public class SonicPulveriserRenderer extends SafeBlockEntityRenderer<SonicPulveriserBlockEntity> {

	/** Corner (x, z) for each ladder rung: NW, NE, SE, SW, tucked between the tines. */
	private static final float[][] CORNERS = { { 5 / 16f, 5 / 16f }, { 11 / 16f, 5 / 16f }, { 11 / 16f, 11 / 16f }, { 5 / 16f, 11 / 16f } };

	public SonicPulveriserRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(SonicPulveriserBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);
		float time = AnimationTickHolder.getRenderTime(be.getLevel());
		float dx = 0, dz = 0;
		if (be.isWorking()) {
			dx = Mth.sin(time * 2.6f) * (1 / 48f);
			dz = Mth.cos(time * 1.9f) * (1 / 48f);
		}
		SuperByteBuffer fork = CachedBuffers.partialFacing(COPartialModels.PULVERISER_FORK, be.getBlockState());
		fork.translate(dx, 0, dz).light(light).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		for (int i = 0; i < CrystalLadder.RUNGS.length; i++) {
			ItemStack crystal = be.crystals.getStackInSlot(i);
			if (crystal.isEmpty())
				continue;
			ms.pushPose();
			ms.translate(CORNERS[i][0] + dx, 0.5 + (be.isWorking() ? Mth.sin(time / 6f + i) * 0.01 : 0), CORNERS[i][1] + dz);
			// static; each corner's sprite faces outward diagonally
			ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(45 + i * 90));
			ms.scale(0.25f, 0.25f, 0.25f);
			Minecraft.getInstance().getItemRenderer().renderStatic(crystal, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(), 0);
			ms.popPose();
		}
	}
}
