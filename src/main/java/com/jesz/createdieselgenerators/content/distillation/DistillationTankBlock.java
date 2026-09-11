package com.jesz.createdieselgenerators.content.distillation;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

import static com.jesz.createdieselgenerators.CDGItems.DISTILLATION_CONTROLLER;

public class DistillationTankBlock extends Block implements IBE<DistillationTankBlockEntity>, IWrenchable, SpecialBlockItemRequirement {
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    public static final EnumProperty<FluidTankBlock.Shape> SHAPE = EnumProperty.create("shape", FluidTankBlock.Shape.class);

    public DistillationTankBlock(Properties properties) {
        super(properties);
    }

    public static boolean isTank(BlockState state) {
        return state.getBlock() instanceof DistillationTankBlock;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {

        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof DistillationTankBlockEntity dtbe){
            int width = dtbe.getControllerBE().getWidth();
            BlockPos pos = dtbe.getController();
            IFluidHandler tank = context.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, dtbe.getBlockPos(), null);
            FluidStack stackInTank = tank == null ? FluidStack.EMPTY : tank.getFluidInTank(0);

            for (int x = 0; x < width; x++) {
                for (int z = 0; z < width; z++) {
                    context.getLevel().setBlockAndUpdate(pos.offset(x, 0, z), AllBlocks.FLUID_TANK.getDefaultState());
                    context.getLevel().updateNeighborsAt(pos.offset(x, 0, z), AllBlocks.FLUID_TANK.get());
                    if (context.getLevel().isClientSide) {
                        for (int i = 0; i < 30; i++) {
                            Vec3 offset = VecHelper.offsetRandomly(VecHelper.getCenterOf(pos.offset(x, 0, z)), context.getLevel().getRandom(), .3f);
                            Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, context.getLevel().getRandom(), .1f);
                            context.getLevel().addParticle(new ItemParticleOption(ParticleTypes.ITEM, DISTILLATION_CONTROLLER.asStack()), offset.x(), offset.y(),
                                    offset.z(), motion.x(), motion.y(), motion.z());
                        }
                    }
                }
            }
            AllSoundEvents.WRENCH_REMOVE.playAt(context.getLevel(), pos.getX() + (double) width / 2, pos.getY() + 0.5, pos.getZ() + (double) width / 2, 2f, 1f, false);
            if (!stackInTank.isEmpty() && context.getLevel().getBlockEntity(pos) instanceof FluidTankBlockEntity be){
                IFluidHandler fTank = context.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), null);
                if (fTank != null)
                    fTank.fill(stackInTank, IFluidHandler.FluidAction.EXECUTE);
            }
            if (!context.getPlayer().isCreative())
                context.getPlayer().getInventory().placeItemBackInInventory(DISTILLATION_CONTROLLER.asStack(width*width));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (direction == Direction.DOWN && neighbourState.getBlock() != this)
            withBlockEntityDo(level, pos, DistillationTankBlockEntity::updateTemperature);
        return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean p_60514_) {
        super.neighborChanged(state, level, pos, block, otherPos, p_60514_);
        withBlockEntityDo(level, pos, DistillationTankBlockEntity::updateVerticalMulti);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TOP, BOTTOM, SHAPE);
    }
    @Override
    public Class<DistillationTankBlockEntity> getBlockEntityClass() {
        return DistillationTankBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DistillationTankBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.DISTILLATION_TANK.get();
    }
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        withBlockEntityDo(context.getLevel(), context.getClickedPos(), DistillationTankBlockEntity::toggleWindows);
        return InteractionResult.SUCCESS;
    }
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof DistillationTankBlockEntity))
                return;
            DistillationTankBlockEntity tankBE = (DistillationTankBlockEntity) be;
            world.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(tankBE);
        }
    }

    @Override
    public Item asItem() {
        return AllBlocks.FLUID_TANK.asItem();
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return AllBlocks.FLUID_TANK.asStack();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        if (oldState.getBlock() == state.getBlock())
            return;
        if (moved)
            return;
        withBlockEntityDo(level, pos, DistillationTankBlockEntity::updateConnectivity);
        withBlockEntityDo(level, pos, DistillationTankBlockEntity::updateVerticalMulti);
    }
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE)
            return state;
        boolean x = mirror == Mirror.FRONT_BACK;
        return switch (state.getValue(SHAPE)) {
            case WINDOW_NE -> state.setValue(SHAPE, x ? FluidTankBlock.Shape.WINDOW_NW : FluidTankBlock.Shape.WINDOW_SE);
            case WINDOW_NW -> state.setValue(SHAPE, x ? FluidTankBlock.Shape.WINDOW_NE : FluidTankBlock.Shape.WINDOW_SW);
            case WINDOW_SE -> state.setValue(SHAPE, x ? FluidTankBlock.Shape.WINDOW_SW : FluidTankBlock.Shape.WINDOW_NE);
            case WINDOW_SW -> state.setValue(SHAPE, x ? FluidTankBlock.Shape.WINDOW_SE : FluidTankBlock.Shape.WINDOW_NW);
            default -> state;
        };
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        for (int i = 0; i < rotation.ordinal(); i++)
            state = rotateOnce(state);
        return state;
    }

    private BlockState rotateOnce(BlockState state) {
        return switch (state.getValue(SHAPE)) {
            case WINDOW_NE -> state.setValue(SHAPE, FluidTankBlock.Shape.WINDOW_SE);
            case WINDOW_NW -> state.setValue(SHAPE, FluidTankBlock.Shape.WINDOW_NE);
            case WINDOW_SE -> state.setValue(SHAPE, FluidTankBlock.Shape.WINDOW_SW);
            case WINDOW_SW -> state.setValue(SHAPE, FluidTankBlock.Shape.WINDOW_NW);
            default -> state;
        };
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
        List<ItemStack> list = new ArrayList<>();
        list.add(AllBlocks.FLUID_TANK.asStack());
        list.add(DISTILLATION_CONTROLLER.asStack());
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, list);
    }
}
