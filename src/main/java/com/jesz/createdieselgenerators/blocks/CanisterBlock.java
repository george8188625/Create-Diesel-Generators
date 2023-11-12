package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.CanisterBlockEntity;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CanisterBlock extends Block implements IBE<CanisterBlockEntity>, ProperWaterloggedBlock, IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public CanisterBlock(Properties properties) {
        super(properties);
        registerDefaultState(super.defaultBlockState().setValue(WATERLOGGED, false));

    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide)
            return;
        if (stack == null)
            return;
        withBlockEntityDo(level, pos, be -> {
            be.setCapacityEnchantLevel(stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get()));
            if (stack.isEnchanted())
                be.setEnchantmentTag(stack.getEnchantmentTags());
            if (stack.hasCustomHoverName())
                be.setCustomName(stack.getHoverName());
        });
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        if(state.getValue(FACING).getAxis() == Direction.Axis.Y)
            return Block.box(2, 0, 2, 14, 16, 14);
        else if(state.getValue(FACING).getAxis() == Direction.Axis.X)
            return Block.box(0, 2, 2, 16, 14, 14);
        else
            return Block.box(2, 2, 0, 14, 14, 16);

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, WATERLOGGED);
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
    public Class<CanisterBlockEntity> getBlockEntityClass() {
        return CanisterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CanisterBlockEntity> getBlockEntityType() {
        return BlockEntityRegistry.CANISTER.get();
    }
}
