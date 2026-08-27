package co.pyragon.jamoss.datagen;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.content.ore.MetalStacks;
import co.pyragon.jamoss.content.recipe.CavitatingRecipe;
import co.pyragon.jamoss.registry.CODataComponents;
import co.pyragon.jamoss.registry.COFluids;
import co.pyragon.jamoss.registry.CORecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/** Step 2 of the ore chain: slurry + steam → metal vapour, Ultrasonic band, Cavitation Chamber only. */
public class COCavitatingRecipeGen extends ProcessingRecipeGen<ProcessingRecipeParams, CavitatingRecipe, CavitatingRecipeBuilder> {

	{
		for (COOreRecipes.Metal metal : COOreRecipes.METALS)
			create("vapour_" + metal.id(), b -> b
				.require(new SizedFluidIngredient(DataComponentFluidIngredient.of(false, CODataComponents.METAL,
					metal.id(), COFluids.ORE_SLURRY.getSource()), 250))
				.require(COFluids.STEAM.getSource(), 250)
				.output(MetalStacks.vapour(metal.id(), 250))
				.duration(200)
				.frequency(FrequencyBand.ULTRASONIC));
	}

	public COCavitatingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, CreateOscillation.MOD_ID);
	}

	@Override
	protected IRecipeTypeInfo getRecipeType() {
		return CORecipeTypes.CAVITATING;
	}

	@Override
	protected CavitatingRecipeBuilder getBuilder(ResourceLocation id) {
		return new CavitatingRecipeBuilder(CavitatingRecipe::new, id);
	}
}
