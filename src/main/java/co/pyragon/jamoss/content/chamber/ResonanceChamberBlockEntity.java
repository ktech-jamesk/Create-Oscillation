package co.pyragon.jamoss.content.chamber;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import co.pyragon.jamoss.content.vibration.VibrationSource;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.fluids.FluidFX;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;

import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.content.recipe.ResonatingRecipe;
import co.pyragon.jamoss.content.resonator.ResonatorBlockEntity;
import co.pyragon.jamoss.registry.COBlockEntityTypes;
import co.pyragon.jamoss.registry.CORecipeTypes;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * A basin that runs its own {@code resonating} recipes whenever a Resonator directly above it is
 * spinning fast enough. Timing follows Create's mixer: a 40-tick cycle with the work happening
 * at tick 20, and the processing time scaling with speed and recipe duration.
 */
public class ResonanceChamberBlockEntity extends BasinBlockEntity implements IHaveGoggleInformation {

	public static final float MIN_SPEED = FrequencyBand.LOW.minSpeed;

	public boolean running;
	public int runningTicks;
	private int processingTicks = -1;
	private boolean recipeDirty = true;
	@Nullable
	private BasinRecipe currentRecipe;
	/** Speed of the resonator above (synced for the client). */
	private float driveSpeed;

	public ResonanceChamberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		// The inventory is created by the basin constructor (after addBehaviours has already run).
		inputInventory.whenContentsChanged($ -> notifyChangeOfContents());
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, COBlockEntityTypes.RESONANCE_CHAMBER.get(),
			(be, context) -> be.itemCapability);
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, COBlockEntityTypes.RESONANCE_CHAMBER.get(),
			(be, context) -> be.fluidCapability);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, COBlockEntityTypes.CAVITATION_CHAMBER.get(),
			(be, context) -> be.itemCapability);
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, COBlockEntityTypes.CAVITATION_CHAMBER.get(),
			(be, context) -> be.fluidCapability);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		inputTank.whenFluidUpdates(this::notifyChangeOfContents);
		outputTank.whenFluidUpdates(this::notifyChangeOfContents);
		// Unlike a plain basin, pipes may only feed the inputs and only draw the outputs: a pump must
		// never steal the mist a chamber is about to process.
		fluidCapability = new com.simibubi.create.foundation.fluid.CombinedTankWrapper(
			new co.pyragon.jamoss.content.fluid.FluidViews.ExtractOnly(outputTank.getCapability()),
			new co.pyragon.jamoss.content.fluid.FluidViews.InsertOnly(inputTank.getCapability()));
	}

	@Override
	public void notifyChangeOfContents() {
		super.notifyChangeOfContents();
		recipeDirty = true;
	}

	public Optional<ResonatorBlockEntity> getResonator() {
		if (level == null)
			return Optional.empty();
		BlockEntity above = level.getBlockEntity(worldPosition.above());
		return above instanceof ResonatorBlockEntity resonator ? Optional.of(resonator) : Optional.empty();
	}

	public float getDriveSpeed() {
		return driveSpeed;
	}

	/** Band the resonator above currently produces, or null when idle/too slow. */
	@Nullable
	public FrequencyBand getBand() {
		return FrequencyBand.of(driveSpeed);
	}

	/** True when the current recipe may run at {@code speed}. */
	private boolean bandMatches(float speed) {
		if (!(currentRecipe instanceof ResonatingRecipe recipe))
			return true;
		return recipe.getBand().accepts(FrequencyBand.of(speed));
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		addFrequencyLine(tooltip, getBand());
		return true;
	}

	public static void addFrequencyLine(List<Component> tooltip, @Nullable FrequencyBand band) {
		LangBuilder value = band == null
			? new LangBuilder(CreateOscillation.MOD_ID).text("-").style(ChatFormatting.DARK_GRAY)
			: new LangBuilder(CreateOscillation.MOD_ID).translate("frequency." + band.getSerializedName()).style(ChatFormatting.AQUA);
		new LangBuilder(CreateOscillation.MOD_ID).translate("gui.goggles.frequency")
			.style(ChatFormatting.GRAY)
			.space()
			.add(value)
			.forGoggles(tooltip);
	}

	/** True while a recipe cycle is under way (drives the shiver and the fluid swirl). */
	public boolean isProcessing() {
		return running && driveSpeed != 0;
	}

	// ---- processing ----

	@Override
	public void tick() {
		super.tick();

		if (level.isClientSide) {
			setAreFluidsMoving(running && runningTicks <= 20);
			if (running && runningTicks == 20)
				renderParticles();
			if (running && runningTicks < 40)
				runningTicks++;
			return;
		}

		float speed = VibrationSource.speedAbove(level, worldPosition);
		if (speed < MIN_SPEED)
			speed = 0;
		if (speed != driveSpeed) {
			if (FrequencyBand.of(speed) != FrequencyBand.of(driveSpeed))
				recipeDirty = true;
			driveSpeed = speed;
			sendData();
		}

		if (running) {
			tickCycle(speed);
			return;
		}

		if (speed == 0 || !canContinueProcessing())
			return;
		if (recipeDirty) {
			recipeDirty = false;
			currentRecipe = findRecipe();
		}
		if (currentRecipe != null)
			startCycle();
	}

	private void tickCycle(float speed) {
		if (runningTicks >= 40) {
			running = false;
			runningTicks = 0;
			processingTicks = -1;
			recipeDirty = true;
			sendData();
			return;
		}

		// stalled or wrong band: rewind the animation instead of finishing
		if (speed == 0 || !bandMatches(speed)) {
			speed = 0;
			if (runningTicks < 20)
				runningTicks = 40 - runningTicks;
			else if (runningTicks == 20)
				runningTicks++;
		}

		if (runningTicks == 20) {
			if (processingTicks < 0) {
				float recipeSpeed = 1;
				if (currentRecipe instanceof StandardProcessingRecipe<?> spr && spr.getProcessingDuration() != 0)
					recipeSpeed = spr.getProcessingDuration() / 100f;
				processingTicks = Math.max((Mth.log2((int) (512 / Math.max(speed, 1))) * Mth.ceil(recipeSpeed * 15)) + 1, 1);
				if (!inputTank.isEmpty() || !outputTank.isEmpty())
					level.playSound(null, worldPosition, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.BLOCKS,
						.75f, speed < 65 ? .75f : 1.5f);
			} else {
				processingTicks--;
				if (processingTicks == 0) {
					runningTicks++;
					processingTicks = -1;
					applyRecipe();
					sendData();
				}
			}
		}

		if (runningTicks != 20)
			runningTicks++;
	}

	private void startCycle() {
		running = true;
		runningTicks = 0;
		processingTicks = -1;
		sendData();
	}

	private void applyRecipe() {
		if (currentRecipe == null || !BasinRecipe.match(this, currentRecipe)) {
			currentRecipe = findRecipe();
			if (currentRecipe == null)
				return;
		}
		if (!BasinRecipe.apply(this, currentRecipe))
			return;
		inputTank.sendDataImmediately();
		notifyChangeOfContents();
		// keep going straight away if the same recipe still fits
		if (BasinRecipe.match(this, currentRecipe) && bandMatches(driveSpeed))
			runningTicks = 20;
	}

	/** Which recipe type this chamber runs; the Cavitation Chamber swaps it. */
	protected RecipeType<? extends ResonatingRecipe> getRecipeType() {
		return CORecipeTypes.RESONATING.<RecipeInput, ResonatingRecipe>getType();
	}

	@Nullable
	private BasinRecipe findRecipe() {
		if (isEmpty())
			return null;
		List<BasinRecipe> matches = new ArrayList<>();
		for (RecipeHolder<? extends ResonatingRecipe> holder : level.getRecipeManager().getAllRecipesFor(getRecipeType()))
			if (holder.value().getBand().accepts(FrequencyBand.of(driveSpeed)) && BasinRecipe.match(this, holder.value()))
				matches.add(holder.value());
		if (matches.isEmpty())
			return null;
		matches.sort((a, b) -> b.getIngredients().size() - a.getIngredients().size());
		return matches.get(0);
	}

	// ---- visuals ----

	private void renderParticles() {
		for (SmartInventory inv : getInvs())
			for (int slot = 0; slot < inv.getSlots(); slot++) {
				ItemStack stack = inv.getItem(slot);
				if (!stack.isEmpty())
					spillParticle(new ItemParticleOption(ParticleTypes.ITEM, stack));
			}
		for (SmartFluidTankBehaviour behaviour : getTanks()) {
			if (behaviour == null)
				continue;
			for (TankSegment segment : behaviour.getTanks())
				if (!segment.isEmpty(0))
					spillParticle(FluidFX.getFluidParticle(segment.getRenderedFluid()));
		}
	}

	private void spillParticle(ParticleOptions data) {
		float angle = level.random.nextFloat() * 360;
		Vec3 offset = VecHelper.rotate(new Vec3(0, 0, 0.25f), angle, Axis.Y);
		Vec3 target = VecHelper.rotate(offset, 25, Axis.Y).add(0, .25f, 0);
		Vec3 center = offset.add(VecHelper.getCenterOf(worldPosition));
		target = VecHelper.offsetRandomly(target.subtract(offset), level.random, 1 / 128f);
		level.addParticle(data, center.x, center.y + 0.25f, center.z, target.x, target.y, target.z);
	}

	// ---- persistence ----

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		running = compound.getBoolean("Running");
		runningTicks = compound.getInt("Ticks");
		driveSpeed = compound.getFloat("DriveSpeed");
		super.read(compound, registries, clientPacket);
		recipeDirty = true;
	}

	@Override
	public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		compound.putBoolean("Running", running);
		compound.putInt("Ticks", runningTicks);
		compound.putFloat("DriveSpeed", driveSpeed);
		super.write(compound, registries, clientPacket);
	}
}
