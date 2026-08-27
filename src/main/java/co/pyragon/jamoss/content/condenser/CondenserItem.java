package co.pyragon.jamoss.content.condenser;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.block.IBE;

import co.pyragon.jamoss.registry.COBlockEntityTypes;
import co.pyragon.jamoss.registry.COBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

/** Copy of Create's FluidTankItem targeting the Condenser: placing on a multiblock fills the whole layer. */
public class CondenserItem extends BlockItem {

	public CondenserItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public InteractionResult place(BlockPlaceContext ctx) {
		InteractionResult initialResult = super.place(ctx);
		if (!initialResult.consumesAction())
			return initialResult;
		tryMultiPlace(ctx);
		return initialResult;
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos blockPos, Level level, Player player, ItemStack itemStack,
		BlockState blockState) {
		MinecraftServer server = level.getServer();
		if (server == null)
			return false;
		CustomData blockEntityData = itemStack.get(DataComponents.BLOCK_ENTITY_DATA);
		if (blockEntityData != null) {
			CompoundTag nbt = blockEntityData.copyTag();
			nbt.remove("Luminosity");
			nbt.remove("Size");
			nbt.remove("Height");
			nbt.remove("Controller");
			nbt.remove("LastKnownPos");
			nbt.remove("OutputItems");
			nbt.remove("Progress");
			for (String key : new String[] { "TankContent", "OutputContent" }) {
				if (!nbt.contains(key))
					continue;
				FluidStack fluid = FluidStack.parseOptional(server.registryAccess(), nbt.getCompound(key));
				if (!fluid.isEmpty()) {
					fluid.setAmount(Math.min(FluidTankBlockEntity.getCapacityMultiplier(), fluid.getAmount()));
					nbt.put(key, fluid.saveOptional(server.registryAccess()));
				}
			}
			BlockEntity.addEntityType(nbt, ((IBE<?>) getBlock()).getBlockEntityType());
			itemStack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(nbt));
		}
		return super.updateCustomBlockEntityTag(blockPos, level, player, itemStack, blockState);
	}

	private void tryMultiPlace(BlockPlaceContext ctx) {
		Player player = ctx.getPlayer();
		if (player == null || player.isShiftKeyDown())
			return;
		Direction face = ctx.getClickedFace();
		if (!face.getAxis().isVertical())
			return;

		ItemStack stack = ctx.getItemInHand();
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockPos placedOnPos = pos.relative(face.getOpposite());
		if (!COBlocks.CONDENSER.has(world.getBlockState(placedOnPos)))
			return;
		if (SymmetryWandItem.presentInHotbar(player))
			return;

		FluidTankBlockEntity tankAt = ConnectivityHandler.partAt(COBlockEntityTypes.CONDENSER.get(), world, placedOnPos);
		if (tankAt == null)
			return;
		FluidTankBlockEntity controllerBE = tankAt.getControllerBE();
		if (controllerBE == null)
			return;

		int width = controllerBE.getWidth();
		if (width == 1)
			return;

		int toPlace = 0;
		BlockPos startPos = face == Direction.DOWN ? controllerBE.getBlockPos().below()
			: controllerBE.getBlockPos().above(controllerBE.getHeight());
		if (startPos.getY() != pos.getY())
			return;

		for (int x = 0; x < width; x++) {
			for (int z = 0; z < width; z++) {
				BlockPos offsetPos = startPos.offset(x, 0, z);
				BlockState blockState = world.getBlockState(offsetPos);
				if (COBlocks.CONDENSER.has(blockState))
					continue;
				if (!blockState.canBeReplaced())
					return;
				toPlace++;
			}
		}
		if (!player.isCreative() && stack.getCount() < toPlace)
			return;

		for (int x = 0; x < width; x++) {
			for (int z = 0; z < width; z++) {
				BlockPos offsetPos = startPos.offset(x, 0, z);
				if (COBlocks.CONDENSER.has(world.getBlockState(offsetPos)))
					continue;
				BlockPlaceContext context = BlockPlaceContext.at(ctx, offsetPos, face);
				player.getPersistentData().putBoolean("SilenceTankSound", true);
				super.place(context);
				player.getPersistentData().remove("SilenceTankSound");
			}
		}
	}
}
