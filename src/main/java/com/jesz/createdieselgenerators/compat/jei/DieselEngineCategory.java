package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.Lang;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
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

import javax.annotation.ParametersAreNonnullByDefault;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.translate;
import static com.simibubi.create.compat.jei.category.CreateRecipeCategory.*;

@ParametersAreNonnullByDefault
public class DieselEngineCategory implements IRecipeCategory<DieselEngineJeiRecipeType> {
    IGuiHelper guiHelper;
    AnimatedDieselEngineElement engine = new AnimatedDieselEngineElement();
    public DieselEngineCategory(IGuiHelper helper) {
        this.guiHelper = helper;

    }

    @Override
    public RecipeType<DieselEngineJeiRecipeType> getRecipeType() {
        return DieselEngineJeiRecipeType.DIESEL_BURNING;
    }

    @Override
    public Component getTitle() {
        return translate("createdieselgenerators.recipe.diesel_combustion");
    }

    @Override
    public IDrawable getBackground() {
        return new EmptyBackground(177,70);
    }

    @Override
    public IDrawable getIcon() {
        return guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, BlockRegistry.DIESEL_ENGINE.asStack());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DieselEngineJeiRecipeType recipe, IFocusGroup iFocusGroup) {


        builder
                .addSlot(RecipeIngredientRole.INPUT, 10, 10)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(ForgeTypes.FLUID_STACK, withImprovedVisibility(recipe.fluids))
                .addTooltipCallback(addFluidTooltip(1000));
    }

    @Override
    public void draw(DieselEngineJeiRecipeType recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {

        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 40, 15);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 75, 40);
        AllGuiTextures.JEI_SHADOW.render(graphics, 28, 52);
        AllIcons.I_REFRESH.render(graphics, 145, 36);
        graphics.drawString(Minecraft.getInstance().font, Lang.number(recipe.burnRate).component().append(Component.translatable("createdieselgenerators.generic.unit.mbps")).withStyle(ChatFormatting.BOLD), 81,
                15, 0xaaaaaa, false);
        graphics.drawString(Minecraft.getInstance().font, Lang.number(recipe.burnRate).component().append(Component.translatable("createdieselgenerators.generic.unit.mbps")).withStyle(ChatFormatting.BOLD), 81,
                14, 0xeeeeee, false);
        graphics.drawString(Minecraft.getInstance().font, Lang.number(recipe.speed).component().append(Component.translatable("create.generic.unit.rpm")).withStyle(ChatFormatting.BOLD), 81,
                33, 0xaaaaaa, false);
        graphics.drawString(Minecraft.getInstance().font, Lang.number(recipe.stress).component().append(Component.translatable("create.generic.unit.stress")).withStyle(ChatFormatting.BOLD), 81,
                50, 0xaaaaaa, false);
        graphics.drawString(Minecraft.getInstance().font, Lang.number(recipe.speed).component().append(Component.translatable("create.generic.unit.rpm")).withStyle(ChatFormatting.BOLD), 80,
                32, 0xeeeeee, false);
        graphics.drawString(Minecraft.getInstance().font, Lang.number(recipe.stress).component().append(Component.translatable("create.generic.unit.stress")).withStyle(ChatFormatting.BOLD), 80,
                49, 0xeeeeee, false);
        engine.draw(graphics, 47, 62);

    }
}