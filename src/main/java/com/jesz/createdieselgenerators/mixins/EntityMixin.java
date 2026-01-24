package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.mixin_interfaces.IEntity;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntity {

    @Unique
    public BlockPos create_diesel_generators$turretPos;

    @Inject(method="load", at = @At("HEAD"), remap = false)
    public void load(CompoundTag tag, CallbackInfo ci){
        if(tag.contains("TurretPos", Tag.TAG_COMPOUND))
            create_diesel_generators$turretPos = NBTHelper.readBlockPos(tag, "TurretPos");
    }
    @Inject(method="save", at = @At("HEAD"), remap = false)
    public void save(CompoundTag tag, CallbackInfoReturnable<Boolean> ci){
        if(create_diesel_generators$turretPos != null)
            tag.put("TurretPos", NbtUtils.writeBlockPos(create_diesel_generators$turretPos));
    }

    @Override
    public BlockPos getTurretPos() {
        return create_diesel_generators$turretPos;
    }

    @Override
    public void setTurretPos(BlockPos pos) {
        create_diesel_generators$turretPos = pos;
    }
}
