<div align="center">

<img src="https://raw.githubusercontent.com/ktech-jamesk/Create-Oscillation/refs/heads/main/media/ponder/logo.gif" alt="Create: Oscillation" width="720">

**Vibration as energy. Gases as a processing medium.**

An addon for [Create](https://github.com/Creators-of-Create/Create) that turns rotation into *frequency* — and frequency into a whole new tier of processing.

[![CurseForge](https://img.shields.io/badge/CurseForge-Create%3A%20Oscillation-f16436?style=flat-square&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/create-oscillation)
![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-5b8c3a?style=flat-square)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1%2B-e46a2e?style=flat-square)
![Create 6.0.10+](https://img.shields.io/badge/Create-6.0.10%2B-c8a24b?style=flat-square)
![License MIT](https://img.shields.io/badge/License-MIT-3f8fd6?style=flat-square)

</div>

---

## What it is

A **Resonator** turns any shaft into vibration, and its speed picks one of four frequency bands: Low, Mid, High, Ultrasonic. Recipes run only in the band they ask for, so the same chamber does different things at different speeds.

From there the mod adds:

- **Gases as Create fluids** — Sonic Mist (cold ultrasonic water mist), quartz vapour and metal vapours that flow through pipes, sit in tanks and get condensed back into liquids and solids.
- **Tuned crystals** — grown tier by tier, then used to tune, power and fuel the brass-age machines.
- **Wireless rotation** — a Resonance Emitter and Receiver pass a kinetic network across open air.
- **The Sonic Pulveriser** — a crystal-fuelled machine that shatters whole layers of blocks, on its own or on a contraption.
- **Ore multiplication** — a data-driven chain giving 2.5 ingots per raw ore; packs add metals with JSON only.

Every block has a Ponder scene (hover the item, press <kbd>W</kbd>).

**➜ [Read the full guide](GUIDE.md)** for how each machine works, the crystal and ore chains, and the pack-maker notes.

## Download

- **[CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-oscillation)**
- [GitHub Releases](https://github.com/ktech-jamesk/Create-Oscillation/releases)

Requires Create 6.0.10+ on NeoForge for Minecraft 1.21.1. JEI is optional but recommended. KubeJS is optional: it adds typed recipe builders and a one-line `Oscillation.addMetal(...)` helper for pack makers (see the [guide](GUIDE.md#adding-a-metal-pack-makers)).

## Building from source

```
./gradlew build              # jar into build/libs
./gradlew runClient          # dev client with Create, JEI, Jade and KubeJS
./gradlew runData            # datagen into src/generated/resources
./gradlew runGameTestServer  # headless gametests
```

Versions live in `gradle.properties`. Releases are cut by pushing a `vX.Y.Z` tag.

## License & credits

MIT — see [LICENSE](LICENSE). Some textures are recoloured from Create's and the Resonance Pump reuses Create's mechanical pump geometry; thank you to the Creators of Create.
