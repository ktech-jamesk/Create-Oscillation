package co.pyragon.jamoss.datagen;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.content.recipe.ResonatingRecipe;
import co.pyragon.jamoss.content.recipe.ResonatingRecipeParams;
import net.minecraft.resources.ResourceLocation;

public class ResonatingRecipeBuilder extends ProcessingRecipeBuilder<ProcessingRecipeParams, ResonatingRecipe, ResonatingRecipeBuilder> {

	public ResonatingRecipeBuilder(ProcessingRecipe.Factory<ProcessingRecipeParams, ResonatingRecipe> factory, ResourceLocation recipeId) {
		super(factory, recipeId);
	}

	@Override
	protected ProcessingRecipeParams createParams() {
		return new ResonatingRecipeParams();
	}

	@Override
	public ResonatingRecipeBuilder self() {
		return this;
	}

	public ResonatingRecipeBuilder frequency(FrequencyBand band) {
		((ResonatingRecipeParams) params).setFrequency(band);
		return this;
	}
}
