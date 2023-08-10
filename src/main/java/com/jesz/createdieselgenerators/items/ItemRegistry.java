package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.CreativeTab;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class ItemRegistry {

    public static final ItemEntry<Item> ENGINEPISTON = REGISTRATE.item("engine_piston", Item::new).register();
    public static final ItemEntry<Item> ENGINESILENCER = REGISTRATE.item("engine_silencer", Item::new).register();


    public static void register() {}
}
