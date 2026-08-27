package co.pyragon.jamoss.datagen;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.api.data.recipe.StandardProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.recipe.CondensingRecipe;
import co.pyragon.jamoss.registry.COFluids;
import co.pyragon.jamoss.registry.COItems;
import co.pyragon.jamoss.content.ore.MetalStacks;
import co.pyragon.jamoss.registry.CODataComponents;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import co.pyragon.jamoss.registry.CORecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public class COCondensingRecipeGen extends StandardProcessingRecipeGen<CondensingRecipe> {

	GeneratedRecipe WATER = create("water", b -> b.require(COFluids.STEAM.getSource(), 250)
		.output(Fluids.WATER, 250)
		.duration(500));

	GeneratedRecipe QUARTZ = create("quartz", b -> b.require(COFluids.QUARTZ_VAPOUR.getSource(), 250)
		.output(Fluids.WATER, 250)
		.output(Items.QUARTZ)
		.output(0.5f, COItems.ROUGH_QUARTZ_CRYSTAL.get())
		.duration(100));

	/** Step 3 of the ore chain: metal vapour → water + 2.5 concentrate. */
	{
		for (COOreRecipes.Metal metal : COOreRecipes.METALS)
			create("concentrate_" + metal.id(), b -> b
				.require(new SizedFluidIngredient(DataComponentFluidIngredient.of(false, CODataComponents.METAL,
					metal.id(), COFluids.METAL_VAPOUR.getSource()), 250))
				.output(Fluids.WATER, 250)
				.output(MetalStacks.concentrate(metal.id(), 2))
				.output(0.5f, MetalStacks.concentrate(metal.id(), 1))
				.duration(100));
	}

	public COCondensingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, CreateOscillation.MOD_ID);
	}

	@Override
	protected IRecipeTypeInfo getRecipeType() {
		return CORecipeTypes.CONDENSING;
	}
}
