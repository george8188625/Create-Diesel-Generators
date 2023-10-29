package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.blocks.DistillationTankBlock;
import com.jesz.createdieselgenerators.blocks.entity.DistillationTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class DistillationControllerItem extends Item {
    public DistillationControllerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidTankBlockEntity ftbe){
            ItemStack item = context.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
            BlockPos cPos = ftbe.getController();
            int width = ftbe.getControllerBE().getWidth();
            int height = ftbe.getControllerBE().getHeight();

            for (int x = 0; x < width; x++) {

                for (int z = 0; z < width; z++) {
                    for (int y = 0; y < height; y++) {
                        if(item.getCount() == 0 && !context.getPlayer().isCreative())
                            break;
                        context.getLevel().setBlock(cPos.offset(x, y, z), BlockRegistry.DISTILLATION_TANK.getDefaultState(), 1);
                        context.getLevel().updateNeighborsAt(cPos.offset(x, y, z), BlockRegistry.DISTILLATION_TANK.getDefaultState().getBlock());
                        if(!context.getPlayer().isCreative())
                            item.shrink(1);
                    }
                }
            }
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < width; z++) {
                    for (int y = 0; y < height; y++) {
                        if(context.getLevel().getBlockEntity(cPos.offset(x, y, z)) instanceof DistillationTankBlockEntity dtbe){
                            dtbe.updateVerticalMulti();
                            dtbe.updateConnectivity();
                        }
                    }
                }
            }

            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}
