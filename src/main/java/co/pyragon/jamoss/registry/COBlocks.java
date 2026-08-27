package co.pyragon.jamoss.registry;

import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.chamber.ResonanceChamberBlock;
import co.pyragon.jamoss.content.condenser.CondenserBlock;
import co.pyragon.jamoss.content.condenser.CondenserItem;
import co.pyragon.jamoss.content.condenser.CondenserModel;
import co.pyragon.jamoss.content.pump.ResonancePumpBlock;
import co.pyragon.jamoss.content.resonator.ResonatorBlock;
import co.pyragon.jamoss.content.sieve.VibratingSieveBlock;
import co.pyragon.jamoss.content.vent.VentBlock;
import co.pyragon.jamoss.content.tuningfork.TuningForkBlock;
import co.pyragon.jamoss.content.chamber.CavitationChamberBlock;
import co.pyragon.jamoss.content.coupler.ResonanceEmitterBlock;
import co.pyragon.jamoss.content.coupler.ResonanceReceiverBlock;
import co.pyragon.jamoss.content.pulveriser.SonicPulveriserBlock;
import co.pyragon.jamoss.content.pulveriser.PulveriserMovementBehaviour;
import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import co.pyragon.jamoss.content.resonator.ResonatorBlockItem;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class COBlocks {
	// Blockstates and item models are hand-written in resources, so Registrate's default model
	// generators are disabled for these entries.

	public static final BlockEntry<ResonanceChamberBlock> RESONANCE_CHAMBER = CreateOscillation.REGISTRATE
		.block("resonance_chamber", ResonanceChamberBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY)
			.sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		.blockstate(NonNullBiConsumer.noop())
		.addLayer(() -> RenderType::cutoutMipped)
		.lang("Resonance Chamber")
		.item()
		.model(NonNullBiConsumer.noop())
		.build()
		.register();

	public static final BlockEntry<ResonatorBlock> RESONATOR = CreateOscillation.REGISTRATE
		.block("resonator", ResonatorBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.STONE))
		.transform(axeOrPickaxe())
		.blockstate(NonNullBiConsumer.noop())
		.addLayer(() -> RenderType::cutoutMipped)
		.onRegister(block -> BlockStressValues.IMPACTS.register(block, () -> 4.0))
		.lang("Resonator")
		.item(ResonatorBlockItem::new)
		.model(NonNullBiConsumer.noop())
		.build()
		.register();

	public static final BlockEntry<ResonancePumpBlock> RESONANCE_PUMP = CreateOscillation.REGISTRATE
		.block("resonance_pump", ResonancePumpBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.mapColor(MapColor.STONE))
		.transform(pickaxeOnly())
		.blockstate(NonNullBiConsumer.noop())
		.onRegister(block -> BlockStressValues.IMPACTS.register(block, () -> 4.0))
		.lang("Resonance Pump")
		.item()
		.model(NonNullBiConsumer.noop())
		.build()
		.register();

	public static final BlockEntry<CondenserBlock> CONDENSER = CreateOscillation.REGISTRATE
		.block("condenser", CondenserBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.COLOR_BLUE)
			.isRedstoneConductor((s, l, pos) -> true))
		.transform(pickaxeOnly())
		.blockstate(NonNullBiConsumer.noop())
		.onRegister(CreateRegistrate.blockModel(() -> CondenserModel::new))
		.addLayer(() -> RenderType::cutoutMipped)
		.lang("Condenser")
		.item(CondenserItem::new)
		.model(NonNullBiConsumer.noop())
		.build()
		.register();

	public static final BlockEntry<VibratingSieveBlock> VIBRATING_SIEVE = CreateOscillation.REGISTRATE
		.block("vibrating_sieve", VibratingSieveBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.STONE))
		.transform(axeOrPickaxe())
		.blockstate(NonNullBiConsumer.noop())
		.addLayer(() -> RenderType::cutoutMipped)
		.lang("Vibrating Sieve")
		.item()
		.model(NonNullBiConsumer.noop())
		.build()
		.register();

	public static final BlockEntry<VentBlock> VENT = CreateOscillation.REGISTRATE
		.block("vent", VentBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.STONE))
		.transform(pickaxeOnly())
		.blockstate(NonNullBiConsumer.noop())
		.addLayer(() -> RenderType::cutoutMipped)
		.lang("Vent")
		.item()
		.model(NonNullBiConsumer.noop())
		.build()
		.register();

	public static final BlockEntry<TuningForkBlock> TUNING_FORK = CreateOscillation.REGISTRATE
		.block("tuning_fork", TuningForkBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.STONE))
		.transform(axeOrPickaxe())
		.blockstate(NonNullBiConsumer.noop())
		.addLayer(() -> RenderType::cutoutMipped)
		.onRegister(block -> BlockStressValues.IMPACTS.register(block, () -> 0.0))
		.lang("Tuning Fork")
		.item()
		.model(NonNullBiConsumer.noop())
		.build()
		.register();

	public static final BlockEntry<CavitationChamberBlock> CAVITATION_CHAMBER = CreateOscillation.REGISTRATE
		.block("cavitation_chamber", CavitationChamberBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY)
			.sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		.blockstate(NonNullBiConsumer.noop())
		.addLayer(() -> RenderType::cutoutMipped)
		.lang("Cavitation Chamber")
		.item()
		.model(NonNullBiConsumer.noop())
		.build()
		.register();

	public static final BlockEntry<ResonanceEmitterBlock> RESONANCE_EMITTER = CreateOscillation.REGISTRATE
		.block("resonance_emitter", ResonanceEmitterBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.TERRACOTTA_YELLOW))
		.transform(pickaxeOnly())
		.blockstate(NonNullBiConsumer.noop())
		.addLayer(() -> RenderType::cutoutMipped)
		.lang("Resonance Emitter")
		.item()
		.model(NonNullBiConsumer.noop())
		.build()
		.register();

	public static final BlockEntry<ResonanceReceiverBlock> RESONANCE_RECEIVER = CreateOscillation.REGISTRATE
		.block("resonance_receiver", ResonanceReceiverBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.TERRACOTTA_YELLOW))
		.transform(pickaxeOnly())
		.blockstate(NonNullBiConsumer.noop())
		.addLayer(() -> RenderType::cutoutMipped)
		.onRegister(block -> BlockStressValues.CAPACITIES.register(block, () -> 4.0)) // baseline; the block entity scales it by crystal tier
		.lang("Resonance Receiver")
		.item()
		.model(NonNullBiConsumer.noop())
		.build()
		.register();

	public static final BlockEntry<SonicPulveriserBlock> SONIC_PULVERISER = CreateOscillation.REGISTRATE
		.block("sonic_pulveriser", SonicPulveriserBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.STONE))
		.transform(pickaxeOnly())
		.blockstate(NonNullBiConsumer.noop())
		.addLayer(() -> RenderType::cutoutMipped)
		.onRegister(block -> BlockStressValues.IMPACTS.register(block, () -> 4.0))
		.onRegister(movementBehaviour(new PulveriserMovementBehaviour()))
		.lang("Sonic Pulveriser")
		.item()
		.model(NonNullBiConsumer.noop())
		.build()
		.register();

	public static void register() {}
}
