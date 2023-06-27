package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.recipes.RecipeRegistry;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

import static com.jesz.createdieselgenerators.blocks.BasinLidBlock.ON_A_BASIN;
import static com.jesz.createdieselgenerators.blocks.BasinLidBlock.OPEN;

public class BasinLidBlockEntity extends BasinOperatingBlockEntity {

    public int procesingTime;
    public boolean running;
    public BasinLidBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("MeltingTime", this.procesingTime);
        compound.putBoolean("Running", this.running);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        this.procesingTime = compound.getInt("MeltingTime");
        this.running = compound.getBoolean("Running");
    }

    @Override
    protected void onBasinRemoved() {
        if (!this.running) return;
        this.procesingTime = 0;
        this.currentRecipe = null;
        this.running = false;
    }

    @Override
    public void tick() {
        super.tick();

        boolean basinPresent = (level.getBlockEntity(worldPosition.below(1)) instanceof BasinBlockEntity);

        if(getBlockState().getValue(ON_A_BASIN) != basinPresent){
            level.setBlock(getBlockPos(), getBlockState().setValue(ON_A_BASIN, basinPresent), 3);
        }

        if (!this.level.isClientSide && (this.currentRecipe == null || this.procesingTime == -1)) {
            this.running = false;
            this.procesingTime = -1;
            this.basinChecker.scheduleUpdate();
        }

        if (this.running && this.level != null) {
            if (!this.level.isClientSide && this.procesingTime <= 0) {
                this.procesingTime = -1;
                this.applyBasinRecipe();
                this.sendData();
            }

            if (this.procesingTime > 0) --this.procesingTime;
        }
    }

    @Override
    protected boolean updateBasin() {
        if (this.running) return true;
        if (this.level == null || this.level.isClientSide) return true;
        if (this.getBasin().filter(BasinBlockEntity::canContinueProcessing).isEmpty()) return true;

        List<Recipe<?>> recipes = this.getMatchingRecipes();
        if (recipes.isEmpty()) return true;
        this.currentRecipe = recipes.get(0);
        this.startProcessingBasin();
        this.sendData();
        return true;
    }

    @Override
    public void startProcessingBasin() {
        if (this.running && this.procesingTime > 0) return;
        super.startProcessingBasin();
        this.running = true;
        this.procesingTime = this.currentRecipe instanceof ProcessingRecipe<?> processed ? processed.getProcessingDuration() : 20;
    }

    @Override
    protected boolean isRunning() {
        return running;
    }


    @Override
    protected Optional<BasinBlockEntity> getBasin() {
        if (level == null)
            return Optional.empty();
        BlockEntity basinBE = level.getBlockEntity(worldPosition.below(1));
        if (!(basinBE instanceof BasinBlockEntity))
            return Optional.empty();
        if(getBlockState().getValue(OPEN))
            return Optional.empty();
        return Optional.of((BasinBlockEntity) basinBE);
    }
    @Override
    protected <C extends Container> boolean matchStaticFilters(Recipe<C> recipe) {
        return recipe.getType() == RecipeRegistry.BASIN_FERMENTING.getType();
    }



    private static final Object BasinFermentingRecipesKey = new Object();
    @Override
    protected Object getRecipeCacheKey() {
        return BasinFermentingRecipesKey;
    }
}
