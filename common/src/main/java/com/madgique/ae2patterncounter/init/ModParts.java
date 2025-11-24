package com.madgique.ae2patterncounter.init;

import com.madgique.ae2patterncounter.AE2PatternCounterMod;
import com.madgique.ae2patterncounter.parts.CraftingPatternCounterPart;
import com.madgique.ae2patterncounter.parts.FluidPatternCounterPart;
import com.madgique.ae2patterncounter.parts.ProcessingPatternCounterPart;
import com.madgique.ae2patterncounter.parts.SmithingPatternCounterPart;
import com.madgique.ae2patterncounter.parts.StonecuttingPatternCounterPart;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import appeng.api.parts.PartModels;
import appeng.items.parts.PartItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

/**
 * Registry for AE2 cable parts.
 */
public class ModParts {

    public static final DeferredRegister<Item> PARTS = DeferredRegister.create(AE2PatternCounterMod.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<PartItem<CraftingPatternCounterPart>> CRAFTING_PATTERN_COUNTER =
        PARTS.register("crafting_pattern_counter", () ->
            new PartItem<>(new Item.Properties().arch$tab(ModCreativeTabs.PATTERN_COUNTERS), CraftingPatternCounterPart.class, CraftingPatternCounterPart::new));

    public static final RegistrySupplier<PartItem<ProcessingPatternCounterPart>> PROCESSING_PATTERN_COUNTER =
        PARTS.register("processing_pattern_counter", () ->
            new PartItem<>(new Item.Properties().arch$tab(ModCreativeTabs.PATTERN_COUNTERS), ProcessingPatternCounterPart.class, ProcessingPatternCounterPart::new));

    public static final RegistrySupplier<PartItem<SmithingPatternCounterPart>> SMITHING_PATTERN_COUNTER =
        PARTS.register("smithing_pattern_counter", () ->
            new PartItem<>(new Item.Properties().arch$tab(ModCreativeTabs.PATTERN_COUNTERS), SmithingPatternCounterPart.class, SmithingPatternCounterPart::new));

    public static final RegistrySupplier<PartItem<StonecuttingPatternCounterPart>> STONECUTTING_PATTERN_COUNTER =
        PARTS.register("stonecutting_pattern_counter", () ->
            new PartItem<>(new Item.Properties().arch$tab(ModCreativeTabs.PATTERN_COUNTERS), StonecuttingPatternCounterPart.class, StonecuttingPatternCounterPart::new));

    public static final RegistrySupplier<PartItem<FluidPatternCounterPart>> FLUID_PATTERN_COUNTER =
        PARTS.register("fluid_pattern_counter", () ->
            new PartItem<>(new Item.Properties().arch$tab(ModCreativeTabs.PATTERN_COUNTERS), FluidPatternCounterPart.class, FluidPatternCounterPart::new));

    public static void register() {
        PARTS.register();

        // Register models with AE2
        PartModels.registerModels(
            CraftingPatternCounterPart.MODEL_OFF,
            CraftingPatternCounterPart.MODEL_ON
        );

        PartModels.registerModels(
            ProcessingPatternCounterPart.MODEL_OFF,
            ProcessingPatternCounterPart.MODEL_ON
        );

        PartModels.registerModels(
            SmithingPatternCounterPart.MODEL_OFF,
            SmithingPatternCounterPart.MODEL_ON
        );

        PartModels.registerModels(
            StonecuttingPatternCounterPart.MODEL_OFF,
            StonecuttingPatternCounterPart.MODEL_ON
        );

        PartModels.registerModels(
            FluidPatternCounterPart.MODEL_OFF,
            FluidPatternCounterPart.MODEL_ON
        );
    }
}
