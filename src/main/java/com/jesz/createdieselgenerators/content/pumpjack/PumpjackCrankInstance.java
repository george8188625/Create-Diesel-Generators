package com.jesz.createdieselgenerators.content.pumpjack;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class PumpjackCrankInstance extends KineticBlockEntityVisual<PumpjackCrankBlockEntity> implements SimpleDynamicVisual {
    protected final TransformedInstance crank;
    protected final TransformedInstance crank_rod;
    protected final TransformedInstance large_crank;
    protected final TransformedInstance large_crank_rod;
    protected final RotatingInstance shaft;

    public PumpjackCrankInstance(VisualizationContext context, PumpjackCrankBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        crank = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(CDGPartialModels.PUMPJACK_CRANK_SMALL))
                .createInstance();
        crank_rod = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(CDGPartialModels.PUMPJACK_CRANK_ROD_SMALL))
                .createInstance();
        large_crank = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(CDGPartialModels.PUMPJACK_CRANK_LARGE))
                .createInstance();
        large_crank_rod = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(CDGPartialModels.PUMPJACK_CRANK_ROD_LARGE))
                .createInstance();
        shaft = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT))
                .createInstance();
        shaft.setup(blockEntity)
                .setPosition(getVisualPosition())
                .rotateToFace(Direction.UP, blockEntity.getBlockState().getValue(HORIZONTAL_FACING).getOpposite())
                .setChanged();
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        float partialTicks = ctx.partialTick();

        BlockState blockState = blockEntity.getBlockState();
        BlockPos pos = blockEntity.getBlockPos();

        boolean active = blockEntity.getBearing() != null;
        float angle = active ? AngleHelper.angleLerp(partialTicks, blockEntity.prevAngle, blockEntity.angle) : 0;

        boolean isXAxis = blockState.getValue(HORIZONTAL_FACING).getAxis() == Direction.Axis.X;
        double v = ((isXAxis ? angle : -angle) + 90) / 180 * Math.PI;

        double sin = Math.sin(v) * (blockEntity.crankSize.getValue() == 0 ? 0.8125 : 1.125);
        double cos = Math.cos(v) * (blockEntity.crankSize.getValue() == 0 ? 0.8125 : 1.125);

        double dstY = -1000-sin-1.25 - pos.getY();
        double dstX = pos.getX()-cos-0.5 - pos.getX();
        double dstZ = pos.getZ()-cos-0.5 - pos.getZ();

        PoseStack ms = new PoseStack();
        TransformStack<PoseTransformStack> msr = TransformStack.of(ms);

        msr.translate(getVisualPosition());

        if (blockEntity.bearingPos != null) {
            PumpjackBearingBlockEntity bearing = blockEntity.bearing.get();

            float interpolatedAngle = 0;
            if (bearing != null)
                interpolatedAngle = bearing.getInterpolatedAngle(partialTicks);
            if (blockEntity.inPonderAngle != Integer.MIN_VALUE){
                interpolatedAngle = blockEntity.inPonderAngle;
            }

            if (!isXAxis)
                interpolatedAngle *= -1;
            Vec2 crankBearingLocation = new Vec2(
                    (float) ((blockEntity.crankBearingLocation.x) * Math.cos(interpolatedAngle/180 * Math.PI) - (blockEntity.crankBearingLocation.y) * Math.sin(interpolatedAngle/180*Math.PI))+0.5f,
                    (float) ((blockEntity.crankBearingLocation.x) * Math.sin(interpolatedAngle/180 * Math.PI) + (blockEntity.crankBearingLocation.y) * Math.cos(interpolatedAngle/180*Math.PI))+0.5f);
            if (isXAxis)
                crankBearingLocation = crankBearingLocation.add(new Vec2((float) blockEntity.bearingPos.getX(), (float) blockEntity.bearingPos.getY()));
            else
                crankBearingLocation = crankBearingLocation.add(new Vec2((float) blockEntity.bearingPos.getZ(), (float) blockEntity.bearingPos.getY()));

            dstY = crankBearingLocation.y-sin-1.25 - pos.getY();
            dstX = crankBearingLocation.x-cos-0.5 - pos.getX();
            dstZ = crankBearingLocation.x-cos-0.5 - pos.getZ();
        }

        if(isXAxis) {
            msr.translate(0.5, 1.25, 0).rotateZDegrees(angle);
        }else {
            msr.translate(0, 1.25, 0.5).rotateYDegrees(90).rotateZDegrees(angle);
        }
        (blockEntity.crankSize.getValue() == 0 ? crank : large_crank).setTransform(ms);
        (blockEntity.crankSize.getValue() == 0 ? large_crank : crank).setZeroTransform();


        ms = new PoseStack();
        msr = TransformStack.of(ms);
        msr.translate(getVisualPosition());

        if(isXAxis) {
            msr.translate(0.5, 1.25, 0).translate(cos, sin, 0).rotateZDegrees((float) (Math.atan2(dstY, dstX)*180/Math.PI-90));
        }else {
            msr.translate(0, 1.25, 0.5).translate(0, sin, cos).rotateYDegrees(90).rotateZDegrees((float) (Math.atan2(dstZ, dstY)*180/Math.PI));
        }
        (blockEntity.crankSize.getValue() == 0 ? crank_rod : large_crank_rod).setTransform(ms);
        (blockEntity.crankSize.getValue() == 0 ? large_crank_rod : crank_rod).setZeroTransform();

        crank.setChanged();
        crank_rod.setChanged();
        large_crank_rod.setChanged();
        large_crank.setChanged();
    }

    @Override
    public void update(float partialTick) {
        shaft.setup(blockEntity)
                .setChanged();
    }

    @Override
    public void updateLight(float pt) {
        relight(pos, shaft, crank, crank_rod, large_crank_rod, large_crank);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(shaft);
        consumer.accept(crank);
        consumer.accept(crank_rod);
        consumer.accept(large_crank);
        consumer.accept(large_crank_rod);
    }

    @Override
    protected void _delete() {
        shaft.delete();
        crank.delete();
        crank_rod.delete();
        large_crank_rod.delete();
        large_crank.delete();
    }
}
