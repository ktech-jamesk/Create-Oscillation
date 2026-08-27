package co.pyragon.jamoss.content.vent;

import com.simibubi.create.foundation.block.IBE;

import co.pyragon.jamoss.registry.COBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Voids any gas piped into it, with a visible plume. Liquids are refused. */
public class VentBlock extends Block implements IBE<VentBlockEntity> {

	private static final VoxelShape SHAPE = Shapes.or(Block.box(2, 0, 2, 14, 10, 14), Block.box(1, 10, 1, 15, 12, 15));

	public VentBlock(Properties properties) {
		super(properties);
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
	public Class<VentBlockEntity> getBlockEntityClass() {
		return VentBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends VentBlockEntity> getBlockEntityType() {
		return COBlockEntityTypes.VENT.get();
	}
}
