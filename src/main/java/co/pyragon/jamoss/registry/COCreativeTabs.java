package co.pyragon.jamoss.registry;

import co.pyragon.jamoss.CreateOscillation;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class COCreativeTabs {
	private static final DeferredRegister<CreativeModeTab> REGISTER =
		DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateOscillation.MOD_ID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BASE = REGISTER.register("base",
		() -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.createoscillation"))
			.icon(() -> COBlocks.RESONATOR.asStack())
			.displayItems((params, output) -> {
				output.accept(COBlocks.RESONANCE_CHAMBER.asStack());
				output.accept(COBlocks.RESONATOR.asStack());
				output.accept(COBlocks.RESONANCE_PUMP.asStack());
				output.accept(COBlocks.CONDENSER.asStack());
				output.accept(COBlocks.VIBRATING_SIEVE.asStack());
				output.accept(COBlocks.VENT.asStack());
				output.accept(COItems.GAS_CANISTER.asStack());
				output.acceptAll(co.pyragon.jamoss.content.canister.GasCanisterItem.allFilled(COItems.GAS_CANISTER.get()));
				output.accept(COBlocks.TUNING_FORK.asStack());
				output.accept(COBlocks.CAVITATION_CHAMBER.asStack());
				output.accept(COBlocks.RESONANCE_EMITTER.asStack());
				output.accept(COBlocks.RESONANCE_RECEIVER.asStack());
				output.accept(COBlocks.RESONANCE_AMPLIFIER.asStack());
				output.accept(COBlocks.SONIC_PULVERISER.asStack());
				output.accept(COItems.ROUGH_QUARTZ_CRYSTAL.asStack());
				output.accept(COItems.TUNED_CRYSTAL_LOW.asStack());
				output.accept(COItems.TUNED_CRYSTAL_MID.asStack());
				output.accept(COItems.TUNED_CRYSTAL_HIGH.asStack());
				output.accept(COItems.TUNED_CRYSTAL_ULTRASONIC.asStack());
				for (var entry : co.pyragon.jamoss.content.ore.Metals.all().values())
					output.accept(co.pyragon.jamoss.content.ore.MetalStacks.concentrate(entry.metal(), 1));
			})
			.build());

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
