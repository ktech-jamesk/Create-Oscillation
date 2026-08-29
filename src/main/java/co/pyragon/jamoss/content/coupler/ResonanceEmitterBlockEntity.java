package co.pyragon.jamoss.content.coupler;

import java.util.List;
import java.util.Optional;

import co.pyragon.jamoss.content.vibration.VibrationSource;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.content.resonator.ResonatorBlockEntity;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ResonanceEmitterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

	/** Beam length by crystal band: 8 / 16 / 32 / 64 blocks. */
	public static int rangeFor(FrequencyBand band) {
		return switch (band) {
			case LOW -> 8;
			case MID -> 16;
			case HIGH -> 32;
			case ULTRASONIC -> 64;
			default -> 0;
		};
	}

	public final CrystalSlot crystal = new CrystalSlot(this::onCrystalChanged);
	/** Distance to the linked receiver (blocks), 0 when not linked. Synced for the beam particles. */
	private int linkDistance;

	public ResonanceEmitterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, co.pyragon.jamoss.registry.COBlockEntityTypes.RESONANCE_EMITTER.get(),
			(be, ctx) -> be.crystal);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	private void onCrystalChanged() {
		setChanged();
		sendData();
	}

	public Optional<ResonatorBlockEntity> getResonator() {
		if (level == null)
			return Optional.empty();
		BlockEntity above = level.getBlockEntity(worldPosition.above());
		return above instanceof ResonatorBlockEntity r ? Optional.of(r) : Optional.empty();
	}

	public Direction getFacing() {
		return getBlockState().getValue(HorizontalDirectionalBlock.FACING);
	}

	@Nullable
	public FrequencyBand getBand() {
		return crystal.band();
	}

	/** The band actually being emitted: the crystal's band, only while the resonator above matches it. */
	@Nullable
	public FrequencyBand getEmittedBand() {
		FrequencyBand band = getBand();
		if (band == null)
			return null;
		float speed = VibrationSource.speedAbove(level, worldPosition);
		return band == FrequencyBand.of(speed) ? band : null;
	}

	public boolean isLinked() {
		return linkDistance > 0;
	}

	public int getLinkDistance() {
		return linkDistance;
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null)
			return;
		if (level.isClientSide) {
			if (isLinked() && level.getGameTime() % 4 == 0)
				spawnBeamParticle();
			return;
		}

		int distance = 0;
		FrequencyBand band = getEmittedBand();
		if (band != null) {
			Direction facing = getFacing();
			BlockPos.MutableBlockPos cursor = worldPosition.mutable();
			int range = rangeFor(band);
			for (int i = 1; i <= range; i++) {
				cursor.move(facing);
				if (!level.isLoaded(cursor))
					break;
				BlockState state = level.getBlockState(cursor);
				if (state.isAir() || state.getCollisionShape(level, cursor).isEmpty())
					continue;
				if (level.getBlockEntity(cursor) instanceof ResonanceReceiverBlockEntity receiver
					&& receiver.accept(band, facing)) {
					distance = i;
				}
				break;
			}
		}
		if (distance != linkDistance) {
			linkDistance = distance;
			sendData();
		}
	}

	private void spawnBeamParticle() {
		Direction facing = getFacing();
		double along = level.random.nextDouble() * linkDistance;
		Vec3 start = Vec3.atCenterOf(worldPosition).add(0, 0.2, 0);
		Vec3 at = start.add(facing.getStepX() * along, 0, facing.getStepZ() * along);
		level.addParticle(ParticleTypes.END_ROD, at.x, at.y, at.z, facing.getStepX() * 0.02, 0, facing.getStepZ() * 0.02);
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		crystal.write(tag, registries);
		tag.putInt("Link", linkDistance);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		crystal.read(tag, registries);
		linkDistance = tag.getInt("Link");
	}

	@Override
	public void destroy() {
		super.destroy();
		if (level != null && !crystal.getStackInSlot(0).isEmpty())
			net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
				crystal.getStackInSlot(0));
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		FrequencyBand band = getBand();
		LangBuilder value = band == null
			? new LangBuilder(CreateOscillation.MOD_ID).translate("gui.goggles.coupler.no_crystal").style(ChatFormatting.DARK_GRAY)
			: new LangBuilder(CreateOscillation.MOD_ID).translate("frequency." + band.getSerializedName()).style(ChatFormatting.AQUA);
		new LangBuilder(CreateOscillation.MOD_ID).translate("gui.goggles.tuning_fork").style(ChatFormatting.GRAY).space().add(value).forGoggles(tooltip);
		if (band != null)
			new LangBuilder(CreateOscillation.MOD_ID).translate("gui.goggles.coupler.range", rangeFor(band)).style(ChatFormatting.GRAY).forGoggles(tooltip);
		String key = isLinked() ? "gui.goggles.coupler.linked" : getEmittedBand() != null ? "gui.goggles.coupler.searching" : "gui.goggles.coupler.idle";
		new LangBuilder(CreateOscillation.MOD_ID).translate(key, linkDistance)
			.style(isLinked() ? ChatFormatting.GREEN : ChatFormatting.GRAY).forGoggles(tooltip);
		return true;
	}
}
