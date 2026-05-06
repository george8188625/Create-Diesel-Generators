package com.jesz.createdieselgenerators.content.diesel_engine.huge;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import static com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock.POWERED;
import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;

public class HugeDieselEngineBlock extends Block implements IBE<HugeDieselEngineBlockEntity>, IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public HugeDieselEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(POWERED, false));
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        for (EngineUpgrades upgrade : EngineUpgrades.allUpgrades) {
            if (upgrade == EngineUpgrades.EMPTY)
                continue;
            if (upgrade.getItem().is(stack.getItem())) {
                withBlockEntityDo(level, pos, be -> {
                    if (!upgrade.canAddOn(be))
                        return;
                    if(be.upgrade != EngineUpgrades.EMPTY)
                        return;

                    if (!player.isCreative())
                        stack.shrink(1);
                    be.upgrade = upgrade;
                    be.sendData();
                    IWrenchable.playRotateSound(level, pos);
                });
                return ItemInteractionResult.SUCCESS;
            }
        }

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(stack))
            return placementHelper.getOffset(player, level, state, pos, hitResult)
                    .placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);

        if (!CDGConfig.ENGINES_FILLED_WITH_ITEMS.get() || stack.isEmpty() || !(level.getBlockEntity(pos) instanceof SmartBlockEntity be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        IFluidHandler tank = level.getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), null);
        if (tank == null)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (stack.getItem() instanceof BucketItem || stack.getItem() instanceof MilkBucketItem) {
            Fluid fluid = stack.getItem() instanceof BucketItem bi ? bi.content : NeoForgeMod.MILK.get();

            if (!tank.getFluidInTank(0).isEmpty())
                return ItemInteractionResult.FAIL;

            tank.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.EXECUTE);
            if (!player.isCreative())
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));

            return ItemInteractionResult.SUCCESS;
        }

        IFluidHandlerItem itemTank = Capabilities.FluidHandler.ITEM.getCapability(stack, null);
        if(itemTank == null)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        itemTank.drain(tank.fill(itemTank.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        withBlockEntityDo(context.getLevel(), context.getClickedPos(), be -> {
            if(be.upgrade != EngineUpgrades.EMPTY){
                if(!context.getLevel().isClientSide) {
                    if (!context.getPlayer().isCreative())
                        context.getPlayer().getInventory().placeItemBackInInventory(be.upgrade.getItem());
                    be.upgrade = EngineUpgrades.EMPTY;
                    be.sendData();
                    IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
                }
            }
        });
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moving) {
        level.setBlockAndUpdate(pos, state.setValue(POWERED, level.hasNeighborSignal(pos)));
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
                    .isShiftKeyDown() ? nearestLookingDirection : nearestLookingDirection.getOpposite());
        }
        return defaultBlockState().setValue(FACING, preferred.getOpposite());
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
        if (!state.is(newState.getBlock()) && !isMoving) {
            withBlockEntityDo(level, pos, be -> {
                if (be.upgrade != EngineUpgrades.EMPTY)
                    popResource(level, pos, be.upgrade.getItem());
            });
            BlockPos shaftPos = pos.relative(state.getValue(FACING), 2);
            BlockState shaftState = level.getBlockState(shaftPos);
            if (CDGBlocks.POWERED_ENGINE_SHAFT.has(shaftState))
                level.scheduleTick(shaftPos, shaftState.getBlock(), 1);
        }

        if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity()))
            level.removeBlockEntity(pos);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public Class<HugeDieselEngineBlockEntity> getBlockEntityClass() {
        return HugeDieselEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HugeDieselEngineBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.HUGE_DIESEL_ENGINE.get();
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
                    s -> BlockHelper.copyProperties(s, CDGBlocks.POWERED_ENGINE_SHAFT.getDefaultState())
                            .setValue(PoweredShaftBlock.AXIS, axis));
        }
    }
}
