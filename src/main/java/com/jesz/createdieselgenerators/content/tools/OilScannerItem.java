package com.jesz.createdieselgenerators.content.tools;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.world.OilChunksSavedData;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.fml.DistExecutor;

public class OilScannerItem extends Item {
    public OilScannerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getY() < CDGConfig.MAX_OIL_SCANNER_LEVEL.get()) {
            stack.getOrCreateTag().putInt("Time", 20);
            stack.getOrCreateTag().putInt("Type", 0);
            if (level.isClientSide)
                player.displayClientMessage(CreateDieselGenerators.lang("actionbar.oil_scanner.searching"), true);

        } else {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(), AllSoundEvents.DENY.getMainEvent(), SoundSource.PLAYERS, 1.2f, 1, true);
            if (level.isClientSide)
                player.displayClientMessage(CreateDieselGenerators.lang("actionbar.oil_scanner.too_high_up"), true);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (stack.getTag() != null) {
            if (stack.getTag().getInt("Type") == 0) {

                    if (level instanceof ServerLevel sl) {
                        if (stack.getTag().getInt("Time") == 0) {
                            stack.getTag().putInt("Time", 20);
                            ChunkPos chunk = new ChunkPos(new BlockPos(entity.getBlockX(), 0, entity.getBlockZ()));

                            int amount = OilChunksSavedData.getChunkOilAmount(sl, chunk);

                            if (amount <= 0)
                                stack.getTag().putInt("Type", 1);
                            else if (amount == Integer.MAX_VALUE)
                                stack.getTag().putInt("Type", 4);
                            else if (amount >= (CDGConfig.OIL_CHUNK_THRESHOLD.get() + CDGConfig.OIL_CHUNK_INFINITE_THRESHOLD.get()) / 2)
                                stack.getTag().putInt("Type", 3);
                            else
                                stack.getTag().putInt("Type", 2);
                            if (entity instanceof ServerPlayer sp) {
                                if (amount <= 0)
                                    sp.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("createdieselgenerators.actionbar.oil_scanner.oil_none", TooltipHelper.makeProgressBar(3, 0)).withStyle(ChatFormatting.GRAY)));
                                else if (amount == Integer.MAX_VALUE)
                                    sp.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("createdieselgenerators.actionbar.oil_scanner.oil_bottomless", TooltipHelper.makeProgressBar(3, 3)).withStyle(ChatFormatting.RED)));
                                else if (amount >= (CDGConfig.OIL_CHUNK_THRESHOLD.get() + CDGConfig.OIL_CHUNK_INFINITE_THRESHOLD.get()) / 2)
                                    sp.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("createdieselgenerators.actionbar.oil_scanner.oil_high", TooltipHelper.makeProgressBar(3, 2)).withStyle(ChatFormatting.GOLD)));
                                else
                                    sp.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("createdieselgenerators.actionbar.oil_scanner.oil_low", TooltipHelper.makeProgressBar(3, 1)).withStyle(ChatFormatting.GREEN)));

                            }

                        }
                        stack.getTag().putInt("Time", stack.getTag().getInt("Time") - 1);
                    }else
                        level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), AllSoundEvents.SCROLL_VALUE.getMainEvent(), SoundSource.PLAYERS, 0.2f, 1, true);
            }
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    public void registerModelOverrides() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ItemProperties.register(CDGItems.OIL_SCANNER.get(), CreateDieselGenerators.rl("oil_scanner_state"), (stack, level, entity, seed) -> {
                CompoundTag tag = stack.getTag();
                return tag == null ? 0 : tag.getInt("Type");
            });
        });
    }

    public static ItemModelBuilder addOverrideModels(DataGenContext<Item, OilScannerItem> c,
                                                     RegistrateItemModelProvider p) {
        ItemModelBuilder builder = p.generated(c::getEntry, p.modLoc("item/oil_scanner_rotating"));

        builder.override()
                .predicate(CreateDieselGenerators.rl("oil_scanner_state"), 0.0f)
                .model(p.getBuilder(c.getName())
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", CreateDieselGenerators.rl("item/oil_scanner_rotating")))
                .end();
        builder.override()
                .predicate(CreateDieselGenerators.rl("oil_scanner_state"), 1.0f)
                .model(p.getBuilder(c.getName() + "_none")
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", CreateDieselGenerators.rl("item/oil_scanner_none")))
                .end();
        builder.override()
                .predicate(CreateDieselGenerators.rl("oil_scanner_state"), 2.0f)
                .model(p.getBuilder(c.getName() + "_medium")
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", CreateDieselGenerators.rl("item/oil_scanner_medium")))
                .end();
        builder.override()
                .predicate(CreateDieselGenerators.rl("oil_scanner_state"), 3.0f)
                .model(p.getBuilder(c.getName() + "_high")
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", CreateDieselGenerators.rl("item/oil_scanner_high")))
                .end();
		builder.override()
                .predicate(CreateDieselGenerators.rl("oil_scanner_state"), 4.0f)
                .model(p.getBuilder(c.getName() + "_bottomless")
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", CreateDieselGenerators.rl("item/oil_scanner_bottomless")))
                .end();
        return builder;
    }
}
