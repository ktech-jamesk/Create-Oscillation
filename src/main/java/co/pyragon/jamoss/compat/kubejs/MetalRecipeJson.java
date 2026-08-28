package co.pyragon.jamoss.compat.kubejs;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import co.pyragon.jamoss.content.frequency.FrequencyBand;
import net.minecraft.resources.ResourceLocation;

/**
 * Builds the recipe JSON for one metal's ore chain (slurry → vapour → concentrate → ingot) in the same
 * shape datagen writes for the built-in metals. Pure JSON so it works (and is testable) without KubeJS;
 * {@link OscillationKubeBinding} feeds it into KubeJS's virtual datapack.
 */
public class MetalRecipeJson {

	public static final String METAL_COMPONENT = "createoscillation:metal";
	private static final String ORE_SLURRY = "createoscillation:ore_slurry";
	private static final String METAL_VAPOUR = "createoscillation:metal_vapour";
	private static final String CONCENTRATE = "createoscillation:metal_concentrate";
	private static final String SONIC_MIST = "createoscillation:sonic_mist";
	private static final String WATER = "minecraft:water";

	/**
	 * Recipe id (without namespace prefix handling — callers decide the namespace) → recipe JSON.
	 * Keys are {@code slurry_<metal>}, {@code vapour_<metal>}, {@code concentrate_<metal>} and, when an ingot
	 * is given, {@code smelting/ingot_<metal>} and {@code blasting/ingot_<metal>}.
	 */
	public static Map<String, JsonObject> chain(String metal, ResourceLocation rawOre, @Nullable ResourceLocation ingot) {
		Map<String, JsonObject> out = new LinkedHashMap<>();
		out.put("slurry_" + metal, slurry(metal, rawOre));
		out.put("vapour_" + metal, vapour(metal));
		out.put("concentrate_" + metal, concentrate(metal));
		if (ingot != null) {
			out.put("smelting/ingot_" + metal, cooking("minecraft:smelting", metal, ingot, 200));
			out.put("blasting/ingot_" + metal, cooking("minecraft:blasting", metal, ingot, 100));
		}
		return out;
	}

	/** Raw ore + 250 mb water → 250 mb slurry, High band, 200 ticks. */
	public static JsonObject slurry(String metal, ResourceLocation rawOre) {
		JsonObject json = processing("createoscillation:resonating", FrequencyBand.HIGH, 200);
		JsonArray ingredients = new JsonArray();
		JsonObject ore = new JsonObject();
		ore.addProperty("item", rawOre.toString());
		ingredients.add(ore);
		ingredients.add(fluidIngredient(WATER, 250));
		json.add("ingredients", ingredients);
		JsonArray results = new JsonArray();
		results.add(metalFluid(ORE_SLURRY, metal, 250));
		json.add("results", results);
		return json;
	}

	/** 250 mb slurry + 250 mb mist → 250 mb vapour, Ultrasonic band, 200 ticks. */
	public static JsonObject vapour(String metal) {
		JsonObject json = processing("createoscillation:cavitating", FrequencyBand.ULTRASONIC, 200);
		JsonArray ingredients = new JsonArray();
		ingredients.add(metalFluidIngredient(ORE_SLURRY, metal, 250));
		ingredients.add(fluidIngredient(SONIC_MIST, 250));
		json.add("ingredients", ingredients);
		JsonArray results = new JsonArray();
		results.add(metalFluid(METAL_VAPOUR, metal, 250));
		json.add("results", results);
		return json;
	}

	/** 250 mb vapour → 2 concentrate + 50% of a third + 250 mb water, 100 ticks. */
	public static JsonObject concentrate(String metal) {
		JsonObject json = processing("createoscillation:condensing", null, 100);
		JsonArray ingredients = new JsonArray();
		ingredients.add(metalFluidIngredient(METAL_VAPOUR, metal, 250));
		json.add("ingredients", ingredients);
		JsonArray results = new JsonArray();
		JsonObject two = metalItem(metal);
		two.addProperty("count", 2);
		results.add(two);
		JsonObject chance = metalItem(metal);
		chance.addProperty("chance", 0.5);
		results.add(chance);
		JsonObject water = new JsonObject();
		water.addProperty("amount", 250);
		water.addProperty("id", WATER);
		results.add(water);
		json.add("results", results);
		return json;
	}

	/** Concentrate → ingot in a furnace or blast furnace. */
	public static JsonObject cooking(String type, String metal, ResourceLocation ingot, int time) {
		JsonObject json = new JsonObject();
		json.addProperty("type", type);
		json.addProperty("category", "misc");
		json.addProperty("cookingtime", time);
		json.addProperty("experience", 0.7);
		JsonObject ingredient = new JsonObject();
		ingredient.addProperty("type", "neoforge:components");
		ingredient.add("components", metalComponents(metal));
		ingredient.addProperty("items", CONCENTRATE);
		json.add("ingredient", ingredient);
		JsonObject result = new JsonObject();
		result.addProperty("count", 1);
		result.addProperty("id", ingot.toString());
		json.add("result", result);
		return json;
	}

	private static JsonObject processing(String type, @Nullable FrequencyBand band, int time) {
		JsonObject json = new JsonObject();
		json.addProperty("type", type);
		if (band != null)
			json.addProperty("frequency", band.getSerializedName());
		json.addProperty("processing_time", time);
		return json;
	}

	private static JsonObject metalComponents(String metal) {
		JsonObject components = new JsonObject();
		components.addProperty(METAL_COMPONENT, metal);
		return components;
	}

	private static JsonObject fluidIngredient(String fluid, int amount) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "neoforge:single");
		json.addProperty("amount", amount);
		json.addProperty("fluid", fluid);
		return json;
	}

	private static JsonObject metalFluidIngredient(String fluid, String metal, int amount) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "neoforge:components");
		json.addProperty("amount", amount);
		json.add("components", metalComponents(metal));
		json.addProperty("fluids", fluid);
		return json;
	}

	private static JsonObject metalFluid(String fluid, String metal, int amount) {
		JsonObject json = new JsonObject();
		json.addProperty("amount", amount);
		json.add("components", metalComponents(metal));
		json.addProperty("id", fluid);
		return json;
	}

	private static JsonObject metalItem(String metal) {
		JsonObject json = new JsonObject();
		json.add("components", metalComponents(metal));
		json.addProperty("id", CONCENTRATE);
		return json;
	}
}
