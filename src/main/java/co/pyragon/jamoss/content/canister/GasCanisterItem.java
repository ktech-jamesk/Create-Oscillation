package co.pyragon.jamoss.content.canister;

import java.util.List;

import co.pyragon.jamoss.registry.CODataComponents;
import co.pyragon.jamoss.registry.COFluidTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

/** The gas "bucket": holds up to 1000 mb of any fluid tagged as a gas. */
public class GasCanisterItem extends Item {

	public static final int CAPACITY = 1000;

	public GasCanisterItem(Properties properties) {
		super(properties);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event, Item item) {
		event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new Handler(stack), item);
	}

	/** A full canister of the given gas. */
	public static ItemStack filled(Item canister, FluidStack gas) {
		ItemStack stack = new ItemStack(canister);
		FluidStack content = gas.copyWithAmount(CAPACITY);
		stack.set(CODataComponents.CANISTER_CONTENT.get(), SimpleFluidContent.copyOf(content));
		return stack;
	}

	/** Every gas a canister can hold, one full canister each (used by JEI and the creative tab). */
	public static java.util.List<ItemStack> allFilled(Item canister) {
		java.util.List<ItemStack> list = new java.util.ArrayList<>();
		list.add(filled(canister, new FluidStack((net.minecraft.world.level.material.Fluid) co.pyragon.jamoss.registry.COFluids.STEAM.getSource(), CAPACITY)));
		list.add(filled(canister, new FluidStack((net.minecraft.world.level.material.Fluid) co.pyragon.jamoss.registry.COFluids.QUARTZ_VAPOUR.getSource(), CAPACITY)));
		java.util.Set<String> metals = new java.util.LinkedHashSet<>();
		for (var entry : co.pyragon.jamoss.content.ore.Metals.all().values())
			metals.add(entry.metal());
		for (String metal : metals)
			list.add(filled(canister, co.pyragon.jamoss.content.ore.MetalStacks.vapour(metal, CAPACITY)));
		return list;
	}

	public static FluidStack getContent(ItemStack stack) {
		SimpleFluidContent content = stack.get(CODataComponents.CANISTER_CONTENT.get());
		return content == null ? FluidStack.EMPTY : content.copy();
	}

	/** Standard NeoForge container interaction, so tanks that don't handle items themselves (e.g. Create's) still work. */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null)
			return InteractionResult.PASS;
		if (FluidUtil.interactWithFluidHandler(player, context.getHand(), context.getLevel(), context.getClickedPos(), context.getClickedFace()))
			return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
		return InteractionResult.PASS;
	}

	@Override
	public Component getName(ItemStack stack) {
		FluidStack content = getContent(stack);
		if (content.isEmpty())
			return super.getName(stack);
		return Component.translatable("item.createoscillation.gas_canister.filled", content.getHoverName());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		FluidStack content = getContent(stack);
		if (content.isEmpty())
			tooltip.add(Component.translatable("item.createoscillation.gas_canister.empty").withStyle(ChatFormatting.GRAY));
		else
			tooltip.add(Component.literal(content.getAmount() + " / " + CAPACITY + " mb").withStyle(ChatFormatting.GRAY));
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return !getContent(stack).isEmpty();
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13f * getContent(stack).getAmount() / CAPACITY);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return 0x8FE3F0;
	}

	public static class Handler extends FluidHandlerItemStack {
		public Handler(ItemStack container) {
			super(CODataComponents.CANISTER_CONTENT, container, CAPACITY);
		}

		@Override
		public boolean canFillFluidType(FluidStack fluid) {
			return COFluidTags.isGas(fluid);
		}
	}
}
