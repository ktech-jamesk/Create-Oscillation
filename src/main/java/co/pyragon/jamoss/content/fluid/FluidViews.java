package co.pyragon.jamoss.content.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** One-way views over fluid handlers, for capabilities exposed to pipes. */
public final class FluidViews {

	private FluidViews() {}

	/** Accepts fluid, never gives any back. */
	public record InsertOnly(IFluidHandler wrapped) implements IFluidHandler {
		public int getTanks() { return wrapped.getTanks(); }
		public FluidStack getFluidInTank(int tank) { return wrapped.getFluidInTank(tank); }
		public int getTankCapacity(int tank) { return wrapped.getTankCapacity(tank); }
		public boolean isFluidValid(int tank, FluidStack stack) { return wrapped.isFluidValid(tank, stack); }
		public int fill(FluidStack resource, FluidAction action) { return wrapped.fill(resource, action); }
		public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
		public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
	}

	/** Gives fluid out, never accepts any. */
	public record ExtractOnly(IFluidHandler wrapped) implements IFluidHandler {
		public int getTanks() { return wrapped.getTanks(); }
		public FluidStack getFluidInTank(int tank) { return wrapped.getFluidInTank(tank); }
		public int getTankCapacity(int tank) { return wrapped.getTankCapacity(tank); }
		public boolean isFluidValid(int tank, FluidStack stack) { return false; }
		public int fill(FluidStack resource, FluidAction action) { return 0; }
		public FluidStack drain(FluidStack resource, FluidAction action) { return wrapped.drain(resource, action); }
		public FluidStack drain(int maxDrain, FluidAction action) { return wrapped.drain(maxDrain, action); }
	}
}
