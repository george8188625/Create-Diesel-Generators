package com.jesz.createdieselgenerators.contraption;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGSoundEvents;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineSoundInstance;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DieselEngineMovementBehaviour implements MovementBehaviour {
    @OnlyIn(Dist.CLIENT)
    static Map<Pair<UUID, BlockPos>, EngineSoundInstance> soundInstances;

    @Override
    public boolean isActive(MovementContext context) {
        return context.contraption instanceof CarriageContraption && MovementBehaviour.super.isActive(context);
    }

    @Nullable
    @Override
    public ItemStack canBeDisabledVia(MovementContext context) {
        return CDGBlocks.DIESEL_ENGINE.asStack();
    }

    @Override
    public void tick(MovementContext context) {
        if (!context.world.isClientSide)
            return;

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> clientTick(context));
    }

    @OnlyIn(Dist.CLIENT)
    protected void clientTick(MovementContext context) {
        CarriageContraption contraption = ((CarriageContraption)context.contraption);
        CarriageContraptionEntity entity = (CarriageContraptionEntity) contraption.entity;

        if (soundInstances == null)
            soundInstances = new HashMap<>();

        if(!CDGConfig.ENGINES_EMIT_SOUND_ON_TRAINS.get() || entity.getCarriage().train.derailed)
            return;

        double trainSpeed = context.motion.length() * 2 / (entity.getCarriage().train.maxSpeed() / 28);
        double lastTrainSpeed = context.data.getDouble("TrainSpeed");
        double acceleration = trainSpeed - lastTrainSpeed;
        context.data.putDouble("TrainSpeed", trainSpeed);

        float throttle = Mth.lerp(0.05f, context.data.getFloat("Throttle"),
                (float) Math.max(0, Math.min(1, acceleration)));
        context.data.putFloat("Throttle", throttle);

        EngineSoundInstance instance = soundInstances.get(Pair.of(entity.getUUID(), context.localPos));

        if (context.disabled) {
            if (instance != null)
                instance.fadeOut();
            return;
        }


        if (instance == null) {
            instance = new EngineSoundInstance(CDGSoundEvents.ENGINE_NORMAL.get(), SoundSource.NEUTRAL, context.position, 0.6f);
            instance.setVolume(1f);
            Minecraft.getInstance().getSoundManager().play(instance);
            soundInstances.put(Pair.of(entity.getUUID(), context.localPos), instance);
        } else if (instance.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(instance))
            soundInstances.remove(Pair.of(entity.getUUID(), context.localPos));

        instance.setPosition(context.position);

        if (instance.active()) {
            instance.keepAlive();
            float pitch = (float) Math.min(2, Math.max(Math.min(0.14, throttle) * 5 + trainSpeed / 28, 0.1f));
            instance.setPitch(pitch);
        }
    }
}
