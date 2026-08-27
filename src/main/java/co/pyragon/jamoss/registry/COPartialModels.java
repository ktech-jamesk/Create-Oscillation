package co.pyragon.jamoss.registry;

import co.pyragon.jamoss.CreateOscillation;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

/** Client-only partial models (rotating parts rendered by block entity renderers / Flywheel visuals). */
public class COPartialModels {

	public static final PartialModel RESONATOR_ROTOR = block("resonator/rotor");
	public static final PartialModel RESONATOR_FORK = block("resonator/fork");
	public static final PartialModel RESONANCE_PUMP_ROTOR = block("resonance_pump/rotor");
	public static final PartialModel SIEVE_MESH = block("vibrating_sieve/mesh");
	public static final PartialModel SIEVE_CAGE = block("vibrating_sieve/cage");
	public static final PartialModel CHAMBER_BODY = block("resonance_chamber/body");
	public static final PartialModel CHAMBER_BODY_DIRECTIONAL = block("resonance_chamber/body_directional");
	public static final PartialModel CAVITATION_BODY = block("cavitation_chamber/body");
	public static final PartialModel PULVERISER_FORK = block("sonic_pulveriser/fork");
	public static final PartialModel CAVITATION_BODY_DIRECTIONAL = block("cavitation_chamber/body_directional");

	private static PartialModel block(String path) {
		return PartialModel.of(CreateOscillation.asResource("block/" + path));
	}

	/** Forces class-load so the partials are registered before model baking. */
	public static void init() {}
}
