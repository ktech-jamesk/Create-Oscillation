package co.pyragon.jamoss.datagen;

import java.util.concurrent.CompletableFuture;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.compat.ponder.COPonderPlugin;
import com.tterrag.registrate.providers.ProviderType;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class CODatagen {
	/**
	 * Must run at mod construction: Registrate builds its providers before GatherDataEvent
	 * listeners fire. Ponder text is generated from the scene code, so the plugin is added here
	 * too (FMLClientSetupEvent does not run during datagen).
	 */
	public static void addExtraRegistrateData() {
		CreateOscillation.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {
			PonderIndex.addPlugin(new COPonderPlugin());
			PonderIndex.getLangAccess().provideLang(CreateOscillation.MOD_ID, provider::add);
		});
	}

	public static void gatherData(GatherDataEvent event) {
		if (!event.getMods().contains(CreateOscillation.MOD_ID))
			return;
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

		generator.addProvider(event.includeServer(), new COResonatingRecipeGen(output, lookup));
		generator.addProvider(event.includeServer(), new COCondensingRecipeGen(output, lookup));
		generator.addProvider(event.includeServer(), new COSiftingRecipeGen(output, lookup));
		generator.addProvider(event.includeServer(), new COCraftingRecipeGen(output, lookup));
		generator.addProvider(event.includeServer(), new COCavitatingRecipeGen(output, lookup));
		generator.addProvider(event.includeServer(), new COMetalsDataMapGen(output, lookup));
		generator.addProvider(event.includeClient(), new COPonderStructureGen(output));

	}
}
