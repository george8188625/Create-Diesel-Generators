package com.jesz.createdieselgenerators.blocks;

import com.simibubi.create.content.contraptions.actors.AttachedActorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class PumpjackHeadBlock extends AttachedActorBlock {
    protected PumpjackHeadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return true;
    }
}
