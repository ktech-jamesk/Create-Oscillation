package co.pyragon.jamoss.content.chamber;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;

import co.pyragon.jamoss.registry.COBlockEntityTypes;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Reinforced chamber for {@code cavitating} recipes (the Ultrasonic ore step). */
public class CavitationChamberBlock extends ResonanceChamberBlock {

	public CavitationChamberBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntityType<? extends BasinBlockEntity> getBlockEntityType() {
		return COBlockEntityTypes.CAVITATION_CHAMBER.get();
	}
}
