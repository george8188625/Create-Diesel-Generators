package com.jesz.createdieselgenerators;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jesz.createdieselgenerators.blocks.ICDGKinetics;
import com.jesz.createdieselgenerators.commands.CDGCommands;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CKinetics;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.loot.LootModifierManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.command.ConfigCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Mod.EventBusSubscriber(modid = "createdieselgenerators")
public class Events {
    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event){
        new CDGCommands(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
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
            FluidStack stack = new FluidStack(ForgeMod.MILK.get(), 1);
            if(item instanceof BucketItem bi)
                stack = new FluidStack(bi.getFluid(), 1);

            if(CreateDieselGenerators.getGeneratedSpeed(stack) != 0){
                if(Screen.hasAltDown()) {
                    tooltip.add(1, Components.translatable("createdieselgenerators.tooltip.holdForFuelStats", Components.translatable("createdieselgenerators.tooltip.keyAlt").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(2, Components.immutableEmpty());
                    tooltip.add(3, Components.translatable("createdieselgenerators.tooltip.fuelSpeed", Lang.number(CreateDieselGenerators.getGeneratedSpeed(stack)).component().withStyle(TooltipHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.GRAY));
                    tooltip.add(4, Components.translatable("createdieselgenerators.tooltip.fuelStress", Lang.number(CreateDieselGenerators.getGeneratedStress(stack)).component().withStyle(TooltipHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.GRAY));
                    tooltip.add(5, Components.translatable("createdieselgenerators.tooltip.fuelBurnRate", Lang.number(CreateDieselGenerators.getBurnRate(stack)).component().withStyle(TooltipHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.GRAY));
                }else {
                    tooltip.add(1, Components.translatable("createdieselgenerators.tooltip.holdForFuelStats", Components.translatable("createdieselgenerators.tooltip.keyAlt").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
        if(ForgeRegistries.ITEMS.getKey(item).getNamespace() != "createdieselgenerators")
            return;
        String path = "createdieselgenerators." + ForgeRegistries.ITEMS.getKey(item).getPath();
        if(!Components.translatable(path + ".tooltip.summary").getString().equals(path + ".tooltip.summary"))
            if(Screen.hasShiftDown()) {
                tooltip.add(1, Lang.translateDirect("tooltip.holdForDescription", Components.translatable("create.tooltip.keyShift").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(2, Components.immutableEmpty());
                tooltip.addAll(3,  TooltipHelper.cutStringTextComponent(Components.translatable(path + ".tooltip.summary").getString(), TooltipHelper.Palette.STANDARD_CREATE));

            }else {
                tooltip.add(1, Lang.translateDirect("tooltip.holdForDescription", Components.translatable("create.tooltip.keyShift").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY));
            }
        CKinetics config = AllConfigs.server().kinetics;

        if(item instanceof BlockItem bi)
            if(bi.getBlock() instanceof ICDGKinetics k && event.getEntity() instanceof Player p){
                boolean hasGoggles = GogglesItem.isWearingGoggles(p);



                if(k.getDefaultStressCapacity() != 0){
                    float stressCapacity = k.getDefaultStressCapacity();
                    float speed = k.getDefaultSpeed();

                    tooltip.add(Components.immutableEmpty());

                    tooltip.add(Components.translatable("create.tooltip.capacityProvided").withStyle(ChatFormatting.GRAY));
                    MutableComponent component;
                    if (k.getDefaultStressCapacity() >= config.highCapacity.get())
                        component = Components.literal(TooltipHelper.makeProgressBar(3, 3)).append(hasGoggles ? Components.empty() : Components.translatable("create.tooltip.capacityProvided.high")).withStyle(IRotate.StressImpact.LOW.getAbsoluteColor());
                    else if (k.getDefaultStressCapacity() >= config.mediumCapacity.get())
                        component = Components.literal(TooltipHelper.makeProgressBar(3, 2)).append(hasGoggles ? Components.empty() : Components.translatable("create.tooltip.capacityProvided.medium")).withStyle(IRotate.StressImpact.MEDIUM.getAbsoluteColor());
                    else
                        component = Components.literal(TooltipHelper.makeProgressBar(3, 1)).append(hasGoggles ? Components.empty() : Components.translatable("create.tooltip.capacityProvided.low")).withStyle(IRotate.StressImpact.HIGH.getAbsoluteColor());

                    if (hasGoggles) {
                        tooltip.add(component.append(Lang.number(stressCapacity / speed)
                                .text("x ")
                                .add(Lang.translate("generic.unit.rpm"))
                                .component()));

                        if (speed != 0) {
                            tooltip.add(Components.literal(" -> ")
                                    .append(Lang.translate("tooltip.up_to", Lang.number(k.getDefaultStressCapacity())).add(Lang.translate("generic.unit.stress")).component()).withStyle(ChatFormatting.DARK_GRAY));
                        }
                    }else
                        tooltip.add(component);
                }else if(k.getDefaultStressStressImpact() != 0){
                    tooltip.add(Components.immutableEmpty());

                    tooltip.add(Components.translatable("create.tooltip.stressImpact").withStyle(ChatFormatting.GRAY));
                    if(k.getDefaultStressStressImpact() >= config.highStressImpact.get())
                        tooltip.add(Components.literal(TooltipHelper.makeProgressBar(3, 3)).append(hasGoggles ? Lang.number(k.getDefaultStressStressImpact()).add(Lang.text("x ").add(Lang.translate("generic.unit.rpm"))).component() : Components.translatable("create.tooltip.stressImpact.high")).withStyle(IRotate.StressImpact.HIGH.getAbsoluteColor()));
                    else if(k.getDefaultStressStressImpact() >= config.mediumStressImpact.get())
                        tooltip.add(Components.literal(TooltipHelper.makeProgressBar(3, 2)).append(hasGoggles ? Lang.number(k.getDefaultStressStressImpact()).add(Lang.text("x ").add(Lang.translate("generic.unit.rpm"))).component() : Components.translatable("create.tooltip.stressImpact.medium")).withStyle(IRotate.StressImpact.MEDIUM.getAbsoluteColor()));
                    else
                        tooltip.add(Components.literal(TooltipHelper.makeProgressBar(3, 1)).append(hasGoggles ? Lang.number(k.getDefaultStressStressImpact()).add(Lang.text("x ").add(Lang.translate("generic.unit.rpm"))).component() : Components.translatable("create.tooltip.stressImpact.low")).withStyle(IRotate.StressImpact.LOW.getAbsoluteColor()));
                }
            }

    }
}
