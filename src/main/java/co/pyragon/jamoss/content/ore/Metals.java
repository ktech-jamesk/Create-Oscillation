package co.pyragon.jamoss.content.ore;

import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.pyragon.jamoss.CreateOscillation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/**
 * Data-driven metal registry. Packs add a metal with a data map entry keyed by its raw ore item
 * ({@code data/<ns>/data_maps/item/createoscillation/metals.json}) plus recipes; see the README.
 */
public class Metals {

	public static final int DEFAULT_COLOUR = 0x9A9A9A;

	public static final DataMapType<Item, MetalEntry> METALS = DataMapType
		.builder(CreateOscillation.asResource("metals"), Registries.ITEM, MetalEntry.CODEC)
		.synced(MetalEntry.CODEC, false)
		.build();

	public static void register(RegisterDataMapTypesEvent event) {
		event.register(METALS);
	}

	public static Map<ResourceKey<Item>, MetalEntry> all() {
		return BuiltInRegistries.ITEM.getDataMap(METALS);
	}

	/** Metal id of a raw ore item, or null if it is not a registered ore. */
	@Nullable
	public static String metalOf(ItemStack stack) {
		MetalEntry entry = stack.getItemHolder().getData(METALS);
		return entry == null ? null : entry.metal();
	}

	public static int colour(@Nullable String metal) {
		if (metal == null)
			return DEFAULT_COLOUR;
		for (MetalEntry entry : all().values())
			if (entry.metal().equals(metal))
				return entry.colour();
		return DEFAULT_COLOUR;
	}

	/** "Iron" — from lang {@code createoscillation.metal.<id>} when present, else the capitalised id. */
	public static Component name(@Nullable String metal) {
		if (metal == null)
			return Component.translatable("createoscillation.metal.unknown");
		String key = "createoscillation.metal." + metal;
		if (net.minecraft.locale.Language.getInstance().has(key))
			return Component.translatable(key);
		String pretty = metal.replace('_', ' ');
		return Component.literal(pretty.substring(0, 1).toUpperCase(Locale.ROOT) + pretty.substring(1));
	}
}
