# Changelog

## 2.0.0

- **Breaking: Sonic Pulveriser rework.** It is no longer kinetic and no longer burns crystals as fuel. Like the Chamber
  and Sieve it is driven by a **Resonator (or Amplifier) directly on top**, and like the Amplifier it holds a **crystal
  ladder**: right-click one crystal of each band in (empty hand takes the top one back; hoppers can feed it), and the
  highest gap-free band sets the tier (Low 1×1 · Mid 3×3 · High 5×5 · Ultrasonic 7×7). It only runs while the source
  above vibrates in **exactly** that band; crystals are seated, never consumed. Facing up is no longer possible — the top
  face belongs to the Resonator. On contraptions the Resonator must ride directly above it (glue them together); an
  Amplifier works too when its ladder matches. Existing Pulverisers lose their stored fuel crystals and charge on first
  load; break and re-place them, then seat a ladder. Drops now go into an inventory below the drop point (the block
  directly in front) when one is there — chest, hopper, depot — and only spill on the ground otherwise. A Pulveriser
  spun in place on a bearing works as an in-place radial quarry. Like Create's drill, a moving contraption now **stalls**
  while the Pulveriser cracks a layer and resumes once it shatters, so fast minecarts no longer skip past blocks.
- The Pulveriser's Ponder scene and goggle readouts (tuning, seated crystals, wrong-band warning) match the new
  mechanics.

## 1.2.0

- **Resonance Amplifier.** Sits between a Resonator and the machine below it and re-emits the vibration at the band its
  crystal ladder reaches: a Low-Tuned Crystal alone gives Low (even from a shaft slower than 32 RPM), Low + Mid gives Mid,
  and so on up to Ultrasonic with all four. A gap in the ladder caps the band below it. If the Resonator itself already
  runs in a higher band than the ladder reaches, the Amplifier overloads and passes nothing. Right-click with a crystal to
  seat it, empty hand to take the top one back; hoppers/funnels can feed it. Goggles, Ponder scene and recipe included.
- Internal: Chamber, Sieve and Emitter now read any `VibrationSource` above them (Resonator or Amplifier).
- **Resonance Coupler range now depends on the crystal:** 8 (Low), 16 (Mid), 32 (High), 64 (Ultrasonic) blocks. Goggles on
  the Emitter show the range; the beam scan stops at unloaded chunks.
- JEI: new *Resonance Coupling* page (per crystal: band the Resonator must run in, beam range); Condensing recipes laid
  out in one row with the Condenser above; Gas Canister drain entries now output an empty canister and Spout filling
  entries exist for every gas.
- Receiver model: the output half-shaft now protrudes from the back so the connection side is visible.
- Ponder: the coupler scene's blocking stone now actually appears.

## 1.1.0

- **Breaking:** the Steam fluid is now **Sonic Mist** (`createoscillation:sonic_mist`, was `createoscillation:steam`).
  Ultrasonic atomisation makes a cold mist, not boiled steam, and the old name clashed with Create's steam engines.
  Any Steam in tanks, canisters or chambers of existing worlds is lost; datapacks and scripts referencing the old id
  must be updated.
- Optional KubeJS integration. Typed recipe builders for `resonating`, `cavitating`, `condensing` and `sifting`
  (`event.recipes.createoscillation.resonating(outputs, inputs).frequency('high')`), and a one-line metal helper
  `Oscillation.addMetal(event, 'tin', 'somemod:raw_tin', 'somemod:tin_ingot', '#C8D0D8')` in
  `ServerEvents.generateData('after_mods', ...)` that adds the data-map entry and the whole ore chain. Nothing changes without KubeJS.
- Condenser wording: it now "cools down" rather than "warms up" (goggles, guide, ponder). Its running state is
  also synced to the client properly when it cools off.

## 1.0.2

- Now builds against and requires Create 6.0.10+ (was 6.0.11+, which is not yet released). Nothing else changed;
  the mod runs on 6.0.11 too.

## 1.0.1

- Pipes and pumps can no longer drain a Resonance/Cavitation Chamber's *input* fluids; only outputs are extractable.
  Previously a Resonance Pump next to a chamber would pull out steam that had just been fed in.
- Dev tooling: `./gradlew runShowcase` captures GIF footage from a shot list in the showcase world.

## 1.0.0

First release, for Minecraft 1.21.1 / NeoForge / Create 6.0.11+.

- Resonator, Resonance Chamber, Resonance Pump, Condenser, Vent, Vibrating Sieve, Gas Canister.
- Frequency bands (Low 32 / Mid 64 / High 128 / Ultrasonic 256 RPM) with band-gated recipes.
- Tuning Fork: a shaft relay that outputs exactly a band's speed.
- Tuned crystals grown tier by tier (nether quartz, rose quartz, amethyst, powdered obsidian).
- Resonance Emitter / Receiver: rotation sent through the air between matching crystals.
- Cavitation Chamber and a data-driven ore multiplication chain (iron, gold, copper, zinc built in; packs add metals with JSON).
- Sonic Pulveriser: crystal-fuelled layer breaker with a filter, usable on contraptions.
- JEI categories, Ponder scenes for every block, translucent gas rendering.
