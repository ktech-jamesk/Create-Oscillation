package co.pyragon.jamoss.content.ore;

import org.jetbrains.annotations.Nullable;

import co.pyragon.jamoss.registry.CODataComponents;
import co.pyragon.jamoss.registry.COFluids;
import co.pyragon.jamoss.registry.COItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/** Builders for the generic, metal-tagged stacks used by the ore chain. */
public class MetalStacks {

	@Nullable
	public static String metal(FluidStack stack) {
		return stack.get(CODataComponents.METAL.get());
	}

	@Nullable
	public static String metal(ItemStack stack) {
		return stack.get(CODataComponents.METAL.get());
	}

	public static FluidStack slurry(String metal, int amount) {
		FluidStack stack = new FluidStack((net.minecraft.world.level.material.Fluid) COFluids.ORE_SLURRY.getSource(), amount);
		stack.set(CODataComponents.METAL.get(), metal);
		return stack;
	}

	public static FluidStack vapour(String metal, int amount) {
		FluidStack stack = new FluidStack((net.minecraft.world.level.material.Fluid) COFluids.METAL_VAPOUR.getSource(), amount);
		stack.set(CODataComponents.METAL.get(), metal);
		return stack;
	}

	public static ItemStack concentrate(String metal, int count) {
		ItemStack stack = new ItemStack(COItems.METAL_CONCENTRATE.get(), count);
		stack.set(CODataComponents.METAL.get(), metal);
		return stack;
	}
}
