package co.pyragon.jamoss.content.condenser;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import co.pyragon.jamoss.content.recipe.CondensingRecipe;
import co.pyragon.jamoss.registry.COBlockEntityTypes;
import co.pyragon.jamoss.registry.COFluidTags;
import co.pyragon.jamoss.registry.CORecipeTypes;
import com.simibubi.create.foundation.utility.CreateLang;

import co.pyragon.jamoss.CreateOscillation;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import co.pyragon.jamoss.content.fluid.FluidViews.ExtractOnly;
import co.pyragon.jamoss.content.fluid.FluidViews.InsertOnly;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

/**
 * Controller-driven condenser. The inherited tank holds the gas; the controller also owns a
 * liquid output tank and an item output buffer. Size affects capacity only, not speed.
 */
public class CondenserBlockEntity extends FluidTankBlockEntity {

	public static final int ITEM_OUTPUT_SLOTS = 9;
	private static final int ITEM_PUSH_INTERVAL = 8;
	/** Once warmed up, a recipe repeats this often while gas keeps coming. */
	public static final int WARM_INTERVAL = 20;
	/** Ticks without work before the condenser cools down again. */
	public static final int COOLDOWN_TICKS = 200;

	protected SmartFluidTank outputTank;
	protected ItemStackHandler outputItems;
	private IFluidHandler condenserFluidHandler;
	/** For hand interaction (no side): gas may be drained back out into containers. */
	private IFluidHandler handFluidHandler;
	private IItemHandler condenserItemHandler;

	private int progress;
	private boolean warm;
	private int idleTicks;
	@Nullable
	private RecipeHolder<CondensingRecipe> currentRecipe;
	private boolean recipeDirty = true;

