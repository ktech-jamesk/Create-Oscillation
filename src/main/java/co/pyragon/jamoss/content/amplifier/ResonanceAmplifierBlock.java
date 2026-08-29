package co.pyragon.jamoss.content.amplifier;

import com.simibubi.create.foundation.block.IBE;

import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.registry.COBlockEntityTypes;
import co.pyragon.jamoss.registry.COItems;
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

/** Sits under a Resonator; raises the band handed to the block below according to the crystals inside. */
public class ResonanceAmplifierBlock extends Block implements IBE<ResonanceAmplifierBlockEntity> {

	private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(2, 3, 2, 14, 13, 14), Block.box(0, 13, 0, 16, 16, 16));

	public ResonanceAmplifierBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit) {
		return getBlockEntityOptional(level, pos).map(be -> use(be.crystals, stack, level, pos, player))
			.orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
	}

	/** A crystal goes into its band's slot if free; an empty hand pops the highest crystal out. Shared with the Pulveriser. */
	public static ItemInteractionResult use(CrystalLadder ladder, ItemStack held, Level level, BlockPos pos, Player player) {
		if (!held.isEmpty()) {
			FrequencyBand band = COItems.bandOf(held.getItem());
			if (band == null)
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			int slot = CrystalLadder.slotOf(band);
			if (!ladder.getStackInSlot(slot).isEmpty())
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			if (level.isClientSide)
				return ItemInteractionResult.SUCCESS;
			ladder.setStackInSlot(slot, held.copyWithCount(1));
			if (!player.isCreative())
				held.shrink(1);
			level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, .6f, 1f + slot * 0.15f);
			return ItemInteractionResult.SUCCESS;
		}
		int top = ladder.topFilled();
		if (top < 0)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (level.isClientSide)
			return ItemInteractionResult.SUCCESS;
		ItemStack inside = ladder.getStackInSlot(top);
		ladder.setStackInSlot(top, ItemStack.EMPTY);
		player.getInventory().placeItemBackInInventory(inside);
		level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, .6f, 1f);
		return ItemInteractionResult.SUCCESS;
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
	public Class<ResonanceAmplifierBlockEntity> getBlockEntityClass() {
		return ResonanceAmplifierBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ResonanceAmplifierBlockEntity> getBlockEntityType() {
		return COBlockEntityTypes.RESONANCE_AMPLIFIER.get();
	}
}
