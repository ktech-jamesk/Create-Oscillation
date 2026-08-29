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

	/** Input on the left, one long arrow, outputs in a 2x2 grid on the right; the condenser sits over the arrow. */
	private static final int ARROW_Y = 40;
	private static final int CONDENSER_Y = 28;
	private static final int GRID_X = 132, GRID_Y = 27, GRID_STEP = 19;

	private static int gridX(int i) {
		return GRID_X + (i % 2) * GRID_STEP;
	}

	private static int gridY(int i) {
		return GRID_Y + (i / 2) * GRID_STEP;
	}

	private final AnimatedCondenser condenser = new AnimatedCondenser();

	public CondensingCategory(Info<CondensingRecipe> info) {
		super(info);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CondensingRecipe recipe, IFocusGroup focuses) {
		if (!recipe.getFluidIngredients().isEmpty())
			addFluidSlot(builder, 27, ARROW_Y - 4, recipe.getFluidIngredients().get(0));

		int i = 0;
		for (FluidStack result : recipe.getFluidResults())
			addFluidSlot(builder, gridX(i), gridY(i++), result);
		List<ProcessingOutput> items = recipe.getRollableResults();
		for (ProcessingOutput output : items) {
			builder.addSlot(RecipeIngredientRole.OUTPUT, gridX(i), gridY(i++))
				.setBackground(getRenderedSlot(output), -1, -1)
				.addItemStack(output.getStack())
				.addRichTooltipCallback(addStochasticTooltip(output));
		}
	}

	@Override
	public void draw(CondensingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
		double mouseY) {
		AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, ARROW_Y);
		condenser.draw(graphics, getBackground().getWidth() / 2 - 13, CONDENSER_Y);
	}
}
