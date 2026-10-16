package com.jesz.createdieselgenerators.content.bulk_fermenter;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class BulkFermenterInventoryWrapper implements IItemHandlerModifiable {
    private IItemHandlerModifiable itemHandler;

    public void setItemHandler(IItemHandlerModifiable itemHandler) {
        this.itemHandler = itemHandler;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (itemHandler != null)
            itemHandler.setStackInSlot(slot, stack);
    }

    @Override
    public int getSlots() {
        return itemHandler == null ? 0 : itemHandler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return itemHandler == null
                ? ItemStack.EMPTY
                : itemHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return itemHandler == null
                ? stack
                : itemHandler.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return itemHandler == null
                ? ItemStack.EMPTY
                : itemHandler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return itemHandler == null
                ? 0
                : itemHandler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return itemHandler != null
                && itemHandler.isItemValid(slot, stack);
    }
}
