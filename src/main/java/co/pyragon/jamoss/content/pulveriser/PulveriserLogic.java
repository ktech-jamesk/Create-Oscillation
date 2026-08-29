package co.pyragon.jamoss.content.pulveriser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.simibubi.create.foundation.utility.BlockHelper;

import co.pyragon.jamoss.content.frequency.FrequencyBand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Sonic Pulveriser's breaking rules, shared by the block entity (stationary) and the
 * contraption actor. The crystal ladder sets the tier: how far it reaches, how wide the
 * cross-section is, how hard a block it can crack, and how fast. The nearest layer of breakable
 * blocks within reach cracks together and shatters together.
 */
public final class PulveriserLogic {

	private PulveriserLogic() {}

	/** @param radius cross-section half-width (0 = 1×1, 1 = 3×3 ...) */
	public record Tier(FrequencyBand band, int reach, int radius, float hardnessCap, float breakSpeed) {
		public int width() {
			return radius * 2 + 1;
		}
	}

	public static final Tier LOW = new Tier(FrequencyBand.LOW, 1, 0, 3f, 0.5f);
	public static final Tier MID = new Tier(FrequencyBand.MID, 2, 1, 5f, 1f);
	public static final Tier HIGH = new Tier(FrequencyBand.HIGH, 3, 2, 50f, 2f);
	public static final Tier ULTRASONIC = new Tier(FrequencyBand.ULTRASONIC, 4, 3, Float.MAX_VALUE, 4f);

	@Nullable
	public static Tier tierOf(@Nullable FrequencyBand band) {
		if (band == null)
			return null;
		return switch (band) {
			case LOW -> LOW;
			case MID -> MID;
			case HIGH -> HIGH;
			case ULTRASONIC -> ULTRASONIC;
			default -> null;
		};
	}

	public static boolean canBreak(Level level, BlockPos pos, BlockState state, Tier tier, Predicate<BlockState> filter) {
		float hardness = state.getDestroySpeed(level, pos);
		if (!BlockBreakingKineticBlockEntity.isBreakable(state, hardness))
			return false;
		if (state.getCollisionShape(level, pos).isEmpty())
			return false;
		if (hardness > tier.hardnessCap())
			return false;
		if (state.hasBlockEntity())
			return false;
		return filter.test(state);
	}

	/** Positions of the nearest layer (distance 1..reach in front of {@code origin}) that has anything to break. */
	public static List<BlockPos> findLayer(Level level, BlockPos origin, Direction facing, Tier tier, Predicate<BlockState> filter) {
		Axis axis = facing.getAxis();
		for (int d = 1; d <= tier.reach(); d++) {
			BlockPos centre = origin.relative(facing, d);
			List<BlockPos> layer = new ArrayList<>();
			for (int a = -tier.radius(); a <= tier.radius(); a++)
				for (int b = -tier.radius(); b <= tier.radius(); b++) {
					BlockPos pos = switch (axis) {
						case X -> centre.offset(0, a, b);
						case Y -> centre.offset(a, 0, b);
						case Z -> centre.offset(a, b, 0);
					};
					if (canBreak(level, pos, level.getBlockState(pos), tier, filter))
						layer.add(pos);
				}
			if (!layer.isEmpty())
				return layer;
		}
		return List.of();
	}

	public static float maxHardness(Level level, List<BlockPos> layer) {
		float max = 0.05f;
		for (BlockPos pos : layer)
			max = Math.max(max, level.getBlockState(pos).getDestroySpeed(level, pos));
		return max;
	}

	public static void showCracks(Level level, int breakerId, List<BlockPos> layer, int progress) {
		for (int i = 0; i < layer.size(); i++)
			level.destroyBlockProgress(breakerId - i, layer.get(i), progress);
	}

	public static void clearCracks(Level level, int breakerId, List<BlockPos> layer) {
		showCracks(level, breakerId, layer, -1);
	}

	/** Breaks every block of the layer. */
	public static void breakLayer(Level level, List<BlockPos> layer, Consumer<ItemStack> drops) {
		for (BlockPos pos : layer) {
			BlockState state = level.getBlockState(pos);
			if (state.isAir())
				continue;
			level.playSound(null, pos, state.getSoundType(level, pos, null).getBreakSound(), SoundSource.BLOCKS, .5f, .8f);
			BlockHelper.destroyBlock(level, pos, 1f, drops);
		}
	}

	/** Progress step for one tick against the hardest block of the layer, like Create's drill. */
	public static int progressStep(Tier tier, float hardness, int progress) {
		return Mth.clamp((int) (tier.breakSpeed() / hardness), 1, 10 - progress);
	}

	public static int ticksBetweenSteps(Tier tier, float hardness) {
		return (int) (hardness / tier.breakSpeed());
	}

	public static ListTag writeLayer(List<BlockPos> layer) {
		ListTag list = new ListTag();
		for (BlockPos pos : layer)
			list.add(LongTag.valueOf(pos.asLong()));
		return list;
	}

	public static List<BlockPos> readLayer(CompoundTag tag, String key) {
		List<BlockPos> layer = new ArrayList<>();
		for (Tag t : tag.getList(key, Tag.TAG_LONG))
			layer.add(BlockPos.of(((LongTag) t).getAsLong()));
		return layer;
	}
}
