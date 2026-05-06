package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SubLevelAssemblyHelper.class, remap = false)
public class SableAssemblyMixin {

    @Inject(
            method = "moveBlocks",
            at = @At("TAIL"),
            remap = false
    )
    private static void onMoveBlocks(
            ServerLevel level,
            SubLevelAssemblyHelper.AssemblyTransform transform,
            Iterable<BlockPos> blocks,
            CallbackInfo ci) {
        ServerLevel resultingLevel = transform.getLevel();
        for (BlockPos block : blocks) {
            BlockPos newPos = transform.apply(block);
            BlockEntity be = resultingLevel.getBlockEntity(newPos);
            if (be instanceof ModularDieselEngineBlockEntity mbe) {
                mbe.resetConnectivity();
            }

        }
    }
}