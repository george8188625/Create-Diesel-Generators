package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;

public class CanisterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    CapacityEnchantedFluidTankBehaviour tank;
    BlockState state;

    private Component customName;

    private int capacityEnchantLevel;
    private ListTag enchantmentTag;

    public CanisterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.state = state;

    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking, getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = CapacityEnchantedFluidTankBehaviour.single(this, Math.abs((ConfigRegistry.CANISTER_CAPACITY.get())), ConfigRegistry.CANISTER_CAPACITY_ENCHANTMENT.get());
        behaviours.add(tank);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap) {
        if(cap == ForgeCapabilities.FLUID_HANDLER)
            return tank.getCapability().cast();
        return super.getCapability(cap);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {

        if(cap == ForgeCapabilities.FLUID_HANDLER)
            return tank.getCapability().cast();
        return super.getCapability(cap, side);
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("CapacityEnchantment", capacityEnchantLevel);
        if (this.customName != null)
            compound.putString("CustomName", Component.Serializer.toJson(this.customName));
        if(this.enchantmentTag != null)
            compound.put("Enchantments", enchantmentTag);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        capacityEnchantLevel = compound.getInt("CapacityEnchantment");
        if(compound.contains("Enchantments"))
            enchantmentTag = compound.getList("Enchantments", Tag.TAG_COMPOUND);
        if (compound.contains("CustomName", 8))
            this.customName = Component.Serializer.fromJson(compound.getString("CustomName"));
    }

    public void setCustomName(Component customName) {
        this.customName = customName;
    }

    public Component getCustomName() {
        return customName;
    }

    public ListTag getEnchantmentTag() {
        return enchantmentTag;
    }

    public void setEnchantmentTag(ListTag enchantmentTag) {
        this.enchantmentTag = enchantmentTag;
    }

    public void setCapacityEnchantLevel(int capacityEnchantLevel) {
        this.capacityEnchantLevel = capacityEnchantLevel;
        tank.getPrimaryHandler().setCapacity(tank.baseCapacity + tank.capacityAddition * capacityEnchantLevel);
    }

    public static class CapacityEnchantedFluidTankBehaviour extends SmartFluidTankBehaviour{
        int capacityAddition;
        int baseCapacity;

        public CapacityEnchantedFluidTankBehaviour(BehaviourType<SmartFluidTankBehaviour> type, SmartBlockEntity be, int tanks, int tankCapacity, boolean enforceVariety, int capacityAddition) {
            super(type, be, tanks, tankCapacity, enforceVariety);
            this.capacityAddition = capacityAddition;
            this.baseCapacity = tankCapacity;
        }
        public static CapacityEnchantedFluidTankBehaviour single(SmartBlockEntity be, int capacity, int capacityAddition) {
            return new CapacityEnchantedFluidTankBehaviour(TYPE, be, 1, capacity, false, capacityAddition);
        }
        @Override
        public void read(CompoundTag compound, boolean clientPacket) {
            super.read(compound, clientPacket);
            if(compound.contains("CapacityEnchantment"))
                getPrimaryHandler().setCapacity(baseCapacity + compound.getInt("CapacityEnchantment") * capacityAddition);
        }

    }
}
