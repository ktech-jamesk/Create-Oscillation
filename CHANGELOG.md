# Changelog

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
