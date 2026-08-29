package co.pyragon.jamoss.compat.jei;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.recipe.CondensingRecipe;
import co.pyragon.jamoss.content.recipe.ResonatingRecipe;
import co.pyragon.jamoss.content.recipe.CavitatingRecipe;
import co.pyragon.jamoss.content.recipe.SiftingRecipe;
import co.pyragon.jamoss.registry.COBlocks;
import co.pyragon.jamoss.registry.CORecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import co.pyragon.jamoss.content.ore.MetalEntry;
import co.pyragon.jamoss.content.canister.GasCanisterItem;
import co.pyragon.jamoss.content.ore.MetalStacks;
import co.pyragon.jamoss.content.ore.Metals;
import co.pyragon.jamoss.registry.COFluids;
import co.pyragon.jamoss.registry.COItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.LinkedHashSet;
import java.util.Set;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

/** JEI integration: shows Resonating recipes using Create's basin category layout. */
@JeiPlugin
public class COJEI implements IModPlugin {

	private static final ResourceLocation ID = CreateOscillation.asResource("jei_plugin");

	private final List<CreateRecipeCategory<?>> categories = new ArrayList<>();

	private void loadCategories() {
		categories.clear();
		categories.add(new CreateRecipeCategory.Builder<BasinRecipe>(ResonatingRecipe.class)
			.addTypedRecipes(CORecipeTypes.RESONATING)
			.catalyst(COBlocks.RESONATOR::get)
			.catalyst(COBlocks.RESONANCE_CHAMBER::get)
			.doubleItemIcon(COBlocks.RESONATOR.get(), COBlocks.RESONANCE_CHAMBER.get())
			.emptyBackground(177, 103)
			.build(CreateOscillation.asResource("resonating"), ResonatingCategory::new));
		categories.add(new CreateRecipeCategory.Builder<BasinRecipe>(CavitatingRecipe.class)
			.addTypedRecipes(CORecipeTypes.CAVITATING)
			.catalyst(COBlocks.RESONATOR::get)
			.catalyst(COBlocks.CAVITATION_CHAMBER::get)
			.doubleItemIcon(COBlocks.RESONATOR.get(), COBlocks.CAVITATION_CHAMBER.get())
			.emptyBackground(177, 103)
			.build(CreateOscillation.asResource("cavitating"), ResonatingCategory::new));
		categories.add(new CreateRecipeCategory.Builder<CondensingRecipe>(CondensingRecipe.class)
			.addTypedRecipes(CORecipeTypes.CONDENSING)
			.catalyst(COBlocks.CONDENSER::get)
			.itemIcon(COBlocks.CONDENSER.get())
			.emptyBackground(177, 70)
			.build(CreateOscillation.asResource("condensing"), CondensingCategory::new));
		categories.add(new CreateRecipeCategory.Builder<SiftingRecipe>(SiftingRecipe.class)
			.addTypedRecipes(CORecipeTypes.SIFTING)
			.catalyst(COBlocks.VIBRATING_SIEVE::get)
			.itemIcon(COBlocks.VIBRATING_SIEVE.get())
			.emptyBackground(177, 53)
			.build(CreateOscillation.asResource("sifting"), SiftingCategory::new));
	}

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		loadCategories();
		registration.addRecipeCategories(categories.toArray(IRecipeCategory[]::new));
		registration.addRecipeCategories(new CouplingCategory(registration.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		categories.forEach(c -> c.registerRecipes(registration));
		registration.addRecipes(CouplingCategory.TYPE, CouplingCategory.entries());
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		categories.forEach(c -> c.registerCatalysts(registration));
		registration.addRecipeCatalyst(COBlocks.RESONANCE_EMITTER.asStack(), CouplingCategory.TYPE);
		registration.addRecipeCatalyst(COBlocks.RESONANCE_RECEIVER.asStack(), CouplingCategory.TYPE);
	}

	// ---- per-metal variants of the generic ore-chain ingredients ----

	private static final ISubtypeInterpreter<ItemStack> METAL_ITEM = new ISubtypeInterpreter<>() {
		@Override
		public Object getSubtypeData(ItemStack stack, UidContext context) {
			return MetalStacks.metal(stack);
		}

		@Override
		public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
			String metal = MetalStacks.metal(stack);
			return metal == null ? "" : metal;
		}
	};

	private static final ISubtypeInterpreter<FluidStack> METAL_FLUID = new ISubtypeInterpreter<>() {
		@Override
		public Object getSubtypeData(FluidStack stack, UidContext context) {
			return MetalStacks.metal(stack);
		}

		@Override
		public String getLegacyStringSubtypeInfo(FluidStack stack, UidContext context) {
			String metal = MetalStacks.metal(stack);
			return metal == null ? "" : metal;
		}
	};

