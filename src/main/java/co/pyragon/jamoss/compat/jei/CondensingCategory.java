package co.pyragon.jamoss.compat.jei;

import java.util.List;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import co.pyragon.jamoss.content.recipe.CondensingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.fluids.FluidStack;

/** Gas in (left) -> Condenser -> liquid / items out (right). */
public class CondensingCategory extends CreateRecipeCategory<CondensingRecipe> {

	private final AnimatedCondenser condenser = new AnimatedCondenser();

	public CondensingCategory(Info<CondensingRecipe> info) {
		super(info);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CondensingRecipe recipe, IFocusGroup focuses) {
		if (!recipe.getFluidIngredients().isEmpty())
			addFluidSlot(builder, 27, 32, recipe.getFluidIngredients().get(0));

		int y = 32;
		for (FluidStack result : recipe.getFluidResults()) {
			addFluidSlot(builder, 132, y, result);
			y += 19;
		}
		List<ProcessingOutput> items = recipe.getRollableResults();
		for (ProcessingOutput output : items) {
			builder.addSlot(RecipeIngredientRole.OUTPUT, 132, y)
				.setBackground(getRenderedSlot(output), -1, -1)
				.addItemStack(output.getStack())
				.addRichTooltipCallback(addStochasticTooltip(output));
			y += 19;
		}
	}

	@Override
	public void draw(CondensingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
		double mouseY) {
		AllGuiTextures.JEI_ARROW.render(graphics, 52, 36);
		AllGuiTextures.JEI_ARROW.render(graphics, 89, 36);
		AllGuiTextures.JEI_SHADOW.render(graphics, 62, 57);
		condenser.draw(graphics, getBackground().getWidth() / 2 + 3, 34);
	}
}
