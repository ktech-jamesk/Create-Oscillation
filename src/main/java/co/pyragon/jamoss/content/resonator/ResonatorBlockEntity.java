package co.pyragon.jamoss.content.resonator;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import co.pyragon.jamoss.content.chamber.ResonanceChamberBlockEntity;
import co.pyragon.jamoss.content.sieve.VibratingSieveBlockEntity;
import co.pyragon.jamoss.content.tuningfork.TuningForkBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

import co.pyragon.jamoss.content.frequency.FrequencyBand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * A vibration source. It only spins; whatever sits directly below it (a Resonance Chamber, a
 * Vibrating Sieve, ...) reads this block's speed and does its own work.
 */
public class ResonatorBlockEntity extends KineticBlockEntity implements co.pyragon.jamoss.content.vibration.VibrationSource {

	@Override
	public float getVibrationSpeed() {
		return getSpeed();
	}

	public ResonatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/** True while the block below is actually using this resonator's vibration. */
	public boolean isWorking() {
		if (level == null || getSpeed() == 0)
			return false;
		BlockEntity below = level.getBlockEntity(worldPosition.below());
		if (below instanceof ResonanceChamberBlockEntity chamber)
			return chamber.isProcessing();
		if (below instanceof VibratingSieveBlockEntity sieve)
			return sieve.isShaking();
		if (below instanceof co.pyragon.jamoss.content.coupler.ResonanceEmitterBlockEntity emitter)
			return emitter.isLinked();
		if (below instanceof co.pyragon.jamoss.content.amplifier.ResonanceAmplifierBlockEntity amplifier)
			return amplifier.isWorking();
		return false;
	}

	@Override
	public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
		boolean connectedViaAxes, boolean connectedViaCogs) {
		if (diff.getY() == 1 && diff.getX() == 0 && diff.getZ() == 0 && target instanceof TuningForkBlockEntity fork)
			return TuningForkBlockEntity.inverseRatioTo(fork, this);
		return 0;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		ResonanceChamberBlockEntity.addFrequencyLine(tooltip, FrequencyBand.of(getSpeed()));
		return true;
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return new AABB(worldPosition);
	}
}
