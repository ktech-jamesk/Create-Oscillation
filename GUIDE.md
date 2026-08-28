<div align="center">

<img src="https://raw.githubusercontent.com/ktech-jamesk/Create-Oscillation/refs/heads/main/media/ponder/logo.gif" alt="Create: Oscillation" width="720">

**Vibration as energy. Gases as a processing medium.**

</div>

Create gives you rotation. Oscillation turns that rotation into **frequency** — and frequency into a new tier of processing: gases you can pipe, tank and carry, crystals that hold a tuning, rotation sent through open air, and a machine that shatters whole walls at once.

Every block has a **Ponder** scene: hover the item and press <kbd>W</kbd>.

---

## Frequency

A **Resonator** takes a shaft from above and shakes whatever sits directly below it. How fast the shaft turns decides the *band* it produces:

| Band | RPM | Unlocks |
|:--|:--:|:--|
| **Low** | 32 | Sonic Mist, sifting, the first tuned crystal |
| **Mid** | 64 | Quartz Vapour, crystal growth |
| **High** | 128 | Ore slurry, the strongest crystals |
| **Ultrasonic** | 256 | Cavitation, ore multiplication, wireless power at scale |

Recipes name the band they need and only run in exactly that band. Water becomes mist at Low — and *stays water* at Mid or above, so a fast chamber keeps its inputs for the recipe you actually want. Goggles show the current band on every Resonator and chamber; JEI shows the band under each recipe.

**Tuning Fork** — a shaft relay you place between the drive shaft and a Resonator. Scroll it to a band and it hands rotation downward at *exactly* that band's speed. One fast shaft can feed a row of chambers, each at its own band.

## Gases

