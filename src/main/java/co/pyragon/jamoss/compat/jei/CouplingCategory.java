package co.pyragon.jamoss.compat.jei;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.coupler.ResonanceEmitterBlockEntity;
import co.pyragon.jamoss.content.frequency.FrequencyBand;
import co.pyragon.jamoss.registry.COBlocks;
import co.pyragon.jamoss.registry.COItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * One entry per Tuned Crystal: emitter + receiver holding it, the band the Resonator must run in,
 * and how far the beam reaches. Not a real recipe; JEI shows it under both coupler blocks.
 */
public class CouplingCategory implements IRecipeCategory<CouplingCategory.Entry> {

	public record Entry(FrequencyBand band) {}

	public static final RecipeType<Entry> TYPE = RecipeType.create(CreateOscillation.MOD_ID, "coupling", Entry.class);
	private static final int WIDTH = 177, HEIGHT = 56;
	private static final float TEXT_SCALE = 0.7f;

	private final IDrawable background;
	private final IDrawable icon;

	public CouplingCategory(IGuiHelper helper) {
		background = helper.createBlankDrawable(WIDTH, HEIGHT);
		icon = helper.createDrawableItemLike(COBlocks.RESONANCE_EMITTER.get());
	}

	public static List<Entry> entries() {
		List<Entry> list = new ArrayList<>();
		for (FrequencyBand band : FrequencyBand.values())
			if (band != FrequencyBand.ANY)
				list.add(new Entry(band));
		return list;
	}

	@Override
	public RecipeType<Entry> getRecipeType() {
		return TYPE;
	}

	@Override
	public Component getTitle() {
		return Component.translatable("createoscillation.recipe.coupling");
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, Entry entry, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 27, 8)
			.setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
			.addItemStack(COItems.tunedCrystal(entry.band()).asStack());
		builder.addSlot(RecipeIngredientRole.CATALYST, 50, 8)
			.setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
			.addItemStack(COBlocks.RESONANCE_EMITTER.asStack());
		builder.addSlot(RecipeIngredientRole.CATALYST, 152, 8)
			.setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
			.addItemStack(COBlocks.RESONANCE_RECEIVER.asStack());
	}

	@Override
	public void draw(Entry entry, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
		com.simibubi.create.foundation.gui.AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52 + 22, 12);
		var font = Minecraft.getInstance().font;
		Component band = Component.translatable("createoscillation.recipe.coupling.band", entry.band().getDisplayName())
			.withStyle(ChatFormatting.DARK_GRAY);
		Component range = Component.translatable("createoscillation.recipe.coupling.range", ResonanceEmitterBlockEntity.rangeFor(entry.band()))
			.withStyle(ChatFormatting.DARK_GRAY);
		drawScaled(graphics, font, band, TEXT_SCALE, 33);
		drawScaled(graphics, font, range, TEXT_SCALE, 42);
	}

	/** Centred text at a fraction of the normal font size (the font itself has one size). */
	private static void drawScaled(GuiGraphics graphics, net.minecraft.client.gui.Font font, Component text, float scale, int y) {
		var ms = graphics.pose();
		ms.pushPose();
		ms.translate((WIDTH - font.width(text) * scale) / 2f, y, 0);
		ms.scale(scale, scale, 1);
		graphics.drawString(font, text, 0, 0, 0xFFFFFF, false);
		ms.popPose();
	}
}
