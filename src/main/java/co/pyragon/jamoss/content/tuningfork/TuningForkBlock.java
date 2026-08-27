package co.pyragon.jamoss.content.tuningfork;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;

import co.pyragon.jamoss.registry.COBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A vertical shaft relay that hands rotation downward at exactly the RPM of its configured
 * frequency band (when the input is at least that fast). Sits between a shaft and a Resonator.
 */
public class TuningForkBlock extends KineticBlock implements IBE<TuningForkBlockEntity> {

	public TuningForkBlock(Properties properties) {
		super(properties);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == Axis.Y;
	}

	@Override
	public Class<TuningForkBlockEntity> getBlockEntityClass() {
		return TuningForkBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends TuningForkBlockEntity> getBlockEntityType() {
		return COBlockEntityTypes.TUNING_FORK.get();
	}
}
