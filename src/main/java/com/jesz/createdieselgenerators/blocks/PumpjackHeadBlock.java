package com.jesz.createdieselgenerators.blocks;

import com.simibubi.create.content.contraptions.actors.AttachedActorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PumpjackHeadBlock extends AttachedActorBlock {
    protected PumpjackHeadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if(state.getValue(FACING) == Direction.SOUTH)
            return Block.box(0, 2, 0, 16, 14, 2);
        if(state.getValue(FACING) == Direction.NORTH)
            return Block.box(0, 2, 14, 16, 14, 16);
        if(state.getValue(FACING) == Direction.EAST)
            return Block.box(0, 2, 0, 2, 14, 16);
        return Block.box(14, 2, 0, 16, 14, 16);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return true;
    }
}
