package co.pyragon.jamoss.compat.ponder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.processing.basin.BasinBlock;

import co.pyragon.jamoss.registry.COBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block layouts for every Ponder scene, written as {@code assets/createoscillation/ponder/*.nbt}
 * by datagen. Coordinates are (x, y, z) with y = 0 the base plate. Shared between the scene
 * registrations (which reference the path) and the structure generator (which builds it).
 */
public class COPonderStructures {

	public static final String RESONATOR = "resonator";
	public static final String CHAMBER = "chamber";
	public static final String PUMP = "pump";
	public static final String CONDENSER = "condenser";
	public static final String VENT = "vent";
	public static final String SIEVE = "sieve";
	public static final String TUNING_FORK = "tuning_fork";
	public static final String ORE_CHAIN = "ore_chain";
	public static final String COUPLER = "coupler";
	public static final String PULVERISER = "pulveriser";
	public static final String AMPLIFIER = "amplifier";

	/** Every structure by path, in a stable order. */
	public static Map<String, StructureBuilder> all() {
		Map<String, StructureBuilder> map = new LinkedHashMap<>();
		map.put(RESONATOR, plate(5, 5, 5, b -> {
			b.put(2, 1, 2, chamber());
			b.put(2, 2, 2, COBlocks.RESONATOR.getDefaultState());
			b.put(2, 3, 2, shaft(Axis.Y));
		}));
		map.put(CHAMBER, plate(5, 5, 5, b -> {
			b.put(2, 1, 2, chamber());
			b.put(2, 2, 2, COBlocks.RESONATOR.getDefaultState());
			b.put(2, 3, 2, shaft(Axis.Y));
			b.put(3, 1, 2, pump(Direction.EAST));
			b.put(4, 1, 2, tank());
		}));
		map.put(PUMP, plate(5, 3, 5, b -> {
			b.put(0, 1, 2, tank());
			b.put(1, 1, 2, pipe(Axis.X));
			b.put(2, 1, 2, pump(Direction.EAST));
			b.put(3, 1, 2, pipe(Axis.X));
			b.put(4, 1, 2, tank());
		}));
		map.put(CONDENSER, plate(6, 4, 5, b -> {
			b.put(0, 1, 2, tank());
			b.put(1, 1, 2, pump(Direction.EAST));
			b.put(2, 1, 2, pipe(Axis.X));
			b.put(2, 2, 2, pipe(Axis.Y));
			b.put(3, 2, 2, COBlocks.CONDENSER.getDefaultState());
			b.put(3, 1, 2, AllBlocks.DEPOT.getDefaultState());
			b.put(4, 2, 2, AllBlocks.MECHANICAL_PUMP.getDefaultState().setValue(PumpBlock.FACING, Direction.EAST));
			b.put(5, 2, 2, tank());
			b.put(5, 1, 2, AllBlocks.ANDESITE_CASING.getDefaultState());
		}));
		map.put(VENT, plate(5, 3, 5, b -> {
			b.put(0, 1, 2, tank());
			b.put(1, 1, 2, pipe(Axis.X));
			b.put(2, 1, 2, pump(Direction.EAST));
			b.put(3, 1, 2, pipe(Axis.X));
			b.put(4, 1, 2, COBlocks.VENT.getDefaultState());
		}));
		map.put(SIEVE, plate(5, 6, 5, b -> {
			b.put(2, 1, 2, AllBlocks.DEPOT.getDefaultState());
			b.put(2, 2, 2, COBlocks.VIBRATING_SIEVE.getDefaultState());
			b.put(2, 3, 2, COBlocks.RESONATOR.getDefaultState());
			b.put(2, 4, 2, shaft(Axis.Y));
		}));
		map.put(TUNING_FORK, plate(5, 6, 5, b -> {
			b.put(2, 1, 2, chamber());
			b.put(2, 2, 2, COBlocks.RESONATOR.getDefaultState());
			b.put(2, 3, 2, COBlocks.TUNING_FORK.getDefaultState());
			b.put(2, 4, 2, shaft(Axis.Y));
		}));
		map.put(AMPLIFIER, plate(5, 6, 5, b -> {
			b.put(2, 1, 2, chamber());
			b.put(2, 2, 2, COBlocks.RESONANCE_AMPLIFIER.getDefaultState());
			b.put(2, 3, 2, COBlocks.RESONATOR.getDefaultState());
			b.put(2, 4, 2, shaft(Axis.Y));
		}));
		map.put(ORE_CHAIN, plate(7, 5, 5, b -> {
			b.put(0, 1, 2, chamber());
			b.put(0, 2, 2, COBlocks.RESONATOR.getDefaultState());
			b.put(0, 3, 2, shaft(Axis.Y));
			b.put(1, 1, 2, AllBlocks.MECHANICAL_PUMP.getDefaultState().setValue(PumpBlock.FACING, Direction.EAST));
			b.put(2, 1, 2, COBlocks.CAVITATION_CHAMBER.getDefaultState().setValue(BasinBlock.FACING, Direction.DOWN));
			b.put(2, 2, 2, COBlocks.RESONATOR.getDefaultState());
			b.put(2, 3, 2, shaft(Axis.Y));
			b.put(3, 1, 2, pump(Direction.EAST));
			b.put(4, 1, 2, AllBlocks.FLUID_PIPE.getDefaultState().setValue(PipeBlock.WEST, true).setValue(PipeBlock.UP, true));
			b.put(4, 2, 2, AllBlocks.FLUID_PIPE.getDefaultState().setValue(PipeBlock.DOWN, true).setValue(PipeBlock.EAST, true));
			b.put(5, 2, 2, COBlocks.CONDENSER.getDefaultState());
			b.put(5, 1, 2, AllBlocks.DEPOT.getDefaultState());
		}));
		// Link A runs east along z=4 (blocked by the stone at x=4 mid-scene); link B runs south along x=3 and crosses it at (3,1,4).
		map.put(COUPLER, plate(9, 5, 9, b -> {
			b.put(1, 1, 4, COBlocks.RESONANCE_EMITTER.getDefaultState().setValue(HorizontalDirectionalBlock.FACING, Direction.EAST));
			b.put(1, 2, 4, COBlocks.RESONATOR.getDefaultState());
			b.put(1, 3, 4, shaft(Axis.Y));
			b.put(6, 1, 4, COBlocks.RESONANCE_RECEIVER.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.EAST));
			b.put(7, 1, 4, shaft(Axis.X));
			b.put(8, 1, 4, AllBlocks.COGWHEEL.getDefaultState().setValue(RotatedPillarKineticBlock.AXIS, Axis.X));
			b.put(4, 1, 4, Blocks.STONE.defaultBlockState());
			b.put(3, 1, 1, COBlocks.RESONANCE_EMITTER.getDefaultState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH));
			b.put(3, 2, 1, COBlocks.RESONATOR.getDefaultState());
			b.put(3, 3, 1, shaft(Axis.Y));
			b.put(3, 1, 7, COBlocks.RESONANCE_RECEIVER.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.SOUTH));
			b.put(3, 1, 8, shaft(Axis.Z));
		}));
		map.put(PULVERISER, plate(7, 5, 5, b -> {
			b.put(0, 2, 2, shaft(Axis.X));
			b.put(1, 2, 2, COBlocks.SONIC_PULVERISER.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.EAST));
			b.fill(3, 1, 1, 4, 3, 3, Blocks.STONE.defaultBlockState());
			b.put(3, 2, 2, Blocks.IRON_ORE.defaultBlockState());
			b.put(4, 1, 1, Blocks.COBBLESTONE.defaultBlockState());
			b.put(4, 3, 3, Blocks.COBBLESTONE.defaultBlockState());
		}));
		return map;
	}

	/** A Create-style chequered base plate at y = 0, then the scene's blocks. */
	private static StructureBuilder plate(int sx, int sy, int sz, Consumer<StructureBuilder> blocks) {
		StructureBuilder b = new StructureBuilder(sx, sy, sz);
		for (int x = 0; x < sx; x++)
			for (int z = 0; z < sz; z++)
				b.put(x, 0, z, ((x + z) % 2 == 0 ? Blocks.WHITE_CONCRETE : Blocks.SNOW_BLOCK).defaultBlockState());
		blocks.accept(b);
		return b;
	}

	private static BlockState chamber() {
		return COBlocks.RESONANCE_CHAMBER.getDefaultState().setValue(BasinBlock.FACING, Direction.DOWN);
	}

	private static BlockState shaft(Axis axis) {
		return AllBlocks.SHAFT.getDefaultState().setValue(RotatedPillarKineticBlock.AXIS, axis);
	}

	private static BlockState pipe(Axis axis) {
		return AllBlocks.FLUID_PIPE.get().getAxisState(axis);
	}

	private static BlockState pump(Direction facing) {
		return COBlocks.RESONANCE_PUMP.getDefaultState().setValue(PumpBlock.FACING, facing);
	}

	private static BlockState tank() {
		return AllBlocks.FLUID_TANK.getDefaultState();
	}
}
