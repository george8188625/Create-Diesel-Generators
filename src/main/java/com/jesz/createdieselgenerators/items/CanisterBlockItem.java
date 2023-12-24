package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.content.equipment.armor.CapacityEnchantment;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CanisterBlockItem extends BlockItem implements CapacityEnchantment.ICapacityEnchantable {
    public CanisterBlockItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }
    public void appendHoverText(ItemStack stack, Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, components, tooltipFlag);
        if(stack.getTag() != null) {
            CompoundTag primaryTankCompound;
                primaryTankCompound = stack.getTag().getCompound("BlockEntityTag").getList("Tanks", Tag.TAG_COMPOUND).getCompound(0).getCompound("TankContent");

            FluidStack fStack = FluidStack.loadFluidStackFromNBT(primaryTankCompound);
            if(fStack.isEmpty()){
                components.add(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));
                this.getBlock().appendHoverText(stack, level, components, tooltipFlag);
                return;
            }
            components.add(Lang.fluidName(fStack).component().withStyle(ChatFormatting.GRAY).append(" ").append(Lang.number(fStack.getAmount()).style(ChatFormatting.GOLD).component()).append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GOLD)).append(Component.literal(" / ")).append(Lang.number(ConfigRegistry.CANISTER_CAPACITY.get() + ConfigRegistry.CANISTER_CAPACITY_ENCHANTMENT.get()*stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get())).style(ChatFormatting.GRAY).component()).append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GRAY)));
            this.getBlock().appendHoverText(stack, level, components, tooltipFlag);
            return;
        }
        components.add(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));
        this.getBlock().appendHoverText(stack, level, components, tooltipFlag);

    }

    @Override
    public InteractionResult useOn(UseOnContext p_40581_) {
        return super.useOn(p_40581_);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return true; }
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if(enchantment == AllEnchantments.CAPACITY.get())
            return true;
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }
    @Override
    public int getBarColor(ItemStack stack) {
        return 0xEFEFEF;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if(stack.getTag() != null) {
                CompoundTag primaryTankCompound = stack.getTag().getCompound("BlockEntityTag").getList("Tanks", Tag.TAG_COMPOUND).getCompound(0).getCompound("TankContent");

            FluidStack fStack = FluidStack.loadFluidStackFromNBT(primaryTankCompound);
            return !fStack.isEmpty();
        }
        return false;
    }
    @Override
    public int getBarWidth(ItemStack stack) {
        if(stack.getTag() == null)
            return 0;
        CompoundTag primaryTankCompound = stack.getTag().getCompound("BlockEntityTag").getList("Tanks", Tag.TAG_COMPOUND).getCompound(0).getCompound("TankContent");

        return Math.round(13 * Mth.clamp(FluidStack.loadFluidStackFromNBT(primaryTankCompound).getAmount()/(float)(ConfigRegistry.CANISTER_CAPACITY.get()+ ConfigRegistry.CANISTER_CAPACITY_ENCHANTMENT.get()*stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get())), 0, 1));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        if(!ModList.get().isLoaded("dungeons_libraries"))
            return new CanisterFluidHandlerItemStack(stack, ConfigRegistry.CANISTER_CAPACITY.get() + stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get()) * ConfigRegistry.CANISTER_CAPACITY_ENCHANTMENT.get());
        return new CanisterFluidHandlerItemStack(stack, ConfigRegistry.CANISTER_CAPACITY.get());
    }
    static class CanisterFluidHandlerItemStack extends FluidHandlerItemStack{
        public CanisterFluidHandlerItemStack(@NotNull ItemStack container, int capacity) {
            super(container, capacity);
        }

        @Override
        public FluidStack getFluid() {
            CompoundTag tagCompound = container.getTag();
            if (tagCompound == null || !tagCompound.getCompound("BlockEntityTag").contains("Tanks")) {
                return FluidStack.EMPTY;
            }
            if(tagCompound.getCompound("BlockEntityTag").getList("Tanks", CompoundTag.TAG_COMPOUND).isEmpty())
                return FluidStack.EMPTY;
            return FluidStack.loadFluidStackFromNBT(tagCompound.getCompound("BlockEntityTag").getList("Tanks", CompoundTag.TAG_COMPOUND).getCompound(0).getCompound("TankContent"));
        }
        @Override
        protected void setFluid(FluidStack fluid)
        {
            if (!container.hasTag()) {
                container.setTag(new CompoundTag());
            }

            CompoundTag fluidTag = new CompoundTag();
            fluidTag.put("TankContent", new CompoundTag());
            fluid.writeToNBT(fluidTag.getCompound("TankContent"));
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            list.add(fluidTag);
            tag.put("Tanks", list);
            container.getTag().put("BlockEntityTag", tag);
        }

        @Override
        protected void setContainerToEmpty()
        {
            if(container.getTag() != null)
                container.getTag().getCompound("BlockEntityTag").remove("Tanks");
        }
    }
}
