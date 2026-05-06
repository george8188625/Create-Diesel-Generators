package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.compat.EveryCompatCompat;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
import com.jesz.createdieselgenerators.content.molds.MoldType;
import com.jesz.createdieselgenerators.content.tools.lighter.LighterModel;
import com.jesz.createdieselgenerators.packets.CDGPackets;
import com.jesz.createdieselgenerators.ponder.CDGPonderPlugin;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.ID;

@Mod(ID)
public class CreateDieselGenerators
{
    public static final String ID = "createdieselgenerators";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );
    public CreateDieselGenerators(IEventBus modEventBus, ModContainer container) {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
        REGISTRATE.registerEventListeners(modEventBus);

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
        CDGPackets.register();
        CDGDataComponents.register(modEventBus);
        CDGDisplaySources.register();

        if (ModList.get().isLoaded("moonlight"))
            EveryCompatCompat.init();
        Mods.COMPUTERCRAFT.executeIfInstalled(() -> CCProxy::register);

        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> onClient(modEventBus, container));
        container.registerConfig(ModConfig.Type.SERVER, CDGConfig.SERVER_SPEC, ID + "-server.toml");
        container.registerConfig(ModConfig.Type.COMMON, CDGConfig.COMMON_SPEC, ID + "-common.toml");
    }

    public static void onClient(IEventBus modEventBus, ModContainer container) {
        CDGPartialModels.init();
        CDGSpriteShifts.init();
        container.registerConfig(ModConfig.Type.CLIENT, CDGConfig.CLIENT_SPEC, ID + "-client.toml");
        modEventBus.addListener(CreateDieselGenerators::clientInit);
        modEventBus.addListener(LighterModel::onModelBake);

    }

    public static void clientInit(final FMLClientSetupEvent event) {
//        ItemBlockRenderTypes.setRenderLayer(CDGFluids.ETHANOL.get(), RenderType.translucent());
//        ItemBlockRenderTypes.setRenderLayer(CDGFluids.ETHANOL.getSource(), RenderType.translucent());
        PonderIndex.addPlugin(new CDGPonderPlugin());
    }

    public static ResourceLocation rl(String path){
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    public static Component lang(String path, Object... args) {
        return Component.translatable(ID+"."+path, args);
    }
}
