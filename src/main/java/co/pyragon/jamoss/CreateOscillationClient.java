package co.pyragon.jamoss;

import co.pyragon.jamoss.compat.ponder.COPonderPlugin;
import co.pyragon.jamoss.content.ore.MetalConcentrateItem;
import net.createmod.ponder.foundation.PonderIndex;

import co.pyragon.jamoss.content.canister.GasCanisterItem;
import co.pyragon.jamoss.registry.COItems;
import co.pyragon.jamoss.registry.COPartialModels;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

@Mod(value = CreateOscillation.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CreateOscillation.MOD_ID, value = Dist.CLIENT)
public class CreateOscillationClient {

	public CreateOscillationClient() {
		COPartialModels.init();
	}

	/** Tint the canister's window (layer 1) with the contained gas's colour. */
	@SubscribeEvent
	static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
		event.register((stack, layer) -> {
			if (layer != 1)
				return -1;
			FluidStack content = GasCanisterItem.getContent(stack);
			if (content.isEmpty())
				return 0xFF6B7A86;
			return IClientFluidTypeExtensions.of(content.getFluid()).getTintColor(content);
		}, COItems.GAS_CANISTER.get());
		event.register(MetalConcentrateItem::colour, COItems.METAL_CONCENTRATE.get());
	}

	@SubscribeEvent
	static void onClientSetup(FMLClientSetupEvent event) {
		PonderIndex.addPlugin(new COPonderPlugin());
	}
}
