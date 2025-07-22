package com.jesz.createdieselgenerators.content.diesel_engine.modular;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineSoundInstance;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.jesz.createdieselgenerators.content.diesel_engine.IEngine;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock.FACING;
import static com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock.PIPE;

public class ModularDieselEngineBlockEntity extends GeneratingKineticBlockEntity implements IEngine, IMultiBlockEntityContainer.Fluid {
    protected ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;
    protected float remainingTicks = 0;
    protected int length = 1;
    @NotNull
    protected EngineUpgrades upgrade = EngineUpgrades.EMPTY;
    protected LazyOptional<IFluidHandler> fluidCapability = LazyOptional.empty();
    protected FluidTank tankInventory = new SmartFluidTank(1000, f -> { sendData();});
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity = false;
    protected boolean updateCapability = false;


    public ModularDieselEngineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new ModularDieselEngineValueBox());
        movementDirection.withCallback(this::onDirectionChanged);

        behaviours.add(movementDirection);
        super.addBehaviours(behaviours);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap != ForgeCapabilities.FLUID_HANDLER)
            return super.getCapability(cap, side);

        if (!fluidCapability.isPresent())
            refreshCapability();

        if (side == null || (side == Direction.UP && getBlockState().getValue(PIPE)))
            return fluidCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (isController()) {
            if (getGeneratedSpeed() == 0)
                return false;
            super.addToGoggleTooltip(tooltip, isPlayerSneaking);
            containedFluidTooltip(tooltip, isPlayerSneaking, fluidCapability.cast());
            return true;
        }
        ModularDieselEngineBlockEntity controller = getControllerBE();
        if (controller == null)
            return false;
        return controller.addToGoggleTooltip(tooltip, isPlayerSneaking);
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
        float capacity = upgrade.getCapacity(getFuelCapacity() * getHeight() * (1 / upgrade.getSpeed(getFuelSpeed(), this)) * getFuelSpeed(), this);
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {
        if(!enabled() || !isController() || remainingTicks < 1)
            return 0;
        return convertToDirection((movementDirection.getValue() == 1 ? -1 : 1) * upgrade.getSpeed(getFuelSpeed(), this), getBlockState().getValue(ModularDieselEngineBlock.FACING));
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

        reActivateSource = true;
        if (enabled()) {
            if (remainingTicks < length + 1) {
                remainingTicks += length / getFuelBurnRate();
                tankInventory.drain(length, IFluidHandler.FluidAction.EXECUTE);
            }

            if (remainingTicks >= 0)
                remainingTicks -= length;
        }

        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::tickClient);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected EngineSoundInstance soundInstance;

    @OnlyIn(Dist.CLIENT)
    protected void tickClient() {
        if (enabled()) {
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
                soundInstance.setPitch(upgrade.getPitchMultiplier(this) * getFuelSoundPitch());
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
        LazyOptional<IFluidHandler> oldCap = fluidCapability;
        fluidCapability = LazyOptional.of(this::handlerForCapability);
        oldCap.invalidate();
    }

    private IFluidHandler handlerForCapability() {
        return isController() ? (tankInventory)
                : ((getControllerBE() != null) ? getControllerBE().handlerForCapability() : new FluidTank(0));
    }

    public void updateConnectivity() {
        if (soundInstance != null)
            soundInstance = null;
        updateConnectivity = false;
        if (level.isClientSide)
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    @Override
    public float getRemainingTicks() {
        return remainingTicks;
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
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);

        BlockPos controllerBefore = controller;
        int prevHeight = length;

        updateConnectivity = tag.contains("Uninitialized");
        upgrade = EngineUpgrades.get(new ResourceLocation(tag.getString("Upgrade")));
        remainingTicks = tag.getFloat("remainingTicks");
        controller = null;
        lastKnownPos = null;

        if (tag.contains("LastKnownPos"))
            lastKnownPos = NbtUtils.readBlockPos(tag.getCompound("LastKnownPos"));
        if (tag.contains("Controller"))
            controller = NbtUtils.readBlockPos(tag.getCompound("Controller"));

        if (isController()) {
            length = tag.getInt("Height");
            tankInventory.readFromNBT(tag.getCompound("TankContent"));
            if (tankInventory.getSpace() < 0)
                tankInventory.drain(-tankInventory.getSpace(), IFluidHandler.FluidAction.EXECUTE);
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
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if (updateConnectivity)
            compound.putBoolean("Uninitialized", true);
        if (lastKnownPos != null)
            compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController())
            compound.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {
            compound.putString("Upgrade", upgrade.getId().toString());
            compound.putFloat("remainingTicks", remainingTicks);
            compound.put("TankContent", tankInventory.writeToNBT(new CompoundTag()));
            compound.putInt("Height", length);
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
}

