package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeTab {
    public static final CreativeModeTab CREATIVE_TAB = new CreativeModeTab("cdg_creative_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(BlockRegistry.DIESEL_ENGINE.get());
        }
    };

}
