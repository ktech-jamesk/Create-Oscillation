package co.pyragon.jamoss.content.chamber;

import co.pyragon.jamoss.registry.COPartialModels;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class CavitationChamberRenderer extends ResonanceChamberRenderer {

	public CavitationChamberRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected PartialModel body() {
		return COPartialModels.CAVITATION_BODY;
	}

	@Override
	protected PartialModel directionalBody() {
		return COPartialModels.CAVITATION_BODY_DIRECTIONAL;
	}
}
