package com.jesz.createdieselgenerators.compat.computercraft;

import com.jesz.createdieselgenerators.blocks.entity.DieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.LargeDieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.compat.computercraft.peripherals.DieselEnginePeripheral;
import com.jesz.createdieselgenerators.compat.computercraft.peripherals.HugeDieselEnginePeripheral;
import com.jesz.createdieselgenerators.compat.computercraft.peripherals.ModularDieselEnginePeripheral;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.registries.ForgeRegistries;

public class ComputerBehaviour extends AbstractComputerBehaviour {
    protected static final Capability<IPeripheral> PERIPHERAL_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {
            });
    LazyOptional<IPeripheral> peripheral;
    NonNullSupplier<IPeripheral> peripheralSupplier;

    public ComputerBehaviour(SmartBlockEntity be) {
        super(be);
        this.peripheralSupplier = getPeripheralFor(be);
    }
    public static NonNullSupplier<IPeripheral> getPeripheralFor(SmartBlockEntity be) {
        if (be instanceof DieselGeneratorBlockEntity dgbe)
            return () -> new DieselEnginePeripheral(dgbe);
        if (be instanceof LargeDieselGeneratorBlockEntity ldgbe)
            return () -> new ModularDieselEnginePeripheral(ldgbe);
        if (be instanceof HugeDieselEngineBlockEntity hdebe)
            return () -> new HugeDieselEnginePeripheral(hdebe);
        throw new IllegalArgumentException(
                "No peripheral available for " + ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(be.getType()));
    }

    @Override
    public <T> boolean isPeripheralCap(Capability<T> cap) {
        return cap == PERIPHERAL_CAPABILITY;
    }

    @Override
    public <T> LazyOptional<T> getPeripheralCapability() {
        if (peripheral == null || !peripheral.isPresent())
            peripheral = LazyOptional.of(peripheralSupplier);
        return peripheral.cast();
    }

    @Override
    public void removePeripheral() {
        if (peripheral != null)
            peripheral.invalidate();
    }
}
