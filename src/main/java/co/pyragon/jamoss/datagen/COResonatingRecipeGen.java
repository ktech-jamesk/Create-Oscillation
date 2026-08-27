package co.pyragon.jamoss.datagen;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.content.recipe.ResonatingRecipe;
import co.pyragon.jamoss.registry.COFluids;
import co.pyragon.jamoss.registry.COItems;
import co.pyragon.jamoss.content.ore.MetalStacks;
import co.pyragon.jamoss.registry.CORecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import com.simibubi.create.AllItems;
import net.minecraft.world.level.material.Fluids;

public class COResonatingRecipeGen extends ProcessingRecipeGen<ProcessingRecipeParams, ResonatingRecipe, ResonatingRecipeBuilder> {

	GeneratedRecipe STEAM = create("steam", b -> b.require(Fluids.WATER, 250)
		.output(COFluids.STEAM.get(), 250)
		.duration(100)
		.frequency(FrequencyBand.LOW));

	GeneratedRecipe QUARTZ_VAPOUR = create("quartz_vapour", b -> b.require(Items.QUARTZ)
		.require(Fluids.WATER, 250)
		.output(COFluids.QUARTZ_VAPOUR.get(), 250)
		.duration(100)
		.frequency(FrequencyBand.MID));

	// Each tier is grown from the previous crystal plus a harder mineral, at its own band.
	GeneratedRecipe TUNED_LOW = tunedCrystal(FrequencyBand.LOW, COItems.ROUGH_QUARTZ_CRYSTAL.get(), Items.QUARTZ);
	GeneratedRecipe TUNED_MID = tunedCrystal(FrequencyBand.MID, COItems.TUNED_CRYSTAL_LOW.get(), AllItems.ROSE_QUARTZ.get());
	GeneratedRecipe TUNED_HIGH = tunedCrystal(FrequencyBand.HIGH, COItems.TUNED_CRYSTAL_MID.get(), Items.AMETHYST_SHARD);
	GeneratedRecipe TUNED_ULTRASONIC = tunedCrystal(FrequencyBand.ULTRASONIC, COItems.TUNED_CRYSTAL_HIGH.get(), AllItems.POWDERED_OBSIDIAN.get());

	private GeneratedRecipe tunedCrystal(FrequencyBand band, ItemLike base, ItemLike mineral) {
		return create("tuned_crystal_" + band.getSerializedName(), b -> b.require(base)
			.require(mineral)
			.require(Fluids.WATER, 250)
			.output(COItems.tunedCrystal(band).get())
			.duration(200)
			.frequency(band));
	}

	/** Step 1 of the ore chain: raw ore + water → slurry, High band. */
	{
		for (COOreRecipes.Metal metal : COOreRecipes.METALS)
			create("slurry_" + metal.id(), b -> b.require(COOreRecipes.rawOreTag(metal.id()))
				.require(Fluids.WATER, 250)
				.output(MetalStacks.slurry(metal.id(), 250))
				.duration(200)
				.frequency(FrequencyBand.HIGH));
	}

	public COResonatingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, CreateOscillation.MOD_ID);
	}

	@Override
	protected IRecipeTypeInfo getRecipeType() {
		return CORecipeTypes.RESONATING;
	}

	@Override
	protected ResonatingRecipeBuilder getBuilder(ResourceLocation id) {
		return new ResonatingRecipeBuilder(ResonatingRecipe::new, id);
	}
}
