package co.pyragon.jamoss.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlock;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.chamber.ResonanceChamberBlockEntity;
import co.pyragon.jamoss.compat.ponder.COPonderStructures;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import com.simibubi.create.AllItems;
import co.pyragon.jamoss.content.pulveriser.SonicPulveriserBlockEntity;
import co.pyragon.jamoss.content.pulveriser.PulveriserInventory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import co.pyragon.jamoss.content.coupler.ResonanceReceiverBlockEntity;
import co.pyragon.jamoss.content.coupler.ResonanceEmitterBlockEntity;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import co.pyragon.jamoss.content.ore.Metals;
import co.pyragon.jamoss.content.ore.MetalStacks;
import net.minecraft.world.item.crafting.RecipeInput;
import co.pyragon.jamoss.content.recipe.CondensingRecipe;
import co.pyragon.jamoss.registry.CORecipeTypes;
import co.pyragon.jamoss.content.tuningfork.TuningForkBlockEntity;
import co.pyragon.jamoss.content.canister.GasCanisterItem;
import co.pyragon.jamoss.content.condenser.CondenserBlockEntity;
import co.pyragon.jamoss.content.sieve.VibratingSieveBlockEntity;
import co.pyragon.jamoss.registry.COItems;
import co.pyragon.jamoss.content.pump.ResonancePumpBlockEntity;
import co.pyragon.jamoss.content.resonator.ResonatorBlockEntity;
import co.pyragon.jamoss.registry.COBlocks;
import co.pyragon.jamoss.registry.COFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CreateOscillation.MOD_ID)
@PrefixGameTestTemplate(false)
public class COGameTests {

	/** Building every creative tab throws if an item is listed twice (e.g. by Registrate and by our tab). */
	@GameTest(template = "gametest/empty_8x6x8")
	public static void creativeTabsBuild(GameTestHelper helper) {
		CreativeModeTabs.tryRebuildTabContents(helper.getLevel().enabledFeatures(), true, helper.getLevel().registryAccess());
		helper.succeed();
	}

