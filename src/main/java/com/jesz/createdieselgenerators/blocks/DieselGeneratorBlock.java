package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.DieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.schematics.requirement.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.jesz.createdieselgenerators.items.ItemRegistry.ENGINE_SILENCER;
import static com.jesz.createdieselgenerators.items.ItemRegistry.ENGINE_TURBO;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;

public class DieselGeneratorBlock extends DirectionalKineticBlock implements ISpecialBlockItemRequirement, IBE<DieselGeneratorBlockEntity>, ProperWaterloggedBlock, ICDGKinetics {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty SILENCED = BooleanProperty.create("silenced");
    public static final BooleanProperty TURBOCHARGED = BooleanProperty.create("turbocharged");
    public enum EngineTypes{
        NORMAL(ConfigRegistry.NORMAL_ENGINES), MODULAR(ConfigRegistry.MODULAR_ENGINES), HUGE(ConfigRegistry.HUGE_ENGINES);

        final Supplier<Boolean> isEnabled;
        EngineTypes(Supplier<Boolean> isEnabled){
            this.isEnabled = isEnabled;
        }
        public boolean enabled(){
            return isEnabled.get();
        }
    }
    public DieselGeneratorBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                super.defaultBlockState()
                        .setValue(WATERLOGGED, false)
                        .setValue(SILENCED, false)
                        .setValue(TURBOCHARGED, false)
                        .setValue(POWERED, false));

    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if(state.getValue(SILENCED))
            if(context.getPlayer() != null && !context.getLevel().isClientSide) {
                if (!context.getPlayer().isCreative())
                    context.getPlayer().getInventory().placeItemBackInInventory(ENGINE_SILENCER.asStack());
                context.getLevel().setBlock(context.getClickedPos(), state.setValue(SILENCED, false), 3);
                playRotateSound(context.getLevel(), context.getClickedPos());
                return InteractionResult.SUCCESS;
            }
        if(state.getValue(TURBOCHARGED))
            if(context.getPlayer() != null && !context.getLevel().isClientSide) {
                if (!context.getPlayer().isCreative())
                    context.getPlayer().getInventory().placeItemBackInInventory(ENGINE_TURBO.asStack());
                context.getLevel().setBlock(context.getClickedPos(), state.setValue(TURBOCHARGED, false), 3);
                playRotateSound(context.getLevel(), context.getClickedPos());
                return InteractionResult.SUCCESS;
            }


        return super.onWrenched(state,context);
    }
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, SILENCED, TURBOCHARGED, POWERED);
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
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moving) {
        level.setBlock(pos, state.setValue(POWERED, level.hasNeighborSignal(pos)), 2);
        super.neighborChanged(state, level, pos, block, otherPos, moving);
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if(state.hasBlockEntity())
            withBlockEntityDo(worldIn, pos, be -> {
                if(worldIn.getBlockEntity(pos.relative(state.getValue(FACING))) instanceof DieselGeneratorBlockEntity nbe && nbe.getBlockState().getValue(FACING) == state.getValue(FACING))
                    be.movementDirection.setValue(nbe.movementDirection.getValue());
                if(worldIn.getBlockEntity(pos.relative(state.getValue(FACING).getOpposite())) instanceof DieselGeneratorBlockEntity nbe && nbe.getBlockState().getValue(FACING) == state.getValue(FACING))
                    be.movementDirection.setValue(nbe.movementDirection.getValue());
                if(worldIn.getBlockEntity(pos.relative(state.getValue(FACING))) instanceof DieselGeneratorBlockEntity nbe && nbe.getBlockState().getValue(FACING) == state.getValue(FACING).getOpposite())
                    be.movementDirection.setValue(nbe.movementDirection.getValue() == 1 ? 0 : 1);
                if(worldIn.getBlockEntity(pos.relative(state.getValue(FACING).getOpposite())) instanceof DieselGeneratorBlockEntity nbe && nbe.getBlockState().getValue(FACING) == state.getValue(FACING).getOpposite())
                    be.movementDirection.setValue(nbe.movementDirection.getValue() == 1 ? 0 : 1);
            });

        super.onPlace(state, worldIn, pos, oldState, isMoving);
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if(ENGINE_SILENCER.isIn(itemInHand) && !state.getValue(SILENCED) && !state.getValue(TURBOCHARGED)) {
            if(!player.isCreative())
                itemInHand.shrink(1);
            level.setBlock(pos, state.setValue(SILENCED, true), 3);
            playRotateSound(level, pos);
            return InteractionResult.SUCCESS;
        }
        if(ENGINE_TURBO.isIn(itemInHand) && !state.getValue(TURBOCHARGED) && !state.getValue(SILENCED)) {
            if(!player.isCreative())
                itemInHand.shrink(1);
            level.setBlock(pos, state.setValue(TURBOCHARGED, true), 3);
            playRotateSound(level, pos);
            return InteractionResult.SUCCESS;
        }
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
        return super.use(state, level, pos, player, hand, hit);
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

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
        List<ItemStack> list = new ArrayList<>();
        list.add(BlockRegistry.DIESEL_ENGINE.asStack());
        if(state.getValue(SILENCED))
            list.add(ItemRegistry.ENGINE_SILENCER.asStack());
        if(state.getValue(TURBOCHARGED))
            list.add(ItemRegistry.ENGINE_TURBO.asStack());
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, list);
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
}
