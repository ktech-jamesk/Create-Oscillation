package co.pyragon.jamoss.content.coupler;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** A kinetic source that spins at its crystal's band RPM while an emitter of that band is aimed at it. */
public class ResonanceReceiverBlockEntity extends GeneratingKineticBlockEntity {

	/** Ticks a link survives without the emitter refreshing it. */
	private static final int LINK_TIMEOUT = 3;

	public final CrystalSlot crystal = new CrystalSlot(this::onCrystalChanged);
	private int drivenTicks;
	private boolean driven;

	public ResonanceReceiverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, co.pyragon.jamoss.registry.COBlockEntityTypes.RESONANCE_RECEIVER.get(),
			(be, ctx) -> be.crystal);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
	}

	@Nullable
	public FrequencyBand getBand() {
		return crystal.band();
	}

	public boolean isDriven() {
		return driven;
	}

	/**
	 * Called by an emitter whose beam (travelling in {@code beamDirection}) reached this block.
	 * Accepts when the crystal matches and the beam enters the dish face (opposite the shaft).
	 */
	public boolean accept(FrequencyBand band, Direction beamDirection) {
		if (band != getBand())
			return false;
		if (beamDirection != getBlockState().getValue(DirectionalKineticBlock.FACING))
			return false;
		drivenTicks = LINK_TIMEOUT;
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide)
			return;
		boolean now = drivenTicks > 0;
		if (drivenTicks > 0)
			drivenTicks--;
		if (now != driven) {
			driven = now;
			updateGeneratedRotation();
			sendData();
		}
	}

	/** Stress capacity per RPM by crystal tier: Low 32, Mid 128, High 512, Ultrasonic 2048 (a link can run a whole build). */
	public static float capacityPerRpm(@Nullable FrequencyBand band) {
		if (band == null)
			return 0;
		return switch (band) {
			case LOW -> 32f;
			case MID -> 128f;
			case HIGH -> 512f;
			case ULTRASONIC -> 2048f;
			default -> 0;
		};
	}

	@Override
	public float calculateAddedStressCapacity() {
		float capacity = capacityPerRpm(getBand());
		this.lastCapacityProvided = capacity;
		return capacity;
	}

	@Override
	public float getGeneratedSpeed() {
		FrequencyBand band = getBand();
		if (!driven || band == null || band == FrequencyBand.ANY)
			return 0;
		return band.minSpeed;
	}

	private void onCrystalChanged() {
		setChanged();
		if (level != null && !level.isClientSide) {
			drivenTicks = 0;
			if (driven) {
				driven = false;
				updateGeneratedRotation();
			}
			sendData();
		}
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		crystal.write(tag, registries);
		tag.putBoolean("Driven", driven);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		crystal.read(tag, registries);
		driven = tag.getBoolean("Driven");
	}

	@Override
	public void destroy() {
		super.destroy();
		if (level != null && !crystal.getStackInSlot(0).isEmpty())
			net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
				crystal.getStackInSlot(0));
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		boolean any = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		FrequencyBand band = getBand();
		LangBuilder value = band == null
			? new LangBuilder(CreateOscillation.MOD_ID).translate("gui.goggles.coupler.no_crystal").style(ChatFormatting.DARK_GRAY)
			: new LangBuilder(CreateOscillation.MOD_ID).translate("frequency." + band.getSerializedName()).style(ChatFormatting.AQUA);
		new LangBuilder(CreateOscillation.MOD_ID).translate("gui.goggles.tuning_fork").style(ChatFormatting.GRAY).space().add(value).forGoggles(tooltip);
		new LangBuilder(CreateOscillation.MOD_ID).translate(driven ? "gui.goggles.coupler.receiving" : "gui.goggles.coupler.idle")
			.style(driven ? ChatFormatting.GREEN : ChatFormatting.GRAY).forGoggles(tooltip);
		return true;
	}
}
