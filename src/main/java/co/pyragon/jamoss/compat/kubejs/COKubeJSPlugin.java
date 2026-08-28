package co.pyragon.jamoss.compat.kubejs;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.codec.CreateCodecs;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.ItemWrapper;
import dev.latvian.mods.kubejs.recipe.component.EnumComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.SizedFluidIngredientComponent;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/**
 * Loaded by KubeJS (via {@code kubejs.plugins.txt}) only when KubeJS is installed. Registers the recipe
 * components the JSON schemas in {@code data/createoscillation/kubejs/recipe_schema/} refer to, and the
 * {@code Oscillation} binding. Component ids are our own so this works with or without KubeJS-Create.
 */
public class COKubeJSPlugin implements KubeJSPlugin {

	/** Create's chance output: item + count + chance (+ components). Accepts plain item stacks in scripts. */
	public static final RecipeComponentType<ProcessingOutput> PROCESSING_OUTPUT = RecipeComponentType.unit(
		CreateOscillation.asResource("processing_output"), type -> new SimpleRecipeComponent<>(type, ProcessingOutput.CODEC_NEW, TypeInfo.of(ProcessingOutput.class)) {
			@Override
			public boolean hasPriority(RecipeMatchContext cx, Object from) {
				return from instanceof ProcessingOutput || ItemWrapper.isItemStackLike(from);
			}

			@Override
			public boolean isEmpty(ProcessingOutput value) {
				return value == ProcessingOutput.EMPTY || value.getStack().isEmpty();
			}
		});

	/** Create's processing params need the {@code type} field on fluid ingredients, which NeoForge's flat codec omits. */
	public static final RecipeComponentType<SizedFluidIngredient> SIZED_FLUID_INGREDIENT = RecipeComponentType.unit(
		CreateOscillation.asResource("sized_fluid_ingredient"),
		type -> new SizedFluidIngredientComponent(type, CreateCodecs.FLAT_SIZED_FLUID_INGREDIENT_WITH_TYPE, false));

	public static final RecipeComponentType<FrequencyBand> FREQUENCY =
		EnumComponent.of(CreateOscillation.asResource("frequency"), FrequencyBand.class, FrequencyBand.CODEC);

	@Override
	public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
		registry.register(PROCESSING_OUTPUT);
		registry.register(SIZED_FLUID_INGREDIENT);
		registry.register(FREQUENCY);
	}

	@Override
	public void registerBindings(BindingRegistry bindings) {
		bindings.add("Oscillation", OscillationKubeBinding.class);
	}

	/** Lets scripts pass plain items or item stacks where a chance output is expected. */
	@Override
	public void registerTypeWrappers(TypeWrapperRegistry registry) {
		registry.register(ProcessingOutput.class, OscillationKubeBinding::wrapOutput);
	}
}
