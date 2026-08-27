package co.pyragon.jamoss.registry;

import co.pyragon.jamoss.CreateOscillation;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CODataComponents {
	private static final DeferredRegister<DataComponentType<?>> REGISTER =
		DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, CreateOscillation.MOD_ID);

	/** Fluid held by a Gas Canister. */
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> CANISTER_CONTENT =
		REGISTER.register("canister_content", () -> DataComponentType.<SimpleFluidContent>builder()
			.persistent(SimpleFluidContent.CODEC)
			.networkSynchronized(SimpleFluidContent.STREAM_CODEC)
			.build());

	/** Metal id carried by ore slurry, metal vapour and metal concentrate. */
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> METAL =
		REGISTER.register("metal", () -> DataComponentType.<String>builder()
			.persistent(com.mojang.serialization.Codec.STRING)
			.networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8)
			.build());

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
