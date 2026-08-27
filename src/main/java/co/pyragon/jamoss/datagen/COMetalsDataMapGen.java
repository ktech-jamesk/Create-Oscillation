package co.pyragon.jamoss.datagen;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllItems;

import co.pyragon.jamoss.content.ore.MetalEntry;
import co.pyragon.jamoss.content.ore.Metals;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.DataMapProvider;

/** Built-in metals: keyed by raw ore item. Packs extend this file with their own entries. */
public class COMetalsDataMapGen extends DataMapProvider {

	public COMetalsDataMapGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
		super(output, lookup);
	}

	@Override
	protected void gather(HolderLookup.Provider provider) {
		var b = builder(Metals.METALS);
		add(b, Items.RAW_IRON, "iron", 0xD8AF93);
		add(b, Items.RAW_GOLD, "gold", 0xFCEE4B);
		add(b, Items.RAW_COPPER, "copper", 0xE0734D);
		add(b, AllItems.RAW_ZINC.get(), "zinc", 0xBFD6C9);
	}

	private static void add(Builder<MetalEntry, Item> b, Item rawOre, String metal, int colour) {
		b.add(BuiltInRegistries.ITEM.wrapAsHolder(rawOre), new MetalEntry(metal, colour), false);
	}
}
