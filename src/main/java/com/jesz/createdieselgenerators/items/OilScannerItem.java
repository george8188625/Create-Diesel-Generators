package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.world.OilChunksSavedData;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;

public class OilScannerItem extends Item {
    public OilScannerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if(player.getY() < ConfigRegistry.MAX_OIL_SCANNER_LEVEL.get()) {
            stack.getOrCreateTag().putInt("Time", 20);
            stack.getOrCreateTag().putInt("Type", 0);
        }else
            level.playLocalSound(player.getX(), player.getY(), player.getZ(), AllSoundEvents.DENY.getMainEvent(), SoundSource.PLAYERS, 1.2f, 1, true);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int p_41407_, boolean p_41408_) {
        if(stack.getTag() != null){
            if(stack.getTag().getInt("Type") == 0) {

                    if(level instanceof ServerLevel) {
                        if (stack.getTag().getInt("Time") == 0) {
                            stack.getTag().putInt("Time", 20);

                            int amount = CreateDieselGenerators.getOilAmount(
                                    level.getBiome(new BlockPos((entity.getBlockX() / 16) * 16, 64, (entity.getBlockZ() / 16) * 16)),
                                    entity.getBlockX() / 16, entity.getBlockZ() / 16, ((ServerLevel) level).getSeed());
                            OilChunksSavedData sd = OilChunksSavedData.load((ServerLevel) level);
                            if (sd.getChunkOilAmount(new ChunkPos(entity.getBlockX() / 16, entity.getBlockZ() / 16)) >= 0)
                                amount = sd.getChunkOilAmount(new ChunkPos(entity.getBlockX() / 16, entity.getBlockZ() / 16));

                            if (amount <= 0)
                                stack.getTag().putInt("Type", 1);
                            else if (entity.getLevel().getBiome(new BlockPos((entity.getBlockX() / 16) * 16, 64, (entity.getBlockZ() / 16) * 16)).is(AllTags.optionalTag(ForgeRegistries.BIOMES, new ResourceLocation("createdieselgenerators:oil_biomes"))))
                                stack.getTag().putInt("Type", 3);
                            else
                                stack.getTag().putInt("Type", 2);
                        }
                        stack.getTag().putInt("Time", stack.getTag().getInt("Time")-1);
                    }else
                        level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), AllSoundEvents.SCROLL_VALUE.getMainEvent(), SoundSource.PLAYERS, 0.2f, 1, true);
            }
        }
        super.inventoryTick(stack, level, entity, p_41407_, p_41408_);
    }

    public void registerModelOverrides() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ItemProperties.register(ItemRegistry.OIL_SCANNER.get(), new ResourceLocation("createdieselgenerators:oil_scanner_state"), (pStack, pLevel, pEntity, pSeed) -> {
                CompoundTag tag = pStack.getTag();
                return tag == null ? 0 : tag.getInt("Type");
            });
        });
    }
}
