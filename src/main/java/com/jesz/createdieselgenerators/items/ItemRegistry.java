package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.CreativeTab;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class ItemRegistry {
    static { REGISTRATE.creativeModeTab(() -> CreativeTab.CREATIVE_TAB); }

    public static final ItemEntry<Item> ENGINEPISTON = REGISTRATE.item("engine_piston", Item::new).register();
    public static final ItemEntry<Item> ENGINESILENCER = REGISTRATE.item("engine_silencer", Item::new).register();


    public static void register() {}
}
