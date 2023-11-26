package com.jesz.createdieselgenerators.compat.computercraft.peripherals;

import com.jesz.createdieselgenerators.blocks.entity.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaFunction;


public class HugeDieselEnginePeripheral extends SyncedPeripheral<HugeDieselEngineBlockEntity> {

    public HugeDieselEnginePeripheral(HugeDieselEngineBlockEntity blockEntity) {
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
        return (FuelTypeManager.getGeneratedStress(blockEntity, blockEntity.tank.getPrimaryHandler().getFluid().getFluid()))/FuelTypeManager.getGeneratedSpeed(blockEntity, blockEntity.tank.getPrimaryHandler().getFluid().getFluid());
    }

    @LuaFunction
    public final float getSpeed(){
        return FuelTypeManager.getGeneratedSpeed(blockEntity, blockEntity.tank.getPrimaryHandler().getFluid().getFluid());
    }

    @LuaFunction
    public final float getFuelAmount(){
        return blockEntity.tank.getPrimaryHandler().getFluid().getAmount();
    }
    @LuaFunction
    public final float getFuelBurnRate(){
        return FuelTypeManager.getBurnRate(blockEntity, blockEntity.tank.getPrimaryHandler().getFluid().getFluid());
    }
}
