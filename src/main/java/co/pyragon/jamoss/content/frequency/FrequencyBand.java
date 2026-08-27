package co.pyragon.jamoss.content.frequency;

import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * The frequency a Resonator vibrates at, derived from its RPM. Bands double like Create's gearing:
 * Low 32+, Mid 64+, High 128+, Ultrasonic 256. {@link #ANY} is a recipe-side wildcard only.
 */
public enum FrequencyBand implements StringRepresentable, INamedIconOptions {

	ANY(0, AllIcons.I_NONE),
	LOW(32, AllIcons.I_PRIORITY_VERY_LOW),
	MID(64, AllIcons.I_PRIORITY_LOW),
	HIGH(128, AllIcons.I_PRIORITY_HIGH),
	ULTRASONIC(256, AllIcons.I_PRIORITY_VERY_HIGH);

	public static final Codec<FrequencyBand> CODEC = StringRepresentable.fromEnum(FrequencyBand::values);
	public static final StreamCodec<ByteBuf, FrequencyBand> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], FrequencyBand::ordinal);

	/** Lowest |speed| (RPM) that produces this band. */
	public final float minSpeed;
	private final AllIcons icon;

	FrequencyBand(float minSpeed, AllIcons icon) {
		this.minSpeed = minSpeed;
		this.icon = icon;
	}

	@Override
	public AllIcons getIcon() {
		return icon;
	}

	@Override
	public String getTranslationKey() {
		return langKey();
	}

	/** The band a resonator spinning at {@code speed} produces, or null below {@link #LOW}. */
	@Nullable
	public static FrequencyBand of(float speed) {
		float abs = Math.abs(speed);
		FrequencyBand result = null;
		for (FrequencyBand band : values())
			if (band != ANY && abs >= band.minSpeed)
				result = band;
		return result;
	}

	/** Whether a recipe requiring this band may run at {@code actual}. Exact match; {@link #ANY} takes any real band. */
	public boolean accepts(@Nullable FrequencyBand actual) {
		if (actual == null)
			return false;
		return this == ANY || this == actual;
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}

	public String langKey() {
		return "createoscillation.frequency." + getSerializedName();
	}

	public Component getDisplayName() {
		return Component.translatable(langKey());
	}
}
