package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.CreativeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "createdieselgenerators");

    public static final RegistryObject<Item> ENGINEPISTON = ITEMS.register("engine_piston",
            () -> new Item(new Item.Properties().tab(CreativeTab.CREATIVE_TAB)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
