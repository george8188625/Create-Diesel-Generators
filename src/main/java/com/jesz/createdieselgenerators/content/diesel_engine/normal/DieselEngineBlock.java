package com.jesz.createdieselgenerators.content.diesel_engine.normal;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;

public class DieselEngineBlock extends DirectionalKineticBlock implements SpecialBlockItemRequirement, IBE<DieselEngineBlockEntity>, ProperWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public DieselEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                super.defaultBlockState()
                        .setValue(WATERLOGGED, false)
                        .setValue(POWERED, false));

    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withWater(super.getStateForPlacement(context), context);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
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
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, POWERED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState otherState,
                                  LevelAccessor level, BlockPos pos, BlockPos otherPos) {
        updateWater(level, state, pos);
        return state;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moving) {
        level.setBlock(pos, state.setValue(POWERED, level.hasNeighborSignal(pos)), 2);
        super.neighborChanged(state, level, pos, block, otherPos, moving);
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if(state.hasBlockEntity())
            withBlockEntityDo(worldIn, pos, be -> {
                if(worldIn.getBlockEntity(pos.relative(state.getValue(FACING))) instanceof DieselEngineBlockEntity nbe && nbe.getBlockState().getValue(FACING) == state.getValue(FACING))
                    be.movementDirection.setValue(nbe.movementDirection.getValue());
                if(worldIn.getBlockEntity(pos.relative(state.getValue(FACING).getOpposite())) instanceof DieselEngineBlockEntity nbe && nbe.getBlockState().getValue(FACING) == state.getValue(FACING))
                    be.movementDirection.setValue(nbe.movementDirection.getValue());
                if(worldIn.getBlockEntity(pos.relative(state.getValue(FACING))) instanceof DieselEngineBlockEntity nbe && nbe.getBlockState().getValue(FACING) == state.getValue(FACING).getOpposite())
                    be.movementDirection.setValue(nbe.movementDirection.getValue() == 1 ? 0 : 1);
                if(worldIn.getBlockEntity(pos.relative(state.getValue(FACING).getOpposite())) instanceof DieselEngineBlockEntity nbe && nbe.getBlockState().getValue(FACING) == state.getValue(FACING).getOpposite())
                    be.movementDirection.setValue(nbe.movementDirection.getValue() == 1 ? 0 : 1);
            });

        super.onPlace(state, worldIn, pos, oldState, isMoving);
    }

    @Override
    public Class<DieselEngineBlockEntity> getBlockEntityClass() {
        return DieselEngineBlockEntity.class;
    }
    @Override
    public BlockEntityType<? extends DieselEngineBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.DIESEL_ENGINE.get();
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

                    if(!player.isCreative())
                        stack.shrink(1);
                    be.upgrade = upgrade;
                    IWrenchable.playRotateSound(level, pos);
                });
                return ItemInteractionResult.SUCCESS;
            }
        }

        if(!CDGConfig.ENGINES_FILLED_WITH_ITEMS.get() || stack.isEmpty() || !(level.getBlockEntity(pos) instanceof SmartBlockEntity be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        IFluidHandler tank = level.getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), null);
        if(tank == null)
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        if (state.getValue(FACING) == NORTH || state.getValue(FACING) == SOUTH){
            return Shapes.or(Block.box(3, 3, 0, 13, 13, 16), Block.box(0,0,0,16,4,16));
        }else if(state.getValue(FACING) == Direction.DOWN){
            return Shapes.or(Block.box(3,0,3, 13, 16, 13), Block.box(0, 4, 4, 16, 12, 12));
        }else if(state.getValue(FACING) == Direction.UP){
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

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
        List<ItemStack> list = new ArrayList<>();
        list.add(CDGBlocks.DIESEL_ENGINE.asStack());
        if(blockEntity instanceof DieselEngineBlockEntity be) {
            ItemStack upgradeItem = be.upgrade.getItem();
            if(!upgradeItem.isEmpty())
                list.add(upgradeItem);
        }
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, list);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())&& !isMoving)
            withBlockEntityDo(level, pos, be -> {
                if (be.upgrade != EngineUpgrades.EMPTY)
                    popResource(level, pos, be.upgrade.getItem());
            });
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
