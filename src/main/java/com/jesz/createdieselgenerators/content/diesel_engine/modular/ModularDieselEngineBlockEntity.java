package com.jesz.createdieselgenerators.content.diesel_engine.modular;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineSoundInstance;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.jesz.createdieselgenerators.content.diesel_engine.IEngine;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock.FACING;
import static com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock.PIPE;

public class ModularDieselEngineBlockEntity extends GeneratingKineticBlockEntity implements IEngine, IMultiBlockEntityContainer.Fluid {
    protected ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;
    protected int length = 1;
    @NotNull
    protected EngineUpgrades upgrade = EngineUpgrades.EMPTY;
    protected IFluidHandler fluidCapability;
    protected FluidTank tankInventory = new SmartFluidTank(1000, f -> sendData());
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity = false;
    protected boolean updateCapability = false;
    private float lastCapacity;
    private float lastSpeed;
    public int analogSignal = 0;
    private float fuelDebt = 0f;
    private boolean signalChanged = false;
    private FuelType cachedFuelType = FuelType.EMPTY;
    private FluidStack lastCachedFluid = FluidStack.EMPTY;
    private float cachedFuelSpeed = 0f;
    private float cachedFuelCapacity = 0f;
    private float cachedBurnRate = 0f;

    public ModularDieselEngineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public void resetConnectivity() {
        updateConnectivity = true;
        controller = null;
        length = 1;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new ModularDieselEngineValueBox());
        movementDirection.withCallback(this::onDirectionChanged);

