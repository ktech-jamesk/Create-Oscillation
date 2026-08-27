package co.pyragon.jamoss.content.coupler;

import org.jetbrains.annotations.Nullable;

import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.registry.COItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/** A single slot that only accepts Tuned Crystals; the crystal's band tunes the coupler block. */
public class CrystalSlot extends ItemStackHandler {

	private final Runnable onChange;

	public CrystalSlot(Runnable onChange) {
		super(1);
		this.onChange = onChange;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return COItems.bandOf(stack.getItem()) != null;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 1;
	}

	@Override
	protected void onContentsChanged(int slot) {
		onChange.run();
	}

	@Nullable
	public FrequencyBand band() {
		return COItems.bandOf(getStackInSlot(0).getItem());
	}

	public void write(CompoundTag tag, HolderLookup.Provider registries) {
		tag.put("Crystal", serializeNBT(registries));
	}

	public void read(CompoundTag tag, HolderLookup.Provider registries) {
		if (tag.contains("Crystal"))
			deserializeNBT(registries, tag.getCompound("Crystal"));
	}
}
