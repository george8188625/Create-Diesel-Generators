package com.jesz.createdieselgenerators.blocks.renderer;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackBearingBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackCrankBlockEntity;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingData;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;
import static com.simibubi.create.foundation.utility.AngleHelper.angleLerp;

public class PumpjackCrankInstance extends KineticBlockEntityInstance<PumpjackCrankBlockEntity> implements DynamicInstance {
    protected final ModelData crank;
    protected final ModelData crank_rod;
    protected final ModelData large_crank;
    protected final ModelData large_crank_rod;
    protected final RotatingData shaft;

    public PumpjackCrankInstance(MaterialManager materialManager, PumpjackCrankBlockEntity blockEntity) {
        super(materialManager, blockEntity);
        crank = getTransformMaterial().getModel(PartialModels.PUMPJACK_CRANK_SMALL)
                .createInstance();
        crank_rod = getTransformMaterial().getModel(PartialModels.PUMPJACK_CRANK_ROD_SMALL)
                .createInstance();
        large_crank = getTransformMaterial().getModel(PartialModels.PUMPJACK_CRANK_LARGE)
                .createInstance();
        large_crank_rod = getTransformMaterial().getModel(PartialModels.PUMPJACK_CRANK_ROD_LARGE)
                .createInstance();
        shaft = setup(getRotatingMaterial().getModel(shaft())
                .createInstance());
    }
    @Override
    public void beginFrame() {
        float partialTicks = AnimationTickHolder.getPartialTicks()*0;
        float angle = angleLerp(partialTicks, blockEntity.prevAngle, blockEntity.angle);
        PoseStack ms = new PoseStack();
        TransformStack msr = TransformStack.cast(ms);

        msr.translate(getInstancePosition());
        boolean isXAxis = blockState.getValue(HORIZONTAL_FACING).getAxis() == Direction.Axis.X;
        double v = ((isXAxis ? angle : -angle) + 90) / 180 * Math.PI;

        double sin = Math.sin(v) * (blockEntity.crankSize.getValue() == 0 ? 0.8125 : 1.125);
        double cos = Math.cos(v) * (blockEntity.crankSize.getValue() == 0 ? 0.8125 : 1.125);
        if(blockEntity.bearingPos == null) {
            if(isXAxis) {
                msr.translate(0.5, 1.25, 0).rotateZ(angle);
            }else {
                msr.translate(0, 1.25, 0.5).rotateY(90).rotateZ(angle);
            }
            (blockEntity.crankSize.getValue() == 0 ? crank : large_crank).setTransform(ms);
            (blockEntity.crankSize.getValue() == 0 ? large_crank : crank).setEmptyTransform();

            double dstY = -1000-sin-1.25 - pos.getY();
            double dstX = pos.getX()-cos-0.5 - pos.getX();
            double dstZ = pos.getZ()-cos-0.5 - pos.getZ();
            ms = new PoseStack();
            msr = TransformStack.cast(ms);
            msr.translate(getInstancePosition());
            if(isXAxis) {
                msr.translate(0.5, 1.25, 0).translate(cos, sin, 0).rotateZ(Math.atan2(dstY, dstX)*180/Math.PI-90);
            }else {
                msr.translate(0, 1.25, 0.5).translate(0, sin, cos).rotateY(90).rotateZ(Math.atan2(dstZ, dstY)*180/Math.PI);
            }
            (blockEntity.crankSize.getValue() == 0 ? crank_rod : large_crank_rod).setTransform(ms);
            (blockEntity.crankSize.getValue() == 0 ? large_crank_rod : crank_rod).setEmptyTransform();
            return;
        }
        PumpjackBearingBlockEntity bearing = blockEntity.bearing.get();
        float interpolatedAngle = 0;
        if(bearing != null)
            interpolatedAngle = bearing.getInterpolatedAngle(partialTicks);
        if(!isXAxis)
            interpolatedAngle *= -1;
        Vec2 crankBearingLocation = new Vec2(
                (float) ((blockEntity.crankBearingLocation.x) * Math.cos(interpolatedAngle/180 * Math.PI) - (blockEntity.crankBearingLocation.y) * Math.sin(interpolatedAngle/180*Math.PI))+0.5f,
                (float) ((blockEntity.crankBearingLocation.x) * Math.sin(interpolatedAngle/180 * Math.PI) + (blockEntity.crankBearingLocation.y) * Math.cos(interpolatedAngle/180*Math.PI))+0.5f);
        if(isXAxis)
            crankBearingLocation = crankBearingLocation.add(new Vec2((float) blockEntity.bearingPos.getX(), (float) blockEntity.bearingPos.getY()));
        else
            crankBearingLocation = crankBearingLocation.add(new Vec2((float) blockEntity.bearingPos.getZ(), (float) blockEntity.bearingPos.getY()));
        if(isXAxis) {
            msr.translate(0.5, 1.25, 0).rotateZ(angle);
        }else {
            msr.translate(0, 1.25, 0.5).rotateY(90).rotateZ(angle);
        }
        (blockEntity.crankSize.getValue() == 0 ? crank : large_crank).setTransform(ms);
        (blockEntity.crankSize.getValue() == 0 ? large_crank : crank).setEmptyTransform();

        ms = new PoseStack();
        msr = TransformStack.cast(ms);
        msr.translate(getInstancePosition());

        double dstY = crankBearingLocation.y-sin-1.25 - pos.getY();
        double dstX = crankBearingLocation.x-cos-0.5 - pos.getX();
        double dstZ = crankBearingLocation.x-cos-0.5 - pos.getZ();

        if(isXAxis) {
            msr.translate(0.5, 1.25, 0).translate(cos, sin, 0).rotateZ(Math.atan2(dstY, dstX)*180/Math.PI-90);
        }else {
            msr.translate(0, 1.25, 0.5).translate(0, sin, cos).rotateY(90).rotateZ(Math.atan2(dstZ, dstY)*180/Math.PI);
        }
        (blockEntity.crankSize.getValue() == 0 ? crank_rod : large_crank_rod).setTransform(ms);
        (blockEntity.crankSize.getValue() == 0 ? large_crank_rod : crank_rod).setEmptyTransform();
    }
    @Override
    public void update() {
        updateRotation(shaft);
    }

    @Override
    public void updateLight() {
        relight(pos, shaft, crank, crank_rod, large_crank_rod, large_crank);
    }

    @Override
    public void remove() {
        shaft.delete();
        crank.delete();
        crank_rod.delete();
        large_crank_rod.delete();
        large_crank.delete();
    }
}
