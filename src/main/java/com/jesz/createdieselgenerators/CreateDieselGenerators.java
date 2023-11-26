package com.jesz.createdieselgenerators;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.blocks.ct.SpriteShifts;
import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.entity.EntityRegistry;
import com.jesz.createdieselgenerators.fluids.FluidRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.jesz.createdieselgenerators.other.CDGPartialModel;
import com.jesz.createdieselgenerators.other.SpoutCanisterFilling;
import com.jesz.createdieselgenerators.ponder.PonderIndex;
import com.jesz.createdieselgenerators.recipes.RecipeRegistry;
import com.jesz.createdieselgenerators.sounds.SoundRegistry;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.utility.Components;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.simibubi.create.AllTags.optionalTag;
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
        EntityRegistry.register();
        SoundRegistry.register(modEventBus);
        RecipeRegistry.register(modEventBus);

        Mods.COMPUTERCRAFT.executeIfInstalled(() -> CCProxy::register);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> onClient(modEventBus, forgeEventBus));
        BlockSpoutingBehaviour.addCustomSpoutInteraction(new ResourceLocation("createdieselgenerators:canister_filling"), new SpoutCanisterFilling());
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigRegistry.SERVER_SPEC, "createdieselgenerators-server.toml");
        MinecraftForge.EVENT_BUS.register(this);
        REGISTRATE.registerEventListeners(modEventBus);
    }
    public static MutableComponent translate(String key, Object... args) {
        return Components.translatable(key, resolveBuilders(args));
    }
    public static Map<String, String> lighterSkins = new HashMap<>();

    public static void onClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        SpriteShifts.init();
        PartialModels.init();

        modEventBus.addListener(CreateDieselGenerators::clientInit);
        modEventBus.addListener(CreateDieselGenerators::onModelRegistry);
        modEventBus.addListener(CDGPartialModel::onModelBake);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigRegistry.CLIENT_SPEC, "createdieselgenerators-client.toml");
    }
    public static void onModelRegistry(final ModelRegistryEvent event){
        lighterSkins.clear();
        Minecraft.getInstance().getResourceManager().getNamespaces().stream().toList().forEach(n -> {
            try {
                Resource resource = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(n, "lighter_skins.json"));
                JsonParser parser = new JsonParser();
                JsonElement data = parser.parse(new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)));
                data.getAsJsonArray().forEach(jsonElement -> {
                    CreateDieselGenerators.lighterSkins.put(jsonElement.getAsJsonObject().getAsJsonPrimitive("name").getAsString(), jsonElement.getAsJsonObject().getAsJsonPrimitive("id").getAsString());
                });
            } catch (IOException ignored) {
            }
        });
        PartialModels.initSkins();
        CDGPartialModel.onModelRegistry(event);
    }
    public static void clientInit(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(FluidRegistry.ETHANOL.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(FluidRegistry.ETHANOL.get().getSource(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.MODULAR_DIESEL_ENGINE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.DIESEL_ENGINE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.CANISTER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.HUGE_DIESEL_ENGINE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.DISTILLATION_TANK.get(), RenderType.translucent());

        PonderIndex.register();
    }

    public static int getOilAmount(Holder<Biome> biome, int x, int z, long seed){
        Random random = new Random(new Random(seed).nextLong() + (long) x * z);
        int amount = Math.abs(random.nextInt());
        boolean isHighInOil = biome == null || biome.is(AllTags.optionalTag(ForgeRegistries.BIOMES, new ResourceLocation("createdieselgenerators:oil_biomes")));

        if(biome != null && biome.is(AllTags.optionalTag(ForgeRegistries.BIOMES, new ResourceLocation("createdieselgenerators:deny_oil_biomes"))))
            return 0;
        if(isHighInOil ? (random.nextFloat(0, 100) >= ConfigRegistry.HIGH_OIL_PERCENTAGE.get()) : (amount % 100 >= ConfigRegistry.OIL_PERCENTAGE.get()))
            return 0;
        if(isHighInOil)
            return (int) (Mth.clamp(amount % 400000, 8000, 400000)*ConfigRegistry.HIGH_OIL_MULTIPLIER.get());
        return (int) (Mth.clamp(amount % 200, 0, 1000)*ConfigRegistry.OIL_MULTIPLIER.get());
    }
}
