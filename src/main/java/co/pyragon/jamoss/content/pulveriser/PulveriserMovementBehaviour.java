package co.pyragon.jamoss.content.pulveriser;

import java.util.List;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;

import co.pyragon.jamoss.content.amplifier.CrystalLadder;
import co.pyragon.jamoss.content.amplifier.ResonanceAmplifierBlock;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.content.pulveriser.PulveriserLogic.Tier;
import co.pyragon.jamoss.content.resonator.ResonatorBlock;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

/**
 * The Pulveriser on a contraption: runs while moving. The block directly above it in the
 * contraption's structure must be a Resonator (band check waived — nothing spins while mounted) or
 * an Amplifier whose ladder band exactly matches the Pulveriser's; the ladder rides along in the
 * block entity data.
 */
public class PulveriserMovementBehaviour implements MovementBehaviour {

	@Nullable
	private static FrequencyBand ladderBand(@Nullable CompoundTag nbt, Level world) {
		if (nbt == null)
			return null;
		CrystalLadder ladder = new CrystalLadder(() -> {});
		ladder.read(nbt, world.registryAccess());
		return ladder.band();
	}

	/** Tier the mounted Pulveriser runs at, or null without a valid source above it. */
	@Nullable
	private static Tier currentTier(MovementContext context) {
		FrequencyBand band = ladderBand(context.blockEntityData, context.world);
		Tier tier = PulveriserLogic.tierOf(band);
		if (tier == null)
			return null;
		StructureBlockInfo above = context.contraption.getBlocks().get(context.localPos.above());
		if (above == null)
			return null;
		if (above.state().getBlock() instanceof ResonatorBlock)
			return tier;
		if (above.state().getBlock() instanceof ResonanceAmplifierBlock)
			return ladderBand(above.nbt(), context.world) == band ? tier : null;
		return null;
	}

	/** Includes rotation on the spot: a bearing sweeping the pulveriser's facing still reports motion. */
	private static Direction facing(MovementContext context) {
		Direction local = context.state.getValue(DirectionalBlock.FACING);
		Vec3 rotated = context.rotation.apply(Vec3.atLowerCornerOf(local.getNormal()));
		return Direction.getNearest(rotated.x, rotated.y, rotated.z);
	}

	@Override
	public boolean isActive(MovementContext context) {
		return MovementBehaviour.super.isActive(context) && currentTier(context) != null;
	}

	@Override
	public void tick(MovementContext context) {
		Level world = context.world;
		if (world.isClientSide)
			return;
		CompoundTag data = context.data;
		if (!data.contains("BreakerId"))
			data.putInt("BreakerId", -BlockBreakingKineticBlockEntity.NEXT_BREAKER_ID.addAndGet(64));
		int breakerId = data.getInt("BreakerId");
		List<BlockPos> layer = PulveriserLogic.readLayer(data, "Layer");

		Tier tier = currentTier(context);
		if (tier == null || context.relativeMotion.equals(Vec3.ZERO) || context.position == null) {
			reset(context, breakerId, layer);
			return;
		}

		BlockPos origin = BlockPos.containing(context.position);
		List<BlockPos> next = PulveriserLogic.findLayer(world, origin, facing(context), tier,
			state -> context.getFilterFromBE().test(world, new ItemStack(state.getBlock().asItem())));
		if (next.isEmpty()) {
			reset(context, breakerId, layer);
			return;
		}
		if (!next.equals(layer)) {
			PulveriserLogic.clearCracks(world, breakerId, layer);
			layer = next;
			data.put("Layer", PulveriserLogic.writeLayer(layer));
			data.putInt("Progress", 0);
			data.putInt("Cooldown", 0);
		}
		// hold the contraption at the face until the layer shatters, like Create's drill
		context.stall = true;
		int cooldown = data.getInt("Cooldown");
		if (cooldown > 0) {
			data.putInt("Cooldown", cooldown - 1);
			return;
		}
		float hardness = PulveriserLogic.maxHardness(world, layer);
		int progress = data.getInt("Progress") + PulveriserLogic.progressStep(tier, hardness, data.getInt("Progress"));
		if (progress >= 10) {
			PulveriserLogic.clearCracks(world, breakerId, layer);
			PulveriserLogic.breakLayer(world, layer, stack -> collectOrDropItem(context, stack));
			data.remove("Layer");
			data.putInt("Progress", 0);
			context.stall = false;
			return;
		}
		PulveriserLogic.showCracks(world, breakerId, layer, progress);
		data.putInt("Progress", progress);
		data.putInt("Cooldown", PulveriserLogic.ticksBetweenSteps(tier, hardness));
	}

	private static void reset(MovementContext context, int breakerId, List<BlockPos> layer) {
		if (!layer.isEmpty())
			PulveriserLogic.clearCracks(context.world, breakerId, layer);
		context.data.remove("Layer");
		context.data.putInt("Progress", 0);
		context.stall = false;
	}

	@Override
	public void cancelStall(MovementContext context) {
		MovementBehaviour.super.cancelStall(context);
		if (!context.world.isClientSide && context.data.contains("BreakerId"))
			reset(context, context.data.getInt("BreakerId"), PulveriserLogic.readLayer(context.data, "Layer"));
	}

	@Override
	public void stopMoving(MovementContext context) {
		if (!context.world.isClientSide && context.data.contains("BreakerId"))
			reset(context, context.data.getInt("BreakerId"), PulveriserLogic.readLayer(context.data, "Layer"));
	}
}
