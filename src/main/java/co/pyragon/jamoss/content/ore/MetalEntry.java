package co.pyragon.jamoss.content.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** One entry of the {@code createoscillation:metals} data map: which metal a raw ore belongs to, and its colour. */
public record MetalEntry(String metal, int colour) {

	private static final Codec<Integer> HEX_COLOUR = Codec.STRING.comapFlatMap(s -> {
		try {
			return com.mojang.serialization.DataResult.success(Integer.parseInt(s.replace("#", ""), 16));
		} catch (NumberFormatException e) {
			return com.mojang.serialization.DataResult.error(() -> "Not a hex colour: " + s);
		}
	}, i -> String.format("#%06X", i & 0xFFFFFF));

	public static final Codec<MetalEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.fieldOf("metal").forGetter(MetalEntry::metal),
		HEX_COLOUR.optionalFieldOf("colour", 0x9A9A9A).forGetter(MetalEntry::colour)
	).apply(i, MetalEntry::new));
}
