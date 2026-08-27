package co.pyragon.jamoss.content.chamber;

import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;

import co.pyragon.jamoss.content.resonator.ResonatorBlockEntity;
import co.pyragon.jamoss.registry.COBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** A glass-covered basin: the vessel in which resonance turns inputs into gas. */
public class ResonanceChamberBlock extends BasinBlock {

	public ResonanceChamberBlock(Properties properties) {
		super(properties);
	}

	/** A Resonator directly above is expected; any other basin operator still blocks placement. */
	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockEntity above = level.getBlockEntity(pos.above());
		if (above instanceof ResonatorBlockEntity)
			return true;
		return super.canSurvive(state, level, pos);
	}

	/**
	 * The Resonator covers the top, so items can't be dropped in: right-clicking any face with an
	 * item inserts it (whole stack; one item while sneaking). Fluid containers still work as before.
	 */
	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hitResult) {
		ItemInteractionResult result = super.useItemOn(stack, state, level, pos, player, hand, hitResult);
		if (result != ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION || stack.isEmpty())
			return result;
		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof ResonanceChamberBlockEntity chamber))
			return result;
		if (level.isClientSide)
			return ItemInteractionResult.SUCCESS;

		ItemStack toInsert = player.isShiftKeyDown() ? stack.copyWithCount(1) : stack.copy();
		ItemStack remainder = ItemHandlerHelper.insertItemStacked(chamber.getInputInventory(), toInsert, false);
		int inserted = toInsert.getCount() - remainder.getCount();
		if (inserted == 0)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (!player.isCreative())
			stack.shrink(inserted);
		level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, .2f, 1f + level.getRandom().nextFloat());
		return ItemInteractionResult.SUCCESS;
	}

	@Override
	public BlockEntityType<? extends BasinBlockEntity> getBlockEntityType() {
		return COBlockEntityTypes.RESONANCE_CHAMBER.get();
	}
}
