package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.compat.EveryCompatCompat;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
import com.jesz.createdieselgenerators.content.molds.MoldType;
import com.jesz.createdieselgenerators.content.tools.lighter.LighterModel;
import com.jesz.createdieselgenerators.datagen.CDGDatagen;
import com.jesz.createdieselgenerators.packets.CDGPackets;
import com.jesz.createdieselgenerators.ponder.CDGPonderPlugin;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.ID;

@Mod(ID)
public class CreateDieselGenerators
{
    public static final String ID = "createdieselgenerators";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );;
    public CreateDieselGenerators() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        CDGItems.register();
        CDGBlocks.register();
        CDGFluids.register();
        CDGBlockEntityTypes.register();
        CDGEntityTypes.register();
        CDGSoundEvents.register(modEventBus);
        CDGRecipes.register(modEventBus);
        CDGMenuTypes.register();
        MoldType.register();
        CDGMountedStorageTypes.register();
        CDGCreativeTab.register(modEventBus);


        if(ModList.get().isLoaded("moonlight"))
            EveryCompatCompat.init();
        Mods.COMPUTERCRAFT.executeIfInstalled(() -> CCProxy::register);


        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> onClient(modEventBus, forgeEventBus));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CDGConfig.SERVER_SPEC, ID + "-server.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CDGConfig.COMMON_SPEC, ID + "-common.toml");
        CDGPackets.registerPackets();
        MinecraftForge.EVENT_BUS.register(this);

        REGISTRATE.registerEventListeners(modEventBus);
        modEventBus.addListener(EventPriority.LOWEST, CDGDatagen::gatherData);

    }

    public static void onClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CDGConfig.CLIENT_SPEC, ID + "-client.toml");
        modEventBus.addListener(CreateDieselGenerators::clientSetup);
        modEventBus.addListener(LighterModel::onModelBake);

    }

    public static void clientSetup(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(CDGFluids.ETHANOL.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(CDGFluids.ETHANOL.getSource(), RenderType.translucent());
        event.enqueueWork(CreateDieselGenerators::clientInit);
    }

    public static void clientInit() {
        PonderIndex.addPlugin(new CDGPonderPlugin());
        CDGPartialModels.init();
        CDGSpriteShifts.init();
    }

    public static ResourceLocation rl(String path){
        return new ResourceLocation(ID, path);
    }

    public static Component lang(String path, Object... args) {
        return Component.translatable(ID+"."+path, args);
    }
}
