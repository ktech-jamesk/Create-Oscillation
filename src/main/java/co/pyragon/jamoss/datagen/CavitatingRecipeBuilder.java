package co.pyragon.jamoss.datagen;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.content.recipe.CavitatingRecipe;
import co.pyragon.jamoss.content.recipe.ResonatingRecipeParams;
import net.minecraft.resources.ResourceLocation;

public class CavitatingRecipeBuilder extends ProcessingRecipeBuilder<ProcessingRecipeParams, CavitatingRecipe, CavitatingRecipeBuilder> {

	public CavitatingRecipeBuilder(ProcessingRecipe.Factory<ProcessingRecipeParams, CavitatingRecipe> factory, ResourceLocation recipeId) {
		super(factory, recipeId);
	}

	@Override
	protected ProcessingRecipeParams createParams() {
		return new ResonatingRecipeParams();
	}

	@Override
	public CavitatingRecipeBuilder self() {
		return this;
	}

	public CavitatingRecipeBuilder frequency(FrequencyBand band) {
		((ResonatingRecipeParams) params).setFrequency(band);
		return this;
	}
}
