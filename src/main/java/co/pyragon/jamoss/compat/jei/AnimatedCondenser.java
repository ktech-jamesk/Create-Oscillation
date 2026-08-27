package co.pyragon.jamoss.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;

import co.pyragon.jamoss.registry.COBlocks;
import net.minecraft.client.gui.GuiGraphics;

/** A single Condenser block, drawn like Create's JEI machine previews. */
public class AnimatedCondenser extends AnimatedKinetics {

	@Override
	public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(xOffset, yOffset, 200);
		ms.mulPose(Axis.XP.rotationDegrees(-15.5f));
		ms.mulPose(Axis.YP.rotationDegrees(22.5f));
		blockElement(COBlocks.CONDENSER.getDefaultState())
			.atLocal(0, 0, 0)
			.scale(23)
			.render(graphics);
		ms.popPose();
	}
}
