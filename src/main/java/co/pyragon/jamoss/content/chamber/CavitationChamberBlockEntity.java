package co.pyragon.jamoss.content.chamber;

import co.pyragon.jamoss.content.recipe.CavitatingRecipe;
import co.pyragon.jamoss.content.recipe.ResonatingRecipe;
import co.pyragon.jamoss.registry.CORecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CavitationChamberBlockEntity extends ResonanceChamberBlockEntity {

	public CavitationChamberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected RecipeType<? extends ResonatingRecipe> getRecipeType() {
		return CORecipeTypes.CAVITATING.<RecipeInput, CavitatingRecipe>getType();
	}
}
