package co.pyragon.jamoss.content.sieve;

import com.simibubi.create.foundation.block.IBE;

import co.pyragon.jamoss.registry.COBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * A full-height mesh cage that sits directly under a Resonator and is shaken by it. Items go
 * in through the open sides; what falls through drops into the inventory below.
 */
public class VibratingSieveBlock extends Block implements IBE<VibratingSieveBlockEntity> {

	private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 0, 0, 16, 2, 16), Block.box(1, 2, 1, 15, 16, 15));

	public VibratingSieveBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hitResult) {
		if (level.isClientSide)
			return ItemInteractionResult.SUCCESS;
		return getBlockEntityOptional(level, pos).map(sieve -> {
			if (!stack.isEmpty()) {
				ItemStack toInsert = player.isShiftKeyDown() ? stack.copyWithCount(1) : stack.copy();
				ItemStack remainder = ItemHandlerHelper.insertItem(sieve.getItemHandler(), toInsert, false);
				int inserted = toInsert.getCount() - remainder.getCount();
				if (inserted == 0)
					return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
				if (!player.isCreative())
					stack.shrink(inserted);
				level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, .2f, 1f + level.getRandom().nextFloat());
				return ItemInteractionResult.SUCCESS;
			}
			// empty hand: take the outputs, or the input if there are none
			boolean emptyOutput = true;
			IItemHandlerModifiable inv = sieve.outputInv;
			for (int slot = 0; slot < inv.getSlots(); slot++) {
				ItemStack s = inv.getStackInSlot(slot);
				if (!s.isEmpty())
					emptyOutput = false;
				player.getInventory().placeItemBackInInventory(s);
				inv.setStackInSlot(slot, ItemStack.EMPTY);
			}
			if (emptyOutput) {
				inv = sieve.inputInv;
				for (int slot = 0; slot < inv.getSlots(); slot++) {
					player.getInventory().placeItemBackInInventory(inv.getStackInSlot(slot));
					inv.setStackInSlot(slot, ItemStack.EMPTY);
				}
			}
			sieve.setChanged();
			sieve.sendData();
			return ItemInteractionResult.SUCCESS;
		}).orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		IBE.onRemove(state, level, pos, newState);
	}

	@Override
	public Class<VibratingSieveBlockEntity> getBlockEntityClass() {
		return VibratingSieveBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends VibratingSieveBlockEntity> getBlockEntityType() {
		return COBlockEntityTypes.VIBRATING_SIEVE.get();
	}
}
