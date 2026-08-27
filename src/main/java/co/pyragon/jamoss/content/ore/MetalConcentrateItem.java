package co.pyragon.jamoss.content.ore;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** "Iron Concentrate": one item for every metal, told apart by the {@code metal} component. */
public class MetalConcentrateItem extends Item {

	public MetalConcentrateItem(Properties properties) {
		super(properties);
	}

	@Override
	public Component getName(ItemStack stack) {
		String metal = MetalStacks.metal(stack);
		if (metal == null)
			return super.getName(stack);
		return Component.translatable("item.createoscillation.metal_concentrate.named", Metals.name(metal));
	}

	/** Layer 1 of the sprite is tinted with the metal colour. */
	public static int colour(ItemStack stack, int layer) {
		if (layer != 1)
			return -1;
		return 0xFF000000 | Metals.colour(MetalStacks.metal(stack));
	}
}
