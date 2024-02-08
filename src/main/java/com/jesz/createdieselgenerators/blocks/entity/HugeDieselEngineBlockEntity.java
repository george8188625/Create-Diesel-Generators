package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.blocks.PoweredEngineShaftBlock;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.jesz.createdieselgenerators.sounds.SoundRegistry;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.POWERED;
import static com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock.FACING;
import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;

public class HugeDieselEngineBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public WeakReference<PoweredEngineShaftBlockEntity> target = new WeakReference<>(null);
    public SmartFluidTankBehaviour tank;

    public HugeDieselEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

    }
    int partialSecond;

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tank.write(tag, clientPacket);
        tag.putInt("PartialSecond", partialSecond);

    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        tank.read(tag, clientPacket);
        partialSecond = tag.getInt("PartialSecond");

    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(2);
    }
    boolean validFuel;
    float oldAngle = 0;
    @Override
    public void tick() {
        super.tick();
        PoweredEngineShaftBlockEntity shaft = getShaft();
        if (shaft == null)
            return;
        if(getBlockState().getValue(POWERED))
            validFuel = false;
        else
            validFuel = FuelTypeManager.getGeneratedSpeed(this, tank.getPrimaryHandler().getFluid().getFluid()) != 0;
        partialSecond++;
        if(partialSecond >= 20){
            partialSecond = 0;
            if(validFuel) {
                if(tank.getPrimaryHandler().getFluid().getAmount() >= FuelTypeManager.getBurnRate(this, tank.getPrimaryHandler().getFluid().getFluid()))
                    tank.getPrimaryHandler().setFluid(FluidHelper.copyStackWithAmount(tank.getPrimaryHandler().getFluid(),
                            tank.getPrimaryHandler().getFluid().getAmount() - FuelTypeManager.getBurnRate(this, tank.getPrimaryHandler().getFluid().getFluid())));
                else
                    tank.getPrimaryHandler().setFluid(FluidStack.EMPTY);
            }
        }
        if(validFuel) {
            if(shaft.movementDirection != 0 && shaft.movementDirection != (movementDirection.get() == WindmillBearingBlockEntity.RotationDirection.CLOCKWISE ? 1 : -1)){
                shaft.removeGenerator(worldPosition);
                onDirectionChanged();
                return;
            }
            shaft.update(worldPosition, movementDirection.get() == WindmillBearingBlockEntity.RotationDirection.CLOCKWISE ? 1 : -1, FuelTypeManager.getGeneratedStress(this, tank.getPrimaryHandler().getFluid().getFluid()), FuelTypeManager.getGeneratedSpeed(this, tank.getPrimaryHandler().getFluid().getFluid()));
            if(!level.isClientSide)
                return;
            Float angle = getTargetAngle();
            if (angle == null)
                return;
            angle = (float) (angle*180/Math.PI);
            angle = angle < 0 ? 360-angle : angle;
            Direction facing = getBlockState().getValue(FACING);
            float shaftR = facing == Direction.NORTH ? 180 : facing == Direction.SOUTH ? 0 : facing == Direction.EAST ? 0 : facing == Direction.WEST ? 180 : facing == Direction.DOWN ? 90 : -90;

            if((oldAngle+shaftR) % 360 > (angle+shaftR) % 360) {
                level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundRegistry.DIESEL_ENGINE_SOUND.get(), SoundSource.BLOCKS, 1f,1f, false);
            }
            oldAngle = angle;

        }else{
            shaft.removeGenerator(worldPosition);
        }
    }

    public PoweredEngineShaftBlockEntity getShaft() {

        PoweredEngineShaftBlockEntity shaft = target.get();
        if (shaft == null || shaft.isRemoved() || !shaft.canBePoweredBy(worldPosition)) {
            if (shaft != null)
                target = new WeakReference<>(null);
            BlockEntity anyShaftAt = level.getBlockEntity(worldPosition.relative(getBlockState().getValue(FACING), 2));
            BlockState sState = level.getBlockState(worldPosition.relative(getBlockState().getValue(FACING), 2));
            if (anyShaftAt instanceof PoweredEngineShaftBlockEntity ps)
                target = new WeakReference<>(shaft = ps);
            else if(sState.getBlock() instanceof ShaftBlock)
                if(sState.getValue(AXIS) != getBlockState().getValue(FACING).getAxis())
                    level.setBlock(worldPosition.relative(getBlockState().getValue(FACING), 2), PoweredEngineShaftBlock.getEquivalent(level.getBlockState(worldPosition.relative(getBlockState().getValue(FACING), 2))), 3);
        }
        return shaft;
    }
    public ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;
    public AbstractComputerBehaviour computerBehaviour;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(computerBehaviour = CCProxy.behaviour(this));
        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
                Lang.translateDirect("contraptions.windmill.rotation_direction"), this, new HugeDieselEngineValueBox());
        movementDirection.withCallback($ -> onDirectionChanged());

        behaviours.add(movementDirection);
        tank = SmartFluidTankBehaviour.single(this, 100);
        behaviours.add(tank);
    }

    private void onDirectionChanged() {
        PoweredEngineShaftBlockEntity shaft = getShaft();
        if(shaft == null)
            return;
        shaft.engines.forEach((p, s) -> {
            if(level.getBlockEntity(p) instanceof HugeDieselEngineBlockEntity be){
                be.movementDirection.setValue(movementDirection.getValue());
            }
        });
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if(cap == ForgeCapabilities.FLUID_HANDLER && side == null)
            return tank.getCapability().cast();
        else if (cap == ForgeCapabilities.FLUID_HANDLER && getBlockState().getValue(BooleanProperty.create(side.toString())))
            if(side.getAxis() != getBlockState().getValue(FACING).getAxis())
                return tank.getCapability().cast();

        return super.getCapability(cap, side);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!IRotate.StressImpact.isEnabled() || !validFuel)
            return false;
        PoweredEngineShaftBlockEntity shaft = getShaft();
        if(shaft == null)
            return false;
        float stressBase = FuelTypeManager.getGeneratedStress(this, tank.getPrimaryHandler().getFluid().getFluid());
        if (Mth.equal(stressBase, 0))
            return false;
        Lang.translate("gui.goggles.generator_stats")
                .forGoggles(tooltip);
        Lang.translate("tooltip.capacityProvided")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        float stressTotal = Math.abs(stressBase);

        Lang.number(stressTotal)
                .translate("generic.unit.stress")
                .style(ChatFormatting.AQUA)
                .space()
                .add(Lang.translate("gui.goggles.at_current_speed")
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);
        return containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability().cast());
    }


    @OnlyIn(Dist.CLIENT)
    public Float getTargetAngle() {
        float angle;
        BlockState state = getBlockState();
        if (!BlockRegistry.HUGE_DIESEL_ENGINE.has(state))
            return null;

        Direction facing = state.getValue(FACING);
        PoweredEngineShaftBlockEntity shaft = getShaft();
        Direction.Axis facingAxis = facing.getAxis();
        Direction.Axis axis;

        if (shaft == null)
            return null;

        axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        angle = KineticBlockEntityRenderer.getAngleForTe(shaft, shaft.getBlockPos(), axis);
        if (axis == facingAxis)
            return null;
        if (axis.isHorizontal() && (facingAxis == Direction.Axis.X ^ facing.getAxisDirection() == Direction.AxisDirection.POSITIVE))
            angle *= -1;
        if (axis == Direction.Axis.X && facing == Direction.DOWN)
            angle *= -1;
        return angle;
    }
}
