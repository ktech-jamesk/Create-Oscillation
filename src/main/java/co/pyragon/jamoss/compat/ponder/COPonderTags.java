package co.pyragon.jamoss.compat.ponder;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.registry.COBlocks;
import co.pyragon.jamoss.registry.COItems;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class COPonderTags {

	public static final ResourceLocation OSCILLATION = CreateOscillation.asResource("oscillation");

	public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
		PonderTagRegistrationHelper<RegistryEntry<?, ?>> h = helper.withKeyFunction(RegistryEntry::getId);

		helper.registerTag(OSCILLATION)
			.addToIndex()
			.item(COBlocks.RESONATOR.get())
			.title("Oscillation")
			.description("Vibration-driven processing and gases")
			.register();

		h.addToTag(OSCILLATION)
			.add(COBlocks.RESONATOR)
			.add(COBlocks.RESONANCE_CHAMBER)
			.add(COBlocks.RESONANCE_PUMP)
			.add(COBlocks.CONDENSER)
			.add(COBlocks.VENT)
			.add(COBlocks.VIBRATING_SIEVE)
			.add(COBlocks.TUNING_FORK)
			.add(COBlocks.CAVITATION_CHAMBER)
			.add(COBlocks.RESONANCE_EMITTER)
			.add(COBlocks.RESONANCE_RECEIVER)
			.add(COBlocks.RESONANCE_AMPLIFIER)
			.add(COBlocks.SONIC_PULVERISER)
			.add(COItems.ROUGH_QUARTZ_CRYSTAL)
			.add(COItems.GAS_CANISTER);

		h.addToTag(AllCreatePonderTags.KINETIC_APPLIANCES)
			.add(COBlocks.RESONATOR)
			.add(COBlocks.TUNING_FORK)
			.add(COBlocks.RESONANCE_RECEIVER)
			.add(COBlocks.SONIC_PULVERISER)
			.add(COBlocks.RESONANCE_PUMP);

		h.addToTag(AllCreatePonderTags.FLUIDS)
			.add(COBlocks.RESONANCE_PUMP)
			.add(COBlocks.CONDENSER)
			.add(COBlocks.VENT)
			.add(COItems.GAS_CANISTER);
	}
}
