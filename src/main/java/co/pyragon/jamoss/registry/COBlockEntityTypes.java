package co.pyragon.jamoss.registry;

import co.pyragon.jamoss.registry.COPartialModels;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.chamber.ResonanceChamberBlockEntity;
import co.pyragon.jamoss.content.chamber.ResonanceChamberRenderer;
import co.pyragon.jamoss.content.condenser.CondenserBlockEntity;
import co.pyragon.jamoss.content.condenser.CondenserRenderer;
import co.pyragon.jamoss.content.pump.ResonancePumpBlockEntity;
import co.pyragon.jamoss.content.pump.ResonancePumpRenderer;
import co.pyragon.jamoss.content.resonator.ResonatorBlockEntity;
import co.pyragon.jamoss.content.resonator.ResonatorRenderer;
import co.pyragon.jamoss.content.sieve.VibratingSieveBlockEntity;
import co.pyragon.jamoss.content.sieve.VibratingSieveRenderer;
import co.pyragon.jamoss.content.vent.VentBlockEntity;
import co.pyragon.jamoss.content.tuningfork.TuningForkBlockEntity;
import co.pyragon.jamoss.content.tuningfork.TuningForkRenderer;
import co.pyragon.jamoss.content.chamber.CavitationChamberBlockEntity;
import co.pyragon.jamoss.content.coupler.ResonanceEmitterBlockEntity;
import co.pyragon.jamoss.content.coupler.ResonanceReceiverBlockEntity;
import co.pyragon.jamoss.content.coupler.ResonanceReceiverRenderer;
import co.pyragon.jamoss.content.pulveriser.SonicPulveriserBlockEntity;
import co.pyragon.jamoss.content.pulveriser.SonicPulveriserRenderer;
import co.pyragon.jamoss.content.chamber.CavitationChamberRenderer;

public class COBlockEntityTypes {

	public static final BlockEntityEntry<ResonanceChamberBlockEntity> RESONANCE_CHAMBER = CreateOscillation.REGISTRATE
		.blockEntity("resonance_chamber", ResonanceChamberBlockEntity::new)
		.validBlocks(COBlocks.RESONANCE_CHAMBER)
		.renderer(() -> ResonanceChamberRenderer::new)
		.register();

	public static final BlockEntityEntry<ResonatorBlockEntity> RESONATOR = CreateOscillation.REGISTRATE
		.blockEntity("resonator", ResonatorBlockEntity::new)
		.visual(() -> SingleAxisRotatingVisual.of(COPartialModels.RESONATOR_ROTOR), true) // BER still draws the shaking fork
		.validBlocks(COBlocks.RESONATOR)
		.renderer(() -> ResonatorRenderer::new)
		.register();

	public static final BlockEntityEntry<ResonancePumpBlockEntity> RESONANCE_PUMP = CreateOscillation.REGISTRATE
		.blockEntity("resonance_pump", ResonancePumpBlockEntity::new)
		.visual(() -> SingleAxisRotatingVisual.ofZ(COPartialModels.RESONANCE_PUMP_ROTOR))
		.validBlocks(COBlocks.RESONANCE_PUMP)
		.renderer(() -> ResonancePumpRenderer::new)
		.register();

	public static final BlockEntityEntry<CondenserBlockEntity> CONDENSER = CreateOscillation.REGISTRATE
		.blockEntity("condenser", CondenserBlockEntity::new)
		.validBlocks(COBlocks.CONDENSER)
		.renderer(() -> CondenserRenderer::new)
		.register();

	public static final BlockEntityEntry<VibratingSieveBlockEntity> VIBRATING_SIEVE = CreateOscillation.REGISTRATE
		.blockEntity("vibrating_sieve", VibratingSieveBlockEntity::new)
		.validBlocks(COBlocks.VIBRATING_SIEVE)
		.renderer(() -> VibratingSieveRenderer::new)
		.register();

	public static final BlockEntityEntry<VentBlockEntity> VENT = CreateOscillation.REGISTRATE
		.blockEntity("vent", VentBlockEntity::new)
		.validBlocks(COBlocks.VENT)
		.register();

	public static final BlockEntityEntry<TuningForkBlockEntity> TUNING_FORK = CreateOscillation.REGISTRATE
		.blockEntity("tuning_fork", TuningForkBlockEntity::new)
		.validBlocks(COBlocks.TUNING_FORK)
		.renderer(() -> TuningForkRenderer::new)
		.register();

	public static final BlockEntityEntry<CavitationChamberBlockEntity> CAVITATION_CHAMBER = CreateOscillation.REGISTRATE
		.blockEntity("cavitation_chamber", CavitationChamberBlockEntity::new)
		.validBlocks(COBlocks.CAVITATION_CHAMBER)
		.renderer(() -> CavitationChamberRenderer::new)
		.register();

	public static final BlockEntityEntry<ResonanceEmitterBlockEntity> RESONANCE_EMITTER = CreateOscillation.REGISTRATE
		.blockEntity("resonance_emitter", ResonanceEmitterBlockEntity::new)
		.validBlocks(COBlocks.RESONANCE_EMITTER)
		.register();

	public static final BlockEntityEntry<ResonanceReceiverBlockEntity> RESONANCE_RECEIVER = CreateOscillation.REGISTRATE
		.blockEntity("resonance_receiver", ResonanceReceiverBlockEntity::new)
		.validBlocks(COBlocks.RESONANCE_RECEIVER)
		.renderer(() -> ResonanceReceiverRenderer::new)
		.register();

	public static final BlockEntityEntry<SonicPulveriserBlockEntity> SONIC_PULVERISER = CreateOscillation.REGISTRATE
		.blockEntity("sonic_pulveriser", SonicPulveriserBlockEntity::new)
		.validBlocks(COBlocks.SONIC_PULVERISER)
		.renderer(() -> SonicPulveriserRenderer::new)
		.register();

	public static void register() {}
}
