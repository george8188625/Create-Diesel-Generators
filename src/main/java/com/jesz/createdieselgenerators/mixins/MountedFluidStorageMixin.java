package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.blocks.entity.OilBarrelBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(com.simibubi.create.content.contraptions.MountedFluidStorage.class)
public abstract class MountedFluidStorageMixin {
    @Shadow protected abstract void onFluidStackChanged(FluidStack fs);

    @Inject(at = @At("HEAD"), method = "canUseAsStorage(Lnet/minecraft/world/level/block/entity/BlockEntity;)Z", cancellable = true, remap = false)
    private static void canUseAsStorage(BlockEntity be, CallbackInfoReturnable<Boolean> cir){
        if(be instanceof OilBarrelBlockEntity)
            if(((OilBarrelBlockEntity) be).isController())
                cir.setReturnValue(true);
    }
    @Inject(at = @At("HEAD"), method = "createMountedTank(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lcom/simibubi/create/foundation/fluid/SmartFluidTank;", cancellable = true, remap = false)
    private void createMountedTank(BlockEntity be, CallbackInfoReturnable<SmartFluidTank> cir){
        if(be instanceof OilBarrelBlockEntity)
            cir.setReturnValue(new SmartFluidTank(
                    ((OilBarrelBlockEntity) be).getTotalTankSize() * OilBarrelBlockEntity.getCapacityMultiplier(),
                    this::onFluidStackChanged));
    }
}
