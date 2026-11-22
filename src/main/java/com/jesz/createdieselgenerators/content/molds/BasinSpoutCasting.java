package com.jesz.createdieselgenerators.content.molds;

import com.jesz.createdieselgenerators.CDGRecipes;
import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class BasinSpoutCasting implements BlockSpoutingBehaviour {
    @Override
    public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
        BasinBlockEntity basin;
        if (level.getBlockEntity(pos) instanceof BasinBlockEntity be && be.canContinueProcessing())
            basin = be;
        else
            return 0;

        List<Recipe<RecipeInput>> recipes = level.getRecipeManager().getAllRecipesFor(CDGRecipes.CASTING.getType()).stream()
                .map(RecipeHolder::value)
                .filter(r -> r instanceof CastingRecipe cr && cr.matches(basin, availableFluid) && cr.getFluidIngredients().get(0).amount() <= availableFluid.getAmount())
                .toList();

        if (recipes.isEmpty())
            return 0;

        CastingRecipe recipe = (CastingRecipe) recipes.get(0);
        return recipe.execute(basin, simulate);
    }
}
