package co.pyragon.jamoss.content.pump;

import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;

import co.pyragon.jamoss.registry.COBlockEntityTypes;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** A mechanical pump that only moves gases. */
public class ResonancePumpBlock extends PumpBlock {

	public ResonancePumpBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntityType<? extends PumpBlockEntity> getBlockEntityType() {
		return COBlockEntityTypes.RESONANCE_PUMP.get();
	}
}
