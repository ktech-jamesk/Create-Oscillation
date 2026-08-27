package co.pyragon.jamoss.content.ore;

import com.simibubi.create.AllFluids.TintedFluidType;
import com.tterrag.registrate.builders.FluidBuilder;

import co.pyragon.jamoss.registry.CODataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;

/** A fluid whose colour and name come from the {@code metal} component on the stack. */
public class MetalFluidType extends TintedFluidType {

	private final String langKey;
	private final int alpha;

	private MetalFluidType(Properties properties, ResourceLocation still, ResourceLocation flow, String langKey, int alpha) {
		super(properties, still, flow);
		this.langKey = langKey;
		this.alpha = alpha;
	}

	/** @param langKey e.g. {@code fluid.createoscillation.ore_slurry.named} = "%s Slurry"; alpha 0xFF for liquids, lower for gases */
	public static FluidBuilder.FluidTypeFactory of(String langKey, int alpha) {
		return (properties, still, flow) -> new MetalFluidType(properties, still, flow, langKey, alpha);
	}

	@Override
	protected int getTintColor(FluidStack stack) {
		return (alpha << 24) | (Metals.colour(MetalStacks.metal(stack)) & 0xFFFFFF);
	}

	@Override
	protected int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
		return (alpha << 24) | Metals.DEFAULT_COLOUR;
	}

	@Override
	public Component getDescription(FluidStack stack) {
		String metal = MetalStacks.metal(stack);
		if (metal == null)
			return super.getDescription(stack);
		return Component.translatable(langKey, Metals.name(metal));
	}
}
