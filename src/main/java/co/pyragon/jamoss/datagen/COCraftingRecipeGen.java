package co.pyragon.jamoss.datagen;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.registry.COBlocks;
import co.pyragon.jamoss.registry.COItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import co.pyragon.jamoss.registry.CODataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/** Vanilla-style crafting recipes for every Oscillation block/item (andesite-age costs). */
public class COCraftingRecipeGen extends RecipeProvider {

	public COCraftingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void buildRecipes(RecipeOutput out) {
		ItemLike andesite = AllItems.ANDESITE_ALLOY.get();

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, COBlocks.RESONANCE_CHAMBER.get())
			.pattern("GGG")
			.pattern(" B ")
			.pattern(" C ")
			.define('G', Items.GLASS)
			.define('B', AllBlocks.BASIN.get())
			.define('C', Items.COPPER_INGOT)
			.unlockedBy("has_basin", has(AllBlocks.BASIN.get()))
			.save(out, id("resonance_chamber"));

		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, COBlocks.CAVITATION_CHAMBER.get())
			.requires(COBlocks.RESONANCE_CHAMBER.get())
			.requires(AllBlocks.BRASS_CASING.get())
			.requires(COItems.TUNED_CRYSTAL_HIGH.get())
			.unlockedBy("has_high_crystal", has(COItems.TUNED_CRYSTAL_HIGH.get()))
			.save(out, id("cavitation_chamber"));

		// Step 4 of the ore chain: concentrate smelts to its ingot
		for (COOreRecipes.Metal metal : COOreRecipes.METALS) {
			Ingredient concentrate = DataComponentIngredient.of(false, CODataComponents.METAL, metal.id(), COItems.METAL_CONCENTRATE.get());
			SimpleCookingRecipeBuilder.smelting(concentrate, RecipeCategory.MISC, metal.ingot(), 0.7f, 200)
				.unlockedBy("has_concentrate", has(COItems.METAL_CONCENTRATE.get()))
				.save(out, id("smelting/ingot_" + metal.id()));
			SimpleCookingRecipeBuilder.blasting(concentrate, RecipeCategory.MISC, metal.ingot(), 0.7f, 100)
				.unlockedBy("has_concentrate", has(COItems.METAL_CONCENTRATE.get()))
				.save(out, id("blasting/ingot_" + metal.id()));
		}

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, COBlocks.RESONANCE_EMITTER.get())
			.pattern(" Q ")
			.pattern("CBC")
			.pattern(" A ")
			.define('Q', Items.QUARTZ_BLOCK)
			.define('C', Items.COPPER_INGOT)
			.define('B', AllBlocks.BRASS_CASING.get())
			.define('A', andesite)
			.unlockedBy("has_brass_casing", has(AllBlocks.BRASS_CASING.get()))
			.save(out, id("resonance_emitter"));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, COBlocks.RESONANCE_RECEIVER.get())
			.pattern("CQC")
			.pattern(" B ")
			.pattern(" S ")
			.define('Q', Items.QUARTZ_BLOCK)
			.define('C', Items.COPPER_INGOT)
			.define('B', AllBlocks.BRASS_CASING.get())
			.define('S', AllBlocks.SHAFT.get())
			.unlockedBy("has_brass_casing", has(AllBlocks.BRASS_CASING.get()))
			.save(out, id("resonance_receiver"));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, COBlocks.SONIC_PULVERISER.get())
			.pattern("IRI")
			.pattern("BCB")
			.pattern(" S ")
			.define('I', Items.IRON_INGOT)
			.define('R', COBlocks.RESONATOR.get())
			.define('B', AllItems.BRASS_INGOT.get())
			.define('C', AllBlocks.BRASS_CASING.get())
			.define('S', AllBlocks.SHAFT.get())
			.unlockedBy("has_resonator", has(COBlocks.RESONATOR.get()))
			.save(out, id("sonic_pulveriser"));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, COBlocks.TUNING_FORK.get())
			.pattern(" C ")
			.pattern("BAB")
			.pattern(" S ")
			.define('C', COItems.TUNED_CRYSTAL_MID.get())
			.define('B', AllItems.BRASS_INGOT.get())
			.define('A', AllBlocks.ANDESITE_CASING.get())
			.define('S', AllBlocks.SHAFT.get())
			.unlockedBy("has_tuned_crystal", has(COItems.TUNED_CRYSTAL_MID.get()))
			.save(out, id("tuning_fork"));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, COBlocks.RESONATOR.get())
			.pattern(" S ")
			.pattern("IQI")
			.pattern(" A ")
			.define('S', AllBlocks.SHAFT.get())
			.define('I', Items.IRON_INGOT)
			.define('Q', Items.QUARTZ)
			.define('A', andesite)
			.unlockedBy("has_andesite_alloy", has(andesite))
			.save(out, id("resonator"));

		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, COBlocks.RESONANCE_PUMP.get())
			.requires(AllBlocks.MECHANICAL_PUMP.get())
			.requires(Items.QUARTZ)
			.requires(Items.COPPER_INGOT)
			.unlockedBy("has_pump", has(AllBlocks.MECHANICAL_PUMP.get()))
			.save(out, id("resonance_pump"));

		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, COBlocks.CONDENSER.get())
			.requires(AllBlocks.FLUID_TANK.get())
			.requires(Items.QUARTZ)
			.requires(Items.COPPER_INGOT)
			.unlockedBy("has_tank", has(AllBlocks.FLUID_TANK.get()))
			.save(out, id("condenser"));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, COItems.GAS_CANISTER.get())
			.pattern("nIn")
			.pattern("CGC")
			.pattern("nCn")
			.define('n', AllItems.COPPER_NUGGET.get())
			.define('I', Items.IRON_INGOT)
			.define('C', Items.COPPER_INGOT)
			.define('G', Items.GLASS)
			.unlockedBy("has_copper", has(Items.COPPER_INGOT))
			.save(out, id("gas_canister"));

		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, COBlocks.VENT.get(), 2)
			.requires(AllBlocks.FLUID_PIPE.get())
			.requires(Items.IRON_NUGGET)
			.requires(andesite)
			.unlockedBy("has_pipe", has(AllBlocks.FLUID_PIPE.get()))
			.save(out, id("vent"));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, COBlocks.VIBRATING_SIEVE.get())
			.pattern("ABA")
			.pattern("IDI")
			.define('A', andesite)
			.define('B', Items.IRON_BARS)
			.define('I', Items.IRON_INGOT)
			.define('D', AllBlocks.DEPOT.get())
			.unlockedBy("has_depot", has(AllBlocks.DEPOT.get()))
			.save(out, id("vibrating_sieve"));
	}

	private static net.minecraft.resources.ResourceLocation id(String path) {
		return CreateOscillation.asResource("crafting/" + path);
	}
}
