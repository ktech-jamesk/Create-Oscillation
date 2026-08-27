package co.pyragon.jamoss.content.coupler;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

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

/**
 * Catches a Resonance Emitter's beam and turns it back into rotation. {@code FACING} is the
 * output shaft side; the dish looks the other way, toward the emitter.
 */
public class ResonanceReceiverBlock extends DirectionalKineticBlock implements IBE<ResonanceReceiverBlockEntity> {

	public ResonanceReceiverBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		// shaft points away from the player, dish toward them
		Direction facing = context.getHorizontalDirection();
		return defaultBlockState().setValue(FACING, facing);
	}

	@Override
	public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
		return face == state.getValue(FACING);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING).getAxis();
	}

	@Override
	public boolean hideStressImpact() {
		return true;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit) {
		return getBlockEntityOptional(level, pos)
			.map(be -> CouplerBlockBase.use(be.crystal, stack, state, level, pos, player, hand, hit))
			.orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	@Override
	public Class<ResonanceReceiverBlockEntity> getBlockEntityClass() {
		return ResonanceReceiverBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ResonanceReceiverBlockEntity> getBlockEntityType() {
		return COBlockEntityTypes.RESONANCE_RECEIVER.get();
	}
}
