package co.pyragon.jamoss.content.vent;

import java.util.List;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import co.pyragon.jamoss.registry.COBlockEntityTypes;
import co.pyragon.jamoss.registry.COFluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class VentBlockEntity extends SmartBlockEntity {

	private static final int PLUME_TICKS = 10;

	private final IFluidHandler handler = new VoidingGasHandler();
	private int venting;

	public VentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, COBlockEntityTypes.VENT.get(), (be, context) -> be.handler);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	@Override
	public void tick() {
		super.tick();
		if (venting <= 0)
			return;
		venting--;
		if (level.isClientSide) {
			double x = worldPosition.getX() + 0.35 + level.random.nextFloat() * 0.3;
			double z = worldPosition.getZ() + 0.35 + level.random.nextFloat() * 0.3;
			level.addParticle(ParticleTypes.CLOUD, x, worldPosition.getY() + 0.8, z, 0, 0.08, 0);
		} else if (venting == PLUME_TICKS - 1 && level.random.nextInt(4) == 0) {
			level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.15f,
				1.6f + level.random.nextFloat() * 0.4f);
		}
	}

	@Override
	protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(compound, registries, clientPacket);
		compound.putInt("Venting", venting);
	}

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(compound, registries, clientPacket);
		venting = compound.getInt("Venting");
	}

	private class VoidingGasHandler implements IFluidHandler {
		public int getTanks() { return 1; }
		public FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }
		public int getTankCapacity(int tank) { return 1000; }
		public boolean isFluidValid(int tank, FluidStack stack) { return COFluidTags.isGas(stack); }

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			if (!isFluidValid(0, resource))
				return 0;
			int accepted = Math.min(resource.getAmount(), getTankCapacity(0));
			if (action.execute() && accepted > 0 && !level.isClientSide) {
				boolean wasIdle = venting <= 0;
				venting = PLUME_TICKS;
				if (wasIdle)
					sendData();
			}
			return accepted;
		}

		public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
		public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
	}
}
