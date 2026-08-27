package co.pyragon.jamoss.content.resonator;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;

import co.pyragon.jamoss.registry.COBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Vibration source. Takes a shaft from above and drives the block beneath it (chamber, sieve).
 */
public class ResonatorBlock extends KineticBlock implements IBE<ResonatorBlockEntity> {

	/** Base plate plus the fork/rotor column. */
	private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(1, 3, 1, 15, 16, 15));

	public ResonatorBlock(Properties properties) {
		super(properties);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
		return face == Direction.UP;
	}

	@Override
	public SpeedLevel getMinimumRequiredSpeedLevel() {
		return SpeedLevel.MEDIUM;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public Class<ResonatorBlockEntity> getBlockEntityClass() {
		return ResonatorBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ResonatorBlockEntity> getBlockEntityType() {
		return COBlockEntityTypes.RESONATOR.get();
	}
}
