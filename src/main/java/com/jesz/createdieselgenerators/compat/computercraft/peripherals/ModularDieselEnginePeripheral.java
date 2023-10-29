package com.jesz.createdieselgenerators.compat.computercraft.peripherals;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.blocks.entity.LargeDieselGeneratorBlockEntity;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaFunction;


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
        if(blockEntity.FrontEngine == null)
            return blockEntity.calculateAddedStressCapacity();
        return blockEntity.FrontEngine.calculateAddedStressCapacity();
    }
    @LuaFunction
    public final int getEngineMultiBlockSize(){
        if(blockEntity.FrontEngine == null)
            return blockEntity.stacked;
        return blockEntity.FrontEngine.stacked;
    }
    @LuaFunction
    public final float getSpeed(){
        if(blockEntity.FrontEngine == null)
            return Math.abs(blockEntity.getGeneratedSpeed());
        return Math.abs(blockEntity.FrontEngine.getGeneratedSpeed());
    }

    @LuaFunction
    public final float getFuelAmount(){
        if(blockEntity.FrontEngine == null)
            return blockEntity.tank.getPrimaryHandler().getFluid().getAmount();
        return blockEntity.FrontEngine.tank.getPrimaryHandler().getFluid().getAmount();
    }
    @LuaFunction
    public final float getFuelBurnRate(){
        if(blockEntity.FrontEngine == null)
            return CreateDieselGenerators.getBurnRate(blockEntity.tank.getPrimaryHandler().getFluid());
        return CreateDieselGenerators.getBurnRate(blockEntity.FrontEngine.tank.getPrimaryHandler().getFluid());

    }
}
