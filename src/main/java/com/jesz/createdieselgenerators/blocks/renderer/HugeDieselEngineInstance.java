package com.jesz.createdieselgenerators.blocks.renderer;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.entity.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.PoweredEngineShaftBlockEntity;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import static com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock.FACING;

public class HugeDieselEngineInstance extends BlockEntityInstance<HugeDieselEngineBlockEntity> implements DynamicInstance {
    protected final ModelData piston;
    protected final ModelData connector;
    protected final ModelData linkage;

    public HugeDieselEngineInstance(MaterialManager materialManager, HugeDieselEngineBlockEntity blockEntity) {
        super(materialManager, blockEntity);
        piston = getTransformMaterial().getModel(PartialModels.ENGINE_PISTON)
                .createInstance();
        connector = getTransformMaterial().getModel(PartialModels.ENGINE_PISTON_CONNECTOR)
                .createInstance();
        linkage = getTransformMaterial().getModel(PartialModels.ENGINE_PISTON_LINKAGE)
                .createInstance();
    }

    @Override
    public void beginFrame() {
        Float angle = blockEntity.getTargetAngle();
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.getValue(FACING);
        Direction.Axis facingAxis = facing.getAxis();
        if (angle == null){
            transformed(piston, facing, false)
                    .translate(0, 0.53475, 0);
            linkage.setEmptyTransform();
            connector.setEmptyTransform();
            return;
        }


        PoweredEngineShaftBlockEntity shaft = blockEntity.getShaft();
        if(shaft == null){
            transformed(piston, facing, false)
                    .translate(0, 0.53475, 0);
            linkage.setEmptyTransform();
            connector.setEmptyTransform();
            return;
        }
        Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);

        boolean roll90 = facingAxis.isHorizontal() && axis == Direction.Axis.Y || facingAxis.isVertical() && axis == Direction.Axis.Z;
        float shaftR = facing == Direction.DOWN ? -90 : facing == Direction.UP ? 90 : facing == Direction.WEST ? -90 : facing == Direction.EAST ? 90 : 0;
        if(roll90)
            shaftR = facing == Direction.NORTH ? 180 : facing == Direction.SOUTH ? 0 : facing == Direction.EAST ? -90 : facing == Direction.WEST ? 90 : 0;
        angle += (float)(shaftR*Math.PI/180);

        float sine = Mth.sin(angle) * (state.getValue(FACING).getAxis() == Direction.Axis.Y ? -1 : 1);
        float sine2 = Mth.sin(angle - Mth.HALF_PI) * (state.getValue(FACING).getAxis() == Direction.Axis.Y ? -1 : 1);
        float pistonOffset = ((1 - sine) / 4) + 0.4375f;

        transformed(piston, facing, roll90)
                .translate(0, pistonOffset, 0);

        transformed(linkage, facing, roll90)
                .centre()
                .translate(0, 1, 0)
                .unCentre()
                .translate(0, pistonOffset, 0)
                .translate(0, 4 / 16f, 8 / 16f)
                .rotateX(sine2 * 23f)
                .translate(0, -4 / 16f, -8 / 16f);
        if(shaft.isEngineForConnectorDisplay(blockEntity.getBlockPos()))
            transformed(connector, facing, roll90)
                    .translate(0, 2, 0)
                    .centre()
                    .rotateXRadians(-angle + Mth.HALF_PI - (facingAxis.isVertical() ? Math.PI : 0))
                    .unCentre();
        else
            connector.setEmptyTransform();
    }
    protected ModelData transformed(ModelData modelData, Direction facing, boolean roll90) {
        return modelData.loadIdentity()
                .translate(getInstancePosition())
                .centre()
                .rotateY(AngleHelper.horizontalAngle(facing))
                .rotateX(AngleHelper.verticalAngle(facing) + 90)
                .rotateY(roll90 ? -90 : 0)
                .unCentre();
    }

    @Override
    public void updateLight() {
        relight(pos, piston, connector, linkage);
    }

    @Override
    protected void remove() {
        piston.delete();
        linkage.delete();
        connector.delete();
    }
}
