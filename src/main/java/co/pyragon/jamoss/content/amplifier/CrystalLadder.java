package co.pyragon.jamoss.content.amplifier;

import org.jetbrains.annotations.Nullable;

import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.registry.COItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Four slots, one per band (Low, Mid, High, Ultrasonic), each taking only that band's Tuned
 * Crystal. The ladder band is the highest band whose every lower rung is filled.
 */
public class CrystalLadder extends ItemStackHandler {

	public static final FrequencyBand[] RUNGS = { FrequencyBand.LOW, FrequencyBand.MID, FrequencyBand.HIGH, FrequencyBand.ULTRASONIC };

	private final Runnable onChange;

	public CrystalLadder(Runnable onChange) {
		super(RUNGS.length);
		this.onChange = onChange;
	}

	public static int slotOf(FrequencyBand band) {
		for (int i = 0; i < RUNGS.length; i++)
			if (RUNGS[i] == band)
				return i;
		return -1;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return COItems.bandOf(stack.getItem()) == RUNGS[slot];
	}

	@Override
	public int getSlotLimit(int slot) {
		return 1;
	}

	@Override
	protected void onContentsChanged(int slot) {
		onChange.run();
	}

	/** Highest band reachable without a gap below it, or null when the Low rung is empty. */
	@Nullable
	public FrequencyBand band() {
		FrequencyBand result = null;
		for (int i = 0; i < RUNGS.length; i++) {
			if (getStackInSlot(i).isEmpty())
				break;
			result = RUNGS[i];
		}
		return result;
	}

	/** Index of the highest filled slot, or -1. */
	public int topFilled() {
		for (int i = RUNGS.length - 1; i >= 0; i--)
			if (!getStackInSlot(i).isEmpty())
				return i;
		return -1;
	}

	public void write(CompoundTag tag, HolderLookup.Provider registries) {
		tag.put("Crystals", serializeNBT(registries));
	}

	public void read(CompoundTag tag, HolderLookup.Provider registries) {
		if (tag.contains("Crystals"))
			deserializeNBT(registries, tag.getCompound("Crystals"));
	}
}
