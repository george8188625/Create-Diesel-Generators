package com.jesz.createdieselgenerators.compat;


import net.mehvahdjukaar.selene.block_set.BlockSetManager;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;

public class EveryCompatCompat {
    public static void init() {
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder
                .simple("createdieselgenerators", "chip_wood", "chip_wood_block", "chip_wood_beam"));
    }
}
