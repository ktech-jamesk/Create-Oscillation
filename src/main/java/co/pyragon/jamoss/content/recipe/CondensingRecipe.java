package co.pyragon.jamoss.content.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;

import co.pyragon.jamoss.registry.CORecipeTypes;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

/** Passive processing inside a Condenser: one gas in, up to one liquid and two items out. */
public class CondensingRecipe extends StandardProcessingRecipe<RecipeInput> {

	public CondensingRecipe(ProcessingRecipeParams params) {
		super(CORecipeTypes.CONDENSING, params);
	}

	/** The gas this recipe consumes. */
	public boolean matchesGas(FluidStack gas) {
		if (getFluidIngredients().isEmpty() || gas.isEmpty())
			return false;
		return getFluidIngredients().get(0).ingredient().test(gas);
	}

	public int getGasAmount() {
		return getFluidIngredients().isEmpty() ? 0 : getFluidIngredients().get(0).amount();
	}

	@Override
	protected int getMaxInputCount() {
		return 0;
	}

	@Override
	protected int getMaxOutputCount() {
		return 2;
	}

	@Override
	protected int getMaxFluidInputCount() {
		return 1;
	}

	@Override
	protected int getMaxFluidOutputCount() {
		return 1;
	}

	@Override
	protected boolean canRequireHeat() {
		return false;
	}

	@Override
	protected boolean canSpecifyDuration() {
		return true;
	}

	@Override
	public boolean matches(RecipeInput input, Level level) {
		return false;
	}
}
