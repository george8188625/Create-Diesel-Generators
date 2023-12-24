package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock;
import com.jesz.createdieselgenerators.blocks.ICDGKinetics;
import com.jesz.createdieselgenerators.commands.CDGCommands;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.jesz.createdieselgenerators.other.EntityTickEvent;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CKinetics;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.command.ConfigCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = "createdieselgenerators")
public class Events {
    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event){
        new CDGCommands(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event){
        event.addListener(FuelTypeManager.ReloadListener.INSTANCE);
    }
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent event){
        if(event.entity instanceof ItemEntity itemEntity)
            if(itemEntity.getItem().is(ItemRegistry.LIGHTER.get()) && ConfigRegistry.COMBUSTIBLES_BLOW_UP.get() && itemEntity.getItem().getTag() != null)
                if(itemEntity.getItem().getTag().getInt("Type") == 2) {
                    FluidState fState = itemEntity.level().getFluidState(new BlockPos(itemEntity.getBlockX(), itemEntity.getBlockY(), itemEntity.getBlockZ()));
                    if(fState.is(Fluids.WATER) || fState.is(Fluids.FLOWING_WATER)) {
                        itemEntity.getItem().getTag().putInt("Type", 1);
                        itemEntity.level().playLocalSound(itemEntity.getPosition(1).x, itemEntity.getPosition(1).y, itemEntity.getPosition(1).z, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f, false);
                        return;
                    }
                    if(FuelTypeManager.getGeneratedSpeed(fState.getType()) != 0)
                        itemEntity.level().explode(null, null, null, itemEntity.getPosition(1).x, itemEntity.getPosition(1).y, itemEntity.getPosition(1).z, 3, true, Level.ExplosionInteraction.BLOCK);
                }
    }
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent event){
        Level level = event.getLevel();
        if(ConfigRegistry.COMBUSTIBLES_BLOW_UP.get() && !level.isClientSide)
            for (int x = -2; x < 2; x++) {
                for (int y = -2; y < 2; y++) {
                    for (int z = -2; z < 2; z++) {
                        BlockPos pos = new BlockPos((int) (x+event.getExplosion().getPosition().x), (int) (y+event.getExplosion().getPosition().y), (int) (z+event.getExplosion().getPosition().z));

                        if (!level.isInWorldBounds(pos)) continue;
                        if(Math.abs(Math.sqrt(x*x+y*y+z*z)) < 2) {
                            FluidState fluidState = level.getFluidState(pos);

                            if (FuelTypeManager.getGeneratedSpeed(fluidState.getType()) != 0) {
                                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                                try {
                                    level.explode(null, null, null, pos.getX(), pos.getY(), pos.getZ(), 3, true, Level.ExplosionInteraction.BLOCK);
                                }catch (StackOverflowError ignored){}
                            }
                            BlockEntity be = level.getBlockEntity(pos);
                            if(be == null)
                                continue;
                            IFluidHandler tank = be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
                            if(tank == null)
                                continue;
                            if(FuelTypeManager.getGeneratedSpeed(tank.getFluidInTank(0).getFluid()) == 0)
                                continue;
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                            try {
                                level.explode(null, null, null, pos.getX(), pos.getY(), pos.getZ(), 3 + ((float) tank.getFluidInTank(0).getAmount() / 500), true, Level.ExplosionInteraction.BLOCK);
                            }catch (StackOverflowError ignored){}
                        }
                    }
                }
            }
    }
    @SubscribeEvent
    public static void addTrade(VillagerTradesEvent event) {
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
        if(!(event.getType() == VillagerProfession.TOOLSMITH))
            return;
        trades.get(2).add((t, r) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 5),
                new ItemStack(ItemRegistry.LIGHTER.get()),
                10,8,0.02f));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void addToItemTooltip(ItemTooltipEvent event) {
        if (!AllConfigs.client().tooltips.get())
            return;
        if (event.getEntity() == null)
            return;
        List<Component> tooltip = event.getToolTip();
        Item item = event.getItemStack().getItem();
        if((item instanceof BucketItem || item instanceof MilkBucketItem) && ConfigRegistry.FUEL_TOOLTIPS.get()){
            Fluid fluid = ForgeMod.MILK.get();
            if(item instanceof BucketItem bi)
                fluid = bi.getFluid();

            if(FuelTypeManager.getGeneratedSpeed(fluid) != 0){
                if(Screen.hasAltDown()) {
                    tooltip.add(1, Components.translatable("createdieselgenerators.tooltip.holdForFuelStats", Components.translatable("createdieselgenerators.tooltip.keyAlt").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(2, Components.immutableEmpty());
                    byte enginesEnabled = (byte) ((DieselGeneratorBlock.EngineTypes.NORMAL.enabled() ? 1 : 0) + (DieselGeneratorBlock.EngineTypes.MODULAR.enabled() ? 1 : 0) + (DieselGeneratorBlock.EngineTypes.HUGE.enabled() ? 1 : 0));
                    int currentEngineIndex = (AnimationTickHolder.getTicks() % (120)) / 20;
                    List<DieselGeneratorBlock.EngineTypes> enabledEngines = Arrays.stream(DieselGeneratorBlock.EngineTypes.values()).filter(DieselGeneratorBlock.EngineTypes::enabled).toList();
                    DieselGeneratorBlock.EngineTypes currentEngine = enabledEngines.get(currentEngineIndex % enginesEnabled);
                    float currentSpeed = FuelTypeManager.getGeneratedSpeed(currentEngine, fluid);
                    float currentCapacity = FuelTypeManager.getGeneratedStress(currentEngine, fluid);
                    float currentBurn = FuelTypeManager.getBurnRate(currentEngine, fluid);
                    if(enginesEnabled != 1)
                        tooltip.add(3, Components.translatable("block.createdieselgenerators."+
                                (currentEngine == DieselGeneratorBlock.EngineTypes.MODULAR ? "large_" : currentEngine == DieselGeneratorBlock.EngineTypes.HUGE ? "huge_" : "")+"diesel_engine").withStyle(ChatFormatting.GRAY));
                    tooltip.add(enginesEnabled != 1 ? 4 : 3, Components.translatable("createdieselgenerators.tooltip.fuelSpeed", Lang.number(currentSpeed).component().withStyle(TooltipHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(enginesEnabled != 1 ? 5 : 4, Components.translatable("createdieselgenerators.tooltip.fuelStress", Lang.number(currentCapacity).component().withStyle(TooltipHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(enginesEnabled != 1 ? 6 : 5, Components.translatable("createdieselgenerators.tooltip.fuelBurnRate", Lang.number(currentBurn).component().withStyle(TooltipHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(enginesEnabled != 1 ? 7 : 6, Components.immutableEmpty());
                }else {
                    tooltip.add(1, Components.translatable("createdieselgenerators.tooltip.holdForFuelStats", Components.translatable("createdieselgenerators.tooltip.keyAlt").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
        if(ForgeRegistries.ITEMS.getKey(item).getNamespace() != "createdieselgenerators")
            return;
        String path = "createdieselgenerators." + ForgeRegistries.ITEMS.getKey(item).getPath();
        List<Component> tooltipList = new ArrayList<>();
        if(!Component.translatable(path + ".tooltip.summary").getString().equals(path + ".tooltip.summary")) {
            if (Screen.hasShiftDown()) {
                tooltipList.add(Lang.translateDirect("tooltip.holdForDescription", Component.translatable("create.tooltip.keyShift").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_GRAY));
                tooltipList.add(Components.immutableEmpty());
                tooltipList.addAll(TooltipHelper.cutStringTextComponent(Component.translatable(path + ".tooltip.summary").getString(), TooltipHelper.Palette.STANDARD_CREATE));

                if(!Component.translatable(path + ".tooltip.condition1").getString().equals(path + ".tooltip.condition1")) {
                    tooltipList.add(Components.immutableEmpty());
                    tooltipList.add(Component.translatable(path + ".tooltip.condition1").withStyle(ChatFormatting.GRAY));
                    tooltipList.addAll(TooltipHelper.cutStringTextComponent(Component.translatable(path + ".tooltip.behaviour1").getString(), TooltipHelper.Palette.STANDARD_CREATE.primary(), TooltipHelper.Palette.STANDARD_CREATE.highlight(), 1));
                    if(!Component.translatable(path + ".tooltip.condition2").getString().equals(path + ".tooltip.condition2")) {
                        tooltipList.add(Component.translatable(path + ".tooltip.condition2").withStyle(ChatFormatting.GRAY));
                        tooltipList.addAll(TooltipHelper.cutStringTextComponent(Component.translatable(path + ".tooltip.behaviour2").getString(), TooltipHelper.Palette.STANDARD_CREATE.primary(), TooltipHelper.Palette.STANDARD_CREATE.highlight(), 1));
                    }
                }
            } else {
                tooltipList.add(Lang.translateDirect("tooltip.holdForDescription", Component.translatable("create.tooltip.keyShift").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        tooltip.addAll(1,tooltipList);
        CKinetics config = AllConfigs.server().kinetics;

        if(item instanceof BlockItem bi)
            if(bi.getBlock() instanceof ICDGKinetics k){
                boolean hasGoggles = GogglesItem.isWearingGoggles(event.getEntity());



                if(k.getDefaultStressCapacity() != 0){
                    float stressCapacity = k.getDefaultStressCapacity();
                    float speed = k.getDefaultSpeed();

                    tooltip.add(Components.immutableEmpty());

                    tooltip.add(Component.translatable("create.tooltip.capacityProvided").withStyle(ChatFormatting.GRAY));
                    MutableComponent component;
                    if (k.getDefaultStressCapacity() >= config.highCapacity.get())
                        component = Components.literal(TooltipHelper.makeProgressBar(3, 3)).append(hasGoggles ? Component.empty() : Component.translatable("create.tooltip.capacityProvided.high")).withStyle(IRotate.StressImpact.LOW.getAbsoluteColor());
                    else if (k.getDefaultStressCapacity() >= config.mediumCapacity.get())
                        component = Components.literal(TooltipHelper.makeProgressBar(3, 2)).append(hasGoggles ? Component.empty() : Component.translatable("create.tooltip.capacityProvided.medium")).withStyle(IRotate.StressImpact.MEDIUM.getAbsoluteColor());
                    else
                        component = Components.literal(TooltipHelper.makeProgressBar(3, 1)).append(hasGoggles ? Component.empty() : Components.translatable("create.tooltip.capacityProvided.low")).withStyle(IRotate.StressImpact.HIGH.getAbsoluteColor());

                    if (hasGoggles) {
                        tooltip.add(component.append(Lang.number(stressCapacity / speed)
                                .text("x ")
                                .add(Lang.translate("generic.unit.rpm"))
                                .component()));

                        if (speed != 0) {
                            tooltip.add(Component.literal(" -> ")
                                    .append(Lang.translate("tooltip.up_to", Lang.number(k.getDefaultStressCapacity())).add(Lang.translate("generic.unit.stress")).component()).withStyle(ChatFormatting.DARK_GRAY));
                        }
                    }else
                        tooltip.add(component);
                }else if(k.getDefaultStressStressImpact() != 0){
                    tooltip.add(Components.immutableEmpty());

                    tooltip.add(Component.translatable("create.tooltip.stressImpact").withStyle(ChatFormatting.GRAY));
                    if(k.getDefaultStressStressImpact() >= config.highStressImpact.get())
                        tooltip.add(Components.literal(TooltipHelper.makeProgressBar(3, 3)).append(hasGoggles ? Lang.number(k.getDefaultStressStressImpact()).add(Lang.text("x ").add(Lang.translate("generic.unit.rpm"))).component() : Component.translatable("create.tooltip.stressImpact.high")).withStyle(IRotate.StressImpact.HIGH.getAbsoluteColor()));
                    else if(k.getDefaultStressStressImpact() >= config.mediumStressImpact.get())
                        tooltip.add(Components.literal(TooltipHelper.makeProgressBar(3, 2)).append(hasGoggles ? Lang.number(k.getDefaultStressStressImpact()).add(Lang.text("x ").add(Lang.translate("generic.unit.rpm"))).component() : Component.translatable("create.tooltip.stressImpact.medium")).withStyle(IRotate.StressImpact.MEDIUM.getAbsoluteColor()));
                    else
                        tooltip.add(Components.literal(TooltipHelper.makeProgressBar(3, 1)).append(hasGoggles ? Lang.number(k.getDefaultStressStressImpact()).add(Lang.text("x ").add(Lang.translate("generic.unit.rpm"))).component() : Component.translatable("create.tooltip.stressImpact.low")).withStyle(IRotate.StressImpact.LOW.getAbsoluteColor()));
                }
            }

    }
}
