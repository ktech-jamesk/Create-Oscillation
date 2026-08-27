package co.pyragon.jamoss.compat.jei;

import com.simibubi.create.compat.jei.category.BasinCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;

import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.content.recipe.ResonatingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;

/** Basin-style layout without a heat bar, with the Resonator animated above the Chamber. */
public class ResonatingCategory extends BasinCategory {

	private final AnimatedResonator resonator = new AnimatedResonator();

	public ResonatingCategory(Info<BasinRecipe> info) {
		super(info, false);
	}

	@Override
	public void draw(BasinRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);
		resonator.draw(graphics, getBackground().getWidth() / 2 + 3, 34);
		// Same spot Create uses for "Heated" on mixing recipes.
		FrequencyBand band = recipe instanceof ResonatingRecipe rr ? rr.getBand() : FrequencyBand.ANY;
		boolean any = band == FrequencyBand.ANY;
		(any ? AllGuiTextures.JEI_NO_HEAT_BAR : AllGuiTextures.JEI_HEAT_BAR).render(graphics, 4, 80);
		Component label = Component.translatable("createoscillation.recipe.frequency", band.getDisplayName());
		Font font = Minecraft.getInstance().font;
		graphics.drawString(font, label, 9, 86, any ? 0xFF888888 : 0xFF3FC7C7, false);
	}
}
