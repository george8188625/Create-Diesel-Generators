package com.jesz.createdieselgenerators.content.bulk_fermenter;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGRecipes;
import com.jesz.createdieselgenerators.content.distillation.DistillationRecipe;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryWrapper;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BulkFermenterBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainerFluidItem, IHaveGoggleInformation {

    private static final int MAX_SIZE = 3;
    IItemHandler itemCapability;
    public ItemStackHandler inventory;
    IFluidHandler fluidCapability;
    BulkFermenterFluidHandler tankInventory;
    BlockPos controller;
    BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected boolean updateCapability;
    int width = 1;
    int height = 1;

    private static final int SYNC_RATE = 8;
    int syncCooldown;
    boolean queuedSync;

    public int processingTime = -1;
    BulkFermentingRecipe currentRecipe;

    BlazeBurnerBlock.HeatLevel highestHeatLevel = BlazeBurnerBlock.HeatLevel.NONE;
    public BulkFermenterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tankInventory = createInventory();
        updateConnectivity = false;
        updateCapability = false;
        inventory = new ItemStackHandler(5) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);

                List<Recipe<?>> r = getMatchingRecipes();
                if (!r.contains(currentRecipe)) {
                    processingTime = -1;
                }
                if (processingTime == -1 && !r.isEmpty()) {
                    currentRecipe = (BulkFermentingRecipe) r.get(0);
                    startProcessing();
                }

                if (!level.isClientSide) {
                    setChanged();
                    sendData();
                }
            }
        };
        refreshCapability();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    protected BulkFermenterFluidHandler createInventory() {
        return new BulkFermenterFluidHandler(6, getCapacityMultiplier(), f -> onFluidStackChanged());
    }

    public void updateConnectivity() {
        updateConnectivity = false;
        if (level.isClientSide)
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    private void startProcessing() {
        if(currentRecipe == null)
            return;
        processingTime = (currentRecipe.getProcessingDuration());
        sendData();
    }
    @Override
    public void tick() {

        if (isController()) {
            if (processingTime >= 0) {
                if(!level.isClientSide && processingTime % 20 == 0 && new Random().nextInt() % 4 == 0)
                    level.playSound(null, worldPosition.offset(width/2, height/2, width/2), SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,
                        SoundSource.BLOCKS, .15f, .75f);

                if (processingTime == 1)
                    level.playSound(null, worldPosition.offset(width/2, height/2, width/2), SoundEvents.BREWING_STAND_BREW,
                            SoundSource.BLOCKS, .15f, .75f);

                if (currentRecipe == null) {
                    List<Recipe<?>> r = getMatchingRecipes();
                    if (r.isEmpty())
                        processingTime = -1;
                    else
                        currentRecipe = (BulkFermentingRecipe) r.get(0);
                } else {
                   if (processingTime == 0 && !level.isClientSide) {
                       currentRecipe.apply(this, false);

                       processingTime = -1;
                   } else {
                       processingTime = (int) Math.max(0, processingTime - Math.sqrt(width * height));
                   }
                }
            }
            if (processingTime == -1) {
                if (currentRecipe != null) {
                    List<Recipe<?>> r = getMatchingRecipes();
                    currentRecipe = null;
                    if (!r.contains(currentRecipe)) {
                        processingTime = -1;
                    }
                    if (processingTime == -1 && !r.isEmpty()) {
                        currentRecipe = (BulkFermentingRecipe) r.get(0);
                        startProcessing();
                    }

                    if (!level.isClientSide) {
                        setChanged();
                        sendData();
                    }
                }
            }
        }
        super.tick();
        if (syncCooldown > 0) {
            syncCooldown--;
            if (syncCooldown == 0 && queuedSync) {
                sendData();
            }
        }

        if (lastKnownPos == null)
            lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition) && worldPosition != null) {
            onPositionChanged();
            return;
        }

        if (updateConnectivity)
            updateConnectivity();

        if (updateCapability) {
            updateCapability = false;
            refreshCapability();
        }
    }

    protected List<Recipe<?>> getMatchingRecipes() {
        List<RecipeHolder<? extends Recipe<?>>> list = RecipeFinder.get(RECIPE_CACHE_KEY, level, recipe -> recipe.value().getType() == CDGRecipes.BULK_FERMENTING.getType());
        return list.stream()
                .map(RecipeHolder::value)
                .sorted((r1, r2) -> {
                    if(r1 instanceof DistillationRecipe recipe1 && r2 instanceof DistillationRecipe recipe2)
                        return recipe2.getRequiredHeat().ordinal() - recipe1.getRequiredHeat().ordinal();
                    return 0;
                })
                .filter(r -> ((BulkFermentingRecipe) r).apply(this, true))
                .collect(Collectors.toList());

    }

    static final Object RECIPE_CACHE_KEY = new Object();

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.getX() == controller.getX()
                && worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
    }

    @Override
    public void initialize() {
        super.initialize();
        sendData();
    }

    private void onPositionChanged() {
        removeController(true);
        lastKnownPos = worldPosition;
    }

    protected void onFluidStackChanged() {
        if (!hasLevel())
            return;

        List<Recipe<?>> r = getMatchingRecipes();
        if (!r.contains(currentRecipe)) {
            processingTime = -1;
        }
        if (processingTime == -1 && !r.isEmpty()) {
            currentRecipe = (BulkFermentingRecipe) r.get(0);
            startProcessing();
        }


        for (int yOffset = 0; yOffset < height; yOffset++) {
            for (int xOffset = 0; xOffset < width; xOffset++) {
                for (int zOffset = 0; zOffset < width; zOffset++) {
                    BlockPos pos = this.worldPosition.offset(xOffset, yOffset, zOffset);
                    BulkFermenterBlockEntity tankAt = ConnectivityHandler.partAt(getType(), level, pos);
                    if (tankAt == null)
                        continue;
                    level.updateNeighbourForOutputSignal(pos, tankAt.getBlockState()
                            .getBlock());
                }
            }
        }

        if (!level.isClientSide) {
            setChanged();
            sendData();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public BulkFermenterBlockEntity getControllerBE() {
        if (isController())
            return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof BulkFermenterBlockEntity)
            return (BulkFermenterBlockEntity) blockEntity;
        return null;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        if (isController())
            return super.createRenderBoundingBox().expandTowards(width - 1, 0, width - 1);
        else
            return super.createRenderBoundingBox();
    }

    public void applyFluidTankSize(int blocks) {
        tankInventory.setCapacity(blocks * getCapacityMultiplier());
    }

    public void removeController(boolean keepContents) {
        if (level.isClientSide)
            return;
        updateConnectivity = true;
        if (!keepContents)
            applyFluidTankSize(1);
        controller = null;
        width = 1;
        height = 1;

        onFluidStackChanged();
        refreshCapability();
        setChanged();
        sendData();
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
        fluidCapability = handlerForCapability();
        invalidateCapabilities();
    }

    void initCapability() {
        if (!isController()) {
            BulkFermenterBlockEntity controllerBE = getControllerBE();
            if (controllerBE == null)
                return;
            controllerBE.initCapability();
            itemCapability = controllerBE.itemCapability;
            return;
        }
        IItemHandlerModifiable[] inventories = new IItemHandlerModifiable[height * width * width];
        for (int yOffset = 0; yOffset < height; yOffset++) {
            for (int xOffset = 0; xOffset < width; xOffset++) {
                for (int zOffset = 0; zOffset < width; zOffset++) {
                    BlockPos vaultPos = worldPosition.offset(xOffset, yOffset, zOffset);
                    BulkFermenterBlockEntity tankAt =
                            ConnectivityHandler.partAt(CDGBlockEntityTypes.BULK_FERMENTER.get(), level, vaultPos);
                    inventories[yOffset * width * width + xOffset * width + zOffset] =
                            tankAt != null ? tankAt.inventory : new ItemStackHandler();
                }
            }
        }

        IItemHandler itemHandler = new VersionedInventoryWrapper(new CombinedInvWrapper(inventories) {
            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (stack.isEmpty())
                    return ItemStack.EMPTY;

                for (int i = 0; i < getSlots(); i++) {
                    ItemStack existing = getStackInSlot(i);
                    if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                        int limit = Math.min(stack.getMaxStackSize(), getSlotLimit(i));
                        int space = limit - existing.getCount();
                        if (space <= 0)
                            continue;

                        ItemStack toInsert = stack.copyWithCount(Math.min(stack.getCount(), space));
                        ItemStack remainder = super.insertItem(i, toInsert, simulate);
                        int inserted = toInsert.getCount() - remainder.getCount();
                        if (inserted > 0) {
                            return stack.getCount() <= inserted
                                    ? ItemStack.EMPTY
                                    : stack.copyWithCount(stack.getCount() - inserted);
                        }
                    }
                }

                for (int i = 0; i < getSlots(); i++) {
                    if (getStackInSlot(i).isEmpty()) {
                        return super.insertItem(i, stack, simulate);
                    }
                }

                return stack;
            }
        });
        itemCapability = itemHandler;
        invalidateCapabilities();
    }
    private IFluidHandler handlerForCapability() {
        return isController() ? tankInventory
                : getControllerBE() != null ? getControllerBE().handlerForCapability() : new BulkFermenterFluidHandler(0, 0, fs -> {});
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = width;
        int prevHeight = height;

        updateConnectivity = tag.contains("Uninitialized");
        controller = null;
        lastKnownPos = null;

        if (tag.contains("LastKnownPos"))
            lastKnownPos = NBTHelper.readBlockPos(tag, "LastKnownPos");
        if (tag.contains("Controller"))
            controller = NBTHelper.readBlockPos(tag, "Controller");

        if (isController()) {
            width = tag.getInt("Size");
            height = tag.getInt("Height");
            highestHeatLevel = BlazeBurnerBlock.HeatLevel.values()[tag.getInt("Heat")];
            tankInventory.setCapacity(getTotalTankSize() * getCapacityMultiplier());
            tankInventory.readFromNBT(registries, tag.getCompound("TankContent"));

            processingTime = tag.getInt("ProcessingTime");
        }

        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));

        updateCapability = true;

        if (!clientPacket)
            return;

        boolean changeOfController =
                !Objects.equals(controllerBefore, controller);

        if (hasLevel() && (changeOfController || prevSize != width || prevHeight != height)) {
            level.setBlocksDirty(getBlockPos(), Blocks.AIR.defaultBlockState(), getBlockState());

            if (isController()) {
                tankInventory.setCapacity(getCapacityMultiplier() * getTotalTankSize());
                invalidateRenderBoundingBox();
            }
        }

    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        if (updateConnectivity)
            tag.putBoolean("Uninitialized", true);
        if (lastKnownPos != null)
            tag.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController())
            tag.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {
            tag.put("TankContent", tankInventory.writeToNBT(registries, new CompoundTag()));
            tag.putInt("Size", width);
            tag.putInt("Height", height);
            tag.putInt("ProcessingTime", processingTime);
            tag.putInt("Heat", highestHeatLevel.ordinal());
        }
        tag.put("Inventory", inventory.serializeNBT(registries));

        if (!clientPacket)
            return;

        if (queuedSync)
            tag.putBoolean("LazySync", true);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CDGBlockEntityTypes.BULK_FERMENTER.get(),
                (be, side) -> {
                    if (be.fluidCapability == null)
                        be.refreshCapability();
                    return be.fluidCapability;
                }
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CDGBlockEntityTypes.BULK_FERMENTER.get(),
                (be, side) -> {
                    be.initCapability();
                    return be.itemCapability;
                }
        );
    }

    public int getTotalTankSize() {
        return width * width * height;
    }

    public static int getCapacityMultiplier() {
        return 1000;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        onFluidStackChanged();
        setChanged();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return Direction.Axis.Y;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        return AllConfigs.server().fluids.fluidTankCapacity.get();
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
        return tankInventory.tanks.get(tank);
    }

    @Override
    public FluidStack getFluid(int tank) {
        return tankInventory.getFluidInTank(tank)
                .copy();
    }

    public BulkFermentingRecipe getRecipe() {
        return currentRecipe;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        BulkFermenterBlockEntity controller = getControllerBE();

        if (controller == null)
            return false;

        IItemHandler items = level.getCapability(Capabilities.ItemHandler.BLOCK, controller.getBlockPos(), null);
        IFluidHandler fluids = level.getCapability(Capabilities.FluidHandler.BLOCK, controller.getBlockPos(), null);

        if (items == null || fluids == null)
            return false;

        boolean isEmpty = true;

        CreateLang.translate("gui.goggles.basin_contents")
                .forGoggles(tooltip);

        Map<Item, Integer> allItems = new HashMap<>();
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stackInSlot = items.getStackInSlot(i);
            if (stackInSlot.isEmpty())
                continue;
            if (allItems.containsKey(stackInSlot.getItem()))
                allItems.replace(stackInSlot.getItem(), stackInSlot.getCount() + allItems.get(stackInSlot.getItem()));
            else
                allItems.put(stackInSlot.getItem(), stackInSlot.getCount());
            isEmpty = false;
        }

        for (Map.Entry<Item, Integer> e : allItems.entrySet()) {
            CreateLang.text("")
                    .add(Component.translatable(e.getKey().getDescriptionId())
                            .withStyle(ChatFormatting.GRAY))
                    .add(CreateLang.text(" x" + e.getValue())
                            .style(ChatFormatting.GREEN))
                    .forGoggles(tooltip, 1);
        }

        LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
        for (int i = 0; i < fluids.getTanks(); i++) {
            FluidStack fluidStack = fluids.getFluidInTank(i);
            if (fluidStack.isEmpty())
                continue;
            CreateLang.text("")
                    .add(CreateLang.fluidName(fluidStack)
                            .add(CreateLang.text(" "))
                            .style(ChatFormatting.GRAY)
                            .add(CreateLang.number(fluidStack.getAmount())
                                    .add(mb)
                                    .style(ChatFormatting.BLUE)))
                    .forGoggles(tooltip, 1);
            isEmpty = false;
        }

        if (isEmpty)
            tooltip.remove(0);

        return true;
    }
    public void updateHeat() {
        BulkFermenterBlockEntity controller = getControllerBE();
        int width;
        if (controller == null)
            width = 1;
        else {
            if (controller != this) {
                controller.updateHeat();
                return;
            }
            width = controller.width;
        }

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
        highestHeatLevel = highestHeat;

        List<Recipe<?>> r = getMatchingRecipes();
        if (!r.contains(currentRecipe)) {
            processingTime = -1;
        }
        if (processingTime == -1 && !r.isEmpty()) {
            currentRecipe = (BulkFermentingRecipe) r.get(0);
            startProcessing();
        }

        if (!level.isClientSide) {
            setChanged();
            sendData();
        }
    }


    public static class BulkFermenterFluidHandler implements IFluidHandler {
        int tankCount;
        NonNullList<FluidTank> tanks = NonNullList.create();
        Consumer<FluidStack> updateCallback;

        public BulkFermenterFluidHandler(int tankCount, int capacity, Consumer<FluidStack> updateCallback){
            for (int i = 0; i < tankCount; i++)
                tanks.add(new FluidTank(capacity));

            this.tankCount = tankCount;
            this.updateCallback = updateCallback;
        }
        @Override
        public int getTanks() {
            return tankCount;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tanks.get(tank).getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return tanks.get(tank).getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            for (FluidTank tank : tanks) {
                if (FluidStack.isSameFluidSameComponents(tank.getFluid(), resource)) {
                    int result = tank.fill(resource, action);
                    if (action.execute())
                        updateCallback.accept(tank.getFluid());
                    return result;
                }
            }

            for (FluidTank tank : tanks) {
                if (tank.getFluid().isEmpty()) {
                    int result = tank.fill(resource, action);
                    if (action.execute())
                        updateCallback.accept(tank.getFluid());
                    return result;
                }
            }
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            for (FluidTank tank : tanks) {
                if (FluidStack.isSameFluidSameComponents(tank.getFluid(), resource)) {
                    FluidStack result = tank.drain(resource, action);
                    if (action.execute())
                        updateCallback.accept(tank.getFluid());
                    return result;
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            for (FluidTank tank : tanks) {
                if (!tank.getFluid().isEmpty()) {
                    FluidStack result = tank.drain(maxDrain, action);
                    if (action.execute())
                        updateCallback.accept(tank.getFluid());
                    return result;
                }
            }
            return FluidStack.EMPTY;
        }

        public CompoundTag writeToNBT(HolderLookup.Provider registries, CompoundTag compound) {
            ListTag list = new ListTag();
            for (FluidTank tank : tanks)
                list.add(tank.writeToNBT(registries, new CompoundTag()));

            compound.put("Tanks", list);
            return compound;
        }

        public void readFromNBT(HolderLookup.Provider registries, CompoundTag compound) {
            for (int i = 0; i < tanks.size(); i++) {
                FluidTank tank = tanks.get(i);
                tank.readFromNBT(registries, compound.getList("Tanks", Tag.TAG_COMPOUND).getCompound(i));
            }
        }

        public void setCapacity(int capacity) {
            for (FluidTank tank : tanks) {
                tank.setCapacity(capacity);
                tank.drain(Math.max(0, tank.getFluidAmount() - capacity), FluidAction.EXECUTE);
            }
        }
    }
}
