package co.pyragon.jamoss.compat.ponder.scenes;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import co.pyragon.jamoss.content.chamber.ResonanceChamberBlockEntity;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.content.tuningfork.TuningForkBlockEntity;
import co.pyragon.jamoss.content.amplifier.ResonanceAmplifierBlockEntity;
import co.pyragon.jamoss.content.coupler.ResonanceEmitterBlockEntity;
import co.pyragon.jamoss.content.coupler.ResonanceReceiverBlockEntity;
import co.pyragon.jamoss.content.pulveriser.SonicPulveriserBlockEntity;
import net.minecraft.world.level.block.Blocks;
import co.pyragon.jamoss.registry.COItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class ResonatorScenes {

	/** Structure "resonator": chamber (2,1,2), resonator (2,2,2), shaft (2,3,2). */
	public static void resonator(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("resonator", "Turning rotation into vibration");
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		BlockPos chamber = util.grid().at(2, 1, 2);
		BlockPos resonator = util.grid().at(2, 2, 2);
		BlockPos shaft = util.grid().at(2, 3, 2);
		Selection kinetics = util.select().fromTo(2, 2, 2, 2, 3, 2);

		scene.world().showSection(util.select().position(resonator), Direction.DOWN);
		scene.idle(10);
		scene.overlay().showText(70)
			.attachKeyFrame()
			.text("The Resonator is a source of vibration. It does nothing on its own.")
			.pointAt(util.vector().blockSurface(resonator, Direction.WEST))
			.placeNearTarget();
		scene.idle(80);

		scene.world().showSection(util.select().position(shaft), Direction.DOWN);
		scene.idle(5);
		scene.world().setKineticSpeed(kinetics, 32);
		scene.effects().rotationSpeedIndicator(shaft);
		scene.idle(10);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("It takes rotation from a shaft on top and needs at least 32 RPM")
			.pointAt(util.vector().blockSurface(shaft, Direction.WEST))
			.placeNearTarget();
		scene.idle(90);

		scene.world().showSection(util.select().position(chamber), Direction.UP);
		scene.idle(10);
		scene.overlay().showOutline(PonderPalette.GREEN, new Object(), util.select().position(chamber), 70);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("Whatever sits directly below is shaken: a Resonance Chamber or a Vibrating Sieve")
			.pointAt(util.vector().blockSurface(chamber, Direction.WEST))
			.placeNearTarget();
		scene.idle(90);

		scene.world().modifyBlockEntity(chamber, ResonanceChamberBlockEntity.class,
			be -> be.inputTank.getCapability().fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE));
		scene.idle(20);
		scene.overlay().showText(80)
			.text("While the block below is working, the tuning fork shivers")
			.pointAt(util.vector().blockSurface(resonator, Direction.NORTH))
			.placeNearTarget();
		scene.idle(90);
		scene.markAsFinished();
	}

	/** Structure "chamber": resonator rig plus a resonance pump (3,1,2) facing east and a tank (4,1,2). */
	public static void chamber(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("resonance_chamber", "Processing in the Resonance Chamber");
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		BlockPos chamber = util.grid().at(2, 1, 2);
		BlockPos resonator = util.grid().at(2, 2, 2);
		BlockPos pump = util.grid().at(3, 1, 2);
		BlockPos tank = util.grid().at(4, 1, 2);
		Selection kinetics = util.select().fromTo(2, 2, 2, 2, 3, 2);

		scene.world().showSection(util.select().position(chamber), Direction.DOWN);
		scene.idle(10);
		scene.overlay().showText(70)
			.attachKeyFrame()
			.text("The Resonance Chamber holds items and fluids, like a Basin")
			.pointAt(util.vector().blockSurface(chamber, Direction.WEST))
			.placeNearTarget();
		scene.idle(80);

		scene.world().modifyBlockEntity(chamber, ResonanceChamberBlockEntity.class,
			be -> be.inputTank.getCapability().fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE));
		scene.overlay().showControls(util.vector().topOf(chamber), Pointing.DOWN, 30).withItem(new ItemStack(Items.WATER_BUCKET));
		scene.idle(40);

		scene.world().showSection(kinetics, Direction.DOWN);
		scene.idle(10);
		scene.world().setKineticSpeed(kinetics, 32);
		scene.idle(10);
		scene.overlay().showText(90)
			.attachKeyFrame()
			.text("With a Resonator spinning on top, Resonating recipes run: here water becomes Sonic Mist in the Low band")
			.pointAt(util.vector().blockSurface(resonator, Direction.WEST))
			.placeNearTarget();
		scene.idle(100);

		scene.world().showSection(util.select().fromTo(3, 1, 2, 4, 1, 2), Direction.WEST);
		scene.idle(10);
		scene.world().setKineticSpeed(util.select().position(pump), 64);
		scene.world().propagatePipeChange(pump);
		scene.idle(10);
		scene.overlay().showText(90)
			.attachKeyFrame()
			.colored(PonderPalette.OUTPUT)
			.text("Gases are fluids: pull them out with a Resonance Pump into tanks and pipes")
			.pointAt(util.vector().blockSurface(tank, Direction.NORTH))
			.placeNearTarget();
		scene.idle(100);

		scene.overlay().showText(70)
			.text("Right-click any side with an item to insert it; wear Goggles to see the frequency")
			.pointAt(util.vector().blockSurface(chamber, Direction.NORTH))
			.placeNearTarget();
		scene.idle(80);
		scene.markAsFinished();
	}

	/** Structure "resonator": same rig, demonstrating frequency bands with quartz. */
	public static void frequency(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("frequency_bands", "Frequency bands");
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		BlockPos chamber = util.grid().at(2, 1, 2);
		BlockPos resonator = util.grid().at(2, 2, 2);
		Selection kinetics = util.select().fromTo(2, 2, 2, 2, 3, 2);

		scene.world().showSection(util.select().fromTo(2, 1, 2, 2, 3, 2), Direction.DOWN);
		scene.idle(10);
		scene.world().setKineticSpeed(kinetics, 32);
		scene.idle(10);

		scene.overlay().showText(90)
			.attachKeyFrame()
			.text("Speed sets the frequency: Low from 32 RPM, Mid from 64, High from 128, Ultrasonic at 256")
			.pointAt(util.vector().blockSurface(resonator, Direction.WEST))
			.placeNearTarget();
		scene.idle(100);

		scene.world().modifyBlockEntity(chamber, ResonanceChamberBlockEntity.class, be -> {
			be.inputTank.getCapability().fill(new FluidStack(Fluids.WATER, 250), FluidAction.EXECUTE);
			ItemHandlerHelper.insertItem(be.inputInventory, new ItemStack(Items.QUARTZ), false);
		});
		scene.overlay().showControls(util.vector().topOf(chamber), Pointing.DOWN, 30).withItem(new ItemStack(Items.QUARTZ));
		scene.idle(40);

		scene.overlay().showText(90)
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.text("Quartz Vapour needs the Mid band. In the Low band the quartz is left untouched")
			.pointAt(util.vector().blockSurface(chamber, Direction.WEST))
			.placeNearTarget();
		scene.idle(100);

		scene.world().setKineticSpeed(kinetics, 64);
		scene.effects().rotationSpeedIndicator(util.grid().at(2, 3, 2));
		scene.idle(10);
		scene.overlay().showText(90)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("Gear up to 64 RPM and the recipe runs. Sonic Mist only forms in the Low band, so faster chambers keep their water")
			.pointAt(util.vector().blockSurface(resonator, Direction.WEST))
			.placeNearTarget();
		scene.idle(100);

		scene.overlay().showText(70)
			.text("Goggles show the current band on the Resonator and the Chamber; JEI shows what each recipe needs")
			.pointAt(util.vector().blockSurface(resonator, Direction.NORTH))
			.placeNearTarget();
		scene.idle(80);
		scene.markAsFinished();
	}

	/** Structure "tuning_fork": chamber (2,1,2), resonator (2,2,2), tuning fork (2,3,2), shaft (2,4,2). */
	public static void tuningFork(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("tuning_fork", "Holding a frequency with the Tuning Fork");
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		BlockPos chamber = util.grid().at(2, 1, 2);
		BlockPos resonator = util.grid().at(2, 2, 2);
		BlockPos fork = util.grid().at(2, 3, 2);
		BlockPos shaft = util.grid().at(2, 4, 2);
		Selection stack = util.select().fromTo(2, 2, 2, 2, 4, 2);

		scene.world().showSection(util.select().fromTo(2, 1, 2, 2, 2, 2), Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(util.select().position(shaft), Direction.DOWN);
		scene.world().showSection(util.select().position(fork), Direction.DOWN);
		scene.idle(10);
		scene.world().setKineticSpeed(stack, 128);
		scene.effects().rotationSpeedIndicator(shaft);
		scene.idle(10);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("A fast shaft puts the Resonator in the High band, so Mid recipes will not run")
			.pointAt(util.vector().blockSurface(resonator, Direction.WEST))
			.placeNearTarget();
		scene.idle(90);

		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("The Tuning Fork sits between the shaft and the Resonator")
			.pointAt(util.vector().blockSurface(fork, Direction.WEST))
			.placeNearTarget();
		scene.idle(90);

		scene.overlay().showCenteredScrollInput(fork, Direction.WEST, 60);
		scene.overlay().showText(70)
			.text("Scroll on it to choose a frequency")
			.pointAt(util.vector().blockSurface(fork, Direction.WEST))
			.placeNearTarget();
		scene.idle(80);

		scene.world().modifyBlockEntity(fork, TuningForkBlockEntity.class, be -> be.band.setValue(FrequencyBand.MID.ordinal()));
		scene.world().setKineticSpeed(util.select().position(fork), 128);
		scene.world().setKineticSpeed(util.select().position(resonator), 64);
		scene.effects().rotationSpeedIndicator(resonator);
		scene.idle(10);
		scene.overlay().showText(90)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("Rotation passed downward is slowed to exactly that band: 64 RPM for Mid")
			.pointAt(util.vector().blockSurface(resonator, Direction.WEST))
			.placeNearTarget();
		scene.idle(100);

		scene.world().modifyBlockEntity(chamber, ResonanceChamberBlockEntity.class, be -> {
			be.inputTank.getCapability().fill(new FluidStack(Fluids.WATER, 250), FluidAction.EXECUTE);
			ItemHandlerHelper.insertItem(be.inputInventory, COItems.TUNED_CRYSTAL_LOW.asStack(), false);
			ItemHandlerHelper.insertItem(be.inputInventory, new ItemStack(com.simibubi.create.AllItems.ROSE_QUARTZ.get()), false);
		});
		scene.overlay().showControls(util.vector().topOf(chamber), Pointing.DOWN, 30).withItem(COItems.TUNED_CRYSTAL_LOW.asStack());
		scene.idle(40);
		scene.overlay().showText(90)
			.attachKeyFrame()
			.text("A Low-Tuned Crystal and Rose Quartz resonated here grow into a Mid-Tuned Crystal; each tier is grown from the last")
			.pointAt(util.vector().blockSurface(chamber, Direction.WEST))
			.placeNearTarget();
		scene.idle(100);

		scene.overlay().showText(70)
			.text("Slower input than the chosen band passes through unchanged: the fork cannot speed things up")
			.pointAt(util.vector().blockSurface(fork, Direction.NORTH))
			.placeNearTarget();
		scene.idle(80);
		scene.markAsFinished();
	}

	/** Structure "coupler": emitter (1,1,2) east under a resonator; receiver (6,1,2) east; shaft (7,1,2); cog (8,1,2). */
	public static void coupler(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("resonance_coupler", "Sending rotation through the air");
		scene.configureBasePlate(0, 0, 9);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		BlockPos emitter = util.grid().at(1, 1, 4);
		BlockPos resonator = util.grid().at(1, 2, 4);
		BlockPos receiver = util.grid().at(6, 1, 4);
		BlockPos stone = util.grid().at(4, 1, 4);
		Selection drive = util.select().fromTo(1, 2, 4, 1, 3, 4);
		Selection output = util.select().fromTo(6, 1, 4, 8, 1, 4);

		scene.world().showSection(util.select().fromTo(1, 1, 4, 1, 3, 4), Direction.DOWN);
		scene.idle(10);
		scene.world().setKineticSpeed(drive, 64);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("A Resonance Emitter under a Resonator throws its vibration forward through open air")
			.pointAt(util.vector().blockSurface(emitter, Direction.NORTH))
			.placeNearTarget();
		scene.idle(90);

		scene.world().modifyBlockEntity(emitter, ResonanceEmitterBlockEntity.class,
			be -> be.crystal.setStackInSlot(0, COItems.TUNED_CRYSTAL_MID.asStack()));
		scene.overlay().showControls(util.vector().blockSurface(emitter, Direction.NORTH), Pointing.DOWN, 30)
			.withItem(COItems.TUNED_CRYSTAL_MID.asStack());
		scene.idle(20);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("It needs a Tuned Crystal, and only emits while the Resonator runs in that crystal's band")
			.pointAt(util.vector().blockSurface(resonator, Direction.NORTH))
			.placeNearTarget();
		scene.idle(90);

		scene.world().showSection(output, Direction.DOWN);
		scene.idle(10);
		scene.world().modifyBlockEntity(receiver, ResonanceReceiverBlockEntity.class,
			be -> be.crystal.setStackInSlot(0, COItems.TUNED_CRYSTAL_MID.asStack()));
		scene.overlay().showControls(util.vector().blockSurface(receiver, Direction.NORTH), Pointing.DOWN, 30)
			.withItem(COItems.TUNED_CRYSTAL_MID.asStack());
		scene.idle(20);
		scene.world().setKineticSpeed(output, 64);
		scene.effects().rotationSpeedIndicator(util.grid().at(7, 1, 4));
		scene.overlay().showText(90)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("A Resonance Receiver with a matching crystal turns the beam back into rotation at that band's speed")
			.pointAt(util.vector().blockSurface(receiver, Direction.NORTH))
			.placeNearTarget();
		scene.idle(100);

		scene.world().showSection(util.select().position(stone), Direction.DOWN);
		scene.world().setKineticSpeed(output, 0);
		scene.idle(10);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.text("Anything solid in the way breaks the link; range grows with the crystal: 8, 16, 32 or 64 blocks")
			.pointAt(util.vector().blockSurface(stone, Direction.NORTH))
			.placeNearTarget();
		scene.idle(90);

		scene.world().destroyBlock(stone);
		scene.world().setKineticSpeed(output, 64);
		scene.idle(10);

		// second link, High crystals, running south along x=3 and crossing the first beam at (3,1,4)
		BlockPos emitter2 = util.grid().at(3, 1, 1);
		BlockPos receiver2 = util.grid().at(3, 1, 7);
		Selection drive2 = util.select().fromTo(3, 2, 1, 3, 3, 1);
		Selection output2 = util.select().fromTo(3, 1, 7, 3, 1, 8);
		scene.world().showSection(util.select().fromTo(3, 1, 1, 3, 3, 1), Direction.DOWN);
		scene.world().showSection(output2, Direction.DOWN);
		scene.idle(10);
		scene.world().modifyBlockEntity(emitter2, ResonanceEmitterBlockEntity.class,
			be -> be.crystal.setStackInSlot(0, COItems.TUNED_CRYSTAL_HIGH.asStack()));
		scene.world().modifyBlockEntity(receiver2, ResonanceReceiverBlockEntity.class,
			be -> be.crystal.setStackInSlot(0, COItems.TUNED_CRYSTAL_HIGH.asStack()));
		scene.overlay().showControls(util.vector().blockSurface(emitter2, Direction.WEST), Pointing.DOWN, 30)
			.withItem(COItems.TUNED_CRYSTAL_HIGH.asStack());
		scene.idle(10);
		scene.world().setKineticSpeed(drive2, 128);
		scene.world().setKineticSpeed(output2, 128);
		scene.effects().rotationSpeedIndicator(util.grid().at(3, 1, 8));
		scene.idle(10);
		scene.overlay().showText(90)
			.attachKeyFrame()
			.text("Crystals of different bands ignore each other, so several links can cross the same room")
			.pointAt(util.vector().centerOf(util.grid().at(3, 1, 4)))
			.placeNearTarget();
		scene.idle(100);
		scene.markAsFinished();
	}

	public static void amplifier(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("amplifier", "Raising a frequency with the Resonance Amplifier");
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		BlockPos chamber = util.grid().at(2, 1, 2);
		BlockPos amplifier = util.grid().at(2, 2, 2);
		BlockPos resonator = util.grid().at(2, 3, 2);
		BlockPos shaft = util.grid().at(2, 4, 2);
		Selection kinetic = util.select().fromTo(2, 3, 2, 2, 4, 2);

		scene.world().showSection(util.select().position(chamber), Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(util.select().position(amplifier), Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(kinetic, Direction.DOWN);
		scene.idle(10);
		scene.world().setKineticSpeed(kinetic, 16);
		scene.effects().rotationSpeedIndicator(shaft);
		scene.idle(10);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("At 16 RPM the Resonator is below the Low band and nothing beneath it would run")
			.pointAt(util.vector().blockSurface(resonator, Direction.WEST))
			.placeNearTarget();
		scene.idle(90);

		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("The Resonance Amplifier sits between the Resonator and the machine and holds Tuned Crystals")
			.pointAt(util.vector().blockSurface(amplifier, Direction.WEST))
			.placeNearTarget();
		scene.idle(90);

		scene.overlay().showControls(util.vector().blockSurface(amplifier, Direction.WEST), Pointing.LEFT, 30).rightClick().withItem(COItems.TUNED_CRYSTAL_LOW.asStack());
		scene.world().modifyBlockEntity(amplifier, ResonanceAmplifierBlockEntity.class, be -> be.crystals.setStackInSlot(0, COItems.TUNED_CRYSTAL_LOW.asStack()));
		scene.idle(10);
		scene.overlay().showText(80)
			.colored(PonderPalette.GREEN)
			.text("With a Low-Tuned Crystal inside, any spin at all is passed down as the Low band")
			.pointAt(util.vector().blockSurface(chamber, Direction.WEST))
			.placeNearTarget();
		scene.idle(90);

		scene.overlay().showControls(util.vector().blockSurface(amplifier, Direction.WEST), Pointing.LEFT, 30).rightClick().withItem(COItems.TUNED_CRYSTAL_MID.asStack());
		scene.world().modifyBlockEntity(amplifier, ResonanceAmplifierBlockEntity.class, be -> be.crystals.setStackInSlot(1, COItems.TUNED_CRYSTAL_MID.asStack()));
		scene.idle(10);
		scene.overlay().showText(90)
			.attachKeyFrame()
			.text("Each higher band needs every crystal below it as well: Low and Mid together reach Mid")
			.pointAt(util.vector().blockSurface(amplifier, Direction.WEST))
			.placeNearTarget();
		scene.idle(100);

		scene.world().setKineticSpeed(kinetic, 128);
		scene.effects().rotationSpeedIndicator(shaft);
		scene.idle(10);
		scene.overlay().showText(90)
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.text("If the Resonator's own band is higher than the crystals can reach, the Amplifier overloads and stops")
			.pointAt(util.vector().blockSurface(amplifier, Direction.WEST))
			.placeNearTarget();
		scene.idle(100);
	}

	public static void pulveriser(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("sonic_pulveriser", "Breaking blocks with the Sonic Pulveriser");
		scene.configureBasePlate(0, 0, 7);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		BlockPos pulveriser = util.grid().at(1, 2, 2);
		Selection wall = util.select().fromTo(3, 1, 1, 4, 3, 3);
		Selection machine = util.select().fromTo(1, 2, 2, 1, 4, 2);
		Selection drive = util.select().fromTo(1, 3, 2, 1, 4, 2);

		scene.world().showSection(wall, Direction.DOWN);
		scene.world().showSection(machine, Direction.DOWN);
		scene.idle(10);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("The Sonic Pulveriser fires the vibration of a Resonator on top of it forward as a breaking beam")
			.pointAt(util.vector().blockSurface(pulveriser, Direction.NORTH))
			.placeNearTarget();
		scene.idle(90);

		scene.world().modifyBlockEntity(pulveriser, SonicPulveriserBlockEntity.class, be -> {
			be.crystals.setStackInSlot(0, COItems.TUNED_CRYSTAL_LOW.asStack());
			be.crystals.setStackInSlot(1, COItems.TUNED_CRYSTAL_MID.asStack());
		});
		scene.overlay().showControls(util.vector().blockSurface(pulveriser, Direction.NORTH), Pointing.DOWN, 30)
			.withItem(COItems.TUNED_CRYSTAL_MID.asStack());
		scene.idle(20);
		scene.overlay().showText(90)
			.attachKeyFrame()
			.text("Crystals seat like in the Amplifier and set the tier: Low breaks 1 block, Mid 3x3 two deep, High 5x5 three deep, Ultrasonic 7x7 four deep")
			.pointAt(util.vector().blockSurface(pulveriser, Direction.NORTH))
			.placeNearTarget();
		scene.idle(100);

		scene.world().setKineticSpeed(drive, 64);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("It only runs while the Resonator's frequency matches the crystals' band exactly")
			.pointAt(util.vector().blockSurface(util.grid().at(1, 3, 2), Direction.WEST))
			.placeNearTarget();
		scene.idle(90);

		for (int y = 1; y <= 3; y++)
			for (int z = 1; z <= 3; z++)
				scene.world().destroyBlock(util.grid().at(3, y, z));
		scene.idle(15);
		scene.overlay().showText(80)
			.colored(PonderPalette.GREEN)
			.text("The nearest layer of blocks cracks together and shatters together; harder blocks take longer")
			.pointAt(util.vector().blockSurface(util.grid().at(3, 2, 2), Direction.NORTH))
			.placeNearTarget();
		scene.idle(90);

		scene.overlay().showFilterSlotInput(util.vector().blockSurface(pulveriser, Direction.NORTH), Direction.NORTH, 60);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("A filter limits what it breaks: set it to cobblestone and the stone stays")
			.pointAt(util.vector().blockSurface(pulveriser, Direction.NORTH))
			.placeNearTarget();
		scene.idle(90);
		scene.world().destroyBlock(util.grid().at(4, 1, 1));
		scene.world().destroyBlock(util.grid().at(4, 3, 3));
		scene.idle(20);

		scene.overlay().showText(90)
			.attachKeyFrame()
			.text("On contraptions it keeps working while moving, as long as its Resonator rides along directly above it")
			.pointAt(util.vector().blockSurface(pulveriser, Direction.UP))
			.placeNearTarget();
		scene.idle(100);
		scene.markAsFinished();
	}
}
