package co.pyragon.jamoss.registry;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.canister.GasCanisterItem;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.content.ore.MetalConcentrateItem;
import net.minecraft.world.item.Item;

public class COItems {

	public static final ItemEntry<GasCanisterItem> GAS_CANISTER = CreateOscillation.REGISTRATE
		.item("gas_canister", GasCanisterItem::new)
		.properties(p -> p.stacksTo(1))
		.model(NonNullBiConsumer.noop())
		.lang("Gas Canister")
		.register();

	public static final ItemEntry<Item> ROUGH_QUARTZ_CRYSTAL = CreateOscillation.REGISTRATE
		.item("rough_quartz_crystal", Item::new)
		.lang("Rough Quartz Crystal")
		.register();

	public static final ItemEntry<Item> TUNED_CRYSTAL_LOW = tunedCrystal("low", "Low-Tuned Crystal");
	public static final ItemEntry<Item> TUNED_CRYSTAL_MID = tunedCrystal("mid", "Mid-Tuned Crystal");
	public static final ItemEntry<Item> TUNED_CRYSTAL_HIGH = tunedCrystal("high", "High-Tuned Crystal");
	public static final ItemEntry<Item> TUNED_CRYSTAL_ULTRASONIC = tunedCrystal("ultrasonic", "Ultrasonic Crystal");

	public static final ItemEntry<MetalConcentrateItem> METAL_CONCENTRATE = CreateOscillation.REGISTRATE
		.item("metal_concentrate", MetalConcentrateItem::new)
		.model(NonNullBiConsumer.noop())
		.lang("Metal Concentrate")
		.register();

	private static ItemEntry<Item> tunedCrystal(String band, String name) {
		return CreateOscillation.REGISTRATE.item("tuned_crystal_" + band, Item::new).lang(name).register();
	}

	/** Band of a Tuned Crystal item, or null for anything else. */
	@org.jetbrains.annotations.Nullable
	public static FrequencyBand bandOf(Item item) {
		if (item == TUNED_CRYSTAL_LOW.get()) return FrequencyBand.LOW;
		if (item == TUNED_CRYSTAL_MID.get()) return FrequencyBand.MID;
		if (item == TUNED_CRYSTAL_HIGH.get()) return FrequencyBand.HIGH;
		if (item == TUNED_CRYSTAL_ULTRASONIC.get()) return FrequencyBand.ULTRASONIC;
		return null;
	}

	/** The crystal produced by resonating a rough crystal in the given band. */
	public static ItemEntry<Item> tunedCrystal(FrequencyBand band) {
		return switch (band) {
			case LOW -> TUNED_CRYSTAL_LOW;
			case MID -> TUNED_CRYSTAL_MID;
			case HIGH -> TUNED_CRYSTAL_HIGH;
			case ULTRASONIC -> TUNED_CRYSTAL_ULTRASONIC;
			default -> throw new IllegalArgumentException("No crystal for " + band);
		};
	}

	public static void register() {}
}
