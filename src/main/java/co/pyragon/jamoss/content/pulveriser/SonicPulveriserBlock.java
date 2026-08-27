package co.pyragon.jamoss.content.pulveriser;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import co.pyragon.jamoss.registry.COBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;

/** Shaft in the back, vibration out the front. Breaks a layer of blocks at a time, tier set by its crystal. */
public class SonicPulveriserBlock extends DirectionalKineticBlock implements IBE<SonicPulveriserBlockEntity> {

	public SonicPulveriserBlock(Properties properties) {
		super(properties);
	}

	/** Snaps its back onto an adjacent shaft; otherwise aims where the player is looking (sneak to flip). */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction preferred = getPreferredFacing(context);
		boolean sneaking = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
		if (preferred != null && !sneaking)
			return defaultBlockState().setValue(FACING, preferred.getOpposite());
		Direction look = context.getNearestLookingDirection();
		return defaultBlockState().setValue(FACING, sneaking ? look.getOpposite() : look);
	}

	@Override
	public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
		return face == state.getValue(FACING).getOpposite();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING).getAxis();
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit) {
		return getBlockEntityOptional(level, pos).map(be -> {
			PulveriserInventory inv = be.inventory;
			if (!stack.isEmpty()) {
				if (!inv.isItemValid(PulveriserInventory.CRYSTALS, stack))
					return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
				if (level.isClientSide)
					return ItemInteractionResult.SUCCESS;
				ItemStack toInsert = player.isShiftKeyDown() ? stack.copyWithCount(1) : stack.copy();
				ItemStack remainder = inv.insertItem(PulveriserInventory.CRYSTALS, toInsert, false);
				int inserted = toInsert.getCount() - remainder.getCount();
				if (inserted == 0)
					return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
				if (!player.isCreative())
					stack.shrink(inserted);
				level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, .6f, 1.2f);
				return ItemInteractionResult.SUCCESS;
			}
			// empty hand: take spent crystals first, then waiting ones
			for (int slot : new int[] { PulveriserInventory.SPENT, PulveriserInventory.CRYSTALS }) {
				ItemStack inside = inv.getStackInSlot(slot);
				if (inside.isEmpty())
					continue;
				if (level.isClientSide)
					return ItemInteractionResult.SUCCESS;
				inv.setStackInSlot(slot, ItemStack.EMPTY);
				player.getInventory().placeItemBackInInventory(inside);
				level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, .6f, 1f);
				return ItemInteractionResult.SUCCESS;
			}
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}).orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
	}

	/** Sneak + wrench: eject the fuel (the burning crystal comes back rough); an empty machine dismantles as usual. */
	@Override
	public net.minecraft.world.InteractionResult onSneakWrenched(BlockState state, net.minecraft.world.item.context.UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (getBlockEntityOptional(level, pos).map(SonicPulveriserBlockEntity::hasFuel).orElse(false)) {
			if (!level.isClientSide)
				getBlockEntityOptional(level, pos).ifPresent(be -> be.ejectFuel(context.getPlayer()));
			return net.minecraft.world.InteractionResult.SUCCESS;
		}
		return super.onSneakWrenched(state, context);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	@Override
	public Class<SonicPulveriserBlockEntity> getBlockEntityClass() {
		return SonicPulveriserBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends SonicPulveriserBlockEntity> getBlockEntityType() {
		return COBlockEntityTypes.SONIC_PULVERISER.get();
	}
}
