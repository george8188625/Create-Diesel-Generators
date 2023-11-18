package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.CreativeTab;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class ItemRegistry {

    public static final ItemEntry<Item> KELP_HANDLE = REGISTRATE.item("kelp_handle", Item::new).register();
    public static final ItemEntry<FurnaceBurnItem> WOOD_CHIPS = REGISTRATE.item("wood_chip", p -> new FurnaceBurnItem(p, 200)).register();
    public static final ItemEntry<Item> ENGINE_PISTON = REGISTRATE.item("engine_piston", Item::new).register();
    public static final ItemEntry<Item> ENGINE_SILENCER = REGISTRATE.item("engine_silencer", Item::new).register();
    public static final ItemEntry<Item> ENGINE_TURBO = REGISTRATE.item("engine_turbocharger", Item::new).register();
    public static final ItemEntry<DistillationControllerItem> DISTILLATION_CONTROLLER = REGISTRATE.item("distillation_controller", DistillationControllerItem::new).register();
    public static final ItemEntry<LighterItem> LIGHTER = REGISTRATE.item("lighter", LighterItem::new).register();
    public static final ItemEntry<ChemicalSprayerItem> CHEMICAL_SPRAYER = REGISTRATE.item("chemical_sprayer", p -> new ChemicalSprayerItem(p, false)).register();
    public static final ItemEntry<ChemicalSprayerItem> CHEMICAL_SPRAYER_LIGHTER = REGISTRATE.item("chemical_sprayer_lighter", p -> new ChemicalSprayerItem(p, true)).register();
    public static final ItemEntry<OilScannerItem> OIL_SCANNER = REGISTRATE.item("oil_scanner", OilScannerItem::new).onRegister(OilScannerItem::registerModelOverrides).register();


    public static void register() {}
}
