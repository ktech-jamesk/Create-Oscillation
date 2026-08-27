package co.pyragon.jamoss.compat.ponder;

import co.pyragon.jamoss.CreateOscillation;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/** Registers Oscillation's Ponder scenes and index tag. Added in client setup and in datagen (for lang). */
public class COPonderPlugin implements PonderPlugin {

	@Override
	public String getModId() {
		return CreateOscillation.MOD_ID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		COPonderScenes.register(helper);
	}

	@Override
	public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
		COPonderTags.register(helper);
	}
}
