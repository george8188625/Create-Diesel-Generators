package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.PoweredEngineShaftBlockEntity;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.placement.IPlacementHelper;
import com.simibubi.create.foundation.placement.PlacementHelpers;
import com.simibubi.create.foundation.placement.PlacementOffset;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.POWERED;
import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;

public class HugeDieselEngineBlock extends Block implements IBE<HugeDieselEngineBlockEntity>, IWrenchable, ICDGKinetics, ProperWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public HugeDieselEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(WATERLOGGED, false)
                .setValue(BlockStateProperties.NORTH, false)
                .setValue(BlockStateProperties.EAST, false)
                .setValue(BlockStateProperties.SOUTH, false)
                .setValue(BlockStateProperties.WEST, false)
                .setValue(BlockStateProperties.UP, false)
                .setValue(BlockStateProperties.DOWN, false)
                .setValue(POWERED, false));
    }
    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
         ItemStack itemInHand = player.getItemInHand(hand);

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(itemInHand))
            return placementHelper.getOffset(player, level, state, pos, hit)
                    .placeInWorld(level, (BlockItem) itemInHand.getItem(), player, hand, hit);
        if(!ConfigRegistry.ENGINES_FILLED_WITH_ITEMS.get())
            return super.use(state, level, pos, player, hand, hit);
        if (itemInHand.isEmpty())
            return InteractionResult.PASS;
        if(level.getBlockEntity(pos) instanceof SmartBlockEntity be){
            IFluidHandler tank = be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
            if(tank == null)
                return InteractionResult.PASS;
            if(itemInHand.getItem() instanceof BucketItem bi) {
                if (!tank.getFluidInTank(0).isEmpty())
                    return InteractionResult.FAIL;
                tank.fill(new FluidStack(bi.getFluid(), 1000), IFluidHandler.FluidAction.EXECUTE);
                if(!player.isCreative())
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                return InteractionResult.SUCCESS;
            }
            if(itemInHand.getItem() instanceof MilkBucketItem) {
                if (!tank.getFluidInTank(0).isEmpty())
                    return InteractionResult.FAIL;
                tank.fill(new FluidStack(ForgeMod.MILK.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
                if(!player.isCreative())
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                return InteractionResult.SUCCESS;
            }
            IFluidHandlerItem itemTank = itemInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
            if(itemTank == null)
                return InteractionResult.PASS;
            itemTank.drain(tank.fill(itemTank.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
        }
        return InteractionResult.PASS;
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, FACING, BlockStateProperties.NORTH, BlockStateProperties.EAST, BlockStateProperties.WEST, BlockStateProperties.SOUTH, BlockStateProperties.UP, BlockStateProperties.DOWN, WATERLOGGED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        boolean c = state.getValue(BooleanProperty.create(context.getClickedFace().toString()));
        if(context.getClickedFace().getAxis() == state.getValue(FACING).getAxis())
            return IWrenchable.super.onWrenched(state, context);
        if(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof  HugeDieselEngineBlockEntity be){
            PoweredEngineShaftBlockEntity shaft = be.getShaft();
            if(shaft != null)
                shaft.removeGenerator(context.getClickedPos());
        }
        context.getLevel().setBlock(context.getClickedPos(), state.setValue(BooleanProperty.create(context.getClickedFace().toString()), !c), 3);
        return InteractionResult.SUCCESS;
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return Block.box(1, 1, 1, 15, 15, 15);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moving) {
        if(state.getValue(BooleanProperty.create(state.getValue(FACING).toString())) || state.getValue(BooleanProperty.create(state.getValue(FACING).getOpposite().toString())))
            level.setBlock(pos, state.setValue(BooleanProperty.create(state.getValue(FACING).toString()), false).setValue(BooleanProperty.create(state.getValue(FACING).getOpposite().toString()), false), 3);

        level.setBlock(pos, state.setValue(POWERED, level.hasNeighborSignal(pos)), 2);

        super.neighborChanged(state, level, pos, block, otherPos, moving);
    }

    public Direction getPreferredFacing(BlockPlaceContext context) {
        Direction preferredSide = null;
        for (Direction side : Iterate.directions) {
            BlockState blockState = context.getLevel()
                    .getBlockState(context.getClickedPos()
                            .relative(side));
            if (blockState.getBlock() instanceof IRotate) {
                if (((IRotate) blockState.getBlock()).hasShaftTowards(context.getLevel(), context.getClickedPos()
                        .relative(side), blockState, side.getOpposite()))
                    if (preferredSide != null && preferredSide.getAxis() != side.getAxis()) {
                        preferredSide = null;
                        break;
                    } else {
                        preferredSide = side;
                    }
            }
        }
        return preferredSide;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        if (preferred == null || (context.getPlayer() != null && context.getPlayer()
                .isShiftKeyDown())) {
            Direction nearestLookingDirection = context.getNearestLookingDirection();
            return defaultBlockState().setValue(FACING, context.getPlayer() != null && context.getPlayer()
                    .isShiftKeyDown() ? nearestLookingDirection : nearestLookingDirection.getOpposite()).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
        }
        return defaultBlockState().setValue(FACING, preferred.getOpposite()).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        BlockPos shaftPos = pos.relative(state.getValue(FACING), 2);
        BlockState shaftState = level.getBlockState(shaftPos);
        if(shaftState.getBlock() instanceof ShaftBlock)
            if(shaftState.getValue(AXIS) != state.getValue(FACING).getAxis())
                level.setBlock(shaftPos, PoweredEngineShaftBlock.getEquivalent(shaftState), 3);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity()))
            level.removeBlockEntity(pos);
        BlockPos shaftPos = pos.relative(state.getValue(FACING), 2);
        BlockState shaftState = level.getBlockState(shaftPos);
        if (BlockRegistry.POWERED_ENGINE_SHAFT.has(shaftState))
            level.scheduleTick(shaftPos, shaftState.getBlock(), 1);
    }
    @Override
    public Class<HugeDieselEngineBlockEntity> getBlockEntityClass() {
         return HugeDieselEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HugeDieselEngineBlockEntity> getBlockEntityType() {
        return BlockEntityRegistry.HUGE_DIESEL_ENGINE.get();
    }

    @Override
    public float getDefaultStressCapacity() {
        return 2048;
    }

    @Override
    public float getDefaultStressStressImpact() {
        return 0;
    }

    @Override
    public float getDefaultSpeed() {
        return 96;
    }

    public enum HugeEngineDirection implements StringRepresentable{
        BOTTOM, DOWN, MIDDLE, UP, TOP;

        @Override
        public String getSerializedName() {
            return Lang.asId(name());
        }
    }

    private static class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return AllBlocks.SHAFT::isIn;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return s -> s.getBlock() instanceof HugeDieselEngineBlock;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos,
                                         BlockHitResult ray) {
            BlockPos shaftPos = pos.relative(state.getValue(FACING), 2);
            BlockState shaft = AllBlocks.SHAFT.getDefaultState();
            for (Direction direction : Direction.orderedByNearest(player)) {
                shaft = shaft.setValue(ShaftBlock.AXIS, direction.getAxis());
                if (shaft.getValue(AXIS) != state.getValue(FACING).getAxis())
                    break;
            }

            BlockState newState = level.getBlockState(shaftPos);
            if (!newState.canBeReplaced())
                return PlacementOffset.fail();

            Direction.Axis axis = shaft.getValue(ShaftBlock.AXIS);
            return PlacementOffset.success(shaftPos,
                    s -> BlockHelper.copyProperties(s, BlockRegistry.POWERED_ENGINE_SHAFT.getDefaultState())
                            .setValue(PoweredShaftBlock.AXIS, axis));
        }
    }
}
