package co.pyragon.jamoss.content.pulveriser;

import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;

import co.pyragon.jamoss.content.amplifier.ResonanceAmplifierBlock;
import co.pyragon.jamoss.registry.COBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Vibration in from the top (Resonator or Amplifier), the breaking beam out the front. Facing up is
 * invalid — that face belongs to the vibration source. Tier set by the crystal ladder inside.
 */
public class SonicPulveriserBlock extends WrenchableDirectionalBlock implements IBE<SonicPulveriserBlockEntity> {

	public SonicPulveriserBlock(Properties properties) {
		super(properties);
	}

	/** Aims where the player is looking (sneak to flip); an upward result clamps to the horizontal look. */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		boolean sneaking = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
		Direction look = context.getNearestLookingDirection();
		Direction facing = sneaking ? look.getOpposite() : look;
		if (facing == Direction.UP)
			facing = sneaking ? context.getHorizontalDirection().getOpposite() : context.getHorizontalDirection();
		return defaultBlockState().setValue(FACING, facing);
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		BlockState rotated = super.getRotatedBlockState(originalState, targetedFace);
		if (rotated.getValue(FACING) == Direction.UP)
			rotated = super.getRotatedBlockState(rotated, targetedFace);
		return rotated;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit) {
		return getBlockEntityOptional(level, pos).map(be -> ResonanceAmplifierBlock.use(be.crystals, stack, level, pos, player))
			.orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		IBE.onRemove(state, level, pos, newState);
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
