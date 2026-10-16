package com.jesz.createdieselgenerators.content.diesel_engine.huge;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock.FACING;

public class HugeDieselEngineInstance extends AbstractBlockEntityVisual<HugeDieselEngineBlockEntity> implements SimpleDynamicVisual {
    protected final TransformedInstance piston;
    protected final TransformedInstance connector;
    protected final TransformedInstance linkage;

    public HugeDieselEngineInstance(VisualizationContext context, HugeDieselEngineBlockEntity blockEntity, float pt) {
        super(context, blockEntity, pt);
        piston = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(CDGPartialModels.ENGINE_PISTON))
                .createInstance();
        connector = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(CDGPartialModels.ENGINE_PISTON_CONNECTOR))
                .createInstance();
        linkage = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(CDGPartialModels.ENGINE_PISTON_LINKAGE))
                .createInstance();
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        Float angle = blockEntity.getTargetAngle();
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.getValue(FACING);
        Direction.Axis facingAxis = facing.getAxis();
        if (angle == null) {
            transformed(piston, facing, false)
                    .translate(0, 0.53475, 0);
            linkage.setZeroTransform().setChanged();
            connector.setZeroTransform().setChanged();
            piston.setChanged();
            return;
        }

        PoweredEngineShaftBlockEntity shaft = blockEntity.target.get();
        if (shaft == null) {
            transformed(piston, facing, false)
                    .translate(0, 0.53475, 0);
            linkage.setZeroTransform().setChanged();
            connector.setZeroTransform().setChanged();
            piston.setChanged();
            return;
        }
        Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);

        boolean roll90 = facingAxis.isHorizontal() && axis == Direction.Axis.Y || facingAxis.isVertical() && axis == Direction.Axis.Z;
        float shaftR = facing == Direction.DOWN ? -90 : facing == Direction.UP ? 90 : facing == Direction.WEST ? -90 : facing == Direction.EAST ? 90 : 0;
        if (roll90)
            shaftR = facing == Direction.NORTH ? 180 : facing == Direction.SOUTH ? 0 : facing == Direction.EAST ? -90 : facing == Direction.WEST ? 90 : 0;
        angle += (float)(shaftR*Math.PI/180);

        float sine = Mth.sin(angle) * (state.getValue(FACING).getAxis() == Direction.Axis.Y ? -1 : 1);
        float sine2 = Mth.sin(angle - Mth.HALF_PI) * (state.getValue(FACING).getAxis() == Direction.Axis.Y ? -1 : 1);
        float pistonOffset = ((1 - sine) / 4) + 0.4375f;

        transformed(piston, facing, roll90)
                .translate(0, pistonOffset, 0);

        transformed(linkage, facing, roll90)
                .center()
                .translate(0, 1, 0)
                .uncenter()
                .translate(0, pistonOffset, 0)
                .translate(0, 4 / 16f, 8 / 16f)
                .rotateXDegrees(sine2 * 23f)
                .translate(0, -4 / 16f, -8 / 16f);
        if (shaft.isEngineForConnectorDisplay(blockEntity.getBlockPos()))
            transformed(connector, facing, roll90)
                    .translate(0, 2, 0)
                    .center()
                    .rotateX((float) (-angle + Mth.HALF_PI - (facingAxis.isVertical() ? Math.PI : 0)))
                    .uncenter();
        else
            connector.setZeroTransform();
        linkage.setChanged();
        connector.setChanged();
        piston.setChanged();
    }

    protected TransformedInstance transformed(TransformedInstance modelData, Direction facing, boolean roll90) {
        return modelData.setIdentityTransform()
                .translate(getVisualPosition())
                .center()
                .rotateYDegrees(AngleHelper.horizontalAngle(facing))
                .rotateXDegrees(AngleHelper.verticalAngle(facing) + 90)
                .rotateYDegrees(roll90 ? -90 : 0)
                .uncenter();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(piston);
        consumer.accept(linkage);
        consumer.accept(connector);
    }

    @Override
    public void updateLight(float partialTick) {
        relight(piston, linkage, connector);
    }

    @Override
    protected void _delete() {
        piston.delete();
        linkage.delete();
        connector.delete();
    }
}
