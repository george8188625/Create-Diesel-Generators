package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.other.EntityTickEvent;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(at = @At("HEAD"),method = "tick()V")
    public void tick(CallbackInfo ci){
        MinecraftForge.EVENT_BUS.post(new EntityTickEvent(this));
    }
}
