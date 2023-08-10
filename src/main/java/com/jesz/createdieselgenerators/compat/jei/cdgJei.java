package com.jesz.createdieselgenerators.compat.jei;

import com.google.common.collect.ImmutableList;
import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.recipes.RecipeRegistry;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.*;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.equipment.blueprint.BlueprintScreen;
import com.simibubi.create.content.logistics.filter.AbstractFilterScreen;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerScreen;
import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import com.simibubi.create.foundation.config.ConfigBase;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.translate;
import static com.simibubi.create.AllTags.optionalTag;
import static com.simibubi.create.compat.jei.CreateJEI.*;

@JeiPlugin
@ParametersAreNonnullByDefault
public class CDGJEI implements IModPlugin {

    private static final ResourceLocation ID = new ResourceLocation("createdieselgenerators", "jei_plugin");
    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }


    private final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();

    private void loadCategories() {
        allCategories.clear();

        CreateRecipeCategory<?>
        basin_fermenting = builder(BasinRecipe.class)
                .addTypedRecipes(RecipeRegistry.BASIN_FERMENTING)
                .catalyst(BlockRegistry.BASIN_LID::get)
                .catalyst(AllBlocks.BASIN::get)
                .doubleItemIcon(AllBlocks.BASIN.get(), BlockRegistry.BASIN_LID.get())
                .emptyBackground(177, 100)
                .build("basin_fermenting", BasinFermentingCategory::new);
    }

    private <T extends Recipe<?>> CategoryBuilder<T> builder(Class<? extends T> recipeClass) {
        return new CategoryBuilder<>(recipeClass);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new DieselEngineCategory(registration.getJeiHelpers().getGuiHelper()));
        loadCategories();
        registration.addRecipeCategories(allCategories.toArray(IRecipeCategory[]::new));
    }
    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        List<FluidStack> fluids;

        //
        fluids = FluidIngredient.fromTag(optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:diesel_engine_fuel_slow_strong")), 1000).getMatchingFluidStacks();
            if(ConfigRegistry.PLANTOIL_TAG.get())
                fluids.addAll(FluidIngredient.fromTag(optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("forge:plantoil")), 1000).getMatchingFluidStacks());
            if(!fluids.isEmpty())
                registration.addRecipes(DieselEngineJeiRecipeType.DIESEL_BURNING, ImmutableList.of( new DieselEngineJeiRecipeType(0, ConfigRegistry.SLOW_SPEED.get().floatValue(),  ConfigRegistry.STRONG_STRESS.get().floatValue(), fluids)));
        //
        fluids = FluidIngredient.fromTag(optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:diesel_engine_fuel_slow_weak")), 1000).getMatchingFluidStacks();
            if(!fluids.isEmpty())
                registration.addRecipes(DieselEngineJeiRecipeType.DIESEL_BURNING, ImmutableList.of(new DieselEngineJeiRecipeType(1, ConfigRegistry.SLOW_SPEED.get().floatValue(),  ConfigRegistry.WEAK_STRESS.get().floatValue(), fluids)));
        //
        fluids = FluidIngredient.fromTag(optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:diesel_engine_fuel_fast_strong")), 1000).getMatchingFluidStacks();
            if(ConfigRegistry.FUEL_TAG.get())
                fluids.addAll(FluidIngredient.fromTag(optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("forge:fuel")), 1000).getMatchingFluidStacks());
            if(ConfigRegistry.BIODIESEL_TAG.get())
                fluids.addAll(FluidIngredient.fromTag(optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("forge:biodiesel")), 1000).getMatchingFluidStacks());
            if(!fluids.isEmpty())
                registration.addRecipes(DieselEngineJeiRecipeType.DIESEL_BURNING, ImmutableList.of(new DieselEngineJeiRecipeType(2, ConfigRegistry.FAST_SPEED.get().floatValue(),  ConfigRegistry.STRONG_STRESS.get().floatValue(), fluids)));
        //
        fluids = FluidIngredient.fromTag(optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:diesel_engine_fuel_fast_weak")), 1000).getMatchingFluidStacks();
            if(ConfigRegistry.ETHANOL_TAG.get())
                fluids.addAll(FluidIngredient.fromTag(optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("forge:ethanol")), 1000).getMatchingFluidStacks());
            if(!fluids.isEmpty())
                registration.addRecipes(DieselEngineJeiRecipeType.DIESEL_BURNING, ImmutableList.of(new DieselEngineJeiRecipeType(3, ConfigRegistry.FAST_SPEED.get().floatValue(),  ConfigRegistry.WEAK_STRESS.get().floatValue(), fluids)));

        allCategories.forEach(c -> c.registerRecipes(registration));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        allCategories.forEach(c -> c.registerCatalysts(registration));
        registration.addRecipeCatalyst(BlockRegistry.DIESEL_ENGINE.asStack(), DieselEngineJeiRecipeType.DIESEL_BURNING);
        registration.addRecipeCatalyst(BlockRegistry.MODULAR_DIESEL_ENGINE.asStack(), DieselEngineJeiRecipeType.DIESEL_BURNING);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AbstractSimiContainerScreen.class, new SlotMover());

        registration.addGhostIngredientHandler(AbstractFilterScreen.class, new GhostIngredientHandler());
        registration.addGhostIngredientHandler(BlueprintScreen.class, new GhostIngredientHandler());
        registration.addGhostIngredientHandler(LinkedControllerScreen.class, new GhostIngredientHandler());
        registration.addGhostIngredientHandler(ScheduleScreen.class, new GhostIngredientHandler());
    }

    private class CategoryBuilder<T extends Recipe<?>> {
        private final Class<? extends T> recipeClass;
        private Predicate<CRecipes> predicate = cRecipes -> true;

        private IDrawable background;
        private IDrawable icon;

        private final List<Consumer<List<T>>> recipeListConsumers = new ArrayList<>();
        private final List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();

        public CategoryBuilder(Class<? extends T> recipeClass) {
            this.recipeClass = recipeClass;
        }

        public CategoryBuilder<T> enableIf(Predicate<CRecipes> predicate) {
            this.predicate = predicate;
            return this;
        }

        public CategoryBuilder<T> enableWhen(Function<CRecipes, ConfigBase.ConfigBool> configValue) {
            predicate = c -> configValue.apply(c).get();
            return this;
        }

        public CategoryBuilder<T> addRecipeListConsumer(Consumer<List<T>> consumer) {
            recipeListConsumers.add(consumer);
            return this;
        }

        public CategoryBuilder<T> addRecipes(Supplier<Collection<? extends T>> collection) {
            return addRecipeListConsumer(recipes -> recipes.addAll(collection.get()));
        }

        public CategoryBuilder<T> addAllRecipesIf(Predicate<Recipe<?>> pred) {
            return addRecipeListConsumer(recipes -> consumeAllRecipes(recipe -> {
                if (pred.test(recipe)) {
                    recipes.add((T) recipe);
                }
            }));
        }

        public CategoryBuilder<T> addAllRecipesIf(Predicate<Recipe<?>> pred, Function<Recipe<?>, T> converter) {
            return addRecipeListConsumer(recipes -> consumeAllRecipes(recipe -> {
                if (pred.test(recipe)) {
                    recipes.add(converter.apply(recipe));
                }
            }));
        }

        public CategoryBuilder<T> addTypedRecipes(IRecipeTypeInfo recipeTypeEntry) {
            return addTypedRecipes(recipeTypeEntry::getType);
        }

        public CategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipes::add, recipeType.get()));
        }

        public CategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType, Function<Recipe<?>, T> converter) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipe -> recipes.add(converter.apply(recipe)), recipeType.get()));
        }

        public CategoryBuilder<T> addTypedRecipesIf(Supplier<RecipeType<? extends T>> recipeType, Predicate<Recipe<?>> pred) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipe -> {
                if (pred.test(recipe)) {
                    recipes.add(recipe);
                }
            }, recipeType.get()));
        }

        public CategoryBuilder<T> addTypedRecipesExcluding(Supplier<RecipeType<? extends T>> recipeType,
                                                                     Supplier<RecipeType<? extends T>> excluded) {
            return addRecipeListConsumer(recipes -> {
                List<Recipe<?>> excludedRecipes = getTypedRecipes(excluded.get());
                CreateJEI.<T>consumeTypedRecipes(recipe -> {
                    for (Recipe<?> excludedRecipe : excludedRecipes) {
                        if (doInputsMatch(recipe, excludedRecipe)) {
                            return;
                        }
                    }
                    recipes.add(recipe);
                }, recipeType.get());
            });
        }

        public CategoryBuilder<T> removeRecipes(Supplier<RecipeType<? extends T>> recipeType) {
            return addRecipeListConsumer(recipes -> {
                List<Recipe<?>> excludedRecipes = getTypedRecipes(recipeType.get());
                recipes.removeIf(recipe -> {
                    for (Recipe<?> excludedRecipe : excludedRecipes)
                        if (doInputsMatch(recipe, excludedRecipe) && doOutputsMatch(recipe, excludedRecipe))
                            return true;
                    return false;
                });
            });
        }

        public CategoryBuilder<T> catalystStack(Supplier<ItemStack> supplier) {
            catalysts.add(supplier);
            return this;
        }

        public CategoryBuilder<T> catalyst(Supplier<ItemLike> supplier) {
            return catalystStack(() -> new ItemStack(supplier.get()
                    .asItem()));
        }

        public CategoryBuilder<T> icon(IDrawable icon) {
            this.icon = icon;
            return this;
        }

        public CategoryBuilder<T> itemIcon(ItemLike item) {
            icon(new ItemIcon(() -> new ItemStack(item)));
            return this;
        }

        public CategoryBuilder<T> doubleItemIcon(ItemLike item1, ItemLike item2) {
            icon(new DoubleItemIcon(() -> new ItemStack(item1), () -> new ItemStack(item2)));
            return this;
        }

        public CategoryBuilder<T> background(IDrawable background) {
            this.background = background;
            return this;
        }

        public CategoryBuilder<T> emptyBackground(int width, int height) {
            background(new EmptyBackground(width, height));
            return this;
        }

        public CreateRecipeCategory<T> build(String name, CreateRecipeCategory.Factory<T> factory) {
            Supplier<List<T>> recipesSupplier;
            if (predicate.test(AllConfigs.server().recipes)) {
                recipesSupplier = () -> {
                    List<T> recipes = new ArrayList<>();
                    for (Consumer<List<T>> consumer : recipeListConsumers)
                        consumer.accept(recipes);
                    return recipes;
                };
            } else {
                recipesSupplier = () -> Collections.emptyList();
            }

            CreateRecipeCategory.Info<T> info = new CreateRecipeCategory.Info<>(
                    new mezz.jei.api.recipe.RecipeType<>(new ResourceLocation("createdieselgenerators", name), recipeClass),
                    translate("createdieselgenerators.recipe." + name), background, icon, recipesSupplier, catalysts);
            CreateRecipeCategory<T> category = factory.create(info);
            allCategories.add(category);
            return category;
        }
    }
}
