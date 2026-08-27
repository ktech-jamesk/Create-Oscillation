package co.pyragon.jamoss.content.resonator;

import co.pyragon.jamoss.registry.COBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Clicking the top of a Resonance Chamber places the Resonator directly on it. */
public class ResonatorBlockItem extends BlockItem {

	public ResonatorBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public InteractionResult place(BlockPlaceContext context) {
		BlockPos placedOnPos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
		Level level = context.getLevel();
		if (context.getClickedFace() == Direction.UP && COBlocks.RESONANCE_CHAMBER.has(level.getBlockState(placedOnPos))) {
			BlockPos up = placedOnPos.above();
			if (!level.getBlockState(up).canBeReplaced())
				return InteractionResult.FAIL;
			Vec3 hit = Vec3.atCenterOf(up).add(0, 0.5, 0);
			context = new BlockPlaceContext(level, context.getPlayer(), context.getHand(), context.getItemInHand(),
				new BlockHitResult(hit, Direction.UP, up, false));
		}
		return super.place(context);
	}
}
