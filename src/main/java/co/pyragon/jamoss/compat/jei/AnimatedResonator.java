package co.pyragon.jamoss.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;

import co.pyragon.jamoss.registry.COBlocks;
import co.pyragon.jamoss.registry.COPartialModels;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;

/** Shaft feeding a Resonator whose rotor spins, sitting flush on a Resonance Chamber. */
public class AnimatedResonator extends AnimatedKinetics {

	@Override
	public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(xOffset, yOffset, 200);
		ms.mulPose(Axis.XP.rotationDegrees(-15.5f));
		ms.mulPose(Axis.YP.rotationDegrees(22.5f));
		int scale = 23;

		blockElement(shaft(Direction.Axis.Y))
			.rotateBlock(0, getCurrentAngle() * 2, 0)
			.atLocal(0, -1, 0)
			.scale(scale)
			.render(graphics);

		blockElement(COBlocks.RESONATOR.getDefaultState())
			.atLocal(0, 0, 0)
			.scale(scale)
			.render(graphics);

		blockElement(COPartialModels.RESONATOR_FORK)
			.atLocal(0, 0, 0)
			.scale(scale)
			.render(graphics);

		blockElement(COPartialModels.RESONATOR_ROTOR)
			.rotateBlock(0, getCurrentAngle() * 2, 0)
			.atLocal(0, 0, 0)
			.scale(scale)
			.render(graphics);

		blockElement(COPartialModels.CHAMBER_BODY)
			.atLocal(0, 1, 0)
			.scale(scale)
			.render(graphics);

		ms.popPose();
	}
}
