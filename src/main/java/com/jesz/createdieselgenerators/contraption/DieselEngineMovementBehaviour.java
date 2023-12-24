package com.jesz.createdieselgenerators.contraption;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock;
import com.jesz.createdieselgenerators.blocks.LargeDieselGeneratorBlock;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.sounds.SoundRegistry;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.render.CachedBufferer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.FACING;
import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.TURBOCHARGED;
import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class DieselEngineMovementBehaviour implements MovementBehaviour {
    @Override
    public boolean isActive(MovementContext context) {
        return context.contraption instanceof CarriageContraption;
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
        double trainSpeed = context.motion.length();
        int angle = (int) (((trainSpeed * AnimationTickHolder.getTicks())*100) % 360)/36;
        VertexConsumer builder = buffer.getBuffer(RenderType.solid());
        if(context.state.getBlock() instanceof DieselGeneratorBlock)
            if(!context.state.getValue(TURBOCHARGED))
                if(context.state.getValue(FACING).getAxis().isHorizontal()){
                    CachedBufferer.partial( angle == 10? PartialModels.ENGINE_PISTONS_0 :
                                            angle == 9 ? PartialModels.ENGINE_PISTONS_1 :
                                            angle == 8 ? PartialModels.ENGINE_PISTONS_2 :
                                            angle == 7 ? PartialModels.ENGINE_PISTONS_3 :
                                            angle == 6 ? PartialModels.ENGINE_PISTONS_4 :
                                            angle == 5 ? PartialModels.ENGINE_PISTONS_4 :
                                            angle == 4 ? PartialModels.ENGINE_PISTONS_3 :
                                            angle == 3 ? PartialModels.ENGINE_PISTONS_2 :
                                            angle == 2 ? PartialModels.ENGINE_PISTONS_1 :
                                                    PartialModels.ENGINE_PISTONS_0
                            , context.state)
                            .transform(matrices.getModel())
                            .centre()
                            .rotateY(context.state.getValue(FACING).toYRot()).unCentre()
                            .renderInto(matrices.getViewProjection(), builder);
                }else {
                     CachedBufferer.partial(angle == 10? PartialModels.ENGINE_PISTONS_VERTICAL_0 :
                                             angle == 9 ? PartialModels.ENGINE_PISTONS_VERTICAL_1 :
                                             angle == 8 ? PartialModels.ENGINE_PISTONS_VERTICAL_2 :
                                             angle == 7 ? PartialModels.ENGINE_PISTONS_VERTICAL_3 :
                                             angle == 6 ? PartialModels.ENGINE_PISTONS_VERTICAL_4 :
                                             angle == 5 ? PartialModels.ENGINE_PISTONS_VERTICAL_4 :
                                             angle == 4 ? PartialModels.ENGINE_PISTONS_VERTICAL_3 :
                                             angle == 3 ? PartialModels.ENGINE_PISTONS_VERTICAL_2 :
                                             angle == 2 ? PartialModels.ENGINE_PISTONS_VERTICAL_1 :
                                                     PartialModels.ENGINE_PISTONS_VERTICAL_0
                                    , context.state)
                             .transform(matrices.getModel())
                             .centre()
                             .rotateY(context.state.getValue(FACING) == Direction.DOWN ? 180 : 270).rotateZ(context.state.getValue(FACING) == Direction.DOWN ? 180 : 0).unCentre()
                             .renderInto(matrices.getViewProjection(), builder);
                }
        if(context.state.getBlock() instanceof LargeDieselGeneratorBlock){
            CachedBufferer.partial( angle == 10? PartialModels.MODULAR_ENGINE_PISTONS_0 :
                                    angle == 9 ? PartialModels.MODULAR_ENGINE_PISTONS_1 :
                                    angle == 8 ? PartialModels.MODULAR_ENGINE_PISTONS_2 :
                                    angle == 7 ? PartialModels.MODULAR_ENGINE_PISTONS_3 :
                                    angle == 6 ? PartialModels.MODULAR_ENGINE_PISTONS_4 :
                                    angle == 5 ? PartialModels.MODULAR_ENGINE_PISTONS_4 :
                                    angle == 4 ? PartialModels.MODULAR_ENGINE_PISTONS_3 :
                                    angle == 3 ? PartialModels.MODULAR_ENGINE_PISTONS_2 :
                                    angle == 2 ? PartialModels.MODULAR_ENGINE_PISTONS_1 :
                                            PartialModels.MODULAR_ENGINE_PISTONS_0
                            , context.state)
                            .transform(matrices.getModel())
                            .centre()
                            .rotateY(context.state.getValue(HORIZONTAL_FACING).toYRot()).unCentre()
                            .renderInto(matrices.getViewProjection(), builder);
            CachedBufferer.block(AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, context.state.getValue(HORIZONTAL_FACING).getAxis()))
                    .transform(matrices.getModel())
                    .rotateCentered(Direction.get(Direction.AxisDirection.POSITIVE, context.state.getValue(HORIZONTAL_FACING).getAxis()), (float) (trainSpeed >= 0.01 ? (0.6 * AnimationTickHolder.getTicks()) : 0))
                    .renderInto(matrices.getViewProjection(), builder);
        }else
            CachedBufferer.block(AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, context.state.getValue(FACING).getAxis()))
                    .transform(matrices.getModel())
                    .rotateCentered(Direction.get(Direction.AxisDirection.POSITIVE, context.state.getValue(FACING).getAxis()), (float) (trainSpeed >= 0.01 ? (0.6 * AnimationTickHolder.getTicks()) : 0))
                    .renderInto(matrices.getViewProjection(), builder);

    }

    @Override
    public void tick(MovementContext context) {
        MovementBehaviour.super.tick(context);
        if(!context.world.isClientSide)
            return;
        CarriageContraption contraption = ((CarriageContraption)context.contraption);
        CarriageContraptionEntity entity = (CarriageContraptionEntity) contraption.entity;
        double trainSpeed = context.motion.length()*2;
        if(ConfigRegistry.ENGINES_EMIT_SOUND_ON_TRAINS.get() && !entity.getCarriage().train.derailed && trainSpeed >= 0.1){
            if(context.data.getInt("tick") >= 10/ Mth.clamp(trainSpeed*10, 4, 5)){
                context.world.playLocalSound(context.position.x, context.position.y, context.position.z, SoundRegistry.DIESEL_ENGINE_SOUND.get(), SoundSource.BLOCKS, (float) (context.state.hasProperty(TURBOCHARGED) && context.state.getValue(TURBOCHARGED) ? 50f*trainSpeed : 30f*trainSpeed), (float) Mth.clamp(context.state.hasProperty(TURBOCHARGED) && context.state.getValue(TURBOCHARGED) ? 10f*trainSpeed : 8f*trainSpeed, 1, 2.4), false);

                context.data.putInt("tick", 0);
            }
            context.data.putInt("tick", context.data.getInt("tick")+1);
        }
    }
}
