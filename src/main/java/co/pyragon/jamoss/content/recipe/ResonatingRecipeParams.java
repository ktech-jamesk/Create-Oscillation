package co.pyragon.jamoss.content.recipe;

import java.util.function.Function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import co.pyragon.jamoss.content.frequency.FrequencyBand;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Create's processing params plus an optional {@code frequency} band (default {@code any}). */
public class ResonatingRecipeParams extends ProcessingRecipeParams {

	public static final MapCodec<ResonatingRecipeParams> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		codec(ResonatingRecipeParams::new).forGetter(Function.identity()),
		FrequencyBand.CODEC.optionalFieldOf("frequency", FrequencyBand.ANY).forGetter(ResonatingRecipeParams::frequency)
	).apply(instance, (params, frequency) -> {
		params.frequency = frequency;
		return params;
	}));
	public static final StreamCodec<RegistryFriendlyByteBuf, ResonatingRecipeParams> STREAM_CODEC = streamCodec(ResonatingRecipeParams::new);

	protected FrequencyBand frequency = FrequencyBand.ANY;

	public ResonatingRecipeParams() {
	}

	public FrequencyBand frequency() {
		return frequency;
	}

	public void setFrequency(FrequencyBand frequency) {
		this.frequency = frequency;
	}

	@Override
	protected void encode(RegistryFriendlyByteBuf buffer) {
		super.encode(buffer);
		FrequencyBand.STREAM_CODEC.encode(buffer, frequency);
	}

	@Override
	protected void decode(RegistryFriendlyByteBuf buffer) {
		super.decode(buffer);
		frequency = FrequencyBand.STREAM_CODEC.decode(buffer);
	}
}
