package co.pyragon.jamoss.content.condenser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.FluidTankCTBehaviour;
import com.simibubi.create.foundation.block.connected.CTModel;

import co.pyragon.jamoss.registry.COSpriteShifts;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

/** Connected-texture model for the Condenser; mirrors Create's FluidTankModel (whose constructor is private). */
public class CondenserModel extends CTModel {

	private static final ModelProperty<boolean[]> CULLED = new ModelProperty<>();

	public CondenserModel(BakedModel originalModel) {
		super(originalModel, new FluidTankCTBehaviour(COSpriteShifts.CONDENSER, COSpriteShifts.CONDENSER_TOP,
			COSpriteShifts.CONDENSER_INNER));
	}

	@Override
	protected ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world, BlockPos pos,
		BlockState state, ModelData blockEntityData) {
		super.gatherModelData(builder, world, pos, state, blockEntityData);
		boolean[] culled = new boolean[4];
		for (Direction d : Iterate.horizontalDirections)
			culled[d.get2DDataValue()] = ConnectivityHandler.isConnected(world, pos, pos.relative(d));
		return builder.with(CULLED, culled);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData,
		RenderType renderType) {
		if (side != null)
			return Collections.emptyList();
		boolean[] culled = extraData.get(CULLED);
		List<BakedQuad> quads = new ArrayList<>();
		for (Direction d : Iterate.directions) {
			if (culled != null && !d.getAxis().isVertical() && culled[d.get2DDataValue()])
				continue;
			quads.addAll(super.getQuads(state, d, rand, extraData, renderType));
		}
		quads.addAll(super.getQuads(state, null, rand, extraData, renderType));
		return quads;
	}
}