	public CondenserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		outputTank = new SmartFluidTank(getCapacityMultiplier(), $ -> onOutputChanged());
		outputItems = new ItemStackHandler(ITEM_OUTPUT_SLOTS) {
			@Override
			protected void onContentsChanged(int slot) {
				onOutputChanged();
			}
		};
		condenserFluidHandler = new CombinedTankWrapper(new InsertOnly(tankInventory), new ExtractOnly(outputTank));
		handFluidHandler = new CombinedTankWrapper(tankInventory, new ExtractOnly(outputTank));
		condenserItemHandler = new CombinedInvWrapper(new ExtractOnlyItems(outputItems));
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, COBlockEntityTypes.CONDENSER.get(),
			(be, context) -> be.getController(context == null ? CondenserBlockEntity::getHandFluidHandler : CondenserBlockEntity::getCondenserFluidHandler));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, COBlockEntityTypes.CONDENSER.get(),
			(be, context) -> be.getController(CondenserBlockEntity::getCondenserItemHandler));
	}

	private <T> T getController(java.util.function.Function<CondenserBlockEntity, T> getter) {
		FluidTankBlockEntity controller = getControllerBE();
		return controller instanceof CondenserBlockEntity condenser ? getter.apply(condenser) : null;
	}

	public IFluidHandler getCondenserFluidHandler() {
		return condenserFluidHandler;
	}

	public IFluidHandler getHandFluidHandler() {
		return handFluidHandler;
	}

	public IItemHandler getCondenserItemHandler() {
		return condenserItemHandler;
	}

	public boolean hasWindow() {
		return window;
	}

	public SmartFluidTank getOutputTank() {
		return outputTank;
	}

	public ItemStackHandler getOutputItems() {
		return outputItems;
	}

	// ---- tank overrides ----

	@Override
	protected SmartFluidTank createInventory() {
		return new GasOnlyTank(getCapacityMultiplier(), this::onFluidStackChanged);
	}

	@Override
	protected void onFluidStackChanged(FluidStack newFluidStack) {
		super.onFluidStackChanged(newFluidStack);
		// Only re-evaluate when the gas itself changes; topping up the same gas must not reset progress.
		if (currentRecipe == null || !currentRecipe.value().matchesGas(newFluidStack))
			recipeDirty = true;
	}

	private void onOutputChanged() {
		if (hasLevel() && !level.isClientSide) {
			setChanged();
			sendData();
		}
	}

	@Override
	public void applyFluidTankSize(int blocks) {
		super.applyFluidTankSize(blocks);
		outputTank.setCapacity(blocks * getCapacityMultiplier());
		int overflow = outputTank.getFluidAmount() - outputTank.getCapacity();
		if (overflow > 0)
			outputTank.drain(overflow, FluidAction.EXECUTE);
	}

	/** Condensers never act as boilers. */
	@Override
	public void updateBoilerState() {}

	// ---- processing ----

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide || !isController())
			return;

		if (recipeDirty)
			refreshRecipe();

		if (currentRecipe != null) {
			CondensingRecipe recipe = currentRecipe.value();
			if (canApply(recipe)) {
				idleTicks = 0;
				progress++; // size never speeds this up; bigger condensers only hold more
				// warm-up: the first conversion takes the full duration, then it runs at WARM_INTERVAL
				int duration = warm ? Math.min(WARM_INTERVAL, Math.max(1, recipe.getProcessingDuration()))
					: Math.max(1, recipe.getProcessingDuration());
				while (progress >= duration && canApply(recipe)) {
					progress -= duration;
					apply(recipe);
					warm = true;
					duration = Math.min(WARM_INTERVAL, Math.max(1, recipe.getProcessingDuration()));
				}
				if (currentRecipe != null && !canApply(currentRecipe.value()))
					progress = 0;
			} else {
				progress = 0;
				coolDown();
			}
		} else {
			coolDown();
		}

		if (level.getGameTime() % ITEM_PUSH_INTERVAL == 0)
			pushItemsBelow();
	}

	private void coolDown() {
		if (!warm)
			return;
		if (++idleTicks >= COOLDOWN_TICKS) {
			warm = false;
			idleTicks = 0;
		}
	}

	public boolean isWarm() {
		return warm;
	}

	private void refreshRecipe() {
		recipeDirty = false;
		RecipeHolder<CondensingRecipe> previous = currentRecipe;
		currentRecipe = null;
		FluidStack gas = tankInventory.getFluid();
		if (!gas.isEmpty()) {
			for (RecipeHolder<CondensingRecipe> holder : level.getRecipeManager()
				.getAllRecipesFor(CORecipeTypes.CONDENSING.<net.minecraft.world.item.crafting.RecipeInput, CondensingRecipe>getType())) {
				if (holder.value().matchesGas(gas)) {
					currentRecipe = holder;
					break;
				}
			}
		}
		if (currentRecipe == null || previous == null || previous.id() != currentRecipe.id())
			progress = 0;
	}

	private boolean canApply(CondensingRecipe recipe) {
		if (tankInventory.getFluidAmount() < recipe.getGasAmount())
			return false;
		for (FluidStack result : recipe.getFluidResults())
			if (outputTank.fill(result, FluidAction.SIMULATE) < result.getAmount())
				return false;
		// Item outputs are rolled; require room for the guaranteed maximum of each result.
		for (var output : recipe.getRollableResults()) {
			ItemStack stack = output.getStack();
			if (!stack.isEmpty() && !ItemHandlerHelper.insertItemStacked(outputItems, stack.copy(), true).isEmpty())
				return false;
		}
		return true;
	}

	private void apply(CondensingRecipe recipe) {
		tankInventory.drain(recipe.getGasAmount(), FluidAction.EXECUTE);
		for (FluidStack result : recipe.getFluidResults())
			outputTank.fill(result.copy(), FluidAction.EXECUTE);
		for (ItemStack stack : recipe.rollResults(level.random))
			ItemHandlerHelper.insertItemStacked(outputItems, stack, false);
		sendData();
	}

	/** Moves buffered items into any inventory directly beneath the structure. */
	private void pushItemsBelow() {
		boolean anyItems = false;
		for (int i = 0; i < outputItems.getSlots(); i++)
			if (!outputItems.getStackInSlot(i).isEmpty()) {
				anyItems = true;
				break;
			}
		if (!anyItems)
			return;

		for (int x = 0; x < width; x++) {
			for (int z = 0; z < width; z++) {
				BlockPos below = worldPosition.offset(x, -1, z);
				IItemHandler target = level.getCapability(Capabilities.ItemHandler.BLOCK, below, Direction.UP);
				if (target == null)
					continue;
				for (int slot = 0; slot < outputItems.getSlots(); slot++) {
					ItemStack stack = outputItems.getStackInSlot(slot);
					if (stack.isEmpty())
						continue;
					ItemStack remainder = ItemHandlerHelper.insertItemStacked(target, stack.copy(), false);
					if (remainder.getCount() != stack.getCount())
						outputItems.setStackInSlot(slot, remainder);
				}
			}
		}
	}

	// ---- persistence ----

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(compound, registries, clientPacket);
		if (isController()) {
			outputTank.setCapacity(getTotalTankSize() * getCapacityMultiplier());
			outputTank.readFromNBT(registries, compound.getCompound("OutputContent"));
			outputItems.deserializeNBT(registries, compound.getCompound("OutputItems"));
			progress = compound.getInt("Progress");
			warm = compound.getBoolean("Warm");
		}
		recipeDirty = true;
	}

	@Override
	public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(compound, registries, clientPacket);
		if (isController()) {
			compound.put("OutputContent", outputTank.writeToNBT(registries, new CompoundTag()));
			compound.put("OutputItems", outputItems.serializeNBT(registries));
			compound.putInt("Progress", progress);
			compound.putBoolean("Warm", warm);
		}
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		FluidTankBlockEntity controller = getControllerBE();
		if (!(controller instanceof CondenserBlockEntity condenser))
			return false;

		lang("gui.goggles.condenser").forGoggles(tooltip);
		fluidLine(tooltip, "gui.goggles.condenser.gas", condenser.tankInventory.getFluid(), condenser.tankInventory.getCapacity());
		fluidLine(tooltip, "gui.goggles.condenser.liquid", condenser.outputTank.getFluid(), condenser.outputTank.getCapacity());

		if (condenser.warm)
			lang("gui.goggles.condenser.warm").style(ChatFormatting.GOLD).forGoggles(tooltip, 1);
		int items = 0;
		for (int i = 0; i < condenser.outputItems.getSlots(); i++)
			items += condenser.outputItems.getStackInSlot(i).getCount();
		if (items > 0)
			lang("gui.goggles.condenser.items", items).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
		return true;
	}

	private static void fluidLine(List<Component> tooltip, String key, FluidStack stack, int capacity) {
		LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
		LangBuilder line = lang(key).style(ChatFormatting.GRAY).space();
		if (stack.isEmpty())
			line.add(CreateLang.text("-").style(ChatFormatting.DARK_GRAY));
		else
			line.add(CreateLang.fluidName(stack).style(ChatFormatting.WHITE));
		line.forGoggles(tooltip, 1);
		CreateLang.builder()
			.add(CreateLang.number(stack.getAmount()).add(mb).style(ChatFormatting.GOLD))
			.text(ChatFormatting.GRAY, " / ")
			.add(CreateLang.number(capacity).add(mb).style(ChatFormatting.DARK_GRAY))
			.forGoggles(tooltip, 1);
	}

	private static LangBuilder lang(String key, Object... args) {
		return new LangBuilder(CreateOscillation.MOD_ID).translate(key, args);
	}

	// ---- helpers ----

	/** Only fluids tagged as gases may enter the condenser. */
	public static class GasOnlyTank extends SmartFluidTank {
		public GasOnlyTank(int capacity, java.util.function.Consumer<FluidStack> updateCallback) {
			super(capacity, updateCallback);
		}

		@Override
		public boolean isFluidValid(FluidStack stack) {
			return COFluidTags.isGas(stack);
		}
	}

	private static class ExtractOnlyItems extends net.neoforged.neoforge.items.wrapper.RangedWrapper {
		public ExtractOnlyItems(ItemStackHandler handler) {
			super(handler, 0, handler.getSlots());
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return stack;
		}
	}

	@Nullable
	public static CondenserBlockEntity at(BlockEntity be) {
		return be instanceof CondenserBlockEntity condenser ? condenser : null;
	}
}
