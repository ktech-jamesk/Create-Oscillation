package co.pyragon.jamoss.content.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;

import co.pyragon.jamoss.registry.CORecipeTypes;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

/** One item shaken apart on a Vibrating Sieve into up to four chance-rolled results. */
public class SiftingRecipe extends StandardProcessingRecipe<RecipeInput> {

	public SiftingRecipe(ProcessingRecipeParams params) {
		super(CORecipeTypes.SIFTING, params);
	}

	@Override
	public boolean matches(RecipeInput input, Level level) {
		if (input.isEmpty())
			return false;
		return ingredients.get(0).test(input.getItem(0));
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}

	@Override
	protected int getMaxOutputCount() {
		return 4;
	}

	@Override
	protected boolean canSpecifyDuration() {
		return true;
	}
}
