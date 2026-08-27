package co.pyragon.jamoss.datagen;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.StandardProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.content.recipe.SiftingRecipe;
import co.pyragon.jamoss.registry.CORecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

public class COSiftingRecipeGen extends StandardProcessingRecipeGen<SiftingRecipe> {

	GeneratedRecipe GRAVEL = create("gravel", b -> b.require(Items.GRAVEL)
		.output(.3f, Items.FLINT)
		.output(.12f, Items.IRON_NUGGET)
		.output(.12f, AllItems.COPPER_NUGGET.get())
		.output(.08f, AllItems.ZINC_NUGGET.get())
		.duration(120));

	GeneratedRecipe SAND = create("sand", b -> b.require(Items.SAND)
		.output(.08f, Items.QUARTZ)
		.output(.05f, Items.BONE_MEAL)
		.output(.03f, Items.GOLD_NUGGET)
		.duration(120));

	GeneratedRecipe RED_SAND = create("red_sand", b -> b.require(Items.RED_SAND)
		.output(.06f, Items.GOLD_NUGGET)
		.output(.05f, Items.REDSTONE)
		.output(.05f, Items.QUARTZ)
		.duration(120));

	GeneratedRecipe SOUL_SAND = create("soul_sand", b -> b.require(Items.SOUL_SAND)
		.output(.2f, Items.QUARTZ)
		.output(.05f, Items.BONE)
		.output(.02f, Items.GLOWSTONE_DUST)
		.duration(160));

	public COSiftingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, CreateOscillation.MOD_ID);
	}

	@Override
	protected IRecipeTypeInfo getRecipeType() {
		return CORecipeTypes.SIFTING;
	}
}
