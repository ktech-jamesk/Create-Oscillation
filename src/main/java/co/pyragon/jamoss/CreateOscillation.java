package co.pyragon.jamoss;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;

import co.pyragon.jamoss.content.chamber.ResonanceChamberBlockEntity;
import co.pyragon.jamoss.content.canister.GasCanisterItem;
import co.pyragon.jamoss.content.condenser.CondenserBlockEntity;
import co.pyragon.jamoss.content.sieve.VibratingSieveBlockEntity;
import co.pyragon.jamoss.content.vent.VentBlockEntity;
import co.pyragon.jamoss.datagen.CODatagen;
import co.pyragon.jamoss.content.ore.Metals;
import co.pyragon.jamoss.registry.COBlockEntityTypes;
import co.pyragon.jamoss.registry.COBlocks;
import co.pyragon.jamoss.registry.COCreativeTabs;
import co.pyragon.jamoss.registry.CODataComponents;
import co.pyragon.jamoss.registry.COItems;
import co.pyragon.jamoss.registry.COFluids;
import co.pyragon.jamoss.registry.CORecipeTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(CreateOscillation.MOD_ID)
public class CreateOscillation {
	public static final String MOD_ID = "createoscillation";
	public static final String NAME = "Create: Oscillation";
	public static final Logger LOGGER = LogUtils.getLogger();

	/** Registrate instance, the same registration helper Create itself uses. */
	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

	public CreateOscillation(IEventBus modEventBus, ModContainer modContainer) {
		REGISTRATE.registerEventListeners(modEventBus);
		CODatagen.addExtraRegistrateData();
		REGISTRATE.addRawLang("itemGroup.createoscillation", NAME);
		REGISTRATE.addRawLang("createoscillation.recipe.resonating", "Resonating");
		REGISTRATE.addRawLang("createoscillation.recipe.condensing", "Condensing");
		REGISTRATE.addRawLang("createoscillation.recipe.sifting", "Sifting");
		REGISTRATE.addRawLang("createoscillation.recipe.cavitating", "Cavitating");
		REGISTRATE.addRawLang("fluid.createoscillation.ore_slurry.named", "%s Slurry");
		REGISTRATE.addRawLang("fluid.createoscillation.metal_vapour.named", "%s Vapour");
		REGISTRATE.addRawLang("item.createoscillation.metal_concentrate.named", "%s Concentrate");
		REGISTRATE.addRawLang("createoscillation.metal.unknown", "Unknown Metal");
		REGISTRATE.addRawLang("createoscillation.metal.iron", "Iron");
		REGISTRATE.addRawLang("createoscillation.metal.gold", "Gold");
		REGISTRATE.addRawLang("createoscillation.metal.copper", "Copper");
		REGISTRATE.addRawLang("createoscillation.metal.zinc", "Zinc");
		REGISTRATE.addRawLang("item.createoscillation.gas_canister.filled", "Gas Canister (%s)");
		REGISTRATE.addRawLang("item.createoscillation.gas_canister.empty", "Empty");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.frequency", "Frequency:");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.tuning_fork", "Tuned to:");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.coupler.no_crystal", "no crystal");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.pulveriser.no_crystal", "Needs a Tuned Crystal");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.pulveriser.no_rotation", "Not turning");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.pulveriser.fuel", "Fuel: %s / %s");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.pulveriser.nothing", "Nothing breakable in reach");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.pulveriser.working", "Pulverising");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.coupler.linked", "Linked to a receiver %s blocks away");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.coupler.searching", "Emitting, no receiver in reach");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.coupler.idle", "Idle");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.coupler.receiving", "Receiving vibration");
		REGISTRATE.addRawLang("createoscillation.gui.tuning_fork.band", "Tuned Frequency");
		REGISTRATE.addRawLang("createoscillation.recipe.frequency", "Frequency: %s");
		REGISTRATE.addRawLang("createoscillation.frequency.any", "Any");
		REGISTRATE.addRawLang("createoscillation.frequency.low", "Low");
		REGISTRATE.addRawLang("createoscillation.frequency.mid", "Mid");
		REGISTRATE.addRawLang("createoscillation.frequency.high", "High");
		REGISTRATE.addRawLang("createoscillation.frequency.ultrasonic", "Ultrasonic");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.condenser", "Condenser Info:");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.condenser.gas", "Gas:");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.condenser.liquid", "Condensed:");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.condenser.items", "%s item(s) waiting to be collected");
		REGISTRATE.addRawLang("createoscillation.gui.goggles.condenser.warm", "Warmed up");

		COCreativeTabs.register(modEventBus);
		CODataComponents.register(modEventBus);
		// Our tab lists items itself; stop Registrate from also adding every item to the search tab
		// (vanilla already mirrors all tabs into search, which would trip NeoForge's duplicate check).
		REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
		REGISTRATE.setCreativeTab(COCreativeTabs.BASE);
		COItems.register();
		COFluids.register();
		COBlocks.register();
		COBlockEntityTypes.register();
		CORecipeTypes.register(modEventBus);

		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(ResonanceChamberBlockEntity::registerCapabilities);
		modEventBus.addListener(CondenserBlockEntity::registerCapabilities);
		modEventBus.addListener(VibratingSieveBlockEntity::registerCapabilities);
		modEventBus.addListener(VentBlockEntity::registerCapabilities);
		modEventBus.addListener(co.pyragon.jamoss.content.coupler.ResonanceEmitterBlockEntity::registerCapabilities);
		modEventBus.addListener(co.pyragon.jamoss.content.coupler.ResonanceReceiverBlockEntity::registerCapabilities);
		modEventBus.addListener(co.pyragon.jamoss.content.pulveriser.SonicPulveriserBlockEntity::registerCapabilities);
		modEventBus.addListener((RegisterCapabilitiesEvent event) -> GasCanisterItem.registerCapabilities(event, COItems.GAS_CANISTER.get()));
		modEventBus.addListener(CODatagen::gatherData);
		modEventBus.addListener(Metals::register);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		LOGGER.info("{} loaded", NAME);
	}

	public static ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
