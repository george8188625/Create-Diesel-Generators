package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackCrankBlockEntity;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PumpjackCrankBlock extends HorizontalKineticBlock implements IBE<PumpjackCrankBlockEntity>, ICDGKinetics {
    public PumpjackCrankBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        if (state.getValue(HORIZONTAL_FACING).getAxis() == Direction.Axis.X)
            return Shapes.or(Block.box(0, 0, 0, 16, 16, 16), Block.box(4, 16, 0, 12, 22, 16));
        else
            return Shapes.or(Block.box(0, 0, 0, 16, 16, 16), Block.box(0, 16, 4, 16, 22, 12));
    }
    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(HORIZONTAL_FACING)
                .getAxis() == face.getAxis();
    }
    @Override
    public Class<PumpjackCrankBlockEntity> getBlockEntityClass() {
        return PumpjackCrankBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PumpjackCrankBlockEntity> getBlockEntityType() {
        return BlockEntityRegistry.PUMPJACK_CRANK.get();
    }
    @Override
    public float getDefaultStressCapacity() {
        return 0;
    }

    @Override
    public float getDefaultStressStressImpact() {
        return 16;
    }

    @Override
    public float getDefaultSpeed() {
        return 0;
    }
}
