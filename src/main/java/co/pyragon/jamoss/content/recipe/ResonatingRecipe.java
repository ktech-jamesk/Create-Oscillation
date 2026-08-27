package co.pyragon.jamoss.content.recipe;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.registry.CORecipeTypes;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * A recipe processed in a Resonance Chamber under a spinning Resonator. Reuses Create's basin
 * matching/applying so items and fluids behave like mixing recipes, and adds a required
 * {@link FrequencyBand} ({@code any} by default).
 */
public class ResonatingRecipe extends BasinRecipe {

	private final FrequencyBand band;

	public ResonatingRecipe(ProcessingRecipeParams params) {
		this(CORecipeTypes.RESONATING, params);
	}

	protected ResonatingRecipe(IRecipeTypeInfo type, ProcessingRecipeParams params) {
		super(type, params);
		this.band = params instanceof ResonatingRecipeParams p ? p.frequency() : FrequencyBand.ANY;
	}

	public FrequencyBand getBand() {
		return band;
	}

	@Override
	protected int getMaxInputCount() {
		return 4;
	}

	@Override
	protected int getMaxOutputCount() {
		return 2;
	}

	@Override
	protected int getMaxFluidInputCount() {
		return 2;
	}

	@Override
	protected int getMaxFluidOutputCount() {
		return 2;
	}

	@Override
	protected boolean canRequireHeat() {
		return false;
	}

	@Override
	protected boolean canSpecifyDuration() {
		return true;
	}

	/** BasinRecipe pins the params type, so this serializer carries the banded params itself. */
	public static class Serializer<R extends ResonatingRecipe> implements RecipeSerializer<R> {

		private final MapCodec<R> codec;
		private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

		public Serializer(Function<ProcessingRecipeParams, R> factory) {
			codec = ResonatingRecipeParams.CODEC.xmap(factory::apply, r -> (ResonatingRecipeParams) r.getParams());
			streamCodec = ResonatingRecipeParams.STREAM_CODEC.map(factory::apply, r -> (ResonatingRecipeParams) r.getParams());
		}

		@Override
		public MapCodec<R> codec() {
			return codec;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
			return streamCodec;
		}
	}
}
