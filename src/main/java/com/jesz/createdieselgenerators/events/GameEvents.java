package com.jesz.createdieselgenerators.events;

import com.jesz.createdieselgenerators.*;
import com.jesz.createdieselgenerators.commands.CDGCommands;
import com.jesz.createdieselgenerators.content.andesite_girder.AndesiteGirderWrenchBehaviour;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineTypes;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilteringRenderer;
import com.jesz.createdieselgenerators.content.entity_filter.ReverseLootTable;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.jesz.createdieselgenerators.mixins.LootPoolAccessor;
import com.jesz.createdieselgenerators.mixins.LootTableAccessor;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CKinetics;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.*;

import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.GRAY;

@EventBusSubscriber(modid = CreateDieselGenerators.ID, bus = EventBusSubscriber.Bus.GAME)
public class GameEvents {

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event){
        new CDGCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public static void loadLootTable(LootTableLoadEvent event){
        LootTable table = event.getTable();
        ResourceLocation tableId = table.getLootTableId();
        if(!tableId.getPath().startsWith("entities/"))
                return;
        List<ItemStack> results = new LinkedList<>();
        ((LootTableAccessor)table).getPools().forEach(pool -> {
            List.of(((LootPoolAccessor) pool).getEntries()).forEach(e -> {
                if(e instanceof LootItem lootItem){
                    lootItem.createItemStack(stack -> {
                        String path = tableId.getPath();
                        path = path.replaceAll("entities/", "");
                        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath(tableId.getNamespace(), path));
                        ReverseLootTable.ALL.computeIfAbsent(stack.getItem(), s -> new ArrayList<>()).add(type);

                    },null);
                }
            });
        });
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {

    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event){
        event.addListener(ReverseLootTable.INSTANCE);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        AndesiteGirderWrenchBehaviour.tick();
        EntityFilteringRenderer.tick();
    }

    @SubscribeEvent
    public static void onServerTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level))
            return;
        if (toExplode.containsKey(level)) {
            List<BlockPos> list = toExplode.get(level).stream().toList();
            if (list.isEmpty())
                return;
            for (BlockPos pos : list) {
                level.explode(null, null, null, pos.getX(), pos.getY(), pos.getZ(), 1, true, Level.ExplosionInteraction.BLOCK);
                toExplode.get(level).remove(pos);
            }
        }
    }

    static Map<Level, Set<BlockPos>> toExplode = new HashMap<>();

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event){
        Level level = event.getLevel();
        if (CDGConfig.COMBUSTIBLES_BLOW_UP.get() && !level.isClientSide)
            for (int x = -2; x < 2; x++) {
                for (int y = -2; y < 2; y++) {
                    for (int z = -2; z < 2; z++) {
                        BlockPos pos = new BlockPos((int) (x+event.getExplosion().center().x), (int) (y+event.getExplosion().center().y), (int) (z+event.getExplosion().center().z));

                        if (!level.isInWorldBounds(pos)) continue;
                        if (Math.abs(Math.sqrt(x*x+y*y+z*z)) < 2) {
                            FluidState fluidState = level.getFluidState(pos);
                            boolean flammable = FuelType.getTypeFor(level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fluidState.getType()).normal().speed() != 0;

                            if (flammable) {
                                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                                if (!toExplode.containsKey(level))
                                    toExplode.put(level, new HashSet<>());
                                toExplode.get(level).add(pos);
                                return;
                            }
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
                new ItemCost(Items.EMERALD, 5),
                new ItemStack(CDGItems.LIGHTER.get()),
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
        if ((item instanceof BucketItem || item instanceof MilkBucketItem) && CDGConfig.FUEL_TOOLTIPS.get()) {
            Fluid fluid = NeoForgeMod.MILK.get();
            if (item instanceof BucketItem bi)
                fluid = bi.content;

            FuelType type = FuelType.getTypeFor(Minecraft.getInstance().level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fluid);

            if (Screen.hasAltDown() && type.normal().speed() != 0) {

                tooltip.add(1, Component.translatable("createdieselgenerators.tooltip.holdForFuelStats", Component.translatable("createdieselgenerators.tooltip.keyAlt").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(2, Component.empty());

                byte enginesEnabled = (byte) ((EngineTypes.NORMAL.enabled() ? 1 : 0) + (EngineTypes.MODULAR.enabled() ? 1 : 0) + (EngineTypes.HUGE.enabled() ? 1 : 0));
                int currentEngineIndex = (AnimationTickHolder.getTicks() % (120)) / 20;
                List<EngineTypes> enabledEngines = Arrays.stream(EngineTypes.values()).filter(EngineTypes::enabled).toList();
                EngineTypes currentEngine = enabledEngines.get(currentEngineIndex % enginesEnabled);
                float currentSpeed = type.getGenerated(currentEngine).speed();
                float currentCapacity = type.getGenerated(currentEngine).strength();
                float currentBurn = type.getGenerated(currentEngine).burn();

                if(enginesEnabled != 1)
                    tooltip.add(3, Component.translatable("block.createdieselgenerators."+
                            (currentEngine == EngineTypes.MODULAR ? "large_" : currentEngine == EngineTypes.HUGE ? "huge_" : "")+"diesel_engine").withStyle(ChatFormatting.GRAY));
                tooltip.add(enginesEnabled != 1 ? 4 : 3, Component.translatable("createdieselgenerators.tooltip.fuelSpeed", CreateLang.number(currentSpeed).component().withStyle(FontHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(enginesEnabled != 1 ? 5 : 4, Component.translatable("createdieselgenerators.tooltip.fuelStress", CreateLang.number(currentCapacity).component().withStyle(FontHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(enginesEnabled != 1 ? 6 : 5, Component.translatable("createdieselgenerators.tooltip.fuelBurnRate", CreateLang.number(currentBurn * 20).component().withStyle(FontHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(enginesEnabled != 1 ? 7 : 6, Component.empty());
                tooltip.add(enginesEnabled != 1 ? 8 : 7, Component.translatable("createdieselgenerators.tooltip.burnerStrength", CreateLang.number(type.burnerStrength() * 100).text(" %").component().withStyle(FontHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(enginesEnabled != 1 ? 9 : 8, Component.empty());
            } else if (type.normal().speed() != 0) {
                tooltip.add(1, Component.translatable("createdieselgenerators.tooltip.holdForFuelStats", Component.translatable("createdieselgenerators.tooltip.keyAlt").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        CKinetics config = AllConfigs.server().kinetics;

        if (!(item instanceof BlockItem bi) ||
                !IRotate.StressImpact.isEnabled() ||
                !(CDGBlocks.DIESEL_ENGINE.is(bi) ||
                CDGBlocks.MODULAR_DIESEL_ENGINE.is(bi) ||
                CDGBlocks.HUGE_DIESEL_ENGINE.is(bi)))
            return;

        tooltip.add(Component.empty());

        int highestRPM = 0;
        int highestCapacity = 0;
        int highestStressCapacity = 0;

        for (var r : Minecraft.getInstance().level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE).listElements().toList()) {
            FuelType type = r.value();
            if (CDGBlocks.DIESEL_ENGINE.is(bi)) {
                highestRPM = (int) Math.max(highestRPM, type.normal().speed());
                highestCapacity = (int) Math.max(highestCapacity, type.normal().strength() / type.normal().speed());
                highestStressCapacity = (int) Math.max(highestStressCapacity, type.normal().strength());
            }
            else if (CDGBlocks.MODULAR_DIESEL_ENGINE.is(bi)) {
                highestRPM = (int) Math.max(highestRPM, type.modular().speed());
                highestCapacity = (int) Math.max(highestCapacity, type.modular().strength() / type.modular().speed());
                highestStressCapacity = (int) Math.max(highestStressCapacity, type.modular().strength());
            }
            else if (CDGBlocks.HUGE_DIESEL_ENGINE.is(bi)) {
                highestRPM = (int) Math.max(highestRPM, type.huge().speed());
                highestCapacity = (int) Math.max(highestCapacity, type.huge().strength() / type.huge().speed());
                highestStressCapacity = (int) Math.max(highestStressCapacity, type.huge().strength());
            }
        }
        boolean hasGoggles = GogglesItem.isWearingGoggles(event.getEntity());

        LangBuilder rpmUnit = CreateLang.translate("generic.unit.rpm");
        LangBuilder suUnit = CreateLang.translate("generic.unit.stress");

        CreateLang.translate("tooltip.capacityProvided")
                .style(GRAY)
                .addTo(tooltip);

        IRotate.StressImpact impactId = highestCapacity >= config.highCapacity.get() ? IRotate.StressImpact.HIGH
                : (highestCapacity >= config.mediumCapacity.get() ? IRotate.StressImpact.MEDIUM : IRotate.StressImpact.LOW);
        IRotate.StressImpact opposite = IRotate.StressImpact.values()[IRotate.StressImpact.values().length - 2 - impactId.ordinal()];
        LangBuilder builder = CreateLang.builder()
                .add(CreateLang.text(TooltipHelper.makeProgressBar(3, impactId.ordinal() + 1))
                        .style(opposite.getAbsoluteColor()));

        if (hasGoggles) {
            builder.add(CreateLang.number(highestCapacity))
                    .text("x ")
                    .add(rpmUnit)
                    .addTo(tooltip);
            LangBuilder amount = CreateLang.number(highestStressCapacity)
                    .add(suUnit);
            CreateLang.text(" -> ")
                    .add(CreateLang.translate("tooltip.up_to", amount))
                    .style(DARK_GRAY)
                    .addTo(tooltip);

        } else
            builder.translate("tooltip.capacityProvided." + Lang.asId(impactId.name()))
                    .addTo(tooltip);

    }
}
