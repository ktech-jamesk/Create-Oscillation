package co.pyragon.jamoss.content.vibration;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Something that shakes the block directly below it: a Resonator (its kinetic speed) or a Resonance
 * Amplifier (its ladder band's speed). Consumers read the speed and derive the band with
 * {@link co.pyragon.jamoss.content.frequency.FrequencyBand#of(float)}.
 */
public interface VibrationSource {

	/** Signed speed in RPM handed to the block below; 0 when idle. */
	float getVibrationSpeed();

	static Optional<VibrationSource> above(Level level, BlockPos pos) {
		if (level == null)
			return Optional.empty();
		BlockEntity above = level.getBlockEntity(pos.above());
		return above instanceof VibrationSource source ? Optional.of(source) : Optional.empty();
	}

	/** Absolute speed of the source above {@code pos}, or 0. */
	static float speedAbove(Level level, BlockPos pos) {
		return above(level, pos).map(s -> Math.abs(s.getVibrationSpeed())).orElse(0f);
	}
}
