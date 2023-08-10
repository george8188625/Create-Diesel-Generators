package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.fluids.FluidRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTab {

    private static final DeferredRegister<CreativeModeTab> TAB_REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "createdieselgenerators");
    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = TAB_REGISTER.register("cdg_creative_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.cdg_creative_tab"))
                    .icon(BlockRegistry.DIESEL_ENGINE::asStack)
                    .displayItems((pParameters, output) -> {
                        output.accept(ItemRegistry.ENGINEPISTON.get());
                        output.accept(ItemRegistry.ENGINESILENCER.get());
                        output.accept(BlockRegistry.DIESEL_ENGINE.get());
                        output.accept(BlockRegistry.MODULAR_DIESEL_ENGINE.get());
                        output.accept(BlockRegistry.BASIN_LID.get());
                        output.accept(FluidRegistry.BIODIESEL.getBucket().get());
                        output.accept(FluidRegistry.ETHANOL.getBucket().get());
                        output.accept(FluidRegistry.PLANT_OIL.getBucket().get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        TAB_REGISTER.register(modEventBus);
    }

}