	/**
	 * Layout (x, y, z) inside the 8x6x8 empty template.
	 * Kinetics: motor(2,4,3) facing DOWN -> shaft(2,3,3) -> resonator(2,2,3) directly on chamber(2,1,3).
	 * Fluids:   chamber(2,1,3) -> pipe(3,1,3) -> resonance pump(4,1,3) facing EAST -> tank(5,1,3).
	 * Pump drive: motor(3,2,3) facing EAST -> cogwheel(4,2,3) on the X axis, meshing with the pump below.
	 */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 400)
	public static void steamLoop(GameTestHelper helper) {
		steamLoop(helper, false);
	}

	/** Same loop, but a small cogwheel (axis Y) sits directly on the resonator instead of a shaft. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 400)
	public static void steamLoopCogOnTop(GameTestHelper helper) {
		steamLoop(helper, true);
	}

	/** Regression: water added while the resonator is already spinning must still be processed. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 400)
	public static void steamLoopWaterAddedLater(GameTestHelper helper) {
		steamLoop(helper, false, true);
	}

	/**
	 * Steam in a Create tank -> pipe -> resonance pump -> 2x2x1 Condenser (4 blocks) -> water pulled
	 * out through a second pump into a Create tank. Also checks the condenser refuses water.
	 */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 900)
	public static void condenserSteamToWater(GameTestHelper helper) {
		BlockPos source = new BlockPos(0, 1, 2);
		BlockPos inPump = new BlockPos(1, 1, 2);
		BlockPos inPumpMotor = new BlockPos(0, 2, 2);
		BlockPos inPumpCog = new BlockPos(1, 2, 2);
		BlockPos condenser = new BlockPos(2, 1, 2); // 2x2 footprint: (2..3, 1, 2..3)
		BlockPos outPump = new BlockPos(4, 1, 2);
		BlockPos outPumpMotor = new BlockPos(3, 2, 2);
		BlockPos outPumpCog = new BlockPos(4, 2, 2);
		BlockPos sink = new BlockPos(5, 1, 2);

		helper.setBlock(source, AllBlocks.FLUID_TANK.getDefaultState());
		helper.setBlock(sink, AllBlocks.FLUID_TANK.getDefaultState());
		for (int x = 0; x < 2; x++)
			for (int z = 0; z < 2; z++)
				helper.setBlock(condenser.offset(x, 0, z), COBlocks.CONDENSER.getDefaultState());
		helper.setBlock(inPump, COBlocks.RESONANCE_PUMP.getDefaultState().setValue(PumpBlock.FACING, Direction.EAST));
		helper.setBlock(outPump, AllBlocks.MECHANICAL_PUMP.getDefaultState().setValue(PumpBlock.FACING, Direction.EAST));
		for (BlockPos cog : new BlockPos[] { inPumpCog, outPumpCog })
			helper.setBlock(cog, AllBlocks.COGWHEEL.getDefaultState()
				.setValue(RotatedPillarKineticBlock.AXIS, Direction.Axis.X));
		for (BlockPos motor : new BlockPos[] { inPumpMotor, outPumpMotor })
			helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState()
				.setValue(DirectionalKineticBlock.FACING, Direction.EAST));

		IFluidHandler sourceTank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(source), null);
		if (sourceTank == null)
			helper.fail("Source tank has no fluid handler", source);
		sourceTank.fill(new FluidStack((net.minecraft.world.level.material.Fluid) COFluids.STEAM.getSource(), 2000), FluidAction.EXECUTE);

		IFluidHandler condenserHandler = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(condenser.offset(1, 0, 1)), null);
		if (condenserHandler == null)
			helper.fail("Condenser exposes no fluid handler", condenser);
		if (condenserHandler.fill(new FluidStack(Fluids.WATER, 100), FluidAction.SIMULATE) != 0)
			helper.fail("Condenser accepted water", condenser);

		for (BlockPos motor : new BlockPos[] { inPumpMotor, outPumpMotor }) {
			if (helper.getBlockEntity(motor) instanceof CreativeMotorBlockEntity motorBE)
				motorBE.generatedSpeed.setValue(64);
			else
				helper.fail("Creative motor missing", motor);
		}

		helper.succeedWhen(() -> {
			IFluidHandler sinkTank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(sink), null);
			if (sinkTank == null)
				helper.fail("Sink tank has no fluid handler", sink);
			boolean water = false;
			for (int i = 0; i < sinkTank.getTanks(); i++) {
				FluidStack stack = sinkTank.getFluidInTank(i);
				if (stack.is(Fluids.WATER) && stack.getAmount() > 0)
					water = true;
				if (stack.getFluid().isSame(COFluids.STEAM.get()))
					helper.fail("Steam bypassed the condenser", sink);
			}
			if (!water)
				helper.fail("No water reached the sink tank yet. condenser=" + fluids(helper, condenser) + " source=" + fluids(helper, source), sink);
		});
	}

	/** Regression: a continuous trickle of steam must not keep resetting the condenser's progress. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 600)
	public static void condenserProgressSurvivesInflow(GameTestHelper helper) {
		BlockPos condenser = new BlockPos(2, 1, 2);
		helper.setBlock(condenser, COBlocks.CONDENSER.getDefaultState());
		if (!(helper.getBlockEntity(condenser) instanceof CondenserBlockEntity be)) {
			helper.fail("Condenser block entity missing", condenser);
			return;
		}
		helper.succeedWhen(() -> {
			// 50 mb every tick, forever: the single block needs 500 ticks per 250 mb of water.
			be.getCondenserFluidHandler().fill(new FluidStack((net.minecraft.world.level.material.Fluid) COFluids.STEAM.getSource(), 50), FluidAction.EXECUTE);
			if (be.getOutputTank().getFluidAmount() <= 0)
				helper.fail("No water condensed yet", condenser);
		});
	}

	/** Solid path, half 1: quartz + water in the chamber becomes quartz vapour. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 400)
	public static void resonateQuartzToVapour(GameTestHelper helper) {
		BlockPos motor = new BlockPos(2, 4, 3);
		BlockPos shaft = new BlockPos(2, 3, 3);
		BlockPos resonator = new BlockPos(2, 2, 3);
		BlockPos chamber = new BlockPos(2, 1, 3);
		helper.setBlock(chamber, COBlocks.RESONANCE_CHAMBER.getDefaultState().setValue(BasinBlock.FACING, Direction.DOWN));
		helper.setBlock(resonator, COBlocks.RESONATOR.getDefaultState());
		helper.setBlock(shaft, AllBlocks.SHAFT.getDefaultState().setValue(RotatedPillarKineticBlock.AXIS, Direction.Axis.Y));
		helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.DOWN));

		IFluidHandler chamberTank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(chamber), null);
		IItemHandler chamberInv = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(chamber), null);
		if (chamberTank == null || chamberInv == null)
			helper.fail("Chamber exposes no handlers", chamber);
		chamberTank.fill(new FluidStack(Fluids.WATER, 250), FluidAction.EXECUTE);
		ItemHandlerHelper.insertItem(chamberInv, new ItemStack(Items.QUARTZ), false);
		if (helper.getBlockEntity(motor) instanceof CreativeMotorBlockEntity motorBE)
			motorBE.generatedSpeed.setValue(64);

		helper.succeedWhen(() -> {
			boolean vapour = false;
			for (int i = 0; i < chamberTank.getTanks(); i++)
				if (chamberTank.getFluidInTank(i).getFluid().isSame(COFluids.QUARTZ_VAPOUR.get()))
					vapour = true;
			if (!vapour)
				helper.fail("No quartz vapour yet: " + fluids(helper, chamber), chamber);
			for (int i = 0; i < chamberInv.getSlots(); i++)
				if (chamberInv.getStackInSlot(i).is(Items.QUARTZ))
					helper.fail("Quartz was not consumed", chamber);
		});
	}

	/**
	 * Frequency bands: quartz vapour requires the Mid band (>= 64 RPM). At 32 RPM (Low) the chamber must
	 * idle; once the motor is raised to 64 the recipe runs.
	 */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 600)
	public static void quartzVapourNeedsMidBand(GameTestHelper helper) {
		BlockPos motor = new BlockPos(2, 4, 3);
		BlockPos chamber = new BlockPos(2, 1, 3);
		IFluidHandler chamberTank = quartzRig(helper, 32);
		ResonanceChamberBlockEntity chamberBE = (ResonanceChamberBlockEntity) helper.getBlockEntity(chamber);

		IItemHandler chamberInv = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(chamber), null);

		// In the Low band the water may become steam (band "any"), but the quartz must stay untouched.
		helper.runAfterDelay(200, () -> {
			if (chamberBE.getBand() != FrequencyBand.LOW)
				helper.fail("Expected Low band at 32 RPM, got " + chamberBE.getBand(), chamber);
			if (hasFluid(chamberTank, COFluids.QUARTZ_VAPOUR.get()))
				helper.fail("Quartz vapour recipe ran in the Low band: " + fluids(helper, chamber), chamber);
			boolean quartz = false;
			for (int i = 0; i < chamberInv.getSlots(); i++)
				if (chamberInv.getStackInSlot(i).is(Items.QUARTZ))
					quartz = true;
			if (!quartz)
				helper.fail("Quartz was consumed in the Low band", chamber);
			chamberTank.fill(new FluidStack(Fluids.WATER, 250), FluidAction.EXECUTE);
			if (helper.getBlockEntity(motor) instanceof CreativeMotorBlockEntity motorBE)
				motorBE.generatedSpeed.setValue(64);
		});

		helper.succeedWhen(() -> {
			if (chamberBE.getDriveSpeed() < 64)
				helper.fail("Motor not yet at 64 RPM", motor);
			if (chamberBE.getBand() != FrequencyBand.MID)
				helper.fail("Expected Mid band at 64 RPM, got " + chamberBE.getBand(), chamber);
			if (!hasFluid(chamberTank, COFluids.QUARTZ_VAPOUR.get()))
				helper.fail("No quartz vapour yet in Mid band: " + fluids(helper, chamber), chamber);
		});
	}

	/** Steam is a Low-band recipe: it runs at 32 RPM... */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 600)
	public static void steamRunsInLowBand(GameTestHelper helper) {
		BlockPos motor = new BlockPos(2, 4, 3);
		BlockPos chamber = new BlockPos(2, 1, 3);
		IFluidHandler chamberTank = steamRig(helper, 32);

		helper.succeedWhen(() -> {
			if (!hasFluid(chamberTank, COFluids.STEAM.get()))
				helper.fail("No steam in Low band: " + fluids(helper, chamber), chamber);
		});
	}

	/** ...and not at 128 RPM, so water can sit in a chamber tuned for other recipes. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 400)
	public static void steamDoesNotRunInHighBand(GameTestHelper helper) {
		BlockPos chamber = new BlockPos(2, 1, 3);
		IFluidHandler chamberTank = steamRig(helper, 128);
		helper.runAfterDelay(300, () -> {
			if (hasFluid(chamberTank, COFluids.STEAM.get()))
				helper.fail("Steam formed in the High band: " + fluids(helper, chamber), chamber);
			helper.succeed();
		});
	}

	/** Motor -> shaft -> resonator -> chamber, with 250 mb water and one quartz in the chamber. */
	private static IFluidHandler quartzRig(GameTestHelper helper, int rpm) {
		IFluidHandler tank = steamRig(helper, rpm);
		BlockPos chamber = new BlockPos(2, 1, 3);
		IItemHandler chamberInv = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(chamber), null);
		if (chamberInv == null)
			helper.fail("Chamber exposes no item handler", chamber);
		ItemHandlerHelper.insertItem(chamberInv, new ItemStack(Items.QUARTZ), false);
		return tank;
	}

	/** Motor -> shaft -> resonator -> chamber, with 250 mb water in the chamber. Returns the chamber's fluid handler. */
	private static IFluidHandler steamRig(GameTestHelper helper, int rpm) {
		BlockPos motor = new BlockPos(2, 4, 3);
		BlockPos shaft = new BlockPos(2, 3, 3);
		BlockPos resonator = new BlockPos(2, 2, 3);
		BlockPos chamber = new BlockPos(2, 1, 3);
		helper.setBlock(chamber, COBlocks.RESONANCE_CHAMBER.getDefaultState().setValue(BasinBlock.FACING, Direction.DOWN));
		helper.setBlock(resonator, COBlocks.RESONATOR.getDefaultState());
		helper.setBlock(shaft, AllBlocks.SHAFT.getDefaultState().setValue(RotatedPillarKineticBlock.AXIS, Direction.Axis.Y));
		helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.DOWN));

		IFluidHandler chamberTank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(chamber), null);
		if (chamberTank == null)
			helper.fail("Chamber exposes no fluid handler", chamber);
		chamberTank.fill(new FluidStack(Fluids.WATER, 250), FluidAction.EXECUTE);
		if (helper.getBlockEntity(motor) instanceof CreativeMotorBlockEntity motorBE)
			motorBE.generatedSpeed.setValue(rpm);
		else
			helper.fail("Creative motor missing", motor);
		return chamberTank;
	}

	private static boolean hasFluid(IFluidHandler handler, net.minecraft.world.level.material.Fluid fluid) {
		for (int i = 0; i < handler.getTanks(); i++)
			if (handler.getFluidInTank(i).getFluid().isSame(fluid))
				return true;
		return false;
	}

	/** Every generated Ponder structure must parse as a template with only registered blocks. */
	@GameTest(template = "gametest/empty_8x6x8")
	public static void ponderStructuresLoad(GameTestHelper helper) {
		for (String path : COPonderStructures.all().keySet()) {
			String resource = "/assets/" + CreateOscillation.MOD_ID + "/ponder/" + path + ".nbt";
			try (var in = COGameTests.class.getResourceAsStream(resource)) {
				if (in == null)
					helper.fail("Ponder structure missing (run runData): " + resource);
				var tag = net.minecraft.nbt.NbtIo.readCompressed(in, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
				for (var entry : tag.getList("palette", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
					String name = ((net.minecraft.nbt.CompoundTag) entry).getString("Name");
					if (!BuiltInRegistries.BLOCK.containsKey(ResourceLocation.parse(name)))
						helper.fail("Ponder structure " + path + " uses unknown block " + name);
				}
				var template = new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate();
				template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
				if (template.getSize().getY() == 0)
					helper.fail("Ponder structure empty: " + path);
			} catch (java.io.IOException e) {
				helper.fail("Ponder structure unreadable: " + path + " (" + e.getMessage() + ")");
			}
		}
		helper.succeed();
	}

	/** Tuning fork set to Mid with a 128 RPM input: the resonator below runs at exactly 64. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 200)
	public static void tuningForkClampsToBand(GameTestHelper helper) {
		BlockPos resonator = forkRig(helper, 128, FrequencyBand.MID);
		helper.succeedWhen(() -> {
			ResonatorBlockEntity be = (ResonatorBlockEntity) helper.getBlockEntity(resonator);
			if (Math.abs(be.getSpeed()) != 64)
				helper.fail("Expected resonator at 64 RPM, got " + be.getSpeed(), resonator);
		});
	}

	/** Tuning fork on Any passes rotation through unchanged. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 200)
	public static void tuningForkPassesThrough(GameTestHelper helper) {
		BlockPos resonator = forkRig(helper, 128, FrequencyBand.ANY);
		helper.succeedWhen(() -> {
			ResonatorBlockEntity be = (ResonatorBlockEntity) helper.getBlockEntity(resonator);
			if (Math.abs(be.getSpeed()) != 128)
				helper.fail("Expected resonator at 128 RPM, got " + be.getSpeed(), resonator);
		});
	}

	/** Input slower than the fork's band is not sped up. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 200)
	public static void tuningForkCannotSpeedUp(GameTestHelper helper) {
		BlockPos resonator = forkRig(helper, 32, FrequencyBand.HIGH);
		helper.succeedWhen(() -> {
			ResonatorBlockEntity be = (ResonatorBlockEntity) helper.getBlockEntity(resonator);
			if (Math.abs(be.getSpeed()) != 32)
				helper.fail("Expected resonator at 32 RPM, got " + be.getSpeed(), resonator);
		});
	}

	/** Motor (2,5,3) -> shaft (2,4,3) -> tuning fork (2,3,3) -> resonator (2,2,3) -> chamber (2,1,3). */
	private static BlockPos forkRig(GameTestHelper helper, int rpm, FrequencyBand band) {
		BlockPos motor = new BlockPos(2, 5, 3);
		BlockPos shaft = new BlockPos(2, 4, 3);
		BlockPos fork = new BlockPos(2, 3, 3);
		BlockPos resonator = new BlockPos(2, 2, 3);
		BlockPos chamber = new BlockPos(2, 1, 3);
		helper.setBlock(chamber, COBlocks.RESONANCE_CHAMBER.getDefaultState().setValue(BasinBlock.FACING, Direction.DOWN));
		helper.setBlock(resonator, COBlocks.RESONATOR.getDefaultState());
		helper.setBlock(fork, COBlocks.TUNING_FORK.getDefaultState());
		helper.setBlock(shaft, AllBlocks.SHAFT.getDefaultState().setValue(RotatedPillarKineticBlock.AXIS, Direction.Axis.Y));
		helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.DOWN));
		if (helper.getBlockEntity(fork) instanceof TuningForkBlockEntity forkBE)
			forkBE.band.setValue(band.ordinal());
		else
			helper.fail("Tuning fork block entity missing", fork);
		if (helper.getBlockEntity(motor) instanceof CreativeMotorBlockEntity motorBE)
			motorBE.generatedSpeed.setValue(rpm);
		else
			helper.fail("Creative motor missing", motor);
		return resonator;
	}

	/** A Low-Tuned crystal and rose quartz resonated at 64 RPM become a Mid-Tuned Crystal (and only that). */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 600)
	public static void tunedCrystalAtMid(GameTestHelper helper) {
		BlockPos chamber = new BlockPos(2, 1, 3);
		steamRig(helper, 64);
		IItemHandler chamberInv = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(chamber), null);
		ItemHandlerHelper.insertItem(chamberInv, COItems.TUNED_CRYSTAL_LOW.asStack(), false);
		ItemHandlerHelper.insertItem(chamberInv, new ItemStack(AllItems.ROSE_QUARTZ.get()), false);
		helper.succeedWhen(() -> {
			boolean mid = false;
			for (int i = 0; i < chamberInv.getSlots(); i++) {
				ItemStack stack = chamberInv.getStackInSlot(i);
				if (stack.is(COItems.TUNED_CRYSTAL_MID.get()))
					mid = true;
				else if (stack.is(COItems.TUNED_CRYSTAL_LOW.get()) || stack.is(COItems.TUNED_CRYSTAL_HIGH.get()))
					helper.fail("Wrong crystal produced: " + stack, chamber);
			}
			if (!mid)
				helper.fail("No Mid-Tuned Crystal yet", chamber);
		});
	}

	/** The condensing recipe for quartz vapour lists the rough crystal as a chance output. */
	@GameTest(template = "gametest/empty_8x6x8")
	public static void condensingListsRoughCrystal(GameTestHelper helper) {
		boolean found = false;
		for (var holder : helper.getLevel().getRecipeManager()
			.getAllRecipesFor(CORecipeTypes.CONDENSING.<RecipeInput, CondensingRecipe>getType()))
			for (var output : holder.value().getRollableResults())
				if (output.getStack().is(COItems.ROUGH_QUARTZ_CRYSTAL.get()))
					found = true;
		if (!found)
			helper.fail("No condensing recipe outputs a Rough Quartz Crystal");
		helper.succeed();
	}

	// ---- ore chain ----

	/** Raw iron + water: nothing at Mid, iron slurry at High. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 600)
	public static void slurryNeedsHighBand(GameTestHelper helper) {
		BlockPos motor = new BlockPos(2, 4, 3);
		BlockPos chamber = new BlockPos(2, 1, 3);
		IFluidHandler chamberTank = steamRig(helper, 64);
		IItemHandler chamberInv = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(chamber), null);
		ItemHandlerHelper.insertItem(chamberInv, new ItemStack(Items.RAW_IRON), false);
		helper.runAfterDelay(200, () -> {
			if (hasFluid(chamberTank, COFluids.ORE_SLURRY.getSource()))
				helper.fail("Slurry made in the Mid band", chamber);
			chamberTank.fill(new FluidStack(Fluids.WATER, 250), FluidAction.EXECUTE);
			if (helper.getBlockEntity(motor) instanceof CreativeMotorBlockEntity motorBE)
				motorBE.generatedSpeed.setValue(128);
		});
		helper.succeedWhen(() -> {
			for (int i = 0; i < chamberTank.getTanks(); i++) {
				FluidStack stack = chamberTank.getFluidInTank(i);
				if (stack.getFluid().isSame(COFluids.ORE_SLURRY.getSource())) {
					if (!"iron".equals(MetalStacks.metal(stack)))
						helper.fail("Slurry has wrong metal: " + MetalStacks.metal(stack), chamber);
					return;
				}
			}
			helper.fail("No iron slurry yet: " + fluids(helper, chamber), chamber);
		});
	}

	/** Cavitation chamber at 256 RPM: iron slurry + steam → iron vapour. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 600)
	public static void cavitationMakesVapour(GameTestHelper helper) {
		BlockPos motor = new BlockPos(2, 4, 3);
		BlockPos shaft = new BlockPos(2, 3, 3);
		BlockPos resonator = new BlockPos(2, 2, 3);
		BlockPos chamber = new BlockPos(2, 1, 3);
		helper.setBlock(chamber, COBlocks.CAVITATION_CHAMBER.getDefaultState().setValue(BasinBlock.FACING, Direction.DOWN));
		helper.setBlock(resonator, COBlocks.RESONATOR.getDefaultState());
		helper.setBlock(shaft, AllBlocks.SHAFT.getDefaultState().setValue(RotatedPillarKineticBlock.AXIS, Direction.Axis.Y));
		helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.DOWN));
		IFluidHandler chamberTank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(chamber), null);
		if (chamberTank == null)
			helper.fail("Cavitation chamber exposes no fluid handler", chamber);
		if (chamberTank.fill(MetalStacks.slurry("iron", 250), FluidAction.EXECUTE) != 250)
			helper.fail("Could not insert slurry", chamber);
		if (chamberTank.fill(new FluidStack((net.minecraft.world.level.material.Fluid) COFluids.STEAM.getSource(), 250), FluidAction.EXECUTE) != 250)
			helper.fail("Could not insert steam", chamber);
		if (helper.getBlockEntity(motor) instanceof CreativeMotorBlockEntity motorBE)
			motorBE.generatedSpeed.setValue(256);
		helper.succeedWhen(() -> {
			for (int i = 0; i < chamberTank.getTanks(); i++) {
				FluidStack stack = chamberTank.getFluidInTank(i);
				if (stack.getFluid().isSame(COFluids.METAL_VAPOUR.getSource()) && "iron".equals(MetalStacks.metal(stack)))
					return;
			}
			helper.fail("No iron vapour yet: " + fluids(helper, chamber), chamber);
		});
	}

	/** Condenser fed iron vapour drops at least two iron concentrate onto the depot below. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 400)
	public static void condensingVapourGivesConcentrate(GameTestHelper helper) {
		BlockPos condenser = new BlockPos(2, 2, 3);
		BlockPos depot = new BlockPos(2, 1, 3);
		helper.setBlock(depot, AllBlocks.DEPOT.getDefaultState());
		helper.setBlock(condenser, COBlocks.CONDENSER.getDefaultState());
		if (!(helper.getBlockEntity(condenser) instanceof CondenserBlockEntity be))
			throw new IllegalStateException("Condenser block entity missing");
		int filled = be.getCondenserFluidHandler().fill(MetalStacks.vapour("iron", 250), FluidAction.EXECUTE);
		if (filled != 250)
			helper.fail("Condenser refused iron vapour, filled " + filled, condenser);
		IItemHandler depotInv = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(depot), null);
		helper.succeedWhen(() -> {
			int count = 0;
			for (int i = 0; i < depotInv.getSlots(); i++) {
				ItemStack stack = depotInv.getStackInSlot(i);
				if (stack.is(COItems.METAL_CONCENTRATE.get())) {
					if (!"iron".equals(MetalStacks.metal(stack)))
						helper.fail("Concentrate has wrong metal", depot);
					count += stack.getCount();
				}
			}
			if (count < 2)
				helper.fail("Expected >= 2 iron concentrate, got " + count, depot);
		});
	}

	@GameTest(template = "gametest/empty_8x6x8")
	public static void concentrateMetalsDoNotStack(GameTestHelper helper) {
		if (ItemStack.isSameItemSameComponents(MetalStacks.concentrate("iron", 1), MetalStacks.concentrate("gold", 1)))
			helper.fail("Iron and gold concentrate would stack");
		if (!ItemStack.isSameItemSameComponents(MetalStacks.concentrate("iron", 1), MetalStacks.concentrate("iron", 3)))
			helper.fail("Two iron concentrates would not stack");
		helper.succeed();
	}

	@GameTest(template = "gametest/empty_8x6x8")
	public static void metalsDataMapLoads(GameTestHelper helper) {
		var metals = Metals.all();
		if (metals.size() < 4)
			helper.fail("Expected >= 4 metals in the data map, got " + metals.size());
		if (!"iron".equals(Metals.metalOf(new ItemStack(Items.RAW_IRON))))
			helper.fail("Raw iron is not mapped to iron");
		if (Metals.colour("gold") == Metals.DEFAULT_COLOUR)
			helper.fail("Gold colour missing");
		helper.succeed();
	}

	/** Furnace recipe matches iron concentrate → iron ingot and does not accept gold concentrate for it. */
	@GameTest(template = "gametest/empty_8x6x8")
	public static void concentrateSmeltsToIngot(GameTestHelper helper) {
		var manager = helper.getLevel().getRecipeManager();
		var iron = manager.getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(MetalStacks.concentrate("iron", 1)), helper.getLevel());
		if (iron.isEmpty() || !iron.get().value().getResultItem(helper.getLevel().registryAccess()).is(Items.IRON_INGOT))
			helper.fail("Iron concentrate does not smelt to an iron ingot");
		var gold = manager.getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(MetalStacks.concentrate("gold", 1)), helper.getLevel());
		if (gold.isEmpty() || !gold.get().value().getResultItem(helper.getLevel().registryAccess()).is(Items.GOLD_INGOT))
			helper.fail("Gold concentrate does not smelt to a gold ingot");
		helper.succeed();
	}

	// ---- resonance coupler ----

	/** Emitter (Mid crystal) under a 64 RPM resonator drives a receiver 4 blocks away; its shaft spins at 64. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 200)
	public static void couplerLinksAcrossAir(GameTestHelper helper) {
		BlockPos shaft = couplerRig(helper, COItems.TUNED_CRYSTAL_MID.asStack(), COItems.TUNED_CRYSTAL_MID.asStack(), 64);
		helper.succeedWhen(() -> {
			KineticBlockEntity be = (KineticBlockEntity) helper.getBlockEntity(shaft);
			if (Math.abs(be.getSpeed()) != 64)
				helper.fail("Expected output shaft at 64 RPM, got " + be.getSpeed(), shaft);
		});
	}

	/** A stone block in the beam stops the link. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 200)
	public static void couplerBlockedBySolid(GameTestHelper helper) {
		BlockPos shaft = couplerRig(helper, COItems.TUNED_CRYSTAL_MID.asStack(), COItems.TUNED_CRYSTAL_MID.asStack(), 64);
		helper.setBlock(new BlockPos(4, 1, 3), Blocks.STONE.defaultBlockState());
		helper.runAfterDelay(100, () -> {
			KineticBlockEntity be = (KineticBlockEntity) helper.getBlockEntity(shaft);
			if (be.getSpeed() != 0)
				helper.fail("Link passed through stone: " + be.getSpeed(), shaft);
			helper.succeed();
		});
	}

	/** Mismatched crystals (Mid emitter, High receiver) or the wrong band at the resonator: no link. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 200)
	public static void couplerNeedsMatchingBand(GameTestHelper helper) {
		BlockPos shaft = couplerRig(helper, COItems.TUNED_CRYSTAL_MID.asStack(), COItems.TUNED_CRYSTAL_HIGH.asStack(), 64);
		helper.runAfterDelay(100, () -> {
			KineticBlockEntity be = (KineticBlockEntity) helper.getBlockEntity(shaft);
			if (be.getSpeed() != 0)
				helper.fail("Mismatched crystals linked: " + be.getSpeed(), shaft);
			helper.succeed();
		});
	}

	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 200)
	public static void couplerNeedsResonatorInBand(GameTestHelper helper) {
		BlockPos shaft = couplerRig(helper, COItems.TUNED_CRYSTAL_MID.asStack(), COItems.TUNED_CRYSTAL_MID.asStack(), 128);
		helper.runAfterDelay(100, () -> {
			KineticBlockEntity be = (KineticBlockEntity) helper.getBlockEntity(shaft);
			if (be.getSpeed() != 0)
				helper.fail("Resonator in High band drove a Mid link: " + be.getSpeed(), shaft);
			helper.succeed();
		});
	}

	/**
	 * Motor (1,4,3) -> shaft (1,3,3) -> resonator (1,2,3) -> emitter (1,1,3) facing east;
	 * receiver (5,1,3) facing east; output shaft (6,1,3) on the X axis. Returns the output shaft.
	 */
	private static BlockPos couplerRig(GameTestHelper helper, ItemStack emitterCrystal, ItemStack receiverCrystal, int rpm) {
		BlockPos motor = new BlockPos(1, 4, 3);
		BlockPos shaft = new BlockPos(1, 3, 3);
		BlockPos resonator = new BlockPos(1, 2, 3);
		BlockPos emitter = new BlockPos(1, 1, 3);
		BlockPos receiver = new BlockPos(5, 1, 3);
		BlockPos out = new BlockPos(6, 1, 3);
		helper.setBlock(emitter, COBlocks.RESONANCE_EMITTER.getDefaultState().setValue(HorizontalDirectionalBlock.FACING, Direction.EAST));
		helper.setBlock(resonator, COBlocks.RESONATOR.getDefaultState());
		helper.setBlock(shaft, AllBlocks.SHAFT.getDefaultState().setValue(RotatedPillarKineticBlock.AXIS, Direction.Axis.Y));
		helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.DOWN));
		helper.setBlock(receiver, COBlocks.RESONANCE_RECEIVER.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.EAST));
		helper.setBlock(out, AllBlocks.SHAFT.getDefaultState().setValue(RotatedPillarKineticBlock.AXIS, Direction.Axis.X));
		if (helper.getBlockEntity(emitter) instanceof ResonanceEmitterBlockEntity e)
			e.crystal.setStackInSlot(0, emitterCrystal);
		else
			helper.fail("Emitter block entity missing", emitter);
		if (helper.getBlockEntity(receiver) instanceof ResonanceReceiverBlockEntity r)
			r.crystal.setStackInSlot(0, receiverCrystal);
		else
			helper.fail("Receiver block entity missing", receiver);
		if (helper.getBlockEntity(motor) instanceof CreativeMotorBlockEntity motorBE)
			motorBE.generatedSpeed.setValue(rpm);
		else
			helper.fail("Creative motor missing", motor);
		return out;
	}

	// ---- sonic pulveriser ----

	/** Mid crystal: a 3x3 stone wall two blocks ahead is cleared. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 300)
	public static void pulveriserClearsLayer(GameTestHelper helper) {
		pulveriserRig(helper, COItems.TUNED_CRYSTAL_MID.asStack());
		for (int y = 1; y <= 3; y++)
			for (int z = 2; z <= 4; z++)
				helper.setBlock(new BlockPos(3, y, z), Blocks.STONE.defaultBlockState());
		helper.succeedWhen(() -> {
			for (int y = 1; y <= 3; y++)
				for (int z = 2; z <= 4; z++)
					if (!helper.getBlockState(new BlockPos(3, y, z)).isAir())
						helper.fail("Stone still standing", new BlockPos(3, y, z));
		});
	}

	/** Low crystal cannot crack obsidian; High can. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 400)
	public static void pulveriserHardnessCap(GameTestHelper helper) {
		SonicPulveriserBlockEntity be = pulveriserRig(helper, COItems.TUNED_CRYSTAL_LOW.asStack());
		BlockPos obsidian = new BlockPos(2, 2, 3);
		helper.setBlock(obsidian, Blocks.OBSIDIAN.defaultBlockState());
		helper.runAfterDelay(120, () -> {
			if (!helper.getBlockState(obsidian).is(Blocks.OBSIDIAN))
				helper.fail("Low crystal broke obsidian", obsidian);
			be.inventory.setStackInSlot(PulveriserInventory.CRYSTALS, COItems.TUNED_CRYSTAL_HIGH.asStack());
			be.dropActiveCrystal();
		});
		helper.succeedWhen(() -> {
			if (helper.getBlockState(obsidian).is(Blocks.OBSIDIAN))
				helper.fail("High crystal has not broken obsidian yet", obsidian);
		});
	}

	/** Filter set to cobblestone: cobble goes, stone stays. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 300)
	public static void pulveriserRespectsFilter(GameTestHelper helper) {
		SonicPulveriserBlockEntity be = pulveriserRig(helper, COItems.TUNED_CRYSTAL_MID.asStack());
		be.filtering.setFilter(new ItemStack(Items.COBBLESTONE));
		BlockPos stone = new BlockPos(2, 2, 3);
		BlockPos cobble = new BlockPos(2, 3, 3);
		helper.setBlock(stone, Blocks.STONE.defaultBlockState());
		helper.setBlock(cobble, Blocks.COBBLESTONE.defaultBlockState());
		helper.runAfterDelay(150, () -> {
			if (!helper.getBlockState(cobble).isAir())
				helper.fail("Cobblestone not broken", cobble);
			if (!helper.getBlockState(stone).is(Blocks.STONE))
				helper.fail("Stone broken despite filter", stone);
			helper.succeed();
		});
	}

	/** Breaking spends charge; a nearly spent crystal leaves a rough crystal in the output slot and the next one is consumed. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 300)
	public static void pulveriserSpendsCharge(GameTestHelper helper) {
		SonicPulveriserBlockEntity be = pulveriserRig(helper, COItems.TUNED_CRYSTAL_LOW.asStack(2));
		be.setCharge(FrequencyBand.LOW, 1);
		BlockPos stone = new BlockPos(2, 2, 3);
		helper.setBlock(stone, Blocks.STONE.defaultBlockState());
		helper.succeedWhen(() -> {
			if (!helper.getBlockState(stone).isAir())
				helper.fail("Stone not broken", stone);
			if (!be.inventory.getStackInSlot(PulveriserInventory.SPENT).is(COItems.ROUGH_QUARTZ_CRYSTAL.get()))
				helper.fail("No rough crystal in the spent slot", stone);
			if (be.inventory.getStackInSlot(PulveriserInventory.CRYSTALS).getCount() != 2)
				helper.fail("Waiting crystals changed unexpectedly", stone);
		});
	}

	/** Crystals can be fed through the item capability (hoppers/funnels); spent crystals come out of it. */
	@GameTest(template = "gametest/empty_8x6x8")
	public static void pulveriserInventoryContract(GameTestHelper helper) {
		SonicPulveriserBlockEntity be = pulveriserRig(helper, ItemStack.EMPTY);
		IItemHandler handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(new BlockPos(1, 2, 3)), null);
		if (handler == null)
			helper.fail("Pulveriser exposes no item handler");
		if (!ItemHandlerHelper.insertItem(handler, COItems.TUNED_CRYSTAL_MID.asStack(3), false).isEmpty())
			helper.fail("Could not insert crystals through the handler");
		if (!ItemHandlerHelper.insertItem(handler, new ItemStack(Items.STONE), true).is(Items.STONE))
			helper.fail("Handler accepted a non-crystal");
		if (!handler.extractItem(PulveriserInventory.CRYSTALS, 1, true).isEmpty())
			helper.fail("Handler let waiting crystals be extracted");
		be.inventory.setStackInSlot(PulveriserInventory.SPENT, COItems.ROUGH_QUARTZ_CRYSTAL.asStack(2));
		if (handler.extractItem(PulveriserInventory.SPENT, 2, false).getCount() != 2)
			helper.fail("Could not extract spent crystals through the handler");
		helper.succeed();
	}

	/** No crystal: nothing happens. Block entities are never broken. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 300)
	public static void pulveriserIdleAndSafe(GameTestHelper helper) {
		SonicPulveriserBlockEntity be = pulveriserRig(helper, ItemStack.EMPTY);
		BlockPos stone = new BlockPos(2, 2, 3);
		BlockPos chest = new BlockPos(2, 3, 3);
		helper.setBlock(stone, Blocks.STONE.defaultBlockState());
		helper.setBlock(chest, Blocks.CHEST.defaultBlockState());
		helper.runAfterDelay(80, () -> {
			if (!helper.getBlockState(stone).is(Blocks.STONE))
				helper.fail("Broke a block with no crystal", stone);
			be.inventory.setStackInSlot(PulveriserInventory.CRYSTALS, COItems.TUNED_CRYSTAL_MID.asStack());
		});
		helper.runAfterDelay(250, () -> {
			if (!helper.getBlockState(stone).isAir())
				helper.fail("Stone not broken once a crystal was inserted", stone);
			if (!helper.getBlockState(chest).is(Blocks.CHEST))
				helper.fail("Pulveriser broke a chest", chest);
			helper.succeed();
		});
	}

	/** Hand-built setup: motor at its default speed, crystal inserted by right-click, every other face covered. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 400)
	public static void pulveriserManualSetup(GameTestHelper helper) {
		BlockPos motor = new BlockPos(1, 2, 3);
		BlockPos pulveriser = new BlockPos(2, 2, 3);
		helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.EAST));
		helper.setBlock(pulveriser, COBlocks.SONIC_PULVERISER.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.EAST));
		for (Direction d : Direction.values())
			if (d != Direction.WEST)
				helper.setBlock(pulveriser.relative(d), Blocks.SAND.defaultBlockState());
		SonicPulveriserBlockEntity be = (SonicPulveriserBlockEntity) helper.getBlockEntity(pulveriser);
		var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
		ItemStack held = COItems.TUNED_CRYSTAL_LOW.asStack();
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, held);
		helper.getBlockState(pulveriser).useItemOn(held, helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND,
			new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(pulveriser)), Direction.NORTH, helper.absolutePos(pulveriser), false));
		if (be.inventory.getStackInSlot(PulveriserInventory.CRYSTALS).isEmpty())
			helper.fail("Crystal was not inserted by right-click path", pulveriser);
		helper.succeedWhen(() -> {
			if (Math.abs(be.getSpeed()) == 0)
				helper.fail("Pulveriser has no speed from the motor", pulveriser);
			if (!helper.getBlockState(pulveriser.east()).isAir())
				helper.fail("Sand in front not broken; working=" + be.isWorking() + " speed=" + be.getSpeed() + " tier=" + be.getTier(), pulveriser.east());
		});
	}

	/** A crystal can't be set as the filter (that would match nothing and silently disable the machine). */
	@GameTest(template = "gametest/empty_8x6x8")
	public static void pulveriserFilterRejectsCrystals(GameTestHelper helper) {
		SonicPulveriserBlockEntity be = pulveriserRig(helper, COItems.TUNED_CRYSTAL_MID.asStack());
		if (be.filtering.setFilter(COItems.TUNED_CRYSTAL_MID.asStack()))
			helper.fail("Filter accepted a crystal");
		if (!be.filtering.getFilter().isEmpty())
			helper.fail("Filter is not empty after rejecting a crystal");
		if (!be.filtering.setFilter(new ItemStack(Items.COBBLESTONE)))
			helper.fail("Filter rejected cobblestone");
		helper.succeed();
	}

	/** Motor (0,2,3) facing east → pulveriser (1,2,3) facing east. Targets sit at x >= 2. */
	private static SonicPulveriserBlockEntity pulveriserRig(GameTestHelper helper, ItemStack crystal) {
		BlockPos motor = new BlockPos(0, 2, 3);
		BlockPos pulveriser = new BlockPos(1, 2, 3);
		helper.setBlock(pulveriser, COBlocks.SONIC_PULVERISER.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.EAST));
		helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.EAST));
		if (!(helper.getBlockEntity(pulveriser) instanceof SonicPulveriserBlockEntity be))
			throw new IllegalStateException("Pulveriser block entity missing");
		be.inventory.setStackInSlot(PulveriserInventory.CRYSTALS, crystal);
		if (helper.getBlockEntity(motor) instanceof CreativeMotorBlockEntity motorBE)
			motorBE.generatedSpeed.setValue(32);
		else
			helper.fail("Creative motor missing", motor);
		return be;
	}

	/** Solid path, half 2: quartz vapour condenses to water plus quartz, delivered to a depot below. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 300)
	public static void condenseVapourToQuartz(GameTestHelper helper) {
		BlockPos depot = new BlockPos(2, 1, 2);
		BlockPos condenser = new BlockPos(2, 2, 2);
		helper.setBlock(depot, AllBlocks.DEPOT.getDefaultState());
		helper.setBlock(condenser, COBlocks.CONDENSER.getDefaultState());
		if (!(helper.getBlockEntity(condenser) instanceof CondenserBlockEntity be)) {
			helper.fail("Condenser block entity missing", condenser);
			return;
		}
		int filled = be.getCondenserFluidHandler().fill(new FluidStack((net.minecraft.world.level.material.Fluid) COFluids.QUARTZ_VAPOUR.getSource(), 250), FluidAction.EXECUTE);
		if (filled != 250)
			helper.fail("Condenser refused quartz vapour, filled " + filled, condenser);
		helper.succeedWhen(() -> {
			if (!be.getOutputTank().getFluid().is(Fluids.WATER))
				helper.fail("No water condensed yet", condenser);
			IItemHandler depotInv = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(depot), Direction.UP);
			boolean quartz = false;
			for (int i = 0; depotInv != null && i < depotInv.getSlots(); i++)
				if (depotInv.getStackInSlot(i).is(Items.QUARTZ))
					quartz = true;
			if (!quartz)
				helper.fail("Quartz not delivered to the depot yet", depot);
		});
	}

	/** Right-clicking the chamber's side with an item inserts it. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 40)
	public static void chamberAcceptsRightClickedItems(GameTestHelper helper) {
		BlockPos chamber = new BlockPos(2, 1, 2);
		helper.setBlock(chamber, COBlocks.RESONANCE_CHAMBER.getDefaultState());
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack held = new ItemStack(Items.QUARTZ, 5);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		BlockPos abs = helper.absolutePos(chamber);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs).add(-0.5, 0, 0), Direction.WEST, abs, false);
		helper.getBlockState(chamber).useItemOn(held, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);

		IItemHandler inv = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, abs, null);
		int count = 0;
		for (int i = 0; inv != null && i < inv.getSlots(); i++)
			if (inv.getStackInSlot(i).is(Items.QUARTZ))
				count += inv.getStackInSlot(i).getCount();
		if (count != 5)
			helper.fail("Expected 5 quartz in the chamber, found " + count + " (hand now " + held.getCount() + ")", chamber);
		helper.succeed();
	}

	/** A Resonator on top shakes the sieve; gravel is sifted and the drops land on the depot below. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 1000)
	public static void sieveUnderResonatorSiftsIntoDepot(GameTestHelper helper) {
		BlockPos depot = new BlockPos(3, 1, 3);
		BlockPos sieve = new BlockPos(3, 2, 3);
		BlockPos resonator = new BlockPos(3, 3, 3);
		BlockPos shaft = new BlockPos(3, 4, 3);
		BlockPos motor = new BlockPos(3, 5, 3);
		helper.setBlock(depot, AllBlocks.DEPOT.getDefaultState());
		helper.setBlock(sieve, COBlocks.VIBRATING_SIEVE.getDefaultState());
		helper.setBlock(resonator, COBlocks.RESONATOR.getDefaultState());
		helper.setBlock(shaft, AllBlocks.SHAFT.getDefaultState().setValue(RotatedPillarKineticBlock.AXIS, Direction.Axis.Y));
		helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.DOWN));
		if (helper.getBlockEntity(motor) instanceof CreativeMotorBlockEntity motorBE)
			motorBE.generatedSpeed.setValue(64);
		if (!(helper.getBlockEntity(sieve) instanceof VibratingSieveBlockEntity sieveBE)) {
			helper.fail("Sieve block entity missing", sieve);
			return;
		}
		// 20 gravel: with 30% flint odds the chance of zero flint is ~0.08%
		ItemStack rest = ItemHandlerHelper.insertItem(sieveBE.getItemHandler(), new ItemStack(Items.GRAVEL, 20), false);
		if (!rest.isEmpty())
			helper.fail("Sieve refused gravel", sieve);
		if (!ItemHandlerHelper.insertItem(sieveBE.getItemHandler(), new ItemStack(Items.DIAMOND), true).is(Items.DIAMOND))
			helper.fail("Sieve accepted an item with no sifting recipe", sieve);

		helper.succeedWhen(() -> {
			if (sieveBE.getDriveSpeed() < VibratingSieveBlockEntity.MIN_SPEED)
				helper.fail("Sieve is not being driven (speed " + sieveBE.getDriveSpeed() + ")", sieve);
			if (!sieveBE.inputInv.getStackInSlot(0).isEmpty())
				helper.fail("Gravel remaining: " + sieveBE.inputInv.getStackInSlot(0).getCount(), sieve);
			// a depot only holds one stack, so the first result type lands there and the rest wait in the buffer
			IItemHandler depotInv = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(depot), Direction.UP);
			boolean depotHasSomething = false, flint = false;
			for (int i = 0; depotInv != null && i < depotInv.getSlots(); i++) {
				ItemStack s = depotInv.getStackInSlot(i);
				if (!s.isEmpty())
					depotHasSomething = true;
				if (s.is(Items.FLINT))
					flint = true;
			}
			for (int i = 0; i < sieveBE.outputInv.getSlots(); i++)
				if (sieveBE.outputInv.getStackInSlot(i).is(Items.FLINT))
					flint = true;
			if (!depotHasSomething)
				helper.fail("Nothing was pushed down onto the depot", depot);
			if (!flint)
				helper.fail("No flint produced", sieve);
		});
	}

	/** A Vent swallows gas (and only gas) pushed into it. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 20)
	public static void ventVoidsGasOnly(GameTestHelper helper) {
		BlockPos vent = new BlockPos(2, 1, 2);
		helper.setBlock(vent, COBlocks.VENT.getDefaultState());
		IFluidHandler handler = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(vent), Direction.NORTH);
		if (handler == null) {
			helper.fail("Vent exposes no fluid handler", vent);
			return;
		}
		if (handler.fill(new FluidStack(Fluids.WATER, 100), FluidAction.EXECUTE) != 0)
			helper.fail("Vent accepted water", vent);
		if (handler.fill(new FluidStack((net.minecraft.world.level.material.Fluid) COFluids.STEAM.getSource(), 100), FluidAction.EXECUTE) != 100)
			helper.fail("Vent refused steam", vent);
		if (!handler.getFluidInTank(0).isEmpty())
			helper.fail("Vent kept the gas instead of voiding it", vent);
		helper.succeed();
	}

	/** Survival player fills a canister from a condenser: the condenser must actually lose the gas. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 20)
	public static void canisterDrainsCondenser(GameTestHelper helper) {
		BlockPos condenser = new BlockPos(2, 1, 2);
		helper.setBlock(condenser, COBlocks.CONDENSER.getDefaultState());
		if (!(helper.getBlockEntity(condenser) instanceof CondenserBlockEntity be)) {
			helper.fail("Condenser block entity missing", condenser);
			return;
		}
		be.getTankInventory().fill(new FluidStack((net.minecraft.world.level.material.Fluid) COFluids.STEAM.getSource(), 1500), FluidAction.EXECUTE);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack held = new ItemStack(COItems.GAS_CANISTER.get());
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		BlockPos abs = helper.absolutePos(condenser);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.WEST, abs, false);
		helper.getBlockState(condenser).useItemOn(held, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
		int left = be.getTankInventory().getFluidAmount();
		if (left != 500)
			helper.fail("Condenser should have 500mb left, has " + left, condenser);
		int carried = 0;
		for (ItemStack s : player.getInventory().items)
			if (s.getItem() == COItems.GAS_CANISTER.get())
				carried += GasCanisterItem.getContent(s).getAmount();
		if (carried != 1000)
			helper.fail("Player should hold a 1000mb canister, holds " + carried, condenser);
		// pipes must still not be able to pull gas out
		IFluidHandler piped = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, abs, Direction.NORTH);
		if (piped == null || !piped.drain(100, FluidAction.SIMULATE).isEmpty())
			helper.fail("Side handler allowed draining gas", condenser);
		helper.succeed();
	}

	/** A survival player can fill a canister from a plain Create fluid tank via the item. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 20)
	public static void canisterFillsFromCreateTank(GameTestHelper helper) {
		BlockPos tank = new BlockPos(2, 1, 2);
		helper.setBlock(tank, AllBlocks.FLUID_TANK.getDefaultState());
		BlockPos abs = helper.absolutePos(tank);
		IFluidHandler tankHandler = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, abs, null);
		tankHandler.fill(new FluidStack((net.minecraft.world.level.material.Fluid) COFluids.STEAM.getSource(), 3000), FluidAction.EXECUTE);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack held = new ItemStack(COItems.GAS_CANISTER.get());
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		net.minecraft.world.item.context.UseOnContext ctx = new net.minecraft.world.item.context.UseOnContext(player, InteractionHand.MAIN_HAND,
			new BlockHitResult(Vec3.atCenterOf(abs), Direction.WEST, abs, false));
		held.useOn(ctx);
		int inTank = tankHandler.getFluidInTank(0).getAmount();
		int carried = 0;
		for (ItemStack s : player.getInventory().items)
			if (s.getItem() == COItems.GAS_CANISTER.get())
				carried += GasCanisterItem.getContent(s).getAmount();
		if (inTank != 2000 || carried != 1000)
			helper.fail("Tank " + inTank + "mb, canister " + carried + "mb", tank);
		helper.succeed();
	}

	/** Gas canister: fills only with gases, and empties into a chamber. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 20)
	public static void gasCanisterHoldsGas(GameTestHelper helper) {
		ItemStack canister = new ItemStack(COItems.GAS_CANISTER.get());
		IFluidHandlerItem cap = canister.getCapability(Capabilities.FluidHandler.ITEM);
		if (cap == null) {
			helper.fail("Canister has no fluid capability", BlockPos.ZERO);
			return;
		}
		if (cap.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE) != 0)
			helper.fail("Canister accepted water", BlockPos.ZERO);
		if (cap.fill(new FluidStack((net.minecraft.world.level.material.Fluid) COFluids.STEAM.getSource(), 1500), FluidAction.EXECUTE) != 1000)
			helper.fail("Canister should take exactly 1000mb steam", BlockPos.ZERO);
		ItemStack filled = cap.getContainer();
		if (GasCanisterItem.getContent(filled).getAmount() != 1000)
			helper.fail("Canister content not stored: " + GasCanisterItem.getContent(filled), BlockPos.ZERO);
		FluidStack drained = filled.getCapability(Capabilities.FluidHandler.ITEM).drain(400, FluidAction.EXECUTE);
		if (drained.getAmount() != 400 || GasCanisterItem.getContent(filled).getAmount() != 600)
			helper.fail("Canister drain wrong: " + drained.getAmount(), BlockPos.ZERO);
		helper.succeed();
	}

	/** Warm-up: first water conversion takes 500 ticks, the next ones ~20 ticks each while fed. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 700)
	public static void condenserWarmsUp(GameTestHelper helper) {
		BlockPos condenser = new BlockPos(2, 1, 2);
		helper.setBlock(condenser, COBlocks.CONDENSER.getDefaultState());
		if (!(helper.getBlockEntity(condenser) instanceof CondenserBlockEntity be)) {
			helper.fail("Condenser block entity missing", condenser);
			return;
		}
		be.getCondenserFluidHandler().fill(new FluidStack((net.minecraft.world.level.material.Fluid) COFluids.STEAM.getSource(), 2000), FluidAction.EXECUTE);
		// 2000 mb = 8 conversions: 500 + 7 * 20 = 640 ticks warm, vs 4000 ticks cold
		helper.runAtTickTime(450, () -> {
			if (be.getOutputTank().getFluidAmount() != 0)
				helper.fail("Converted before the first full duration elapsed", condenser);
		});
		helper.succeedWhen(() -> {
			if (be.getOutputTank().getFluidAmount() < 2000)
				helper.fail("Only " + be.getOutputTank().getFluidAmount() + "mb condensed so far", condenser);
		});
	}

		/** Item outputs are pushed into an inventory below the condenser. */
	@GameTest(template = "gametest/empty_8x6x8", timeoutTicks = 200)
	public static void condenserPushesItemsBelow(GameTestHelper helper) {
		BlockPos depot = new BlockPos(2, 1, 2);
		BlockPos condenser = new BlockPos(2, 2, 2);
		helper.setBlock(depot, AllBlocks.DEPOT.getDefaultState());
		helper.setBlock(condenser, COBlocks.CONDENSER.getDefaultState());
		if (!(helper.getBlockEntity(condenser) instanceof CondenserBlockEntity be)) {
			helper.fail("Condenser block entity missing", condenser);
			return;
		}
		be.getOutputItems().insertItem(0, new ItemStack(Items.QUARTZ, 3), false);
		helper.succeedWhen(() -> {
			IItemHandler depotInv = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(depot), Direction.UP);
			if (depotInv == null)
				helper.fail("Depot has no item handler", depot);
			boolean found = false;
			for (int i = 0; i < depotInv.getSlots(); i++)
				if (depotInv.getStackInSlot(i).is(Items.QUARTZ))
					found = true;
			if (!found)
				helper.fail("Quartz not delivered to depot yet", depot);
		});
	}

	private static void steamLoop(GameTestHelper helper, boolean cogOnTop) {
		steamLoop(helper, cogOnTop, false);
	}

	private static void steamLoop(GameTestHelper helper, boolean cogOnTop, boolean waterAddedLater) {
		BlockPos motor = new BlockPos(2, 4, 3);
		BlockPos shaft = new BlockPos(2, 3, 3);
		BlockPos resonator = new BlockPos(2, 2, 3);
		BlockPos chamber = new BlockPos(2, 1, 3);
		BlockPos pipe = new BlockPos(3, 1, 3);
		BlockPos pump = new BlockPos(4, 1, 3);
		BlockPos tank = new BlockPos(5, 1, 3);
		BlockPos pumpMotor = new BlockPos(3, 2, 3);
		BlockPos pumpCog = new BlockPos(4, 2, 3);

		helper.setBlock(chamber, COBlocks.RESONANCE_CHAMBER.getDefaultState()
			.setValue(BasinBlock.FACING, Direction.DOWN));
		helper.setBlock(resonator, COBlocks.RESONATOR.getDefaultState());
		helper.setBlock(pipe, AllBlocks.FLUID_PIPE.getDefaultState());
		helper.setBlock(tank, AllBlocks.FLUID_TANK.getDefaultState());
		helper.setBlock(pump, COBlocks.RESONANCE_PUMP.getDefaultState()
			.setValue(PumpBlock.FACING, Direction.EAST));
		helper.setBlock(shaft, (cogOnTop ? AllBlocks.COGWHEEL : AllBlocks.SHAFT).getDefaultState()
			.setValue(RotatedPillarKineticBlock.AXIS, Direction.Axis.Y));
		helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState()
			.setValue(DirectionalKineticBlock.FACING, Direction.DOWN));
		helper.setBlock(pumpCog, AllBlocks.COGWHEEL.getDefaultState()
			.setValue(RotatedPillarKineticBlock.AXIS, Direction.Axis.X));
		helper.setBlock(pumpMotor, AllBlocks.CREATIVE_MOTOR.getDefaultState()
			.setValue(DirectionalKineticBlock.FACING, Direction.EAST));

		if (!(helper.getBlockEntity(chamber) instanceof ResonanceChamberBlockEntity))
			helper.fail("Chamber block entity missing", chamber);
		Runnable fillWater = () -> {
			IFluidHandler chamberTank = helper.getLevel()
				.getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(chamber), null);
			if (chamberTank == null)
				helper.fail("Chamber exposes no fluid handler", chamber);
			int filled = chamberTank.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE);
			if (filled != 1000)
				helper.fail("Could not fill chamber with water, filled " + filled, chamber);
		};
		if (!waterAddedLater)
			fillWater.run();

		for (BlockPos motorPos : new BlockPos[] { motor, pumpMotor }) {
			if (helper.getBlockEntity(motorPos) instanceof CreativeMotorBlockEntity motorBE)
				motorBE.generatedSpeed.setValue(motorPos.equals(motor) ? 32 : 64); // steam is a Low-band recipe
			else
				helper.fail("Creative motor missing", motorPos);
		}

		if (waterAddedLater)
			helper.runAfterDelay(60, fillWater); // resonator has been idling at speed for 3 seconds

		helper.succeedWhen(() -> {
			IFluidHandler tankHandler = helper.getLevel()
				.getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(tank), null);
			if (tankHandler == null)
				helper.fail("Tank exposes no fluid handler", tank);
			boolean hasSteam = false;
			for (int i = 0; i < tankHandler.getTanks(); i++) {
				FluidStack stack = tankHandler.getFluidInTank(i);
				if (stack.is(Fluids.WATER))
					helper.fail("Resonance pump moved water", tank);
				if (stack.getFluid().isSame(COFluids.STEAM.get()) && stack.getAmount() > 0)
					hasSteam = true;
			}
			if (!hasSteam)
				helper.fail("No steam reached the tank yet. " + describe(helper, resonator, chamber, pump, tank), tank);
		});
	}

	private static String describe(GameTestHelper helper, BlockPos resonator, BlockPos chamber, BlockPos pump, BlockPos tank) {
		StringBuilder sb = new StringBuilder();
		if (helper.getBlockEntity(resonator) instanceof ResonatorBlockEntity res)
			sb.append("resonator speed=").append(res.getSpeed()).append(" working=").append(res.isWorking()).append("; ");
		else
			sb.append("resonator BE missing; ");
		if (helper.getBlockEntity(pump) instanceof ResonancePumpBlockEntity p)
			sb.append("pump speed=").append(p.getSpeed()).append("; ");
		sb.append("chamber=").append(fluids(helper, chamber)).append("; ");
		sb.append("tank=").append(fluids(helper, tank));
		return sb.toString();
	}

	private static String fluids(GameTestHelper helper, BlockPos pos) {
		IFluidHandler h = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(pos), null);
		if (h == null)
			return "<no handler>";
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < h.getTanks(); i++) {
			FluidStack f = h.getFluidInTank(i);
			sb.append(f.isEmpty() ? "empty" : f.getAmount() + "mb " + f.getFluid().builtInRegistryHolder().key().location()).append(", ");
		}
		return sb.append("]").toString();
	}
}
