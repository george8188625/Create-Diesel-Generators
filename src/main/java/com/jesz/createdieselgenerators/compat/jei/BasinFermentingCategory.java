package com.jesz.createdieselgenerators.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedMillstone;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.Pair;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class BasinFermentingCategory extends CreateRecipeCategory<BasinRecipe> {

    private final BasinFermentingStationElement BasinFermentingStation = new BasinFermentingStationElement();

    public BasinFermentingCategory(Info<BasinRecipe> info) {
        super(info);
    }


@Override
public void setRecipe(IRecipeLayoutBuilder builder, BasinRecipe recipe, IFocusGroup focuses) {
    List<Pair<Ingredient, MutableInt>> condensedIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());

    int size = condensedIngredients.size() + recipe.getFluidIngredients().size();
    int xOffset = size < 2 ? (2 - size) * 19 / 2 : 0;
    int i = 0;

    for (Pair<Ingredient, MutableInt> pair : condensedIngredients) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack itemStack : pair.getFirst().getItems()) {
            ItemStack copy = itemStack.copy();
            copy.setCount(pair.getSecond().getValue());
            stacks.add(copy);
        }

        int xPosition = 20 - (size % 2 != 0 && i == size - 1 ? 0 : i % 2 == 0 ? 10 : -9);
        int yPosition = -19 * (i / 2) + 20;

        builder
                .addSlot(RecipeIngredientRole.INPUT, xPosition, yPosition)
                .setBackground(getRenderedSlot(), -1, -1)
                .addItemStacks(stacks);
        i++;
    }
    for (FluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
        int xPosition = 20 - (size % 2 != 0 && i == size - 1 ? 0 : i % 2 == 0 ? 10 : -9);
        int yPosition = -19 * (i / 2) + 20;
        builder
                .addSlot(RecipeIngredientRole.INPUT, xPosition, yPosition)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(ForgeTypes.FLUID_STACK, withImprovedVisibility(fluidIngredient.getMatchingFluidStacks()))
                .addTooltipCallback(addFluidTooltip(fluidIngredient.getRequiredAmount()));
        i++;
    }

    size = recipe.getRollableResults().size() + recipe.getFluidResults().size();
    i = 0;

    for (ProcessingOutput result : recipe.getRollableResults()) {
        int xPosition = 142 - (size % 2 != 0 && i == size - 1 ? 0 : i % 2 == 0 ? 10 : -9);
        int yPosition = -20 * (i / 2) + 30;

        builder
                .addSlot(RecipeIngredientRole.OUTPUT, xPosition, yPosition)
                .setBackground(getRenderedSlot(result), -1, -1)
                .addItemStack(result.getStack())
                .addTooltipCallback(addStochasticTooltip(result));
        i++;
    }

    for (FluidStack fluidResult : recipe.getFluidResults()) {
        int xPosition = 142 - (size % 2 != 0 && i == size - 1 ? 0 : i % 2 == 0 ? 10 : -9);
        int yPosition = -20 * (i / 2) + 30;

        builder
                .addSlot(RecipeIngredientRole.OUTPUT, xPosition, yPosition)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredient(ForgeTypes.FLUID_STACK, withImprovedVisibility(fluidResult))
                .addTooltipCallback(addFluidTooltip(fluidResult.getAmount()));
        i++;
    }
}
    @Override
    public void draw(BasinRecipe recipe, IRecipeSlotsView iRecipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
        AllGuiTextures.JEI_ARROW.render(matrixStack, 85, 32);
        AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 50, 4);
        BasinFermentingStation.draw(matrixStack, 50, 27);
    }

}