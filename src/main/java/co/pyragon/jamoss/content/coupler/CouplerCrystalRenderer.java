package co.pyragon.jamoss.content.coupler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Draws a coupler block's crystal as a flat, static sprite on both side faces of its body
 * (model x = 3 and x = 13), so the tuning can be read from either side.
 */
public final class CouplerCrystalRenderer {

	private CouplerCrystalRenderer() {}

	/**
	 * @param modelRotation the same rotation the blockstate applies to the block model, so the sprites
	 *                      follow the block's facing (blockstate {@code x}/{@code y} map to {@code XP(-x)}/{@code YP(-y)})
	 * @param z             model-space depth (0..16) of the body's centre, along the unrotated model
	 * @param y             model-space height (0..16) of the body's centre
	 */
	public static void drawSides(ItemStack crystal, Level level, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
		java.util.function.Consumer<PoseStack> modelRotation, float y, float z) {
		if (crystal.isEmpty())
			return;
		ms.pushPose();
		ms.translate(0.5, 0.5, 0.5);
		modelRotation.accept(ms);
		ms.translate(-0.5, -0.5, -0.5);
		for (int side = 0; side < 2; side++) {
			ms.pushPose();
			float x = side == 0 ? 3 / 16f - 0.02f : 13 / 16f + 0.02f;
			ms.translate(x, y / 16f, z / 16f);
			// FIXED renders the sprite flat in the XY plane; turn it to face out of the side
			ms.mulPose(Axis.YP.rotationDegrees(side == 0 ? -90 : 90));
			ms.scale(0.35f, 0.35f, 0.35f);
			Minecraft.getInstance().getItemRenderer().renderStatic(crystal, ItemDisplayContext.FIXED, light, overlay, ms, buffer, level, 0);
			ms.popPose();
		}
		ms.popPose();
	}
}
