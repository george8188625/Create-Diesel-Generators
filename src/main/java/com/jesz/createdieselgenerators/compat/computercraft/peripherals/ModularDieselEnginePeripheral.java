package com.jesz.createdieselgenerators.compat.computercraft.peripherals;

import com.jesz.createdieselgenerators.blocks.entity.LargeDieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.templates.FluidTank;


public class ModularDieselEnginePeripheral extends SyncedPeripheral<LargeDieselGeneratorBlockEntity> {

    public ModularDieselEnginePeripheral(LargeDieselGeneratorBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public String getType() {
        return "CDG_DieselEngine";
    }

    @LuaFunction
    public final void setMovementDirection(boolean direction){
        blockEntity.movementDirection.setValue(direction ? 1 : 0);
    }

    @LuaFunction
    public final boolean getMovementDirection(){
        return blockEntity.movementDirection.getValue() == 1;
    }

    @LuaFunction
    public final float getStressCapacity(){
        LargeDieselGeneratorBlockEntity frontEngine = blockEntity.frontEngine.get();

        if(frontEngine == null)
            return blockEntity.calculateAddedStressCapacity();
        return frontEngine.calculateAddedStressCapacity();
    }
    @LuaFunction
    public final int getEngineMultiBlockSize(){
        LargeDieselGeneratorBlockEntity frontEngine = blockEntity.frontEngine.get();

        if(frontEngine == null)
            return blockEntity.stacked;
        return frontEngine.stacked;
    }
    @LuaFunction
    public final float getSpeed(){
        LargeDieselGeneratorBlockEntity frontEngine = blockEntity.frontEngine.get();
        if(frontEngine == null)
            return Math.abs(blockEntity.getGeneratedSpeed());
        return Math.abs(frontEngine.getGeneratedSpeed());
    }

    @LuaFunction
    public final float getFuelAmount(){
        LargeDieselGeneratorBlockEntity frontEngine = blockEntity.frontEngine.get();
        if(frontEngine == null)
            return blockEntity.tank.getPrimaryHandler().getFluid().getAmount();
        return frontEngine.tank.getPrimaryHandler().getFluid().getAmount();
    }
    @LuaFunction
    public final float getFuelBurnRate(){
        return FuelTypeManager.getBurnRate(blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(new FluidTank(1)).getFluidInTank(0).getFluid());
    }
}
