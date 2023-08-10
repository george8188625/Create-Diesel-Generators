package com.jesz.createdieselgenerators.recipes;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import org.slf4j.Logger;

public class BasinFermentingRecipe extends BasinRecipe {
    public BasinFermentingRecipe(ProcessingRecipeParams params) {
        super(RecipeRegistry.BASIN_FERMENTING, params);
    }
}
