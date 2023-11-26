package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.entity.item.ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    public ItemEntityMixin(EntityType<?> p_19870_, Level p_19871_) {super(p_19870_, p_19871_);}

    @Shadow public abstract ItemStack getItem();

    @Inject(at = @At("HEAD"),method = "tick()V")
    public void tick(CallbackInfo ci){
        if(this.getItem().is(ItemRegistry.LIGHTER.get()) && ConfigRegistry.COMBUSTIBLES_BLOW_UP.get() && this.getItem().getTag() != null)
            if(this.getItem().getTag().getInt("Type") == 2) {
                FluidState fState = getLevel().getFluidState(new BlockPos(this.getPosition(1)));
                if(fState.is(Fluids.WATER) || fState.is(Fluids.FLOWING_WATER)) {
                    this.getItem().getTag().putInt("Type", 1);
                    getLevel().playLocalSound(this.getPosition(1).x, this.getPosition(1).y, this.getPosition(1).z, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f, false);
                    return;
                }
                if(FuelTypeManager.getGeneratedSpeed(fState.getType()) != 0)
                    getLevel().explode(null, null, null, this.getPosition(1).x, this.getPosition(1).y, this.getPosition(1).z, 3, true, Explosion.BlockInteraction.BREAK);
            }
    }
}
