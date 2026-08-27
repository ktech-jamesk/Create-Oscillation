package co.pyragon.jamoss.registry;

import co.pyragon.jamoss.CreateOscillation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class COFluidTags {
	/** Every fluid Harmonics treats as a gas. Other mods can add their gases here. */
	public static final TagKey<Fluid> GASES = TagKey.create(Registries.FLUID, CreateOscillation.asResource("gases"));

	public static boolean isGas(FluidStack stack) {
		return !stack.isEmpty() && stack.is(GASES);
	}
}
