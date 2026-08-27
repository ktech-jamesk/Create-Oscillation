package co.pyragon.jamoss.content.tuningfork;

import java.util.List;

import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TuningForkBlockEntity extends KineticBlockEntity {

	public ScrollOptionBehaviour<FrequencyBand> band;

	public TuningForkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		band = new ScrollOptionBehaviour<>(FrequencyBand.class,
			Component.translatable("createoscillation.gui.tuning_fork.band"), this,
			new CenteredSideValueBoxTransform((state, dir) -> dir.getAxis().isHorizontal()));
		band.withCallback(i -> updateRotation());
		behaviours.add(band);
	}

	public FrequencyBand getBand() {
		return band.get();
	}

	/** The ratio applied to rotation handed to the block below, or 0 for a plain 1:1 shaft. */
	public static float ratioFor(FrequencyBand band, float inputSpeed) {
		if (band == FrequencyBand.ANY || inputSpeed == 0)
			return 0;
		float abs = Math.abs(inputSpeed);
		if (abs <= band.minSpeed)
			return 0;
		return band.minSpeed / abs;
	}

	@Override
	public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
		boolean connectedViaAxes, boolean connectedViaCogs) {
		if (diff.getY() == -1 && diff.getX() == 0 && diff.getZ() == 0 && connectedViaAxes)
			return ratioFor(getBand(), getTheoreticalSpeed());
		if (diff.getY() == 1 && diff.getX() == 0 && diff.getZ() == 0 && connectedViaAxes
			&& target instanceof TuningForkBlockEntity fork)
			return inverseRatioTo(fork, this);
		return 0;
	}

	/** Mirror of the fork's downward ratio, used by blocks below so both directions agree. */
	public static float inverseRatioTo(TuningForkBlockEntity fork, KineticBlockEntity below) {
		float ratio = ratioFor(fork.getBand(), fork.getTheoreticalSpeed());
		return ratio == 0 ? 0 : 1 / ratio;
	}

	private void updateRotation() {
		if (level == null || level.isClientSide)
			return;
		if (hasNetwork())
			getOrCreateNetwork().remove(this);
		RotationPropagator.handleRemoved(level, worldPosition, this);
		removeSource();
		attachKinetics();
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		FrequencyBand b = getBand();
		new LangBuilder(CreateOscillation.MOD_ID).translate("gui.goggles.tuning_fork")
			.style(ChatFormatting.GRAY)
			.space()
			.add(new LangBuilder(CreateOscillation.MOD_ID).translate("frequency." + b.getSerializedName())
				.style(b == FrequencyBand.ANY ? ChatFormatting.DARK_GRAY : ChatFormatting.AQUA))
			.forGoggles(tooltip);
		return true;
	}
}
