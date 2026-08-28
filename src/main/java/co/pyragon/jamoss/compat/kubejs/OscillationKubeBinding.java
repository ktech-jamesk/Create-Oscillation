package co.pyragon.jamoss.compat.kubejs;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

import com.google.gson.JsonObject;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;

import co.pyragon.jamoss.content.ore.MetalEntry;
import co.pyragon.jamoss.content.ore.Metals;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.ItemWrapper;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.StringUtilsWrapper;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * The {@code Oscillation} script binding. Use it in {@code ServerEvents.generateData}:
 *
 * <pre>
 * ServerEvents.generateData('after_mods', event => {
 *   Oscillation.addMetal(event, 'tin', 'somemod:raw_tin', 'somemod:tin_ingot', '#C8D0D8')
 * })
 * </pre>
 *
 * Also {@link #output(ItemStack, double)} for chance outputs in {@code ServerEvents.recipes}:
 * {@code Oscillation.output('minecraft:flint', 0.3)} (the JSON form {@code {id: 'minecraft:flint', chance: 0.3}} works too).
 *
 * One call registers the metal in the {@code createoscillation:metals} data map and writes the whole
 * ore chain as datapack recipes under {@code kubejs:createoscillation/*}, so they load like any other
 * recipe and can still be removed or edited in {@code ServerEvents.recipes}.
 */
public class OscillationKubeBinding {

	private OscillationKubeBinding() {
	}

	/** Full chain including smelting/blasting of the concentrate into {@code ingot}. */
	public static void addMetal(KubeDataGenerator event, String metal, ResourceLocation rawOre, ResourceLocation ingot, String colour) {
		add(event, metal, rawOre, ingot, colour);
	}

	/** Chain without the smelting/blasting recipes, for packs that route the concentrate elsewhere. */
	public static void addMetal(KubeDataGenerator event, String metal, ResourceLocation rawOre, String colour) {
		add(event, metal, rawOre, null, colour);
	}

	private static void add(KubeDataGenerator event, String metal, ResourceLocation rawOre, @Nullable ResourceLocation ingot, String colour) {
		if (metal == null || metal.isBlank() || !metal.equals(metal.toLowerCase(java.util.Locale.ROOT)))
			throw new IllegalArgumentException("Metal id must be lowercase, e.g. 'tin': " + metal);
		Item ore = BuiltInRegistries.ITEM.getOptional(rawOre)
			.orElseThrow(() -> new IllegalArgumentException("Unknown raw ore item: " + rawOre));
		if (ingot != null && !BuiltInRegistries.ITEM.containsKey(ingot))
			throw new IllegalArgumentException("Unknown ingot item: " + ingot);

		event.dataMap(Metals.METALS, file -> file.add(ore, new MetalEntry(metal, parseColour(colour))));
		MetalRecipeJson.chain(metal, rawOre, ingot).forEach((name, json) ->
			event.json(ResourceLocation.fromNamespaceAndPath("kubejs", "recipe/createoscillation/" + name), json));
	}

	/** A recipe output that appears with probability {@code chance} (0–1). */
	public static ProcessingOutput output(ItemStack stack, double chance) {
		return new ProcessingOutput(stack, (float) Mth.clamp(chance, 0.0, 1.0));
	}

	public static ProcessingOutput output(ItemStack stack) {
		return new ProcessingOutput(stack, 1F);
	}

	/** Type wrapper: anything item-like becomes a full-chance output; maps/JSON with {@code chance} also accept an {@code output} item. */
	@HideFromJS
	public static ProcessingOutput wrapOutput(Context cx, @Nullable Object from) {
		return switch (from) {
			case null -> ProcessingOutput.EMPTY;
			case ProcessingOutput o -> o;
			case ItemStack s -> s.isEmpty() ? ProcessingOutput.EMPTY : new ProcessingOutput(s, 1F);
			case ItemLike i -> new ProcessingOutput(i.asItem(), 1, 1F);
			case JsonObject json when json.has("chance") -> fromMap(cx, json, json.has("output") ? json.get("output") : json, json.get("chance"));
			case Map<?, ?> map when map.containsKey("chance") -> fromMap(cx, map, map.containsKey("output") ? map.get("output") : map, map.get("chance"));
			default -> new ProcessingOutput(ItemWrapper.wrap(cx, from), 1F);
		};
	}

	private static ProcessingOutput fromMap(Context cx, Object whole, Object item, Object chance) {
		float c = (float) Mth.clamp(StringUtilsWrapper.parseDouble(chance, 1.0), 0.0, 1.0);
		return new ProcessingOutput(ItemWrapper.wrap(cx, item), c);
	}

	static int parseColour(String colour) {
		try {
			return Integer.parseInt(colour.replace("#", "").replace("0x", ""), 16) & 0xFFFFFF;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Colour must be a hex string like '#C8D0D8': " + colour);
		}
	}
}
