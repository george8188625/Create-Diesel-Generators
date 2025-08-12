package com.jesz.createdieselgenerators.content.tools;

import com.jesz.createdieselgenerators.CDGConfig;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface FueledToolItem {
    default int getBaseCapacity(ItemStack stack){
        return CDGConfig.TOOL_CAPACITY.get();
    }

    default int getCapacityEnchantmentAddition(ItemStack stack){
        return CDGConfig.TOOL_CAPACITY_ENCHANTMENT.get();
    }

    default int getCapacity(ItemStack stack) {
        if (!ModList.get().isLoaded("dungeons_libraries"))
            return getBaseCapacity(stack) + (getCapacityEnchantmentAddition(stack) * stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get()));
        return getBaseCapacity(stack);

    }

    default FluidStack readFluid(ItemStack stack){
        return FluidStack.loadFluidStackFromNBT(stack.getOrCreateTag().getCompound("Fluid"));
    }

    default void writeFluid(ItemStack stack, FluidStack fluid){
        stack.getOrCreateTag().put("Fluid", fluid.writeToNBT(new CompoundTag()));
    }

    default int getCurrentFillLevel(ItemStack stack){
        return readFluid(stack).getAmount();
    }

    default void createTooltip(List<Component> tooltip, ItemStack stack) {
        if (stack.getTag() != null) {
            FluidStack fluid = readFluid(stack);
            if(fluid.isEmpty()){
                tooltip.add(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));
                return;
            }
            tooltip.add(CreateLang.fluidName(fluid).component()
                    .withStyle(ChatFormatting.GRAY)
                    .append(" ")
                    .append(CreateLang.number(fluid.getAmount()).style(ChatFormatting.GOLD).component())
                    .append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" / "))
                    .append(CreateLang.number(getCapacity(stack)).style(ChatFormatting.GRAY).component())
                    .append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GRAY)));
            return;
        }
        tooltip.add(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));
    }

    default FluidHandlerItemStack getFluidHandler(ItemStack stack) {
        return new ToolItemFluidHandler(stack, getCapacity(stack), this::readFluid, this::writeFluid);
    }

    class ToolItemFluidHandler extends FluidHandlerItemStack {
        BiConsumer<ItemStack, FluidStack> write;
        Function<ItemStack, FluidStack> read;
        public ToolItemFluidHandler(ItemStack container, int capacity, Function<ItemStack, FluidStack> read, BiConsumer<ItemStack, FluidStack> write) {
            super(container, capacity);
            this.write = write;
            this.read = read;
        }
        @Override
        public FluidStack getFluid() {
            return read.apply(container);
        }

        @Override
        protected void setContainerToEmpty() {
            write.accept(container, FluidStack.EMPTY);

        }

        @Override
        protected void setFluid(FluidStack fluid) {
            write.accept(container, fluid);
        }

    }

}
