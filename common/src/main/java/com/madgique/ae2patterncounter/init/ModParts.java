package com.madgique.ae2patterncounter.init;

import com.madgique.ae2patterncounter.AE2PatternCounterMod;
import com.madgique.ae2patterncounter.parts.PatternCounterPart;
import dev.architectury.registry.registries.DeferredRegister;
import appeng.api.parts.PartModels;
import appeng.items.parts.PartItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

/**
 * Registry for AE2 cable parts: one pattern counter item per {@link PatternCounterPart.Type}.
 */
public class ModParts {

    public static final DeferredRegister<Item> PARTS = DeferredRegister.create(AE2PatternCounterMod.MOD_ID, Registries.ITEM);

    public static void register() {
        for (var type : PatternCounterPart.Type.values()) {
            PARTS.register(type.name().toLowerCase() + "_pattern_counter", () ->
                new PartItem<>(new Item.Properties().arch$tab(ModCreativeTabs.PATTERN_COUNTERS),
                    PatternCounterPart.class, item -> new PatternCounterPart(item, type)));
            PartModels.registerModels(type.modelOff, type.modelOn);
        }
        PARTS.register();
    }
}
