package co.pyragon.jamoss.content.coupler;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Right-click handling shared by emitter and receiver: crystal in with a crystal, crystal out with an empty hand. */
public final class CouplerBlockBase {

	private CouplerBlockBase() {}

	public static ItemInteractionResult useCrystal(CrystalSlot slot, ItemStack held, Level level, BlockPos pos, Player player) {
		if (!held.isEmpty()) {
			if (!slot.isItemValid(0, held))
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			if (level.isClientSide)
				return ItemInteractionResult.SUCCESS;
			ItemStack previous = slot.getStackInSlot(0);
			slot.setStackInSlot(0, held.copyWithCount(1));
			if (!player.isCreative())
				held.shrink(1);
			if (!previous.isEmpty())
				player.getInventory().placeItemBackInInventory(previous);
			level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, .6f, 1.2f);
			return ItemInteractionResult.SUCCESS;
		}
		ItemStack inside = slot.getStackInSlot(0);
		if (inside.isEmpty())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (level.isClientSide)
			return ItemInteractionResult.SUCCESS;
		slot.setStackInSlot(0, ItemStack.EMPTY);
		player.getInventory().placeItemBackInInventory(inside);
		level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, .6f, 1f);
		return ItemInteractionResult.SUCCESS;
	}

	public static ItemInteractionResult use(CrystalSlot slot, ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit) {
		return useCrystal(slot, stack, level, pos, player);
	}
}