Gases are real Create fluids: they flow through pipes, sit in tanks, appear in JEI, and can be carried in a **Gas Canister** (1000 mb, tinted by what's inside). They render as drifting vapour rather than liquid, and in a chamber they gather under the glass dome while liquids pool below.

- **Resonance Chamber** — a glass-domed basin driven by a Resonator. Right-click any side with an item to insert; pipe liquids in from the sides. Runs *resonating* recipes: water → Sonic Mist (Low), quartz + water → Quartz Vapour (Mid), raw ore + water → ore slurry (High), crystal growth… Pipes can only *feed* a chamber's inputs and *draw* its outputs, so a pump never steals what the recipe needs.
- **Resonance Pump** — a Mechanical Pump that only moves gases. Liquids stay put.
- **Condenser** — passive, resizable up to 3×3 wide. Gas in, liquid and solids out: mist → water, Quartz Vapour → water + quartz (and sometimes a Rough Quartz Crystal), metal vapour → concentrate. The first conversion is slow while it cools down; after that it stays *cold* and keeps up as long as it's fed. Solids drop into the inventory below or are pulled from the sides.
- **Vent** — voids any gas piped into it with a plume; refuses liquids.
- **Vibrating Sieve** — a mesh cage that sits under a Resonator. Gravel, sand, red sand and soul sand shake down into flint, nuggets and more; results land in the inventory below.

## Crystals

Rough Quartz Crystals come from condensing Quartz Vapour. In a chamber they grow tier by tier, each step at its own band:

| Step | Ingredients | Band |
|:--|:--|:--:|
| Low-Tuned Crystal | Rough Quartz Crystal + Nether Quartz + water | Low |
| Mid-Tuned Crystal | Low-Tuned Crystal + Rose Quartz + water | Mid |
| High-Tuned Crystal | Mid-Tuned Crystal + Amethyst Shard + water | High |
| Ultrasonic Crystal | High-Tuned Crystal + Powdered Obsidian + water | Ultrasonic |

Crystals are ordinary stackable items. They tune the Resonance Emitter and Receiver, craft the Tuning Fork and the Cavitation Chamber, and fuel the Sonic Pulveriser.

## Sonic Pulveriser

<img src="https://raw.githubusercontent.com/ktech-jamesk/Create-Oscillation/refs/heads/main/media/ponder/pulveriser_mid.gif" alt="Sonic Pulveriser with a Mid-Tuned Crystal" width="640">

A Resonator turned sideways: shaft in the back, vibration out the front. Any rotation drives it, but it does nothing until it has a crystal — the crystal is both its **fuel** and its **tier**.

| Crystal | Breaks | Reach | Up to |
|:--|:--:|:--:|:--|
| Low | 1 block | 1 | stone, ores |
| Mid | 3×3 | 2 | metal blocks |
| High | 5×5 | 3 | obsidian |
| Ultrasonic | 7×7 | 4 | anything breakable |

<img src="https://raw.githubusercontent.com/ktech-jamesk/Create-Oscillation/refs/heads/main/media/ponder/pulveriser_ultrasonic.gif" alt="Sonic Pulveriser with an Ultrasonic Crystal" width="640">

The nearest layer of blocks in front cracks together and shatters together; harder blocks take longer and cost more fuel. It never breaks blocks with inventories or machinery, and a Create **filter** on the side limits what it takes. Each crystal burns down like furnace fuel (512 → 4096 charge by tier) and leaves a Rough Quartz Crystal in the output slot; hoppers and funnels can keep it fed. Sneak + wrench ejects the fuel. Mount it on a piston, gantry or bearing and it works while the contraption moves, dropping into attached storage.

## Resonance Emitter & Receiver

<img src="https://raw.githubusercontent.com/ktech-jamesk/Create-Oscillation/refs/heads/main/media/ponder/coupler.gif" alt="Resonance Emitter and Receiver" width="640">

Put matching crystals in both, aim them at each other through open air, and rotation crosses the gap.

- The **Emitter** sits under a Resonator and beams along its facing for up to 16 blocks while the Resonator runs in the crystal's band.
- The **Receiver** with the same crystal, dish toward the emitter, becomes a kinetic source turning its back-side shaft at that band's speed. Capacity scales with the crystal: Low 32, Mid 128, High 512, **Ultrasonic 2048 SU per RPM** — enough to run an entire build across the gap.
- Anything solid in the way breaks the link within a few ticks. Crystals of different bands ignore each other, so several links can cross the same room.

## Ore multiplication — 2.5 ingots per raw ore

<img src="https://raw.githubusercontent.com/ktech-jamesk/Create-Oscillation/refs/heads/main/media/ponder/ore_line.gif" alt="Ore multiplication line" width="640">

1. **Resonance Chamber, High band**: raw ore + 250 mb water → ore slurry.
2. **Cavitation Chamber, Ultrasonic**: slurry + 250 mb mist → metal vapour. The Cavitation Chamber is a reinforced chamber (Resonance Chamber + brass casing + High-Tuned Crystal) that only runs cavitating recipes.
3. **Condenser**: vapour → water + 2 concentrate, with a 50% chance of a third.
4. Smelt or blast the concentrate into ingots.

Iron, gold, copper and zinc are built in, matched by `c:raw_materials/*` tags so other mods' ores of those metals work too. Slurry, vapour and concentrate are single items that carry their metal with them — each metal gets its own JEI entries, name and colour.

### Adding a metal (pack makers)

**With KubeJS** (optional), one line in a server script adds the data-map entry and the whole recipe chain:

```js
ServerEvents.generateData('after_mods', event => {
  Oscillation.addMetal(event, 'tin', 'somemod:raw_tin', 'somemod:tin_ingot', '#C8D0D8')
})
```

Leave out the ingot to skip the smelting/blasting recipes. The recipes land as `kubejs:createoscillation/*` and can be removed or edited in `ServerEvents.recipes` like any other. For a display name add `ClientEvents.lang('en_us', e => e.add('createoscillation.metal.tin', 'Tin'))` (it falls back to "Tin" from the id anyway).

The four recipe types also get typed builders:

```js
ServerEvents.recipes(event => {
  event.recipes.createoscillation.resonating([Fluid.of('createoscillation:sonic_mist', 500)], [Fluid.water(500)])
    .frequency('mid').processingTime(60)
  event.recipes.createoscillation.cavitating([Fluid.of('createoscillation:quartz_vapour', 250)], [Fluid.of('createoscillation:sonic_mist', 250), 'minecraft:quartz'])
    .frequency('ultrasonic')
  event.recipes.createoscillation.condensing(['minecraft:ice', Fluid.water(250)], [Fluid.of('createoscillation:sonic_mist', 250)])
  event.recipes.createoscillation.sifting([Oscillation.output('minecraft:bone_meal', 0.25), { id: 'minecraft:stick', chance: 0.5 }], ['minecraft:dirt'])
  event.remove({ type: 'createoscillation:resonating', output: 'createoscillation:quartz_vapour' })
})
```

Outputs come first, inputs second (Create's convention); `frequency` is `low` / `mid` / `high` / `ultrasonic` (default any). Chance outputs are `Oscillation.output(item, chance)` or `{ id, count, chance }` objects.

**With plain JSON**, no code needed:

1. Map the raw ore in `data/<ns>/data_maps/item/createoscillation/metals.json`:
   ```json
   { "values": { "somemod:raw_tin": { "metal": "tin", "colour": "#C8D0D8" } } }
   ```
2. Copy the iron recipes from `data/createoscillation/recipe/` — `resonating/slurry_iron`, `cavitating/vapour_iron`, `condensing/concentrate_iron`, `crafting/smelting/ingot_iron` and `blasting/ingot_iron` — and replace `iron` with `tin`. Ingredients that must match a metal use `neoforge:components` with `createoscillation:metal`.
3. Optionally add lang `createoscillation.metal.tin` = `Tin`.

## Progression at a glance

**Andesite age** — Resonator, Resonance Chamber, Resonance Pump, Condenser, Vent, Gas Canister, Vibrating Sieve. Sonic Mist and Quartz Vapour.

**Brass age** — crystals, Tuning Fork, Emitter & Receiver, Cavitation Chamber and the ore line, Sonic Pulveriser.

## Compatibility

- **Create 6.0.10+** required (Registrate, Ponder and Flywheel come with it).
- **JEI** optional: Resonating, Cavitating, Condensing and Sifting categories with the required band shown.

Released under the MIT License. Some textures are recoloured from Create's, and the Resonance Pump reuses Create's mechanical pump geometry — thank you to the Creators of Create.
