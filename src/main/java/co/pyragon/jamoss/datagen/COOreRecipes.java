package co.pyragon.jamoss.datagen;

import java.util.List;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import com.simibubi.create.AllItems;

/** The built-in metals the recipe generators emit chains for. Must match {@link COMetalsDataMapGen}. */
public class COOreRecipes {

	public record Metal(String id, Item ingot) {}

	public static final List<Metal> METALS = List.of(
		new Metal("iron", Items.IRON_INGOT),
		new Metal("gold", Items.GOLD_INGOT),
		new Metal("copper", Items.COPPER_INGOT),
		new Metal("zinc", AllItems.ZINC_INGOT.get()));

	/** {@code c:raw_materials/<metal>} — the conventional tag for raw ores. */
	public static net.minecraft.tags.TagKey<Item> rawOreTag(String metal) {
		return net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
			net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "raw_materials/" + metal));
	}
}
