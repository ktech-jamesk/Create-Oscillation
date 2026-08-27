package co.pyragon.jamoss.content.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import co.pyragon.jamoss.registry.CORecipeTypes;

/** A resonating-style recipe that only the Cavitation Chamber runs. */
public class CavitatingRecipe extends ResonatingRecipe {

	public CavitatingRecipe(ProcessingRecipeParams params) {
		super(CORecipeTypes.CAVITATING, params);
	}
}
