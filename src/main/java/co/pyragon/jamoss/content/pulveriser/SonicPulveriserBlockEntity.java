package co.pyragon.jamoss.content.pulveriser;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.registry.COItems;
import co.pyragon.jamoss.content.pulveriser.PulveriserLogic.Tier;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class SonicPulveriserBlockEntity extends KineticBlockEntity {

	public final PulveriserInventory inventory = new PulveriserInventory(this::onInventoryChanged);
	public FilteringBehaviour filtering;
	/** Band of the crystal currently burning, null when none has been consumed yet. */
	@Nullable
	private FrequencyBand activeBand;
	/** Remaining charge of the burning crystal. */
	private int charge;

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
			(be, ctx) -> be.inventory.external());
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		filtering = new FilteringBehaviour(this, new FilterSlot())
			// crystals go in the crystal slot, never into the filter (a crystal filter matches no block)
			.withPredicate(stack -> co.pyragon.jamoss.registry.COItems.bandOf(stack.getItem()) == null);
		behaviours.add(filtering);
	}

	/** Filter box centred on every side face, slid toward the shaft end. */
	private static class FilterSlot extends CenteredSideValueBoxTransform {
		FilterSlot() {
			super((state, dir) -> dir.getAxis() != state.getValue(DirectionalKineticBlock.FACING).getAxis());
		}

		@Override
		public net.minecraft.world.phys.Vec3 getLocalOffset(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state) {
			net.minecraft.world.phys.Vec3 base = super.getLocalOffset(level, pos, state);
			Direction back = state.getValue(DirectionalKineticBlock.FACING).getOpposite();
			return base.add(back.getStepX() * 4.5 / 16, back.getStepY() * 4.5 / 16, back.getStepZ() * 4.5 / 16);
		}
	}

	public Direction getFacing() {
		return getBlockState().getValue(DirectionalKineticBlock.FACING);
	}

	/** The tier in use: the burning crystal's, or the next crystal waiting in the slot. */
	@Nullable
	public Tier getTier() {
		if (activeBand != null)
			return PulveriserLogic.tierOf(activeBand);
		return PulveriserLogic.tierOf(inventory.getStackInSlot(PulveriserInventory.CRYSTALS));
	}

	public int getCharge() {
		return charge;
	}

	public boolean hasFuel() {
		return activeBand != null || !inventory.getStackInSlot(PulveriserInventory.CRYSTALS).isEmpty()
			|| !inventory.getStackInSlot(PulveriserInventory.SPENT).isEmpty();
	}

	/** The crystal shown inside the housing: the one burning, else the next waiting one. */
	public ItemStack getDisplayedCrystal() {
		if (activeBand != null)
			return COItems.tunedCrystal(activeBand).asStack();
		return inventory.getStackInSlot(PulveriserInventory.CRYSTALS);
	}

	/** Hands every crystal back; the burning one always comes back as a rough crystal (its charge is gone). */
	public void ejectFuel(@Nullable net.minecraft.world.entity.player.Player player) {
		java.util.List<ItemStack> out = new java.util.ArrayList<>();
		if (activeBand != null) {
			out.add(COItems.ROUGH_QUARTZ_CRYSTAL.asStack());
			activeBand = null;
			charge = 0;
		}
		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			ItemStack stack = inventory.getStackInSlot(slot);
			if (!stack.isEmpty())
				out.add(stack);
			inventory.setStackInSlot(slot, ItemStack.EMPTY);
		}
		for (ItemStack stack : out) {
			if (player != null)
				player.getInventory().placeItemBackInInventory(stack);
			else
				net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
		}
		reset();
		level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_BREAK, net.minecraft.sounds.SoundSource.BLOCKS, .6f, .9f);
		sendData();
	}

	/** Sets the burning crystal and its remaining charge directly. */
	public void setCharge(FrequencyBand band, int remaining) {
		activeBand = band;
		charge = remaining;
	}

	/** Discards the burning crystal so the next waiting one is consumed. */
	public void dropActiveCrystal() {
		activeBand = null;
		charge = 0;
	}

	/** Consumes the next waiting crystal when nothing is burning. */
	private boolean ensureCharged() {
		if (activeBand != null && charge > 0)
			return true;
		ItemStack next = inventory.takeCrystal();
		Tier tier = PulveriserLogic.tierOf(next);
		if (tier == null)
			return false;
		activeBand = tier.band();
		charge = tier.charge();
		sendData();
		return true;
	}

	private void spendCharge(int cost) {
		charge -= cost;
		if (charge > 0)
			return;
		charge = 0;
		activeBand = null;
		ItemStack rough = COItems.ROUGH_QUARTZ_CRYSTAL.asStack();
		ItemStack left = inventory.addSpent(rough);
		if (!left.isEmpty())
			net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), left);
		sendData();
	}

	public boolean isWorking() {
		return working;
	}

	private void onInventoryChanged() {
		setChanged();
		sendData();
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide)
			return;

		Tier tier = getTier();
		if (tier == null || getSpeed() == 0) {
			reset();
			return;
		}
		if (!ensureCharged()) {
			reset();
			return;
		}
		tier = getTier();
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
			int cost = PulveriserLogic.breakLayer(level, layer, this::dropStack);
			spendCharge(cost);
			layer = List.of();
			progress = 0;
			cooldown = 0;
			return;
		}
		PulveriserLogic.showCracks(level, breakerId, layer, progress);
		cooldown = PulveriserLogic.ticksBetweenSteps(tier, hardness);
	}

	private void dropStack(ItemStack stack) {
		if (stack.isEmpty() || !level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS))
			return;
		Vec3 at = Vec3.atCenterOf(worldPosition.relative(getFacing()));
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
		for (int i = 0; i < inventory.getSlots(); i++)
			if (!inventory.getStackInSlot(i).isEmpty())
				net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
					inventory.getStackInSlot(i));
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.put("Inventory", inventory.serializeNBT(registries));
		tag.putBoolean("Working", working);
		tag.putInt("Charge", charge);
		if (activeBand != null)
			tag.putString("ActiveBand", activeBand.getSerializedName());
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		if (tag.contains("Inventory"))
			inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
		working = tag.getBoolean("Working");
		charge = tag.getInt("Charge");
		activeBand = tag.contains("ActiveBand") ? FrequencyBand.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE,
			net.minecraft.nbt.StringTag.valueOf(tag.getString("ActiveBand"))).result().orElse(null) : null;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		Tier tier = getTier();
		LangBuilder value = tier == null
			? new LangBuilder(CreateOscillation.MOD_ID).translate("gui.goggles.coupler.no_crystal").style(ChatFormatting.DARK_GRAY)
			: new LangBuilder(CreateOscillation.MOD_ID).translate("frequency." + tier.band().getSerializedName()).style(ChatFormatting.AQUA);
		new LangBuilder(CreateOscillation.MOD_ID).translate("gui.goggles.tuning_fork").style(ChatFormatting.GRAY).space().add(value).forGoggles(tooltip);
		if (tier != null)
			new LangBuilder(CreateOscillation.MOD_ID).translate("gui.goggles.pulveriser.fuel", activeBand == null ? 0 : charge, tier.charge())
				.style(ChatFormatting.GRAY).forGoggles(tooltip);
		String state = tier == null ? "gui.goggles.pulveriser.no_crystal"
			: getSpeed() == 0 ? "gui.goggles.pulveriser.no_rotation"
			: working ? "gui.goggles.pulveriser.working" : "gui.goggles.pulveriser.nothing";
		new LangBuilder(CreateOscillation.MOD_ID).translate(state)
			.style(working ? ChatFormatting.GREEN : ChatFormatting.GRAY).forGoggles(tooltip);
		return true;
	}
}
