package co.pyragon.jamoss.compat.jei;

import java.util.List;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import co.pyragon.jamoss.content.recipe.SiftingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;

/** Milling-style layout: input top-left, up to four chance outputs on the right. */
public class SiftingCategory extends CreateRecipeCategory<SiftingRecipe> {

	private final AnimatedSieve sieve = new AnimatedSieve();

	public SiftingCategory(Info<SiftingRecipe> info) {
		super(info);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, SiftingRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 15, 9)
			.setBackground(getRenderedSlot(), -1, -1)
			.addIngredients(recipe.getIngredients().get(0));
		List<ProcessingOutput> results = recipe.getRollableResults();
		boolean single = results.size() == 1;
		int i = 0;
		for (ProcessingOutput output : results) {
			int xOffset = i % 2 == 0 ? 0 : 19;
			int yOffset = (i / 2) * -19;
			builder.addSlot(RecipeIngredientRole.OUTPUT, single ? 139 : 133 + xOffset, 27 + yOffset)
				.setBackground(getRenderedSlot(output), -1, -1)
				.addItemStack(output.getStack())
				.addRichTooltipCallback(addStochasticTooltip(output));
			i++;
		}
	}

	@Override
	public void draw(SiftingRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_ARROW.render(graphics, 85, 32);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 43, 4);
		sieve.draw(graphics, 48, 27);
	}
}
