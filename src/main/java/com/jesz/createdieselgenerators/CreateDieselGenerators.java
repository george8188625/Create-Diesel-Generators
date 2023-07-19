package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.fluids.FluidRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.jesz.createdieselgenerators.ponder.PonderIndex;
import com.jesz.createdieselgenerators.recipes.RecipeRegistry;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.utility.Components;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.simibubi.create.foundation.utility.Lang.resolveBuilders;

@Mod("createdieselgenerators")
public class CreateDieselGenerators
{
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create("createdieselgenerators");
    public CreateDieselGenerators()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;


        ItemRegistry.register();
        BlockRegistry.register();
        FluidRegistry.register();
        BlockEntityRegistry.register();
        RecipeRegistry.register(modEventBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> onClient(modEventBus, forgeEventBus));

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigRegistry.SERVER_SPEC, "createdieselgenerators-server.toml");
        MinecraftForge.EVENT_BUS.register(this);
        REGISTRATE.registerEventListeners(modEventBus);
    }
    public static MutableComponent translate(String key, Object... args) {
        return Components.translatable(key, resolveBuilders(args));
    }

    public static void onClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(CreateDieselGenerators::clientInit);

    }

    public static void clientInit(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(FluidRegistry.ETHANOL.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(FluidRegistry.ETHANOL.getSource(), RenderType.translucent());
        PonderIndex.register();
    }
}
