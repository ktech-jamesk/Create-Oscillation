package co.pyragon.jamoss.content.chamber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRenderer;

import co.pyragon.jamoss.registry.COPartialModels;
import net.neoforged.neoforge.fluids.FluidStack;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import co.pyragon.jamoss.registry.COFluidTags;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

/** Draws the chamber body (shivering while the Resonator works), then Create's basin contents. */
public class ResonanceChamberRenderer extends BasinRenderer {

	public ResonanceChamberRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(BasinBlockEntity basin, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		Direction facing = basin.getBlockState().getValue(BasinBlock.FACING);
		boolean directional = facing != Direction.DOWN;
		SuperByteBuffer body = CachedBuffers.partial(
			directional ? directionalBody() : body(), basin.getBlockState());
		if (directional)
			body.center().rotateYDegrees(yRotation(facing)).uncenter();

		if (basin.areFluidsMoving()) {
			float time = AnimationTickHolder.getRenderTime(basin.getLevel());
			body.translate(Mth.sin(time * 2.3f) * (1 / 96f), 0, Mth.cos(time * 1.7f) * (1 / 96f));
		}
		body.light(light).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		super.renderSafe(basin, partialTicks, ms, buffer, light, overlay);
	}

	protected PartialModel body() {
		return COPartialModels.CHAMBER_BODY;
	}

	protected PartialModel directionalBody() {
		return COPartialModels.CHAMBER_BODY_DIRECTIONAL;
	}

	/**
	 * Liquids pool at the bottom as in Create's basin; gases hang from the dome downward. Each group
	 * is laid out side by side across the chamber. Returns the liquid level (items float on it).
	 */
	@Override
	protected float renderFluids(BasinBlockEntity basin, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		SmartFluidTankBehaviour[] tanks = { basin.getBehaviour(SmartFluidTankBehaviour.INPUT), basin.getBehaviour(SmartFluidTankBehaviour.OUTPUT) };
		float liquidUnits = 0, gasUnits = 0;
		for (SmartFluidTankBehaviour behaviour : tanks) {
			if (behaviour == null)
				continue;
			for (TankSegment segment : behaviour.getTanks()) {
				FluidStack fluid = segment.getRenderedFluid();
				if (fluid.isEmpty())
					continue;
				float units = segment.getTotalUnits(partialTicks);
				if (COFluidTags.isGas(fluid))
					gasUnits += units;
				else
					liquidUnits += units;
			}
		}

		final float bottom = 2 / 16f, top = 14 / 16f, zMin = 2 / 16f, zMax = 14 / 16f;
		float liquidTop = bottom;
		if (liquidUnits >= 1) {
			float level = curve(liquidUnits);
			liquidTop = bottom + 12 / 16f * level;
			renderGroup(tanks, partialTicks, ms, buffer, light, false, liquidUnits, bottom, liquidTop, zMin, zMax);
		}
		if (gasUnits >= 1) {
			float level = curve(gasUnits);
			float gasBottom = Math.max(liquidTop, top - 12 / 16f * level);
			renderGroup(tanks, partialTicks, ms, buffer, light, true, gasUnits, gasBottom, top, zMin, zMax);
		}
		return liquidTop;
	}

	private static float curve(float units) {
		float level = Mth.clamp(units / 2000, 0, 1);
		return 1 - ((1 - level) * (1 - level));
	}

	private static void renderGroup(SmartFluidTankBehaviour[] tanks, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, boolean gases, float totalUnits, float yMin, float yMax, float zMin, float zMax) {
		float xMin = 2 / 16f, xMax = 2 / 16f;
		for (SmartFluidTankBehaviour behaviour : tanks) {
			if (behaviour == null)
				continue;
			for (TankSegment segment : behaviour.getTanks()) {
				FluidStack fluid = segment.getRenderedFluid();
				if (fluid.isEmpty() || COFluidTags.isGas(fluid) != gases)
					continue;
				float units = segment.getTotalUnits(partialTicks);
				if (units < 1)
					continue;
				xMax += Mth.clamp(units / totalUnits, 0, 1) * 12 / 16f;
				NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluid, xMin, yMin, zMin, xMax, yMax, zMax, buffer, ms, light, false, false);
				xMin = xMax;
			}
		}
	}

	/** Matches the y rotations the blockstate used for the directional model. */
	private static float yRotation(Direction facing) {
		return switch (facing) {
			case EAST -> 270;
			case NORTH -> 180;
			case WEST -> 90;
			default -> 0;
		};
	}
}
