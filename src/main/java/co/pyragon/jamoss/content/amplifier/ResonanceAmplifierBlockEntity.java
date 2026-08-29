package co.pyragon.jamoss.content.amplifier;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.chamber.ResonanceChamberBlockEntity;
import co.pyragon.jamoss.content.coupler.ResonanceEmitterBlockEntity;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.content.sieve.VibratingSieveBlockEntity;
import co.pyragon.jamoss.content.vibration.VibrationSource;
import co.pyragon.jamoss.registry.COBlockEntityTypes;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Sits under a Resonator and re-emits its vibration at the band its crystal ladder reaches. If the
 * Resonator's own band is higher than the ladder the amplifier overloads and emits nothing.
 */
public class ResonanceAmplifierBlockEntity extends SmartBlockEntity implements VibrationSource, IHaveGoggleInformation {

	public final CrystalLadder crystals = new CrystalLadder(this::onCrystalsChanged);
	private final IItemHandler insertOnly = crystals.insertOnly();

	public ResonanceAmplifierBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, COBlockEntityTypes.RESONANCE_AMPLIFIER.get(), (be, ctx) -> be.insertOnly);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	private void onCrystalsChanged() {
		setChanged();
		sendData();
	}

	/** Signed speed of the Resonator above, 0 if absent. */
	public float getInputSpeed() {
		return VibrationSource.above(level, worldPosition).map(VibrationSource::getVibrationSpeed).orElse(0f);
	}

	@Nullable
	public FrequencyBand getLadderBand() {
		return crystals.band();
	}

	/** Band the resonator above produces on its own (null below Low or idle). */
	@Nullable
	public FrequencyBand getInputBand() {
		return FrequencyBand.of(getInputSpeed());
	}

	public boolean isOverloaded() {
		FrequencyBand ladder = getLadderBand();
		FrequencyBand input = getInputBand();
		return ladder != null && input != null && input.ordinal() > ladder.ordinal();
	}

	@Override
	public float getVibrationSpeed() {
		float input = getInputSpeed();
		FrequencyBand ladder = getLadderBand();
		if (input == 0 || ladder == null || isOverloaded())
			return 0;
		return Math.copySign(ladder.minSpeed, input);
	}

	public boolean isAmplifying() {
		return getVibrationSpeed() != 0;
	}

	/** True while the block below is actually using the amplified vibration. */
	public boolean isWorking() {
		if (level == null || !isAmplifying())
			return false;
		BlockEntity below = level.getBlockEntity(worldPosition.below());
		if (below instanceof ResonanceChamberBlockEntity chamber)
			return chamber.isProcessing();
		if (below instanceof VibratingSieveBlockEntity sieve)
			return sieve.isShaking();
		if (below instanceof ResonanceEmitterBlockEntity emitter)
			return emitter.isLinked();
		return false;
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		crystals.write(tag, registries);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		crystals.read(tag, registries);
	}

	@Override
	public void destroy() {
		super.destroy();
		if (level == null)
			return;
		for (int i = 0; i < crystals.getSlots(); i++)
			if (!crystals.getStackInSlot(i).isEmpty())
				Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), crystals.getStackInSlot(i));
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		FrequencyBand ladder = getLadderBand();
		LangBuilder value = ladder == null
			? lang().translate("gui.goggles.coupler.no_crystal").style(ChatFormatting.DARK_GRAY)
			: lang().translate("frequency." + ladder.getSerializedName()).style(ChatFormatting.AQUA);
		lang().translate("gui.goggles.tuning_fork").style(ChatFormatting.GRAY).space().add(value).forGoggles(tooltip);

		LangBuilder rungs = lang().translate("gui.goggles.amplifier.crystals").style(ChatFormatting.GRAY);
		for (int i = 0; i < CrystalLadder.RUNGS.length; i++) {
			rungs.space();
			if (crystals.getStackInSlot(i).isEmpty())
				rungs.add(lang().text("-").style(ChatFormatting.DARK_GRAY));
			else
				rungs.add(lang().translate("frequency." + CrystalLadder.RUNGS[i].getSerializedName()).style(ChatFormatting.AQUA));
		}
		rungs.forGoggles(tooltip);

		if (isOverloaded())
			lang().translate("gui.goggles.amplifier.overloaded", getInputBand().getDisplayName(), ladder.getDisplayName())
				.style(ChatFormatting.RED).forGoggles(tooltip);
		else if (isAmplifying())
			lang().translate("gui.goggles.amplifier.amplifying").style(ChatFormatting.GREEN).forGoggles(tooltip);
		else
			lang().translate("gui.goggles.coupler.idle").style(ChatFormatting.GRAY).forGoggles(tooltip);
		return true;
	}

	private static LangBuilder lang() {
		return new LangBuilder(CreateOscillation.MOD_ID);
	}
}