        behaviours.add(movementDirection);
        super.addBehaviours(behaviours);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                CDGBlockEntityTypes.MODULAR_DIESEL_ENGINE.get(),
                (be, side) -> {
                    if (be.fluidCapability == null)
                        be.refreshCapability();
                    if (side == null || (side == Direction.UP && be.getBlockState().getValue(PIPE)))
                        return be.fluidCapability;
                    return null;
                });
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!isController()) {
            ModularDieselEngineBlockEntity controller = getControllerBE();
            if (controller == null)
                return false;
            return controller.addToGoggleTooltip(tooltip, isPlayerSneaking);
        }
        if (getGeneratedSpeed() != 0)
            super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return containedFluidTooltip(tooltip, isPlayerSneaking, fluidCapability);
    }

    public void onDirectionChanged(int v) {
        ModularDieselEngineBlockEntity controller = getControllerBE();
        if (controller != null) {
            controller.movementDirection.setValue(v);
            controller.reActivateSource = true;

            for (int i = 0; i < controller.getHeight(); i++) {
                if (level.getBlockEntity(controller.getBlockPos().relative(controller.getBlockState().getValue(FACING).getAxis(), i)) instanceof ModularDieselEngineBlockEntity be && be.movementDirection.getValue() != v)
                    be.movementDirection.setValue(v);
            }
        }
    }

    @Override
    public float calculateAddedStressCapacity() {
        float speed = upgrade.getSpeed(getFuelSpeed(), this) * getThrottle();
        float capacity = upgrade.getCapacity(
                getFuelCapacity() * getHeight() * (1 / Math.max(speed, 0.001f)) * speed, this);
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {
        if (!enabled() || !isController()) return 0;
        float throttle = getThrottle();
        if (throttle == 0f) return 0;
        return convertToDirection(
                (movementDirection.getValue() == 1 ? -1 : 1)
                        * upgrade.getSpeed(getFuelSpeed(), this)
                        * throttle,
                getBlockState().getValue(ModularDieselEngineBlock.FACING));
    }

    @Override
    public void tick() {
        super.tick();

        if (updateCapability) {
            updateCapability = false;
            refreshCapability();
        }
        if (updateConnectivity)
            updateConnectivity();

        if (!isController()) {
            if (upgrade == EngineUpgrades.EMPTY)
                return;
            ModularDieselEngineBlockEntity controller = getControllerBE();

            if (controller.upgrade == EngineUpgrades.EMPTY)
                controller.upgrade = upgrade;
            else
                Block.popResource(level, getBlockPos(), upgrade.getItem());
            upgrade = EngineUpgrades.EMPTY;

            return;
        }

        if (signalChanged) {
            signalChanged = false;
            reActivateSource = true;
            setChanged();
            sendData();
        }

        boolean effectivelyOff = getThrottle() == 0f || !validFS();
        if (effectivelyOff) {
            if (hasNetwork())
                getOrCreateNetwork().remove(this);
            detachKinetics();
            removeSource();
            return;
        }

        if (!level.isClientSide && validFS()) {
            float throttle = getThrottle();
            float currentSpeed = getGeneratedSpeed();
            float currentCapacity = upgrade.getCapacity(
                    getFuelCapacity() * getHeight() * (1 / Math.max(upgrade.getSpeed(getFuelSpeed(), this) * throttle, 0.001f))
                            * upgrade.getSpeed(getFuelSpeed(), this) * throttle, this);
            if (lastSpeed != currentSpeed || lastCapacity != currentCapacity) {
                reActivateSource = true;
                lastSpeed = currentSpeed;
                lastCapacity = currentCapacity;
            }
        }

        if (isOverStressed())
            return;

        fuelDebt += (length * cachedBurnRate) * getFuelThrottle();
        while (fuelDebt >= 1f) {
            tankInventory.drain(length, IFluidHandler.FluidAction.EXECUTE);
            fuelDebt -= 1f;
        }

        if (level.isClientSide) {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> this::tickClient);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected EngineSoundInstance soundInstance;

    @OnlyIn(Dist.CLIENT)
    protected void tickClient() {
        if (enabled() && getThrottle() > 0 && !isOverStressed()) {
            Vec3 pos = Vec3.atCenterOf(getBlockPos());
            if (getBlockState().getValue(FACING).getAxis() == Direction.Axis.X)
                pos = pos.add((double) length / 2 - 0.5, 0, 0);
            else
                pos = pos.add(0, 0, (double) length / 2 - 0.5);
            if (soundInstance == null || soundInstance.isStopped() || soundInstance.getX() != pos.x || soundInstance.getZ() != pos.z) {
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(soundInstance = upgrade.createSoundInstance(this, pos));
            } else if (soundInstance.active()) {
                soundInstance.keepAlive();
                soundInstance.setPitch(upgrade.getPitchMultiplier(this) * getFuelSoundPitch() * getThrottle());
                soundInstance.setVolume(upgrade.getVolume(this));
            }
        } else {
            if (soundInstance != null) {
                soundInstance.fadeOut();
                soundInstance = null;
            }
        }
    }

    void refreshCapability() {
        fluidCapability = handlerForCapability();
        invalidateCapabilities();
    }
    private IFluidHandler handlerForCapability() {
        return isController() ? (tankInventory)
                : ((getControllerBE() != null) ? getControllerBE().handlerForCapability() : new FluidTank(0));
    }

    public void updateConnectivity() {
        updateConnectivity = false;
        if (level.isClientSide)
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    @Override
    public SmartBlockEntity self() {
        return this;
    }

    @Override
    public FluidTank getTank() {
        return tankInventory;
    }

    @Override
    public EngineUpgrades getUpgrade() {
        return upgrade;
    }

    @Override
    public void setUpgrade(EngineUpgrades upgrade) {
        this.upgrade = upgrade;
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    public ModularDieselEngineBlockEntity getControllerBE() {
        if (isController() || !hasLevel())
            return this;
        BlockEntity be = level.getBlockEntity(controller);
        if (be instanceof ModularDieselEngineBlockEntity)
            return (ModularDieselEngineBlockEntity) be;
        return null;
    }

    @Override
    public boolean isController() {
        return controller == null || controller.equals(worldPosition);
    }

    @Override
    public void setController(BlockPos controller) {
        if (level.isClientSide && !isVirtual())
            return;
        if (controller.equals(this.controller))
            return;
        this.controller = controller;
        refreshCapability();
        setChanged();
        sendData();
    }

    @Override
    public void removeController(boolean keepContents) {
        if (level.isClientSide)
            return;
        updateConnectivity = true;
        controller = null;
        length = 1;
        reActivateSource = true;

        refreshCapability();
        setChanged();
        sendData();
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        BlockPos controllerBefore = controller;
        int prevHeight = length;

        updateConnectivity = compound.contains("Uninitialized");
        upgrade = EngineUpgrades.get(ResourceLocation.parse(compound.getString("Upgrade")));
        controller = null;
        lastKnownPos = null;

        if (compound.contains("LastKnownPos"))
            lastKnownPos = NBTHelper.readBlockPos(compound, "LastKnownPos");
        if (compound.contains("Controller"))
            controller = NBTHelper.readBlockPos(compound, "Controller");

        if (isController()) {
            length = compound.getInt("Height");
            tankInventory.readFromNBT(registries, compound.getCompound("TankContent"));
            if (tankInventory.getSpace() < 0)
                tankInventory.drain(-tankInventory.getSpace(), IFluidHandler.FluidAction.EXECUTE);
            analogSignal = compound.contains("AnalogSignal") ? compound.getInt("AnalogSignal") : 0;
            fuelDebt = 0f;
            invalidateFuelCache();
        }

        updateCapability = true;

        if (!clientPacket)
            return;

        boolean changeOfController = !Objects.equals(controllerBefore, controller);
        if (changeOfController || prevHeight != length) {
            if (hasLevel())
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
            if (isController())
                tankInventory.setCapacity(1000);
            invalidateRenderBoundingBox();
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);

        if (updateConnectivity)
            compound.putBoolean("Uninitialized", true);
        if (lastKnownPos != null)
            compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController())
            compound.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {
            compound.putString("Upgrade", upgrade.getId().toString());
            compound.put("TankContent", tankInventory.writeToNBT(registries, new CompoundTag()));
            compound.putInt("Height", length);
            compound.putInt("AnalogSignal", analogSignal);
        }
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        reActivateSource = true;
        setChanged();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return getBlockState().getValue(FACING).getAxis();
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        return 21;
    }

    @Override
    public int getMaxWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return length;
    }

    @Override
    public void setHeight(int height) {
        length = height;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public void setWidth(int width) {

    }

    @Override
    public boolean enabled() {
        if (!IEngine.super.enabled())
            return false;
        if (CDGConfig.ANALOG_SPEED_CONTROL.get())
            return true;
        if (!CDGConfig.ENGINES_DISABLED_WITH_REDSTONE.get())
            return true;
        for (int i = 1; i < length; i++) {
            BlockState state = level.getBlockState(getBlockPos().relative(getMainConnectionAxis(), i));
            if (CDGBlocks.MODULAR_DIESEL_ENGINE.has(state))
                if (state.getValue(DieselEngineBlock.POWERED))
                    return false;
        }
        return true;
    }

    @Override
    public int getAnalogSignal() {
        return analogSignal;
    }

    public void setAnalogSignal(int newSignal) { analogSignal = newSignal; }
    public void setSignalChanged(boolean newSignal) { signalChanged = newSignal; }

    @Override public FuelType getCachedFuelType() { return cachedFuelType; }
    @Override public void setCachedFuelType(FuelType t) { cachedFuelType = t; }
    @Override public FluidStack getLastCachedFluid() { return lastCachedFluid; }
    @Override public void setLastCachedFluid(FluidStack f) { lastCachedFluid = f; }
    @Override public float getCachedFuelSpeed() { return cachedFuelSpeed; }
    @Override public void setCachedFuelSpeed(float s) { cachedFuelSpeed = s; }
    @Override public float getCachedFuelCapacity() { return cachedFuelCapacity; }
    @Override public void setCachedFuelCapacity(float c) { cachedFuelCapacity = c; }
    @Override public float getCachedBurnRate() { return cachedBurnRate; }
    @Override public void setCachedBurnRate(float r) { cachedBurnRate = r; }
}

