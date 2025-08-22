package com.jesz.createdieselgenerators.content.bulk_fermenter;

import com.jesz.createdieselgenerators.CDGRecipes;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.item.SmartInventory;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.*;

public class BulkFermentingRecipe extends ProcessingRecipe<SmartInventory> {
    public BulkFermentingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params){
        super(CDGRecipes.BULK_FERMENTING, params);
    }
    @Override
    protected int getMaxInputCount() {
        return 9;
    }

    @Override
    protected int getMaxOutputCount() {
        return 4;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 2;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 2;
    }

    @Override
    protected boolean canRequireHeat() {
        return true;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(SmartInventory inventory, Level level) {
        return false;
    }

    public boolean test(IItemHandler container){
        if(container == null)
            return false;
        for (Ingredient ingredient : getIngredients()) {
            boolean valid = false;
            for (int i = 0; i < container.getSlots(); i++) {
                ItemStack stack = container.getStackInSlot(i);
                if (!ingredient.test(stack))
                    continue;
                ItemStack[] items = ingredient.getItems();
                if (items.length == 0 || items[0].getCount() > stack.getCount())
                    continue;
                valid = true;
            }
            if (!valid)
                return false;
        }
        return true;
    }

    public boolean test(BulkFermenterBlockEntity.BulkFermenterFluidHandler container){
        if(container == null)
            return false;
        for(FluidIngredient ingredient : getFluidIngredients()){
            boolean valid = false;
            for (int i = 0; i < container.getTanks(); i++) {
                FluidStack fluidInTank = container.getFluidInTank(i);
                if (ingredient.test(fluidInTank) && ingredient.getRequiredAmount() <= fluidInTank.getAmount()) {
                    valid = true;
                    break;
                }
            }
            if(!valid)
                return false;
        }
        return true;
    }

    public void remove(BulkFermenterBlockEntity.BulkFermenterFluidHandler container){
        if (container == null)
            return;
        for (FluidIngredient ingredient : getFluidIngredients()){
            for (int i = 0; i < container.getTanks(); i++) {
                FluidStack fluidInTank = container.getFluidInTank(i);
                if (ingredient.test(fluidInTank) && ingredient.getRequiredAmount() <= fluidInTank.getAmount()) {
                    FluidStack toDrain = fluidInTank.copy();
                    toDrain.setAmount(ingredient.getRequiredAmount());
                    container.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                    break;
                }
            }
        }
    }
    public void remove(IItemHandler container){
        if(container == null)
            return;
        for (Ingredient ingredient : getIngredients()){
            for (int i = 0; i < container.getSlots(); i++) {
                ItemStack stack = container.getStackInSlot(i);
                if(!ingredient.test(stack))
                    continue;
                ItemStack[] items = ingredient.getItems();
                if(items.length == 0 || items[0].getCount() > stack.getCount())
                    continue;
                container.extractItem(i, items[0].getCount(), false);
                break;
            }
        }
    }

    public boolean apply(BulkFermenterBlockEntity be, boolean test) {
        IItemHandler availableItems = be.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .orElse(null);
        IFluidHandler fluidCap = be.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElse(null);

        if (availableItems == null || fluidCap == null ||
                !(fluidCap instanceof BulkFermenterBlockEntity.BulkFermenterFluidHandler availableFluids))
            return false;


        BlazeBurnerBlock.HeatLevel heat = be.highestHeatLevel;
        if (!getRequiredHeat().testBlazeBurner(heat))
            return false;

        List<ItemStack> recipeOutputItems = new ArrayList<>();
        List<FluidStack> recipeOutputFluids = new ArrayList<>();

        List<Ingredient> ingredients = new LinkedList<>(getIngredients());
        List<FluidIngredient> fluidIngredients = getFluidIngredients();

        for (boolean simulate : Iterate.trueAndFalse) {

            if (!simulate && test)
                return true;

            int[] extractedItemsFromSlot = new int[availableItems.getSlots()];
            int[] extractedFluidsFromTank = new int[availableFluids.getTanks()];

            Ingredients:
            for (Ingredient ingredient : ingredients) {
                for (int slot = 0; slot < availableItems.getSlots(); slot++) {
                    if (simulate && availableItems.getStackInSlot(slot)
                            .getCount() <= extractedItemsFromSlot[slot])
                        continue;

                    ItemStack extracted = availableItems.extractItem(slot, 1, true);
                    if (!ingredient.test(extracted))
                        continue;
                    if (!simulate)
                        availableItems.extractItem(slot, 1, false);
                    extractedItemsFromSlot[slot]++;
                    continue Ingredients;
                }

                return false;
            }

            boolean fluidsAffected = false;
            FluidIngredients:
            for (FluidIngredient fluidIngredient : fluidIngredients) {
                int amountRequired = fluidIngredient.getRequiredAmount();

                for (int tank = 0; tank < availableFluids.getTanks(); tank++) {
                    FluidStack fluidStack = availableFluids.getFluidInTank(tank);
                    if (simulate && fluidStack.getAmount() <= extractedFluidsFromTank[tank])
                        continue;
                    if (!fluidIngredient.test(fluidStack))
                        continue;
                    int drainedAmount = Math.min(amountRequired, fluidStack.getAmount());
                    if (!simulate) {
                        fluidStack.shrink(drainedAmount);
                        fluidsAffected = true;
                    }
                    amountRequired -= drainedAmount;
                    if (amountRequired != 0)
                        continue;
                    extractedFluidsFromTank[tank] += drainedAmount;
                    continue FluidIngredients;
                }

                return false;
            }

            if (fluidsAffected)
                be.onFluidStackChanged();

            if (simulate) {
                recipeOutputItems.addAll(rollResults());

                for (FluidStack fluidStack : getFluidResults())
                    if (!fluidStack.isEmpty())
                        recipeOutputFluids.add(fluidStack);
            }

            if (!applyOutputs(be, recipeOutputItems, recipeOutputFluids, simulate))
                return false;
        }

        return true;
    }

    private boolean applyOutputs(BulkFermenterBlockEntity be, List<ItemStack> outputItems, List<FluidStack> outputFluids, boolean test) {
        IItemHandler availableItems = be.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .orElse(null);
        IFluidHandler fluidCap = be.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElse(null);

        if (availableItems == null || fluidCap == null ||
                !(fluidCap instanceof BulkFermenterBlockEntity.BulkFermenterFluidHandler availableFluids))
            return false;

        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < availableItems.getSlots(); i++)
            items.add(availableItems.getStackInSlot(i).copy());


        for (ProcessingOutput result : getRollableResults()) {
            ItemStack stack = result.getStack().copy();

            int left = stack.getCount();
            for (ItemStack slot : items) {
                if (slot.getItem() == stack.getItem() && Objects.equals(slot.getTag(), stack.getTag())) {
                    if ((availableItems.getSlotLimit(0) - slot.getCount()) >= left) {
                        left = 0;
                        break;
                    } else
                        return false;
                }
            }

            if (left > 0) {
                for (ItemStack slot : items) {
                    if (slot.isEmpty()) {
                        left = 0;
                        break;
                    }
                }
                if (left > 0)
                    return false;
            }
        }

        boolean[] emptyTanksFilled = new boolean[availableFluids.tankCount];
        for (FluidStack result : getFluidResults()) {
            result = result.copy();

            boolean filled = false;
            for (FluidTank tank : availableFluids.tanks) {
                if (tank.getFluid().isFluidEqual(result)) {
                    if (tank.fill(result, IFluidHandler.FluidAction.SIMULATE) < result.getAmount())
                        return false;
                    else
                        filled = true;
                }
            }

            if (!filled) {
                NonNullList<FluidTank> tanks = availableFluids.tanks;
                for (int i = 0; i < tanks.size(); i++) {
                    FluidTank tank = tanks.get(i);

                    if (tank.getFluid().isEmpty() && !emptyTanksFilled[i]) {
                        if (tank.fill(result, IFluidHandler.FluidAction.SIMULATE) < result.getAmount())
                            return false;
                        else
                            emptyTanksFilled[i] = true;
                    }
                }
            }
        }

        if (test)
            return true;

        for (ItemStack stack : outputItems) {
            stack = stack.copy();
            ItemHandlerHelper.insertItemStacked(availableItems, stack, false);
        }

        for (FluidStack output : outputFluids)
            availableFluids.fill(output.copy(), IFluidHandler.FluidAction.EXECUTE);

        return true;
    }
}
