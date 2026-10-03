package com.madgique.ae2patterncounter;

import java.util.ArrayList;
import java.util.List;

import com.madgique.ae2patterncounter.parts.PatternCounterPart;

import appeng.api.parts.PartModels;
import appeng.items.parts.PartItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(AE2PatternCounterMod.MOD_ID)
public class AE2PatternCounterMod {

    public static final String MOD_ID = "ae2patterncounter";

    public AE2PatternCounterMod(IEventBus modBus) {
        var items = DeferredRegister.createItems(MOD_ID);
        var tabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

        List<DeferredItem<?>> counters = new ArrayList<>();
        for (var type : PatternCounterPart.Type.values()) {
            counters.add(items.register(type.id, () -> new PartItem<>(new Item.Properties(),
                    PatternCounterPart.class, item -> new PatternCounterPart(item, type))));
            PartModels.registerModels(type.modelOff, type.modelOn);
        }

        tabs.register("pattern_counters", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.ae2patterncounter.pattern_counters"))
                .icon(() -> counters.getFirst().toStack())
                .displayItems((params, output) -> counters.forEach(output::accept))
                .build());

        items.register(modBus);
        tabs.register(modBus);
    }
}
