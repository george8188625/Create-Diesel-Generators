package com.jesz.createdieselgenerators.contraption;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.sounds.SoundRegistry;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.TURBOCHARGED;

public class DieselEngineMovementBehaviour implements MovementBehaviour {
    @Override
    public boolean isActive(MovementContext context) {
        return context.contraption instanceof CarriageContraption && MovementBehaviour.super.isActive(context);
    }

    @Override
    public boolean renderAsNormalBlockEntity() {
        return true;
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
                context.world.playLocalSound(context.position.x, context.position.y, context.position.z, SoundRegistry.DIESEL_ENGINE_SOUND.get(), SoundSource.BLOCKS, 0.5f, (float) Mth.clamp(context.state.hasProperty(TURBOCHARGED) && context.state.getValue(TURBOCHARGED) ? 3f*trainSpeed : 2f*trainSpeed, 1, 2.4), false);

                context.data.putInt("tick", 0);
            }
            context.data.putInt("tick", context.data.getInt("tick")+1);
        }
    }
}
