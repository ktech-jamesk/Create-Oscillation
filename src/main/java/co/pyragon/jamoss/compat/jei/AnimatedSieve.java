package co.pyragon.jamoss.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import co.pyragon.jamoss.registry.COBlocks;
import co.pyragon.jamoss.registry.COPartialModels;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/** A Resonator sitting on a sieve, the mesh shaking. */
public class AnimatedSieve extends AnimatedKinetics {

	@Override
	public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(xOffset, yOffset, 0);
		AllGuiTextures.JEI_SHADOW.render(graphics, -16, 13);
		ms.translate(-2, 18, 0);
		int scale = 18;

		blockElement(COBlocks.RESONATOR.getDefaultState())
			.rotateBlock(22.5, 22.5, 0)
			.atLocal(0, -1, 0)
			.scale(scale)
			.render(graphics);

		blockElement(COPartialModels.RESONATOR_FORK)
			.rotateBlock(22.5, 22.5, 0)
			.atLocal(0, -1, 0)
			.scale(scale)
			.render(graphics);

		blockElement(COPartialModels.RESONATOR_ROTOR)
			.rotateBlock(22.5, 22.5 + getCurrentAngle() * 2, 0)
			.atLocal(0, -1, 0)
			.scale(scale)
			.render(graphics);

		blockElement(COPartialModels.SIEVE_CAGE)
			.rotateBlock(22.5, 22.5, 0)
			.scale(scale)
			.render(graphics);

		float shake = Mth.sin(AnimationTickHolder.getRenderTime() * 1.6f) * (1 / 32f);
		blockElement(COPartialModels.SIEVE_MESH)
			.rotateBlock(22.5, 22.5, 0)
			.atLocal(0, shake, 0)
			.scale(scale)
			.render(graphics);

		ms.popPose();
	}
}
