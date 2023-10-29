package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public class ExplosionMixin  {
    @Shadow @Final private Level level;

    @Shadow @Final private Vec3 position;

    @Shadow @Final private float radius;

    @Inject(at = @At(value = "HEAD"),method = "finalizeExplosion(Z)V")
    public void finalizeExplosion(boolean p_46076_,CallbackInfo ci){

        if(ConfigRegistry.COMBUSTIBLES_BLOW_UP.get() && !level.isClientSide)
            for (int x = (int) -radius; x < radius; x++) {
                for (int y = (int) -radius; y < radius; y++) {
                    for (int z = (int) -radius; z < radius; z++) {
                        BlockPos pos = new BlockPos((int) (x+position.x), (int) (y+position.y), (int) (z+position.z));

                        if (!this.level.isInWorldBounds(pos)) continue;
                        if(Math.abs(Math.sqrt(x*x+y*y+z*z)) < radius) {
                            FluidState fluidState = this.level.getFluidState(pos);

                            if (CreateDieselGenerators.getGeneratedSpeed(new FluidStack(fluidState.getType(), 1)) != 0) {
                                this.level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                                this.level.explode(null, null, null, pos.getX(), pos.getY(), pos.getZ(), 3, true, Level.ExplosionInteraction.BLOCK);
                            }
                            BlockEntity be = this.level.getBlockEntity(pos);
                            if(be == null)
                                continue;
                            IFluidHandler tank = be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
                            if(tank == null)
                                continue;
                            if(CreateDieselGenerators.getGeneratedSpeed(tank.getFluidInTank(0)) == 0)
                                continue;
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                            level.explode(null, null, null, pos.getX(), pos.getY(), pos.getZ(), 3+((float)tank.getFluidInTank(0).getAmount()/500), true, Level.ExplosionInteraction.BLOCK);
                        }
                    }
                }
            }
    }
}
