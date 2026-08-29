package co.pyragon.jamoss.content.sieve;

import java.util.List;
import java.util.Optional;

import co.pyragon.jamoss.content.vibration.VibrationSource;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;

import co.pyragon.jamoss.content.recipe.SiftingRecipe;
import co.pyragon.jamoss.content.resonator.ResonatorBlockEntity;
import co.pyragon.jamoss.registry.COBlockEntityTypes;
import co.pyragon.jamoss.registry.CORecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

/**
 * Shaken by the Resonator directly above it. One input slot is sifted into a 9-slot buffer,
 * which is pushed into any inventory below (or extracted from the sides).
 */
public class VibratingSieveBlockEntity extends SmartBlockEntity {

	public static final float MIN_SPEED = 32;
	private static final int PUSH_INTERVAL = 8;

	public final ItemStackHandler inputInv = new ItemStackHandler(1);
	public final ItemStackHandler outputInv = new ItemStackHandler(9);
	private final IItemHandler itemHandler = new SieveInventoryHandler();

	public int timer;
	private SiftingRecipe lastRecipe;
	/** Synced for the renderer. */
	private float driveSpeed;

	public VibratingSieveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, COBlockEntityTypes.VIBRATING_SIEVE.get(),
			(be, context) -> be.itemHandler);
	}

	public IItemHandler getItemHandler() {
		return itemHandler;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));
	}

	public Optional<ResonatorBlockEntity> getResonator() {
		if (level == null)
			return Optional.empty();
		BlockEntity above = level.getBlockEntity(worldPosition.above());
		return above instanceof ResonatorBlockEntity resonator ? Optional.of(resonator) : Optional.empty();
	}

	/** Speed of the resonator above, 0 if absent or too slow. */
	public float getDriveSpeed() {
		return driveSpeed;
	}

	public boolean isShaking() {
		return driveSpeed != 0 && !inputInv.getStackInSlot(0).isEmpty();
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide) {
			if (isShaking())
				spawnParticles();
			return;
		}

		float speed = VibrationSource.speedAbove(level, worldPosition);
		if (speed < MIN_SPEED)
			speed = 0;
		if (speed != driveSpeed) {
			driveSpeed = speed;
			sendData();
		}

		if (level.getGameTime() % PUSH_INTERVAL == 0)
			pushOutputsBelow();

		if (driveSpeed == 0)
			return;
		for (int i = 0; i < outputInv.getSlots(); i++)
			if (outputInv.getStackInSlot(i).getCount() == outputInv.getSlotLimit(i))
				return;

		if (timer > 0) {
			timer -= getProcessingSpeed();
			if (timer <= 0)
				process();
			return;
		}

		if (inputInv.getStackInSlot(0).isEmpty())
			return;

		RecipeWrapper input = new RecipeWrapper(inputInv);
		if (lastRecipe == null || !lastRecipe.matches(input, level)) {
			Optional<RecipeHolder<SiftingRecipe>> recipe = find(input);
			if (recipe.isEmpty()) {
				timer = 100;
			} else {
				lastRecipe = recipe.get().value();
				timer = lastRecipe.getProcessingDuration();
			}
			sendData();
			return;
		}
		timer = lastRecipe.getProcessingDuration();
		sendData();
	}

	private Optional<RecipeHolder<SiftingRecipe>> find(RecipeWrapper input) {
		return level.getRecipeManager()
			.getRecipeFor(CORecipeTypes.SIFTING.<RecipeInput, SiftingRecipe>getType(), input, level);
	}

	private void process() {
		RecipeWrapper input = new RecipeWrapper(inputInv);
		if (lastRecipe == null || !lastRecipe.matches(input, level)) {
			Optional<RecipeHolder<SiftingRecipe>> recipe = find(input);
			if (recipe.isEmpty())
				return;
			lastRecipe = recipe.get().value();
		}
		ItemStack stack = inputInv.getStackInSlot(0);
		ItemStack remaining = stack.getCraftingRemainingItem();
		stack.shrink(1);
		inputInv.setStackInSlot(0, stack);
		lastRecipe.rollResults(level.random).forEach(result -> ItemHandlerHelper.insertItemStacked(outputInv, result, false));
		if (!remaining.isEmpty())
			ItemHandlerHelper.insertItemStacked(outputInv, remaining, false);
		sendData();
		setChanged();
	}

	/** What falls through the mesh lands in the inventory below. */
	private void pushOutputsBelow() {
		IItemHandler below = level.getCapability(Capabilities.ItemHandler.BLOCK, worldPosition.below(), Direction.UP);
		if (below == null)
			return;
		boolean changed = false;
		for (int slot = 0; slot < outputInv.getSlots(); slot++) {
			ItemStack stack = outputInv.getStackInSlot(slot);
			if (stack.isEmpty())
				continue;
			ItemStack remainder = ItemHandlerHelper.insertItemStacked(below, stack.copy(), false);
			if (remainder.getCount() != stack.getCount()) {
				outputInv.setStackInSlot(slot, remainder);
				changed = true;
			}
		}
		if (changed) {
			setChanged();
			sendData();
		}
	}

	public int getProcessingSpeed() {
		return Mth.clamp((int) (driveSpeed / 16f), 1, 512);
	}

	private boolean canProcess(ItemStack stack) {
		ItemStackHandler tester = new ItemStackHandler(1);
		tester.setStackInSlot(0, stack);
		RecipeWrapper input = new RecipeWrapper(tester);
		if (lastRecipe != null && lastRecipe.matches(input, level))
			return true;
		return find(input).isPresent();
	}

	private void spawnParticles() {
		ItemStack stack = inputInv.getStackInSlot(0);
		if (stack.isEmpty() || level.random.nextInt(3) != 0)
			return;
		ItemParticleOption data = new ItemParticleOption(ParticleTypes.ITEM, stack);
		double x = worldPosition.getX() + 0.25 + level.random.nextFloat() * 0.5;
		double z = worldPosition.getZ() + 0.25 + level.random.nextFloat() * 0.5;
		level.addParticle(data, x, worldPosition.getY() + 0.6, z, 0, 0.05, 0);
	}

	@Override
	public void destroy() {
		super.destroy();
		ItemHelper.dropContents(level, worldPosition, inputInv);
		ItemHelper.dropContents(level, worldPosition, outputInv);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		invalidateCapabilities();
	}

	@Override
	public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		compound.putInt("Timer", timer);
		compound.putFloat("DriveSpeed", driveSpeed);
		compound.put("InputInventory", inputInv.serializeNBT(registries));
		compound.put("OutputInventory", outputInv.serializeNBT(registries));
		super.write(compound, registries, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		timer = compound.getInt("Timer");
		driveSpeed = compound.getFloat("DriveSpeed");
		inputInv.deserializeNBT(registries, compound.getCompound("InputInventory"));
		outputInv.deserializeNBT(registries, compound.getCompound("OutputInventory"));
		super.read(compound, registries, clientPacket);
	}

	/** Insert only sift-able items into the input; extract only from the outputs. */
	private class SieveInventoryHandler extends CombinedInvWrapper {
		SieveInventoryHandler() {
			super(inputInv, outputInv);
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			if (outputInv == getHandlerFromIndex(getIndexForSlot(slot)))
				return false;
			return canProcess(stack) && super.isItemValid(slot, stack);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (outputInv == getHandlerFromIndex(getIndexForSlot(slot)) || !isItemValid(slot, stack))
				return stack;
			return super.insertItem(slot, stack, simulate);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (inputInv == getHandlerFromIndex(getIndexForSlot(slot)))
				return ItemStack.EMPTY;
			return super.extractItem(slot, amount, simulate);
		}
	}
}
