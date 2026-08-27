package co.pyragon.jamoss.content.condenser;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;

import co.pyragon.jamoss.registry.COBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A resizable tank (same multiblock rules as Create's Fluid Tank) that slowly condenses
 * gases into liquids and solids. Uses its own block entity type so it never merges with
 * Create's tanks.
 */
public class CondenserBlock extends FluidTankBlock {

	public CondenserBlock(Properties properties) {
		super(properties, false);
	}

	@Override
	public BlockEntityType<? extends FluidTankBlockEntity> getBlockEntityType() {
		return COBlockEntityTypes.CONDENSER.get();
	}

	/** Create's tank only allows container interaction in creative; the condenser allows it always. */
	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hitResult) {
		if (stack.isEmpty())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		FluidTankBlockEntity be = ConnectivityHandler.partAt(getBlockEntityType(), level, pos);
		if (be == null)
			return ItemInteractionResult.FAIL;
		if (FluidHelper.tryEmptyItemIntoBE(level, player, hand, stack, be)
			|| FluidHelper.tryFillItemFromBE(level, player, hand, stack, be))
			return ItemInteractionResult.SUCCESS;
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	/** No boiler behaviour: skip the tank's heat re-evaluation on neighbour changes. */
	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
		BlockPos pos, BlockPos neighborPos) {
		return state;
	}
}
