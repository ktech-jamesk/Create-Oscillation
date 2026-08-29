package co.pyragon.jamoss.content.pulveriser;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.amplifier.CrystalLadder;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.content.pulveriser.PulveriserLogic.Tier;
import co.pyragon.jamoss.content.vibration.VibrationSource;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Driven by a VibrationSource (Resonator or Amplifier) directly above it, like the Chamber and
 * Sieve. The crystal ladder sets the tier; it runs only while the source's band exactly equals the
 * ladder's band. Crystals are seated, never consumed.
 */
public class SonicPulveriserBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

	public final CrystalLadder crystals = new CrystalLadder(this::onCrystalsChanged);
	public FilteringBehaviour filtering;

	private final int breakerId = -BlockBreakingKineticBlockEntity.NEXT_BREAKER_ID.addAndGet(64);
	private List<BlockPos> layer = List.of();
	private int progress;
	private int cooldown;
	/** Synced: true while a layer is cracking (drives the fork shiver). */
	private boolean working;

	public SonicPulveriserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, co.pyragon.jamoss.registry.COBlockEntityTypes.SONIC_PULVERISER.get(),
			(be, ctx) -> be.crystals.insertOnly());
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		filtering = new FilteringBehaviour(this, new FilterSlot())
			// crystals go into the ladder, never into the filter (a crystal filter matches no block)
			.withPredicate(stack -> co.pyragon.jamoss.registry.COItems.bandOf(stack.getItem()) == null);
		behaviours.add(filtering);
	}

	/** Filter box centred on every side face, slid toward the back. */
	private static class FilterSlot extends CenteredSideValueBoxTransform {
		FilterSlot() {
			super((state, dir) -> dir.getAxis() != state.getValue(DirectionalBlock.FACING).getAxis());
		}

		@Override
		public net.minecraft.world.phys.Vec3 getLocalOffset(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state) {
			net.minecraft.world.phys.Vec3 base = super.getLocalOffset(level, pos, state);
			Direction back = state.getValue(DirectionalBlock.FACING).getOpposite();
			return base.add(back.getStepX() * 4.5 / 16, back.getStepY() * 4.5 / 16, back.getStepZ() * 4.5 / 16);
		}
	}

	public Direction getFacing() {
		return getBlockState().getValue(DirectionalBlock.FACING);
	}

	/** Tier the ladder reaches, or null while the Low rung is empty. */
	@Nullable
	public Tier getTier() {
		return PulveriserLogic.tierOf(crystals.band());
	}

	/** Band of the vibration source above, null when idle or absent. */
	@Nullable
	public FrequencyBand getInputBand() {
		return FrequencyBand.of(VibrationSource.speedAbove(level, worldPosition));
	}

	/** True while the source above runs in exactly the ladder's band. */
	public boolean isDriven() {
		Tier tier = getTier();
		return tier != null && getInputBand() == tier.band();
	}

	public boolean isWorking() {
		return working;
	}

	private void onCrystalsChanged() {
		setChanged();
		sendData();
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide)
			return;

		Tier tier = getTier();
		if (tier == null || !isDriven()) {
			reset();
			return;
		}
		List<BlockPos> next = PulveriserLogic.findLayer(level, worldPosition, getFacing(), tier,
			state -> filtering.test(new ItemStack(state.getBlock().asItem())));
		if (next.isEmpty()) {
			reset();
			return;
		}
		if (!next.equals(layer)) {
			PulveriserLogic.clearCracks(level, breakerId, layer);
			layer = next;
			progress = 0;
			cooldown = 0;
		}
		setWorking(true);
		if (cooldown-- > 0)
			return;

		float hardness = PulveriserLogic.maxHardness(level, layer);
		progress += PulveriserLogic.progressStep(tier, hardness, progress);
		if (progress >= 10) {
			PulveriserLogic.clearCracks(level, breakerId, layer);
			PulveriserLogic.breakLayer(level, layer, this::dropStack);
			layer = List.of();
			progress = 0;
			cooldown = 0;
			return;
		}
		PulveriserLogic.showCracks(level, breakerId, layer, progress);
		cooldown = PulveriserLogic.ticksBetweenSteps(tier, hardness);
	}

	/** Drops land in the inventory below the drop point when there is one; the rest is spat out in front. */
	private void dropStack(ItemStack stack) {
		if (stack.isEmpty() || !level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS))
			return;
		BlockPos front = worldPosition.relative(getFacing());
		IItemHandler below = level.getCapability(Capabilities.ItemHandler.BLOCK, front.below(), Direction.UP);
		if (below != null) {
			stack = ItemHandlerHelper.insertItemStacked(below, stack, false);
			if (stack.isEmpty())
				return;
		}
		Vec3 at = Vec3.atCenterOf(front);
		ItemEntity entity = new ItemEntity(level, at.x, at.y, at.z, stack);
		entity.setDefaultPickUpDelay();
		entity.setDeltaMovement(Vec3.ZERO);
		level.addFreshEntity(entity);
	}

	private void reset() {
		if (!layer.isEmpty())
			PulveriserLogic.clearCracks(level, breakerId, layer);
		layer = List.of();
		progress = 0;
		cooldown = 0;
		setWorking(false);
	}

	private void setWorking(boolean value) {
		if (working == value)
			return;
		working = value;
		sendData();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (level != null && !level.isClientSide)
			PulveriserLogic.clearCracks(level, breakerId, layer);
	}

	@Override
	public void destroy() {
		super.destroy();
		if (level == null)
			return;
		for (int i = 0; i < crystals.getSlots(); i++)
			if (!crystals.getStackInSlot(i).isEmpty())
				Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
					crystals.getStackInSlot(i));
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		crystals.write(tag, registries);
		tag.putBoolean("Working", working);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		crystals.read(tag, registries);
		working = tag.getBoolean("Working");
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		Tier tier = getTier();
		LangBuilder value = tier == null
			? lang().translate("gui.goggles.coupler.no_crystal").style(ChatFormatting.DARK_GRAY)
			: lang().translate("frequency." + tier.band().getSerializedName()).style(ChatFormatting.AQUA);
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

		FrequencyBand input = getInputBand();
		if (tier == null)
			lang().translate("gui.goggles.pulveriser.no_crystal").style(ChatFormatting.GRAY).forGoggles(tooltip);
		else if (input == null)
			lang().translate("gui.goggles.pulveriser.no_vibration").style(ChatFormatting.GRAY).forGoggles(tooltip);
		else if (input != tier.band())
			lang().translate("gui.goggles.pulveriser.wrong_band", input.getDisplayName(), tier.band().getDisplayName())
				.style(ChatFormatting.RED).forGoggles(tooltip);
		else if (working)
			lang().translate("gui.goggles.pulveriser.working").style(ChatFormatting.GREEN).forGoggles(tooltip);
		else
			lang().translate("gui.goggles.pulveriser.nothing").style(ChatFormatting.GRAY).forGoggles(tooltip);
		return true;
	}

	private static LangBuilder lang() {
		return new LangBuilder(CreateOscillation.MOD_ID);
	}
}
