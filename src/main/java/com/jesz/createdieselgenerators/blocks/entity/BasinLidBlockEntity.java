package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.recipes.RecipeRegistry;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Optional;

import static com.jesz.createdieselgenerators.blocks.BasinLidBlock.OPEN;

public class BasinLidBlockEntity extends BasinOperatingBlockEntity {

    public int processingTime;
    public boolean running;
    public BasinLidBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("MeltingTime", this.processingTime);
        compound.putBoolean("Running", this.running);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        this.processingTime = compound.getInt("MeltingTime");
        this.running = compound.getBoolean("Running");
    }

    @Override
    protected void onBasinRemoved() {
        if (!this.running) return;
        this.processingTime = 0;
        this.currentRecipe = null;
        this.running = false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level.isClientSide && (this.currentRecipe == null || this.processingTime == -1)) {
            this.running = false;
            this.processingTime = -1;
            this.basinChecker.scheduleUpdate();
        }

        if (this.running && this.level != null) {
            if (!this.level.isClientSide && this.processingTime <= 0) {
                this.processingTime = -1;
                this.applyBasinRecipe();
                this.sendData();
            }
            if(!this.level.isClientSide && processingTime % 20 == 0){
                level.playSound(null, worldPosition, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
                        SoundSource.BLOCKS, .75f, speed < 65 ? .75f : 1.5f);
            }
            if (this.processingTime > 0) --this.processingTime;
        }
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        super.tickAudio();

        boolean slow = Math.abs(getSpeed()) < 65;
        if (slow && AnimationTickHolder.getTicks() % 2 == 0)
            return;
        if (processingTime == 20)
            AllSoundEvents.MIXING.playAt(level, worldPosition, .75f, 1, true);
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
        if (this.running && this.processingTime > 0) return;
        super.startProcessingBasin();
        this.running = true;
        this.processingTime = this.currentRecipe instanceof ProcessingRecipe<?> processed ? processed.getProcessingDuration() : 20;
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
