package co.pyragon.jamoss.compat.ponder.scenes;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import co.pyragon.jamoss.registry.COFluids;
import co.pyragon.jamoss.content.chamber.ResonanceChamberBlockEntity;
import co.pyragon.jamoss.content.ore.MetalStacks;
import net.createmod.catnip.math.Pointing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class GasScenes {

	private static FluidStack mist(int amount) {
		return new FluidStack((Fluid) COFluids.SONIC_MIST.getSource(), amount);
	}

	private static FluidStack quartzVapour(int amount) {
		return new FluidStack((Fluid) COFluids.QUARTZ_VAPOUR.getSource(), amount);
	}

	private static void fillTank(CreateSceneBuilder scene, BlockPos pos, FluidStack stack) {
		scene.world().modifyBlockEntity(pos, FluidTankBlockEntity.class, be -> {
			be.getTankInventory().setFluid(FluidStack.EMPTY);
			be.getTankInventory().fill(stack, FluidAction.EXECUTE);
		});
	}

	/** Structure "pump": tank (0,1,2) - pipe - resonance pump (2,1,2) east - pipe - tank (4,1,2). */
	public static void pump(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("resonance_pump", "Moving gases with the Resonance Pump");
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		BlockPos source = util.grid().at(0, 1, 2);
		BlockPos pump = util.grid().at(2, 1, 2);
		BlockPos sink = util.grid().at(4, 1, 2);
		Selection row = util.select().fromTo(0, 1, 2, 4, 1, 2);

		fillTank(scene, source, mist(4000));
		scene.world().showSection(row, Direction.DOWN);
		scene.idle(10);
		scene.world().setKineticSpeed(util.select().position(pump), 64);
		scene.world().propagatePipeChange(pump);
		scene.effects().rotationDirectionIndicator(pump);
		scene.idle(10);

		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("The Resonance Pump works like a Mechanical Pump, but only for gases")
			.pointAt(util.vector().topOf(pump))
			.placeNearTarget();
		scene.idle(90);

		scene.overlay().showText(70)
			.colored(PonderPalette.OUTPUT)
			.text("Sonic Mist flows from the left tank to the right one")
			.pointAt(util.vector().blockSurface(sink, Direction.NORTH))
			.placeNearTarget();
		scene.idle(80);

		fillTank(scene, source, new FluidStack(Fluids.WATER, 4000));
		scene.idle(20);
		scene.overlay().showOutline(PonderPalette.RED, new Object(), util.select().position(source), 80);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.text("Liquids are refused: water stays where it is")
			.pointAt(util.vector().blockSurface(source, Direction.NORTH))
			.placeNearTarget();
		scene.idle(90);
		scene.markAsFinished();
	}

	/**
	 * Structure "condenser": tank (0,1,2) - resonance pump (1,1,2) - pipe (2,1,2) up to (2,2,2) -
	 * condenser (3,2,2) over a depot (3,1,2) - mechanical pump (4,2,2) - tank (5,2,2).
	 */
	public static void condenser(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("condenser", "Condensing gases back into liquids");
		scene.configureBasePlate(0, 0, 6);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		BlockPos source = util.grid().at(0, 1, 2);
		BlockPos inPump = util.grid().at(1, 1, 2);
		BlockPos condenser = util.grid().at(3, 2, 2);
		BlockPos depot = util.grid().at(3, 1, 2);
		BlockPos outPump = util.grid().at(4, 2, 2);
		BlockPos sink = util.grid().at(5, 2, 2);

		scene.world().showSection(util.select().position(condenser), Direction.DOWN);
		scene.idle(10);
		scene.overlay().showText(70)
			.attachKeyFrame()
			.text("The Condenser turns gases back into liquids and items. It needs no power")
			.pointAt(util.vector().blockSurface(condenser, Direction.WEST))
			.placeNearTarget();
		scene.idle(80);

		fillTank(scene, source, mist(4000));
		scene.world().showSection(util.select().fromTo(0, 1, 2, 2, 2, 2), Direction.EAST);
		scene.idle(10);
		scene.world().setKineticSpeed(util.select().position(inPump), 64);
		scene.world().propagatePipeChange(inPump);
		scene.idle(10);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("Pipe Sonic Mist in. The first conversion takes a while...")
			.pointAt(util.vector().blockSurface(condenser, Direction.NORTH))
			.placeNearTarget();
		scene.idle(90);

		scene.overlay().showText(80)
			.colored(PonderPalette.GREEN)
			.text("...but once the Condenser has cooled down it keeps converting quickly while it is fed")
			.pointAt(util.vector().blockSurface(condenser, Direction.NORTH))
			.placeNearTarget();
		scene.idle(90);

		scene.world().showSection(util.select().fromTo(4, 2, 2, 5, 2, 2), Direction.WEST);
		scene.world().showSection(util.select().position(5, 1, 2), Direction.WEST);
		scene.idle(10);
		scene.world().setKineticSpeed(util.select().position(outPump), 64);
		scene.world().propagatePipeChange(outPump);
		scene.idle(10);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.colored(PonderPalette.OUTPUT)
			.text("Condensed liquid is pulled out by any pump or pipe")
			.pointAt(util.vector().blockSurface(sink, Direction.NORTH))
			.placeNearTarget();
		scene.idle(90);

		fillTank(scene, source, quartzVapour(4000));
		scene.world().showSection(util.select().position(depot), Direction.UP);
		scene.idle(20);
		scene.overlay().showText(90)
			.attachKeyFrame()
			.text("Some gases leave solids behind: Quartz Vapour gives water and quartz, dropped into the inventory below")
			.pointAt(util.vector().blockSurface(depot, Direction.NORTH))
			.placeNearTarget();
		scene.idle(100);

		scene.overlay().showText(70)
			.text("Condensers can be built larger, up to 3x3 wide, for more capacity")
			.pointAt(util.vector().topOf(condenser))
			.placeNearTarget();
		scene.idle(80);
		scene.markAsFinished();
	}

	/**
	 * Structure "ore_chain": chamber+resonator (0,*,2) → mech pump (1,1,2) → cavitation chamber+resonator (2,*,2)
	 * → resonance pump (3,1,2) → pipes (4,1..2,2) → condenser (5,2,2) over depot (5,1,2).
	 */
	public static void oreChain(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("ore_chain", "Ore multiplication with cavitation");
		scene.configureBasePlate(0, 0, 7);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		BlockPos chamber = util.grid().at(0, 1, 2);
		BlockPos cavitation = util.grid().at(2, 1, 2);
		BlockPos condenser = util.grid().at(5, 2, 2);
		BlockPos depot = util.grid().at(5, 1, 2);

		scene.world().showSection(util.select().fromTo(0, 1, 2, 0, 3, 2), Direction.DOWN);
		scene.idle(10);
		scene.world().setKineticSpeed(util.select().fromTo(0, 2, 2, 0, 3, 2), 128);
		scene.world().modifyBlockEntity(chamber, ResonanceChamberBlockEntity.class, be -> {
			be.inputTank.getCapability().fill(new FluidStack(Fluids.WATER, 250), FluidAction.EXECUTE);
			ItemHandlerHelper.insertItem(be.inputInventory, new ItemStack(Items.RAW_IRON), false);
		});
		scene.overlay().showControls(util.vector().topOf(chamber), Pointing.DOWN, 30).withItem(new ItemStack(Items.RAW_IRON));
		scene.idle(20);
		scene.overlay().showText(90)
			.attachKeyFrame()
			.text("Step 1: raw ore and water in a Resonance Chamber at the High band become Ore Slurry")
			.pointAt(util.vector().blockSurface(chamber, Direction.NORTH))
			.placeNearTarget();
		scene.idle(100);

		scene.world().showSection(util.select().fromTo(1, 1, 2, 2, 3, 2), Direction.DOWN);
		scene.idle(10);
		scene.world().setKineticSpeed(util.select().position(1, 1, 2), 64);
		scene.world().propagatePipeChange(util.grid().at(1, 1, 2));
		scene.world().setKineticSpeed(util.select().fromTo(2, 2, 2, 2, 3, 2), 256);
		scene.world().modifyBlockEntity(cavitation, ResonanceChamberBlockEntity.class, be -> {
			be.inputTank.getCapability().fill(MetalStacks.slurry("iron", 250), FluidAction.EXECUTE);
			be.inputTank.getCapability().fill(mist(250), FluidAction.EXECUTE);
		});
		scene.idle(10);
		scene.overlay().showText(100)
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.text("Step 2: slurry and Sonic Mist in the Cavitation Chamber at the Ultrasonic band (256 RPM) become Metal Vapour")
			.pointAt(util.vector().blockSurface(cavitation, Direction.NORTH))
			.placeNearTarget();
		scene.idle(110);

		scene.world().showSection(util.select().fromTo(3, 1, 2, 5, 2, 2), Direction.WEST);
		scene.idle(10);
		scene.world().setKineticSpeed(util.select().position(3, 1, 2), 64);
		scene.world().propagatePipeChange(util.grid().at(3, 1, 2));
		scene.idle(10);
		scene.overlay().showText(100)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("Step 3: a Condenser turns the vapour back into water and Metal Concentrate, two and a half per ore")
			.pointAt(util.vector().blockSurface(condenser, Direction.NORTH))
			.placeNearTarget();
		scene.idle(110);

		scene.overlay().showOutline(PonderPalette.GREEN, new Object(), util.select().position(depot), 70);
		scene.overlay().showText(80)
			.text("Concentrate smelts into ingots. Any raw ore listed in the metals data map works")
			.pointAt(util.vector().blockSurface(depot, Direction.NORTH))
			.placeNearTarget();
		scene.idle(90);
		scene.markAsFinished();
	}

	/** Structure "vent": tank (0,1,2) - pipe - resonance pump (2,1,2) east - pipe - vent (4,1,2). */
	public static void vent(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("vent", "Venting unwanted gas");
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		BlockPos source = util.grid().at(0, 1, 2);
		BlockPos pump = util.grid().at(2, 1, 2);
		BlockPos vent = util.grid().at(4, 1, 2);

		scene.world().showSection(util.select().position(vent), Direction.DOWN);
		scene.idle(10);
		scene.overlay().showText(70)
			.attachKeyFrame()
			.text("The Vent destroys any gas piped into it")
			.pointAt(util.vector().blockSurface(vent, Direction.WEST))
			.placeNearTarget();
		scene.idle(80);

		fillTank(scene, source, mist(4000));
		scene.world().showSection(util.select().fromTo(0, 1, 2, 3, 1, 2), Direction.EAST);
		scene.idle(10);
		scene.world().setKineticSpeed(util.select().position(pump), 64);
		scene.world().propagatePipeChange(pump);
		scene.idle(10);
		scene.overlay().showText(80)
			.colored(PonderPalette.OUTPUT)
			.text("Handy for flushing a pipe network or disposing of by-products")
			.pointAt(util.vector().topOf(vent))
			.placeNearTarget();
		scene.idle(90);

		fillTank(scene, source, new FluidStack(Fluids.WATER, 4000));
		scene.idle(20);
		scene.overlay().showOutline(PonderPalette.RED, new Object(), util.select().position(vent), 70);
		scene.overlay().showText(70)
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.text("Liquids are refused, so nothing valuable is lost by accident")
			.pointAt(util.vector().blockSurface(vent, Direction.NORTH))
			.placeNearTarget();
		scene.idle(80);
		scene.markAsFinished();
	}
}
