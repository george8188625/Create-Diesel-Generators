package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.pumpjack.PumpjackOilAmountDisplaySource;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;

import java.util.function.Supplier;
import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class CDGDisplaySources {
    public static final RegistryEntry<DisplaySource, PumpjackOilAmountDisplaySource> PUMPJACK_OIL_AMOUNT = simple("pumpjack_oil_amount", PumpjackOilAmountDisplaySource::new);

    private static <T extends DisplaySource> RegistryEntry<DisplaySource, T> simple(String name, Supplier<T> supplier) {
        return REGISTRATE.displaySource(name, supplier).register();
    }

    public static void register() {
    }
}
