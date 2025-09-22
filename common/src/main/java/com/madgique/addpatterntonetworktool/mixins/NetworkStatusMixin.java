package com.madgique.addpatterntonetworktool.mixins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.madgique.addpatterntonetworktool.NetworkStatusExtension;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEFluidKey;
import appeng.core.definitions.AEItems;
import net.minecraft.world.item.Items;
import appeng.menu.me.networktool.MachineGroup;
import appeng.menu.me.networktool.NetworkStatus;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

@Mixin(value = NetworkStatus.class, remap = false)
public class NetworkStatusMixin implements NetworkStatusExtension {

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("AddPatternToNetworkTool");

    @Shadow
    private List<MachineGroup> groupedMachines;

    @Unique
    private int add_pattern_to_network_tool$patternCount = 0;

    @Inject(method = "fromGrid", at = @At("RETURN"))
    private static void addPatternCount(IGrid grid, CallbackInfoReturnable<NetworkStatus> cir) {
        NetworkStatus status = cir.getReturnValue();
        LOGGER.info("[AddPatternToNetworkTool] fromGrid method called!");

        if (grid != null) {
            LOGGER.info("[AddPatternToNetworkTool] Grid is not null, counting patterns...");

            // Count patterns by type by analyzing craftables and their pattern details
            int craftingPatterns = 0;
            int processingPatterns = 0;
            int smithingPatterns = 0;
            int stonecuttingPatterns = 0;
            int fluidPatterns = 0;
            
            try {
                ICraftingService craftingService = grid.getCraftingService();
                if (craftingService != null) {
                    LOGGER.info("[AddPatternToNetworkTool] Crafting service found, analyzing pattern types...");
                    
                    // Step 1: Get all craftable items
                    var craftableItems = craftingService.getCraftables(appeng.api.stacks.AEItemKey.filter());
                    LOGGER.info("[AddPatternToNetworkTool] Found {} craftable items", craftableItems.size());
                    
                    // Step 2: For each craftable, get its patterns and analyze their types
                    for (var craftableItem : craftableItems) {
                        var patterns = craftingService.getCraftingFor(craftableItem);
                        LOGGER.debug("[AddPatternToNetworkTool] Item {} has {} patterns", craftableItem, patterns.size());
                        
                        // Step 3: Analyze each pattern to determine its type
                        for (var pattern : patterns) {
                            try {
                                // Get the pattern definition (the physical item)
                                var patternDefinition = pattern.getDefinition();
                                LOGGER.debug("[AddPatternToNetworkTool] Pattern definition: {}", patternDefinition);
                                
                                // Check the pattern type based on its definition
                                if (AEItems.CRAFTING_PATTERN.isSameAs(patternDefinition.toStack())) {
                                    craftingPatterns++;
                                    LOGGER.debug("[AddPatternToNetworkTool] Found crafting pattern for {}", craftableItem);
                                } else if (AEItems.PROCESSING_PATTERN.isSameAs(patternDefinition.toStack())) {
                                    processingPatterns++;
                                    LOGGER.debug("[AddPatternToNetworkTool] Found processing pattern for {}", craftableItem);
                                } else if (AEItems.SMITHING_TABLE_PATTERN.isSameAs(patternDefinition.toStack())) {
                                    smithingPatterns++;
                                    LOGGER.debug("[AddPatternToNetworkTool] Found smithing pattern for {}", craftableItem);
                                } else if (AEItems.STONECUTTING_PATTERN.isSameAs(patternDefinition.toStack())) {
                                    stonecuttingPatterns++;
                                    LOGGER.debug("[AddPatternToNetworkTool] Found stonecutting pattern for {}", craftableItem);
                                } else {
                                    // Fallback: count as crafting pattern
                                    craftingPatterns++;
                                    LOGGER.debug("[AddPatternToNetworkTool] Unknown pattern type, counting as crafting for {}", craftableItem);
                                }
                            } catch (Exception e) {
                                LOGGER.debug("[AddPatternToNetworkTool] Error analyzing pattern: {}", e.getMessage());
                                // Fallback: count as crafting pattern
                                craftingPatterns++;
                            }
                        }
                    }
                    
                    // Also check fluid patterns
                    var craftableFluids = craftingService.getCraftables(appeng.api.stacks.AEFluidKey.filter());
                    LOGGER.info("[AddPatternToNetworkTool] Found {} craftable fluids", craftableFluids.size());
                    
                    for (var craftableFluid : craftableFluids) {
                        var patterns = craftingService.getCraftingFor(craftableFluid);
                        fluidPatterns += patterns.size();
                        LOGGER.debug("[AddPatternToNetworkTool] Fluid {} has {} patterns", craftableFluid, patterns.size());
                    }
                    
                    LOGGER.info("[AddPatternToNetworkTool] Pattern analysis complete:");
                    LOGGER.info("[AddPatternToNetworkTool] Crafting: {}, Processing: {}, Smithing: {}, Stonecutting: {}, Fluid: {}", 
                        craftingPatterns, processingPatterns, smithingPatterns, stonecuttingPatterns, fluidPatterns);

                    // Create pattern machine groups and modify the groupedMachines list
                    NetworkStatusMixin statusMixin = (NetworkStatusMixin) (Object) status;
                    statusMixin.addCraftingPatternGroup(craftingPatterns);
                    statusMixin.addProcessingPatternGroup(processingPatterns);
                    statusMixin.addSmithingPatternGroup(smithingPatterns);
                    statusMixin.addStonecuttingPatternGroup(stonecuttingPatterns);
                    statusMixin.addFluidPatternGroup(fluidPatterns);

                    LOGGER.info("[AddPatternToNetworkTool] Pattern counting method executed, modified groupedMachines");
                } else {
                    LOGGER.warn("[AddPatternToNetworkTool] Crafting service is null!");
                }
            } catch (Exception e) {
                LOGGER.error("[AddPatternToNetworkTool] Error counting patterns: {}", e.getMessage());
            }

            // Store total pattern count for backward compatibility
            int totalPatterns = craftingPatterns + processingPatterns + smithingPatterns + stonecuttingPatterns + fluidPatterns;
            ((NetworkStatusMixin) (Object) status).add_pattern_to_network_tool$patternCount = totalPatterns;
            LOGGER.info("[AddPatternToNetworkTool] FINAL Pattern count: {}", totalPatterns);
        } else {
            LOGGER.warn("[AddPatternToNetworkTool] Grid is null!");
        }
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void readPatternCount(FriendlyByteBuf data, CallbackInfoReturnable<NetworkStatus> cir) {
        NetworkStatus status = cir.getReturnValue();
        try {
            int patternCount = data.readInt();
            ((NetworkStatusMixin) (Object) status).add_pattern_to_network_tool$patternCount = patternCount;
        } catch (Exception e) {
            // If reading fails, set to 0
            ((NetworkStatusMixin) (Object) status).add_pattern_to_network_tool$patternCount = 0;
        }
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void writePatternCount(FriendlyByteBuf data, CallbackInfo ci) {
        LOGGER.info("[AddPatternToNetworkTool] Writing pattern count: {}",
                this.add_pattern_to_network_tool$patternCount);
        data.writeInt(this.add_pattern_to_network_tool$patternCount);
    }

    @Unique
    public int add_pattern_to_network_tool$getPatternCount() {
        return this.add_pattern_to_network_tool$patternCount;
    }

    @Unique
    @SuppressWarnings("unchecked")
    private void addCraftingPatternGroup(int craftingPatterns) {
        if (craftingPatterns <= 0) {
            LOGGER.info("[AddPatternToNetworkTool] No crafting patterns to add, skipping");
            return;
        }
        addPatternGroupToList(craftingPatterns, "Crafting Patterns", AEItems.CRAFTING_PATTERN);
    }

    @Unique
    @SuppressWarnings("unchecked")
    private void addProcessingPatternGroup(int processingPatterns) {
        if (processingPatterns <= 0) {
            LOGGER.info("[AddPatternToNetworkTool] No processing patterns to add, skipping");
            return;
        }
        addPatternGroupToList(processingPatterns, "Processing Patterns", AEItems.PROCESSING_PATTERN);
    }

    @Unique
    @SuppressWarnings("unchecked")
    private void addSmithingPatternGroup(int smithingPatterns) {
        if (smithingPatterns <= 0) {
            LOGGER.info("[AddPatternToNetworkTool] No smithing patterns to add, skipping");
            return;
        }
        addPatternGroupToList(smithingPatterns, "Smithing Patterns", AEItems.SMITHING_TABLE_PATTERN);
    }

    @Unique
    @SuppressWarnings("unchecked")
    private void addStonecuttingPatternGroup(int stonecuttingPatterns) {
        if (stonecuttingPatterns <= 0) {
            LOGGER.info("[AddPatternToNetworkTool] No stonecutting patterns to add, skipping");
            return;
        }
        addPatternGroupToList(stonecuttingPatterns, "Stonecutting Patterns", AEItems.STONECUTTING_PATTERN);
    }

    @Unique
    @SuppressWarnings("unchecked")
    private void addFluidPatternGroup(int fluidPatterns) {
        if (fluidPatterns <= 0) {
            LOGGER.info("[AddPatternToNetworkTool] No fluid patterns to add, skipping");
            return;
        }
        addPatternGroupToList(fluidPatterns, "Fluid Patterns", Items.WATER_BUCKET);
    }

    @Unique
    @SuppressWarnings("unchecked")
    private void addPatternGroupToList(int patternCount, String patternType, net.minecraft.world.level.ItemLike patternItem) {
        try {
            LOGGER.info("[AddPatternToNetworkTool] Creating {} group with count: {}", patternType, patternCount);
            
            // Create AEItemKey for pattern with the correct icon
            AEItemKey patternKey = AEItemKey.of(patternItem);
            LOGGER.info("[AddPatternToNetworkTool] Created pattern key for {}: {}", patternType, patternKey);
            
            // Create MachineGroupKey using reflection
            Class<?> keyClass = Class.forName("appeng.menu.me.networktool.MachineGroupKey");
            java.lang.reflect.Constructor<?> keyConstructor = keyClass.getDeclaredConstructor(
                appeng.api.stacks.AEItemKey.class, 
                boolean.class
            );
            keyConstructor.setAccessible(true);
            Object machineGroupKey = keyConstructor.newInstance(patternKey, false);
            LOGGER.info("[AddPatternToNetworkTool] Created machine group key for {}: {}", patternType, machineGroupKey);
            
            // Create MachineGroup using reflection
            Class<?> machineGroupClass = Class.forName("appeng.menu.me.networktool.MachineGroup");
            java.lang.reflect.Constructor<?> groupConstructor = machineGroupClass.getDeclaredConstructor(keyClass);
            groupConstructor.setAccessible(true);
            Object patternGroup = groupConstructor.newInstance(machineGroupKey);
            
            // Set count using reflection
            java.lang.reflect.Method setCountMethod = machineGroupClass.getDeclaredMethod("setCount", int.class);
            setCountMethod.setAccessible(true);
            setCountMethod.invoke(patternGroup, patternCount);
            
            // Set power usage using reflection
            java.lang.reflect.Method setIdlePowerMethod = machineGroupClass.getDeclaredMethod("setIdlePowerUsage", double.class);
            setIdlePowerMethod.setAccessible(true);
            setIdlePowerMethod.invoke(patternGroup, 0.0);
            
            java.lang.reflect.Method setPowerCapacityMethod = machineGroupClass.getDeclaredMethod("setPowerGenerationCapacity", double.class);
            setPowerCapacityMethod.setAccessible(true);
            setPowerCapacityMethod.invoke(patternGroup, 0.0);
            LOGGER.info("[AddPatternToNetworkTool] Created {} group: {}", patternType, patternGroup);
            
            // Create new list with existing machines plus our pattern group
            java.util.List<MachineGroup> newGroupedMachines = new java.util.ArrayList<>(this.groupedMachines);
            newGroupedMachines.add((MachineGroup) patternGroup);
            
            // Replace the immutable list with our new list
            this.groupedMachines = com.google.common.collect.ImmutableList.copyOf(newGroupedMachines);
            LOGGER.info("[AddPatternToNetworkTool] Successfully added {} to groupedMachines. Total groups: {}", 
                patternType, this.groupedMachines.size());
            
        } catch (Exception e) {
            LOGGER.error("[AddPatternToNetworkTool] Error creating {} group: {}", patternType, e.getMessage(), e);
        }
    }

}