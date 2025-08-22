package com.jesz.createdieselgenerators.content.distillation;

import com.jesz.createdieselgenerators.CDGRecipes;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DistillationTankBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer.Fluid, IHaveGoggleInformation {
    private static final int MAX_SIZE = 3;

    public float progress;
    protected LazyOptional<IFluidHandler> fluidCapability;
    protected boolean forceFluidLevelUpdate;
    public FluidTank tankInventory;
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    public boolean window;
    protected int luminosity;
    protected int width;
    protected int height;

    private static final int SYNC_RATE = 8;
    protected int syncCooldown;
    protected boolean queuedSync;

    // For rendering purposes only
    private LerpedFloat fluidLevel;

    BlazeBurnerBlock.HeatLevel highestHeatLevel = BlazeBurnerBlock.HeatLevel.NONE;

    public DistillationTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tankInventory = createInventory();
        fluidCapability = LazyOptional.of(() -> tankInventory);
        forceFluidLevelUpdate = true;
        updateConnectivity = false;
        window = false;
        height = 1;
        width = 1;
        refreshCapability();
    }

    protected SmartFluidTank createInventory() {return new SmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);}
    public BlazeBurnerBlock.HeatLevel getHeat() {
        int width = getControllerBE().width;
        BlazeBurnerBlock.HeatLevel highestHeat = BlazeBurnerBlock.HeatLevel.NONE;

        for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {
                BlockPos pos = getController().offset(xOffset, -1, zOffset);
                BlockState blockState = level.getBlockState(pos);
                BlazeBurnerBlock.HeatLevel heat = BasinBlockEntity.getHeatLevelOf(blockState);
                if(!highestHeat.isAtLeast(heat))
                    highestHeat = heat;
            }
        }
        return highestHeat;
    }
    public void updateConnectivity() {
        updateConnectivity = false;
        if (level.isClientSide)
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    int processingTime = -1;
    DistillationRecipe currentRecipe;
    private void startProcessing() {
        if(currentRecipe == null)
            return;
        processingTime = (currentRecipe.getProcessingDuration());
        if(!level.isClientSide)
            sendData();
    }

    @Override
    public void tick() {
        if (isController() && isBottom()) {

            if (processingTime > -1 && currentRecipe != null) {
                boolean canFill = true;
                for (int i = 0; i < currentRecipe.getFluidResults().size(); i++) {
                    if (level.getBlockEntity(getBlockPos().above(i+1)) instanceof DistillationTankBlockEntity be) {
                        if (!isSameMultiBlock(be)) {
                            canFill = false;
                            break;
                        }
                        if (be.tankInventory.getSpace() < (currentRecipe.getFluidResults().get(i).getAmount())) {
                            canFill = false;
                            break;
                        }
                    } else {
                        canFill = false;
                        break;
                    }
                }
                if (canFill)
                    processingTime--;

                if (!(tankInventory.getFluid().getAmount() >= currentRecipe.getFluidIngredients().get(0).getRequiredAmount() && currentRecipe.getRequiredHeat().testBlazeBurner(highestHeatLevel))) {
                    currentRecipe = null;
                    processingTime = -1;
                }
            }
            if (processingTime == 0 && currentRecipe != null) {
                if (tankInventory.getFluid().getAmount() >= currentRecipe.getFluidIngredients().get(0).getRequiredAmount()  && currentRecipe.getRequiredHeat().testBlazeBurner(highestHeatLevel)) {
                    tankInventory.drain(currentRecipe.getFluidIngredients().get(0).getRequiredAmount(), IFluidHandler.FluidAction.EXECUTE);
                    if (currentRecipe != null)
                        for (int i = 0; i < currentRecipe.getFluidResults().size(); i++) {
                            if (level.getBlockEntity(getBlockPos().above(i+1)) instanceof DistillationTankBlockEntity be) {
                                if(!isSameMultiBlock(be))
                                    break;
                                be.tankInventory.fill(currentRecipe.getFluidResults().get(i), IFluidHandler.FluidAction.EXECUTE);
                            } else {
                                break;
                            }
                        }
                }

                currentRecipe = null;
                processingTime = -1;
                if (!tankInventory.isEmpty() && isController() && isBottom())
                    checkForRecipes();
            }

            progress = currentRecipe != null ? (float) processingTime / (currentRecipe.getProcessingDuration()) : 0;
        }

        super.tick();
        if (syncCooldown > 0) {
            syncCooldown--;
            if (syncCooldown == 0 && queuedSync)
                sendData();
        }

        if (lastKnownPos == null)
            lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition)) {
            onPositionChanged();
            return;
        }

        if (updateConnectivity)
            updateConnectivity();
        if (fluidLevel != null)
            fluidLevel.tickChaser();
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.getX() == controller.getX() && worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
    }

    @Override
    public void initialize() {
        super.initialize();
        updateTemperature();
        checkForRecipes();
        sendData();
        if (level.isClientSide)
            invalidateRenderBoundingBox();
    }

    private void onPositionChanged() {
        removeController(true);
        lastKnownPos = worldPosition;
    }
    protected List<Recipe<?>> getMatchingRecipes() {

        List<Recipe<?>> list = RecipeFinder.get(getRecipeCacheKey(), level, recipe -> recipe.getType() == CDGRecipes.DISTILLATION.getType());
        return list.stream()
                .sorted((r1, r2) -> {
                    if(r1 instanceof DistillationRecipe recipe1 && r2 instanceof DistillationRecipe recipe2)
                        return recipe2.getRequiredHeat().ordinal() - recipe1.getRequiredHeat().ordinal();
                    return 0;
                })
                .filter(r ->{
                            if(r instanceof DistillationRecipe recipe){
                                if(!recipe.getRequiredHeat().testBlazeBurner(highestHeatLevel))
                                    return false;
                                return recipe.getFluidIngredients().get(0).test(tankInventory.getFluid());
                            }
                            return false;
                        })
                .collect(Collectors.toList());
    }
    static final Object RECIPE_CACHE_KEY = new Object();
    Object getRecipeCacheKey() {
        return RECIPE_CACHE_KEY;
    }

    protected void onFluidStackChanged(FluidStack newFluidStack) {
        if (!hasLevel())
            return;
        if(!tankInventory.isEmpty() && isController() && isBottom())
            checkForRecipes();

        FluidType attributes = newFluidStack.getFluid()
                .getFluidType();
        int luminosity = (int) (attributes.getLightLevel(newFluidStack) / 1.2f);
        boolean reversed = attributes.isLighterThanAir();
        int maxY = (int) ((getFillState() * height) + 1);

        for (int yOffset = 0; yOffset < height; yOffset++) {
            boolean isBright = reversed ? (height - yOffset <= maxY) : (yOffset < maxY);
            int actualLuminosity = isBright ? luminosity : luminosity > 0 ? 1 : 0;

            for (int xOffset = 0; xOffset < width; xOffset++) {
                for (int zOffset = 0; zOffset < width; zOffset++) {
                    BlockPos pos = this.worldPosition.offset(xOffset, yOffset, zOffset);
                    DistillationTankBlockEntity tankAt = ConnectivityHandler.partAt(getType(), level, pos);
                    if (tankAt == null)
                        continue;
                    level.updateNeighbourForOutputSignal(pos, tankAt.getBlockState()
                            .getBlock());
                    if (tankAt.luminosity == actualLuminosity)
                        continue;
                    tankAt.setLuminosity(actualLuminosity);
                }
            }
        }

        if (!level.isClientSide) {
            setChanged();
            sendData();
        }

        if (isVirtual()) {
            if (fluidLevel == null)
                fluidLevel = LerpedFloat.linear()
                        .startWithValue(getFillState());
            fluidLevel.chase(getFillState(), .5f, LerpedFloat.Chaser.EXP);
        }
    }

    protected void setLuminosity(int luminosity) {
        if (level.isClientSide)
            return;
        if (this.luminosity == luminosity)
            return;
        this.luminosity = luminosity;
        sendData();
    }

    @SuppressWarnings("unchecked")
    @Override
    public DistillationTankBlockEntity getControllerBE() {
        if (isController())
            return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof DistillationTankBlockEntity)
            return (DistillationTankBlockEntity) blockEntity;
        return null;
    }

    public void applyFluidTankSize(int blocks) {
        tankInventory.setCapacity(blocks * getCapacityMultiplier());
        int overflow = tankInventory.getFluidAmount() - tankInventory.getCapacity();
        if (overflow > 0)
            tankInventory.drain(overflow, IFluidHandler.FluidAction.EXECUTE);
        forceFluidLevelUpdate = true;
    }

    public void removeController(boolean keepFluids) {
        if (level.isClientSide)
            return;
        updateConnectivity = true;
        if (!keepFluids)
            applyFluidTankSize(1);
        controller = null;
        width = 1;
        height = 1;
        onFluidStackChanged(tankInventory.getFluid());

        BlockState state = getBlockState();
        if (DistillationTankBlock.isTank(state)) {
            state = state.setValue(DistillationTankBlock.BOTTOM, true);
            state = state.setValue(DistillationTankBlock.TOP, true);
            state = state.setValue(DistillationTankBlock.SHAPE, window ? FluidTankBlock.Shape.WINDOW : FluidTankBlock.Shape.PLAIN);
            getLevel().setBlock(worldPosition, state, 6);
        }

        refreshCapability();
        setChanged();
        sendData();
    }
    public void toggleWindows() {
        DistillationTankBlockEntity be = getControllerBE();
        if (be == null)
            return;
        be.setWindows(!be.window);
    }

    @Override
    public void sendData() {
        if (syncCooldown > 0) {
            queuedSync = true;
            return;
        }
        super.sendData();
        queuedSync = false;
        syncCooldown = SYNC_RATE;
    }

    public void setWindows(boolean window) {
        this.window = window;
        for (int yOffset = 0; yOffset < height; yOffset++) {
            for (int xOffset = 0; xOffset < width; xOffset++) {
                for (int zOffset = 0; zOffset < width; zOffset++) {

                    BlockPos pos = this.worldPosition.offset(xOffset, yOffset, zOffset);
                    BlockState blockState = level.getBlockState(pos);
                    if (!DistillationTankBlock.isTank(blockState))
                        continue;

                    FluidTankBlock.Shape shape = FluidTankBlock.Shape.PLAIN;
                    if (window) {

                        if (width == 1)
                            shape = FluidTankBlock.Shape.WINDOW;

                        if (width == 2)
                            shape = xOffset == 0 ? zOffset == 0 ? FluidTankBlock.Shape.WINDOW_NW : FluidTankBlock.Shape.WINDOW_SW
                                    : zOffset == 0 ? FluidTankBlock.Shape.WINDOW_NE : FluidTankBlock.Shape.WINDOW_SE;

                        if (width == 3 && Math.abs(xOffset - zOffset) == 1)
                            shape = FluidTankBlock.Shape.WINDOW;
                    }
                    level.setBlock(pos, blockState.setValue(DistillationTankBlock.SHAPE, shape), 22);
                    level.getChunkSource()
                            .getLightEngine()
                            .checkBlock(pos);
                }
            }
        }
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

    private void refreshCapability() {
        LazyOptional<IFluidHandler> oldCap = fluidCapability;
        fluidCapability = LazyOptional.of(() -> handlerForCapability());
        oldCap.invalidate();
    }

    private IFluidHandler handlerForCapability() {
        return isController() ? tankInventory
                : getControllerBE() != null ? getControllerBE().handlerForCapability() : new FluidTank(0);
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        if (isController())
            return super.createRenderBoundingBox().expandTowards(width - 1, 0, width - 1);
        else
            return super.createRenderBoundingBox();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        DistillationTankBlockEntity controllerBE = getControllerBE();
        if (controllerBE == null)
            return false;
        return containedFluidTooltip(tooltip, isPlayerSneaking,
                controllerBE.getCapability(ForgeCapabilities.FLUID_HANDLER));
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = width;
        int prevHeight = height;
        int prevLum = luminosity;

        updateConnectivity = compound.contains("Uninitialized");
        luminosity = compound.getInt("Luminosity");
        controller = null;
        lastKnownPos = null;

        if (compound.contains("LastKnownPos"))
            lastKnownPos = NbtUtils.readBlockPos(compound.getCompound("LastKnownPos"));
        if (compound.contains("Controller"))
            controller = NbtUtils.readBlockPos(compound.getCompound("Controller"));

        if (isController()) {
            window = compound.getBoolean("Window");
            width = compound.getInt("Size");
            height = compound.getInt("Height");
            tankInventory.setCapacity(getTotalTankSize() * getCapacityMultiplier());
            tankInventory.readFromNBT(compound.getCompound("TankContent"));
            if (tankInventory.getSpace() < 0)
                tankInventory.drain(-tankInventory.getSpace(), IFluidHandler.FluidAction.EXECUTE);
        }
        if (compound.contains("ForceFluidLevel") || fluidLevel == null)
            fluidLevel = LerpedFloat.linear()
                    .startWithValue(getFillState());

        if (!clientPacket)
            return;

        boolean changeOfController =
                !Objects.equals(controllerBefore, controller);
        if (changeOfController || prevSize != width || prevHeight != height) {
            if (hasLevel())
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
            if (isController())
                tankInventory.setCapacity(getCapacityMultiplier() * getTotalTankSize());
            invalidateRenderBoundingBox();
        }
        if (isController()) {
            float fillState = getFillState();
            if (compound.contains("ForceFluidLevel") || fluidLevel == null)
                fluidLevel = LerpedFloat.linear()
                        .startWithValue(fillState);
            fluidLevel.chase(fillState, 0.5f, LerpedFloat.Chaser.EXP);
            processingTime = compound.getInt("Progress");
        }
        if (luminosity != prevLum && hasLevel())
            level.getChunkSource()
                    .getLightEngine()
                    .checkBlock(worldPosition);

        if (compound.contains("LazySync"))
            fluidLevel.chase(fluidLevel.getChaseTarget(), 0.125f, LerpedFloat.Chaser.EXP);
        updateTemperature();
        List<Recipe<?>> r = getMatchingRecipes();
        if (!r.isEmpty()) {
            currentRecipe = (DistillationRecipe) r.get(0);
            if(processingTime <= 0)
                startProcessing();
        }}
    public float getFillState() {
        return (float) tankInventory.getFluidAmount() / tankInventory.getCapacity();
    }
    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        if (updateConnectivity)
            compound.putBoolean("Uninitialized", true);
        if (lastKnownPos != null)
            compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController())
            compound.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {
            compound.putBoolean("Window", window);
            compound.put("TankContent", tankInventory.writeToNBT(new CompoundTag()));
            compound.putInt("Size", width);
            compound.putInt("Height", height);
            compound.putInt("Progress", processingTime);

        }
        compound.putInt("Luminosity", luminosity);
        super.write(compound, clientPacket);

        if (!clientPacket)
            return;
        if (forceFluidLevelUpdate)
            compound.putBoolean("ForceFluidLevel", true);
        if (queuedSync)
            compound.putBoolean("LazySync", true);
        forceFluidLevelUpdate = false;
    }
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (!fluidCapability.isPresent())
            refreshCapability();
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return fluidCapability.cast();
        return super.getCapability(cap, side);
    }
    @Override
    public void invalidate() {
        super.invalidate();
    }
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}
    public int getTotalTankSize() {
        return width * width;
    }
    public static int getCapacityMultiplier() {
        return AllConfigs.server().fluids.fluidTankCapacity.get()*1000;
    }
    public LerpedFloat getFluidLevel() {
        return fluidLevel;
    }
    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }
    @Override
    public void notifyMultiUpdated() {
        BlockState state = this.getBlockState();
        if (DistillationTankBlock.isTank(state)) { // safety
            state = state.setValue(DistillationTankBlock.BOTTOM, getBottomConnectivity());
            state = state.setValue(DistillationTankBlock.TOP, getTopConnectivity());
            level.setBlock(getBlockPos(), state, 6);
        }
        if (isController())
            setWindows(window);
        onFluidStackChanged(tankInventory.getFluid());
        setChanged();
    }

    private boolean getBottomConnectivity() {
        if ((level.getBlockEntity(getBlockPos().below()) instanceof DistillationTankBlockEntity be))
            return !isSameMultiBlock(be);
        return true;
    }
    private boolean getTopConnectivity() {
        if ((level.getBlockEntity(getBlockPos().above()) instanceof DistillationTankBlockEntity be))
            return !isSameMultiBlock(be);
        return true;
    }

    @Override
    public void setExtraData(@Nullable Object data) {
        if (data instanceof Boolean)
            window = (boolean) data;
    }

    @Override
    @Nullable
    public Object getExtraData() {
        return window;
    }

    @Override
    public Object modifyExtraData(Object data) {
        if (data instanceof Boolean windows) {
            windows |= window;
            return windows;
        }
        return data;
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return Direction.Axis.Y;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
            if (longAxis == Direction.Axis.Y)
                return 1;
            return getMaxWidth();
    }

    @Override
    public int getMaxWidth() {
        return MAX_SIZE;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public boolean hasTank() {
        return true;
    }

    @Override
    public int getTankSize(int tank) {
        return getCapacityMultiplier();
    }

    @Override
    public void setTankSize(int tank, int blocks) {
        applyFluidTankSize(blocks);
    }

    @Override
    public IFluidTank getTank(int tank) {
        return tankInventory;
    }

    @Override
    public FluidStack getFluid(int tank) {
        return tankInventory.getFluid()
                .copy();
    }

    public void updateVerticalMulti() {
        BlockState state = this.getBlockState();
        if (DistillationTankBlock.isTank(state)) { // safety
            state = state.setValue(DistillationTankBlock.BOTTOM, getBottomConnectivity());
            state = state.setValue(DistillationTankBlock.TOP, getTopConnectivity());
            if(state != this.getBlockState())
                level.setBlock(getBlockPos(), state, 3);
        }
        if(level.getBlockEntity(getBlockPos().below()) instanceof DistillationTankBlockEntity be)
            be.updateVerticalMulti();
    }

    public boolean isBottom() {
        return !(level.getBlockEntity(getBlockPos().below()) instanceof DistillationTankBlockEntity be && isSameMultiBlock(be));
    }
    void checkForRecipes() {
        if(processingTime <= -1) {
            List<Recipe<?>> r = getMatchingRecipes();
            if (!r.isEmpty()) {
                currentRecipe = (DistillationRecipe) r.get(0);
                startProcessing();
            } else {
                currentRecipe = null;
            }
        }
    }

    private boolean isSameMultiBlock(DistillationTankBlockEntity be) {
        DistillationTankBlockEntity otherControllerBE = be.getControllerBE();
        DistillationTankBlockEntity controllerBE = getControllerBE();
        if(otherControllerBE == null || controllerBE == null)
            return false;
        if(otherControllerBE == controllerBE)
            return true;
        if(otherControllerBE.getBlockPos().getX() == controllerBE.getBlockPos().getX() && otherControllerBE.getBlockPos().getZ() == controllerBE.getBlockPos().getZ())
            return otherControllerBE.width == controllerBE.width;
        return false;
    }

    public void updateTemperature() {
        if (!isBottom())
            return;
        if (isController()) {
            highestHeatLevel = getHeat();
            sendData();
            checkForRecipes();
            return;
        }
        DistillationTankBlockEntity be = getControllerBE();
        if(be == null)
            return;
        be.updateTemperature();
    }
}
