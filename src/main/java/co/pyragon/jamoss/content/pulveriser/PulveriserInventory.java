package co.pyragon.jamoss.content.pulveriser;

import co.pyragon.jamoss.registry.COItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Slot 0: Tuned Crystals waiting to be burned (any tier, stackable). Slot 1: Rough Quartz Crystals
 * left behind by spent ones. The exposed handler only inserts into 0 and only extracts from 1, so
 * hoppers, funnels and contraption storage can keep a Pulveriser fed.
 */
public class PulveriserInventory extends ItemStackHandler {

	public static final int CRYSTALS = 0;
	public static final int SPENT = 1;

	private final Runnable onChange;
	private final IItemHandler external = new External();

	public PulveriserInventory(Runnable onChange) {
		super(2);
		this.onChange = onChange;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return slot == CRYSTALS ? COItems.bandOf(stack.getItem()) != null : stack.is(COItems.ROUGH_QUARTZ_CRYSTAL.get());
	}

	@Override
	protected void onContentsChanged(int slot) {
		onChange.run();
	}

	public IItemHandler external() {
		return external;
	}

	/** Takes one crystal from the input, or empty if none. */
	public ItemStack takeCrystal() {
		return extractItem(CRYSTALS, 1, false);
	}

	/** Puts a spent crystal's remains in the output; returns what did not fit. */
	public ItemStack addSpent(ItemStack rough) {
		return insertItem(SPENT, rough, false);
	}

	private class External implements IItemHandler {
		@Override
		public int getSlots() {
			return 2;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return PulveriserInventory.this.getStackInSlot(slot);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return slot == CRYSTALS ? PulveriserInventory.this.insertItem(slot, stack, simulate) : stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return slot == SPENT ? PulveriserInventory.this.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return slot == CRYSTALS && PulveriserInventory.this.isItemValid(slot, stack);
		}
	}
}
