package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.DieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;

public class DieselGeneratorBlock extends DirectionalKineticBlock implements IBE<DieselGeneratorBlockEntity>, ProperWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public DieselGeneratorBlock(Properties properties) {
        super(properties);
        registerDefaultState(super.defaultBlockState().setValue(POWERED, false));
        registerDefaultState(super.defaultBlockState().setValue(WATERLOGGED, false));
    }
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        builder.add(WATERLOGGED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
                                  LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        updateWater(pLevel, pState, pCurrentPos);
        return pState;
    }

    @Override
    public Class<DieselGeneratorBlockEntity> getBlockEntityClass() {
        return DieselGeneratorBlockEntity.class;
    }
    @Override
    public BlockEntityType<? extends DieselGeneratorBlockEntity> getBlockEntityType() {
        return BlockEntityRegistry.DIESEL_ENGINE.get();
    }




    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {

        if (pState.getValue(FACING) == NORTH || pState.getValue(FACING) == SOUTH){
            return Shapes.or(Block.box(3, 3, 0, 13, 13, 16), Block.box(0,0,0,16,4,16));
        }else if(pState.getValue(FACING) == Direction.DOWN){
            return Shapes.or(Block.box(3,0,3, 13, 16, 13), Block.box(0, 4, 4, 16, 12, 12));
        }else if(pState.getValue(FACING) == Direction.UP){
            return Shapes.or(Block.box(3,0,3, 13, 16, 13), Block.box(4, 4, 0, 12, 12, 16));
        }else{
            return Shapes.or(Block.box(0, 3, 3, 16, 13, 13), Block.box(0,0,0,16,4,16));
        }
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(FACING)
                .getAxis() == face.getAxis();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState blockState) {
        return blockState.getValue(FACING)
                .getAxis();
    }
}
