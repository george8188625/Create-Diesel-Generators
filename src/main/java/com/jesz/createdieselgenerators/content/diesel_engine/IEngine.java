package com.jesz.createdieselgenerators.content.diesel_engine;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGRegistries;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public interface IEngine {

    default boolean enabled() {
        if (!validFS())
            return false;
        if (CDGConfig.ANALOG_SPEED_CONTROL.get())
            return true;
        return !(CDGConfig.ENGINES_DISABLED_WITH_REDSTONE.get()
                && self().getBlockState().getValue(DieselEngineBlock.POWERED));
    }

    FuelType getCachedFuelType();
    void setCachedFuelType(FuelType type);
    FluidStack getLastCachedFluid();
    void setLastCachedFluid(FluidStack fluid);
    float getCachedFuelSpeed();
    void setCachedFuelSpeed(float speed);
    float getCachedFuelCapacity();
    void setCachedFuelCapacity(float capacity);
    float getCachedBurnRate();
    void setCachedBurnRate(float rate);

    default void invalidateFuelCache() {
        setLastCachedFluid(FluidStack.EMPTY);
    }

    default FuelType getFuelType() {
        FluidStack current = getTank().getFluid();
        if (!FluidStack.isSameFluid(current, getLastCachedFluid())) {
            setLastCachedFluid(current.copy());
            FuelType type = FuelType.getTypeFor(
                    self().getLevel().registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE),
                    current.getFluid());
            setCachedFuelType(type);
            float speed = type.getGenerated(self()).speed();
            setCachedFuelSpeed(speed);
            setCachedFuelCapacity(speed == 0 ? 0 :
                    type.getGenerated(self()).strength() / speed);
            setCachedBurnRate(type.getGenerated(self()).burn());
        }
        return getCachedFuelType();
    }

    default boolean validFS() {
        if (fs().isEmpty()) return false;
        return getFuelType() != FuelType.EMPTY;
    }

    default FluidStack fs() {
        return getTank().getFluid();
    }

    default float getFuelSpeed() { getFuelType(); return getCachedFuelSpeed(); }
    default float getFuelCapacity() { getFuelType(); return getCachedFuelCapacity(); }
    default float getFuelBurnRate() { getFuelType(); return getCachedBurnRate(); }
    default float getFuelSoundPitch() { return getFuelType().soundPitch(); }

    int getAnalogSignal();

    default float getThrottle() {
        return CDGConfig.ANALOG_SPEED_CONTROL.get() ? (15 - getAnalogSignal()) / 15f : 1f;
    }

    default float getFuelThrottle() {
        float throttle = getThrottle();
        if (throttle == 0f) return 0f;
        return 0.25f + (throttle * 0.75f);
    }

    SmartBlockEntity self();

    FluidTank getTank();

    EngineUpgrades getUpgrade();
    void setUpgrade(EngineUpgrades upgrade);
}
