package co.pyragon.jamoss.content.coupler;

import com.simibubi.create.foundation.block.IBE;

import co.pyragon.jamoss.registry.COBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Sits under a Resonator and beams its vibration along {@link #FACING} through open air, to a
 * Resonance Receiver holding a crystal of the same band.
 */
public class ResonanceEmitterBlock extends HorizontalDirectionalBlock implements IBE<ResonanceEmitterBlockEntity> {

	public static final com.mojang.serialization.MapCodec<ResonanceEmitterBlock> CODEC = simpleCodec(ResonanceEmitterBlock::new);
	private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 0, 0, 16, 4, 16), Block.box(2, 4, 2, 14, 16, 14));

	public ResonanceEmitterBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected com.mojang.serialization.MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit) {
		return getBlockEntityOptional(level, pos)
			.map(be -> CouplerBlockBase.use(be.crystal, stack, state, level, pos, player, hand, hit))
			.orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
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
	public Class<ResonanceEmitterBlockEntity> getBlockEntityClass() {
		return ResonanceEmitterBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ResonanceEmitterBlockEntity> getBlockEntityType() {
		return COBlockEntityTypes.RESONANCE_EMITTER.get();
	}
}
