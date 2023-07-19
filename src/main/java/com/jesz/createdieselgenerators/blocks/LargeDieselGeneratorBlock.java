package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.LargeDieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;

public class LargeDieselGeneratorBlock extends HorizontalKineticBlock implements IBE<LargeDieselGeneratorBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public static final BooleanProperty PIPE = BooleanProperty.create("pipe");

    public LargeDieselGeneratorBlock(Properties properties) {
        super(properties);
        registerDefaultState(super.defaultBlockState().setValue(POWERED, false));
        registerDefaultState(super.defaultBlockState().setValue(PIPE, true));
    }
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if(context.getClickedFace() == Direction.UP){
            if(state.getValue(PIPE)) {
                context.getLevel().setBlock(context.getClickedPos(), state.setValue(PIPE, false), 3);
            }else{
                context.getLevel().setBlock(context.getClickedPos(), state.setValue(PIPE, true), 3);
            }
            return InteractionResult.SUCCESS;
        }
        Level world = context.getLevel();
        BlockState rotated = getRotatedBlockState(state, context.getClickedFace());
        if (!rotated.canSurvive(world, context.getClickedPos()))
            return InteractionResult.PASS;

        KineticBlockEntity.switchToBlockState(world, context.getClickedPos(), updateAfterWrenched(rotated, context));

        BlockEntity be = context.getLevel()
                .getBlockEntity(context.getClickedPos());
        if (be instanceof GeneratingKineticBlockEntity) {
            ((GeneratingKineticBlockEntity) be).reActivateSource = true;
        }

        if (world.getBlockState(context.getClickedPos()) != state)
            playRotateSound(world, context.getClickedPos());

        return InteractionResult.SUCCESS;
    }
    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState;
    }
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        builder.add(PIPE);
        super.createBlockStateDefinition(builder);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        if(pContext.getPlayer().isShiftKeyDown())
            return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
        else
            return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }
    @Override
    public Class<LargeDieselGeneratorBlockEntity> getBlockEntityClass() {
        return LargeDieselGeneratorBlockEntity.class;
    }
    @Override
    public BlockEntityType<? extends LargeDieselGeneratorBlockEntity> getBlockEntityType() {
        return BlockEntityRegistry.LARGE_DIESEL_ENGINE.get();
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {

        if (pState.getValue(FACING) == NORTH || pState.getValue(FACING) == SOUTH){
            return Shapes.or(Block.box(0,0,0,16,16,16), Block.box(-2,0,0,18,4,16));
        }else{
            return Shapes.or(Block.box(0,0,0,16,16,16), Block.box(0,0,-2,16,4,18));
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
