package co.pyragon.jamoss.registry;

import java.util.function.Supplier;

import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.recipe.CavitatingRecipe;
import co.pyragon.jamoss.content.recipe.CondensingRecipe;
import co.pyragon.jamoss.content.recipe.ResonatingRecipe;
import co.pyragon.jamoss.content.recipe.SiftingRecipe;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public enum CORecipeTypes implements IRecipeTypeInfo {

	RESONATING(() -> new ResonatingRecipe.Serializer<>(ResonatingRecipe::new)),
	CAVITATING(() -> new ResonatingRecipe.Serializer<>(CavitatingRecipe::new)),
	CONDENSING(CondensingRecipe::new),
	SIFTING(SiftingRecipe::new);

	public final ResourceLocation id;
	private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> serializerObject;
	private final DeferredHolder<RecipeType<?>, RecipeType<?>> typeObject;

	CORecipeTypes(StandardProcessingRecipe.Factory<?> factory) {
		this(() -> new StandardProcessingRecipe.Serializer<>(factory));
	}

	CORecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier) {
		String name = Lang.asId(name());
		id = CreateOscillation.asResource(name);
		serializerObject = Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);
		typeObject = Registers.TYPE_REGISTER.register(name, () -> RecipeType.simple(id));
	}

	public static void register(IEventBus modEventBus) {
		Registers.SERIALIZER_REGISTER.register(modEventBus);
		Registers.TYPE_REGISTER.register(modEventBus);
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RecipeSerializer<?>> T getSerializer() {
		return (T) serializerObject.get();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
		return (RecipeType<R>) typeObject.get();
	}

	private static class Registers {
		private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER =
			DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, CreateOscillation.MOD_ID);
		private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER =
			DeferredRegister.create(Registries.RECIPE_TYPE, CreateOscillation.MOD_ID);
	}
}
