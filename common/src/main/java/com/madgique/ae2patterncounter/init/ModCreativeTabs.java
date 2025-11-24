package com.madgique.ae2patterncounter.init;

import com.madgique.ae2patterncounter.AE2PatternCounterMod;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Registers creative mode tabs for the mod.
 */
public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(AE2PatternCounterMod.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> PATTERN_COUNTERS = TABS.register(
        "pattern_counters",
        () -> CreativeTabRegistry.create(
            Component.translatable("itemGroup.ae2patterncounter.pattern_counters"),
            // Use a vanilla item as icon to avoid FabricItem compile issues
            // The actual part icon will show in-game when the tab is populated
            () -> new ItemStack(Items.CRAFTING_TABLE)
        )
    );

    public static void register() {
        TABS.register();
    }
}
