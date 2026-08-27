package co.pyragon.jamoss.content.pulveriser;

import java.util.List;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;

import co.pyragon.jamoss.content.pulveriser.PulveriserLogic.Tier;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** The Pulveriser on a contraption: runs while moving, tier from the crystal stored in its block entity data. */
public class PulveriserMovementBehaviour implements MovementBehaviour {

	/** The mounted block's inventory, read from and written back to its saved data. */
	private static PulveriserInventory inventory(MovementContext context) {
		PulveriserInventory inv = new PulveriserInventory(() -> {});
		if (context.blockEntityData != null && context.blockEntityData.contains("Inventory"))
			inv.deserializeNBT(context.world.registryAccess(), context.blockEntityData.getCompound("Inventory"));
		return inv;
	}

	private static void store(MovementContext context, PulveriserInventory inv) {
		context.blockEntityData.put("Inventory", inv.serializeNBT(context.world.registryAccess()));
	}

	@Nullable
	private static FrequencyBand activeBand(MovementContext context) {
		if (context.blockEntityData == null || !context.blockEntityData.contains("ActiveBand"))
			return null;
		String id = context.blockEntityData.getString("ActiveBand");
		for (FrequencyBand band : FrequencyBand.values())
			if (band.getSerializedName().equals(id))
				return band;
		return null;
	}

	/** Tier available: the burning crystal, else the next one waiting (consumed on first use). */
	@Nullable
	private static Tier currentTier(MovementContext context, boolean consume) {
		Tier active = PulveriserLogic.tierOf(activeBand(context));
		if (active != null && context.blockEntityData.getInt("Charge") > 0)
			return active;
		PulveriserInventory inv = inventory(context);
		Tier next = PulveriserLogic.tierOf(inv.getStackInSlot(PulveriserInventory.CRYSTALS));
		if (next == null)
			return null;
		if (consume) {
			inv.takeCrystal();
			store(context, inv);
			context.blockEntityData.putString("ActiveBand", next.band().getSerializedName());
			context.blockEntityData.putInt("Charge", next.charge());
		}
		return next;
	}

	private static void spend(MovementContext context, int cost) {
		int charge = context.blockEntityData.getInt("Charge") - cost;
		if (charge > 0) {
			context.blockEntityData.putInt("Charge", charge);
			return;
		}
		context.blockEntityData.putInt("Charge", 0);
		context.blockEntityData.remove("ActiveBand");
		PulveriserInventory inv = inventory(context);
		ItemStack left = inv.addSpent(co.pyragon.jamoss.registry.COItems.ROUGH_QUARTZ_CRYSTAL.asStack());
		store(context, inv);
	}

	private static Direction facing(MovementContext context) {
		Direction local = context.state.getValue(DirectionalKineticBlock.FACING);
		Vec3 rotated = context.rotation.apply(Vec3.atLowerCornerOf(local.getNormal()));
		return Direction.getNearest(rotated.x, rotated.y, rotated.z);
	}

	@Override
	public boolean isActive(MovementContext context) {
		return MovementBehaviour.super.isActive(context) && currentTier(context, false) != null;
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

		Tier tier = currentTier(context, false);
		if (tier == null || context.relativeMotion.equals(Vec3.ZERO) || context.position == null) {
			reset(world, breakerId, layer, data);
			return;
		}

		BlockPos origin = BlockPos.containing(context.position);
		List<BlockPos> next = PulveriserLogic.findLayer(world, origin, facing(context), tier,
			state -> context.getFilterFromBE().test(world, new ItemStack(state.getBlock().asItem())));
		if (next.isEmpty()) {
			reset(world, breakerId, layer, data);
			return;
		}
		if (!next.equals(layer)) {
			PulveriserLogic.clearCracks(world, breakerId, layer);
			layer = next;
			data.put("Layer", PulveriserLogic.writeLayer(layer));
			data.putInt("Progress", 0);
			data.putInt("Cooldown", 0);
		}
		int cooldown = data.getInt("Cooldown");
		if (cooldown > 0) {
			data.putInt("Cooldown", cooldown - 1);
			return;
		}
		float hardness = PulveriserLogic.maxHardness(world, layer);
		int progress = data.getInt("Progress") + PulveriserLogic.progressStep(tier, hardness, data.getInt("Progress"));
		if (progress >= 10) {
			PulveriserLogic.clearCracks(world, breakerId, layer);
			if (currentTier(context, true) == null) {
				reset(world, breakerId, layer, data);
				return;
			}
			int cost = PulveriserLogic.breakLayer(world, layer, stack -> collectOrDropItem(context, stack));
			spend(context, cost);
			data.remove("Layer");
			data.putInt("Progress", 0);
			return;
		}
		PulveriserLogic.showCracks(world, breakerId, layer, progress);
		data.putInt("Progress", progress);
		data.putInt("Cooldown", PulveriserLogic.ticksBetweenSteps(tier, hardness));
	}

	private static void reset(Level world, int breakerId, List<BlockPos> layer, CompoundTag data) {
		if (!layer.isEmpty())
			PulveriserLogic.clearCracks(world, breakerId, layer);
		data.remove("Layer");
		data.putInt("Progress", 0);
	}

	@Override
	public void stopMoving(MovementContext context) {
		if (!context.world.isClientSide && context.data.contains("BreakerId"))
			reset(context.world, context.data.getInt("BreakerId"), PulveriserLogic.readLayer(context.data, "Layer"), context.data);
	}
}
