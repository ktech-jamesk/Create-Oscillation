package co.pyragon.jamoss.registry;

import com.simibubi.create.AllFluids.TintedFluidType;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.ore.MetalFluidType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;

public class COFluids {
	private static final ResourceLocation LIQUID_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
	private static final ResourceLocation LIQUID_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");
	/** Water ripples with a wispy alpha mask (~50% solid), so gases read as vapour in tanks and pipes. */
	private static final ResourceLocation GAS_STILL = CreateOscillation.asResource("block/gas_still");
	private static final ResourceLocation GAS_FLOW = CreateOscillation.asResource("block/gas_flow");
	public static final int GAS_ALPHA = 0xFF; // transparency comes from the wisp texture itself

	public static final FluidEntry<VirtualFluid> SONIC_MIST = CreateOscillation.REGISTRATE
		.virtualFluid("sonic_mist", GAS_STILL, GAS_FLOW, GasFluidType.of(0xDDE6EA),
			VirtualFluid::createSource, VirtualFluid::createFlowing)
		.lang("Sonic Mist")
		.properties(p -> p
			.density(-500) // negative density => FluidType#isLighterThanAir
			.viscosity(100)
			.temperature(400)
			.canSwim(false)
			.canDrown(false))
		.tag(COFluidTags.GASES)
		.register();

	public static final FluidEntry<VirtualFluid> QUARTZ_VAPOUR = CreateOscillation.REGISTRATE
		.virtualFluid("quartz_vapour", GAS_STILL, GAS_FLOW, GasFluidType.of(0xF2D9E6),
			VirtualFluid::createSource, VirtualFluid::createFlowing)
		.lang("Quartz Vapour")
		.properties(p -> p
			.density(-300)
			.viscosity(100)
			.temperature(500)
			.canSwim(false)
			.canDrown(false))
		.tag(COFluidTags.GASES)
		.register();

	public static final FluidEntry<VirtualFluid> ORE_SLURRY = CreateOscillation.REGISTRATE
		.virtualFluid("ore_slurry", LIQUID_STILL, LIQUID_FLOW, MetalFluidType.of("fluid.createoscillation.ore_slurry.named", 0xFF),
			VirtualFluid::createSource, VirtualFluid::createFlowing)
		.lang("Ore Slurry")
		.properties(p -> p
			.density(1500)
			.viscosity(3000)
			.temperature(320)
			.canSwim(false)
			.canDrown(false))
		.register();

	public static final FluidEntry<VirtualFluid> METAL_VAPOUR = CreateOscillation.REGISTRATE
		.virtualFluid("metal_vapour", GAS_STILL, GAS_FLOW, MetalFluidType.of("fluid.createoscillation.metal_vapour.named", GAS_ALPHA),
			VirtualFluid::createSource, VirtualFluid::createFlowing)
		.lang("Metal Vapour")
		.properties(p -> p
			.density(-200)
			.viscosity(100)
			.temperature(900)
			.canSwim(false)
			.canDrown(false))
		.tag(COFluidTags.GASES)
		.register();

	public static void register() {}

	/** Solid-tinted, gaseous fluid type. */
	public static class GasFluidType extends TintedFluidType {
		private final int color;

		private GasFluidType(Properties properties, ResourceLocation still, ResourceLocation flow, int color) {
			super(properties, still, flow);
			this.color = color;
		}

		public static FluidBuilder.FluidTypeFactory of(int color) {
			return (properties, still, flow) -> new GasFluidType(properties, still, flow, (GAS_ALPHA << 24) | (color & 0xFFFFFF));
		}

		@Override
		protected int getTintColor(FluidStack stack) {
			return color;
		}

		@Override
		protected int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
			return color;
		}
	}
}
