package com.jesz.createdieselgenerators.other;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

public class EntityTickEvent extends Event implements IModBusEvent {
    public EntityTickEvent(Entity entity){
        this.entity = entity;
    }
    public Entity entity;
}