	private static final ISubtypeInterpreter<ItemStack> CANISTER_CONTENT = new ISubtypeInterpreter<>() {
		@Override
		public Object getSubtypeData(ItemStack stack, UidContext context) {
			FluidStack content = GasCanisterItem.getContent(stack);
			if (content.isEmpty())
				return null;
			String metal = MetalStacks.metal(content);
			return net.neoforged.neoforge.registries.NeoForgeRegistries.FLUID_TYPES.getKey(content.getFluidType()) + (metal == null ? "" : "/" + metal);
		}

		@Override
		public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
			Object data = getSubtypeData(stack, context);
			return data == null ? "" : data.toString();
		}
	};

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.registerSubtypeInterpreter(COItems.METAL_CONCENTRATE.get(), METAL_ITEM);
		registration.registerSubtypeInterpreter(COItems.GAS_CANISTER.get(), CANISTER_CONTENT);
	}

	@Override
	public <T> void registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
		for (var entry : List.of(COFluids.ORE_SLURRY, COFluids.METAL_VAPOUR)) {
			registration.registerSubtypeInterpreter(NeoForgeTypes.FLUID_STACK, entry.getSource(), METAL_FLUID);
			registration.registerSubtypeInterpreter(NeoForgeTypes.FLUID_STACK, entry.get(), METAL_FLUID);
		}
	}

	/** The component-less base ingredients only exist as templates; hide them from the list. */
	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		IIngredientManager ingredients = runtime.getIngredientManager();
		ingredients.removeIngredientsAtRuntime(NeoForgeTypes.FLUID_STACK, List.of(
			new FluidStack((net.minecraft.world.level.material.Fluid) COFluids.ORE_SLURRY.getSource(), 1000),
			new FluidStack((net.minecraft.world.level.material.Fluid) COFluids.METAL_VAPOUR.getSource(), 1000)));
		fixCanisterRecipes(runtime.getRecipeManager());
	}

	/**
	 * Create's Item Drain category lists the *input* stack as the emptied container when the container keeps its
	 * item type (fine for buckets, wrong for canisters), and its Spout category skips such containers entirely.
	 * Replace the drain entries with ones that output an empty canister and add the matching spout fillings.
	 */
	private static void fixCanisterRecipes(mezz.jei.api.recipe.IRecipeManager recipes) {
		net.minecraft.world.item.Item canister = COItems.GAS_CANISTER.get();
		mezz.jei.api.recipe.RecipeType<net.minecraft.world.item.crafting.RecipeHolder<com.simibubi.create.content.fluids.transfer.EmptyingRecipe>> draining =
			mezz.jei.api.recipe.RecipeType.createRecipeHolderType(com.simibubi.create.Create.asResource("draining"));
		mezz.jei.api.recipe.RecipeType<net.minecraft.world.item.crafting.RecipeHolder<com.simibubi.create.content.fluids.transfer.FillingRecipe>> filling =
			mezz.jei.api.recipe.RecipeType.createRecipeHolderType(com.simibubi.create.Create.asResource("spout_filling"));

		List<net.minecraft.world.item.crafting.RecipeHolder<com.simibubi.create.content.fluids.transfer.EmptyingRecipe>> wrong =
			recipes.createRecipeLookup(draining).includeHidden().get()
				.filter(h -> h.value().getIngredients().stream().anyMatch(i -> java.util.Arrays.stream(i.getItems()).anyMatch(s -> s.is(canister))))
				.toList();
		if (!wrong.isEmpty())
			recipes.hideRecipes(draining, wrong);

		List<net.minecraft.world.item.crafting.RecipeHolder<com.simibubi.create.content.fluids.transfer.EmptyingRecipe>> drains = new ArrayList<>();
		List<net.minecraft.world.item.crafting.RecipeHolder<com.simibubi.create.content.fluids.transfer.FillingRecipe>> fills = new ArrayList<>();
		for (ItemStack full : GasCanisterItem.allFilled(canister)) {
			FluidStack gas = GasCanisterItem.getContent(full);
			String metal = MetalStacks.metal(gas);
			String suffix = net.neoforged.neoforge.registries.NeoForgeRegistries.FLUID_TYPES.getKey(gas.getFluidType()).getPath() + (metal == null ? "" : "_" + metal);
			ResourceLocation drainId = CreateOscillation.asResource("empty_canister_of_" + suffix);
			drains.add(new net.minecraft.world.item.crafting.RecipeHolder<>(drainId,
				new com.simibubi.create.content.processing.recipe.StandardProcessingRecipe.Builder<>(com.simibubi.create.content.fluids.transfer.EmptyingRecipe::new, drainId)
					.withItemIngredients(net.minecraft.world.item.crafting.Ingredient.of(full))
					.withFluidOutputs(gas.copy())
					.withSingleItemOutput(new ItemStack(canister))
					.build()));
			ResourceLocation fillId = CreateOscillation.asResource("fill_canister_with_" + suffix);
			fills.add(new net.minecraft.world.item.crafting.RecipeHolder<>(fillId,
				new com.simibubi.create.content.processing.recipe.StandardProcessingRecipe.Builder<>(com.simibubi.create.content.fluids.transfer.FillingRecipe::new, fillId)
					.withItemIngredients(net.minecraft.world.item.crafting.Ingredient.of(new ItemStack(canister)))
					.withFluidIngredients(new net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient(
						net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient.of(false, gas.copy()), gas.getAmount()))
					.withSingleItemOutput(full.copy())
					.build()));
		}
		recipes.addRecipes(draining, drains);
		recipes.addRecipes(filling, fills);
	}

	/** One slurry, vapour and concentrate entry per metal in the ingredient list. */
	@Override
	public void registerExtraIngredients(IExtraIngredientRegistration registration) {
		List<ItemStack> items = new ArrayList<>();
		List<FluidStack> fluids = new ArrayList<>();
		Set<String> seen = new LinkedHashSet<>();
		for (MetalEntry entry : Metals.all().values())
			seen.add(entry.metal());
		for (String metal : seen) {
			items.add(MetalStacks.concentrate(metal, 1));
			fluids.add(MetalStacks.slurry(metal, 1000));
			fluids.add(MetalStacks.vapour(metal, 1000));
		}
		items.addAll(GasCanisterItem.allFilled(COItems.GAS_CANISTER.get()));
		registration.addExtraItemStacks(items);
		registration.addExtraIngredients(NeoForgeTypes.FLUID_STACK, fluids);
	}
}
