package com.madgique.ae2patterncounter.parts;

import com.mojang.blaze3d.vertex.PoseStack;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.client.render.BlockEntityRenderHelper;
import appeng.crafting.pattern.AESmithingTablePattern;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractMonitorPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Part that counts and displays the number of smithing patterns in the AE2
 * network.
 * Uses AbstractMonitorPart for the rendering system but disables item
 * interaction.
 */
public class SmithingPatternCounterPart extends AbstractMonitorPart implements IGridTickable {

    @PartModels
    public static final ResourceLocation MODEL_OFF = new ResourceLocation("ae2patterncounter",
            "part/smithing_pattern_counter_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = new ResourceLocation("ae2patterncounter",
            "part/smithing_pattern_counter_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    private int cachedPatternCount = 0;
    private boolean hasInitialSync = false;

    public SmithingPatternCounterPart(IPartItem<?> partItem) {
        super(partItem, true); // requireChannel = true
        getMainNode().addService(IGridTickable.class, this);
    }

    @Override
    protected void updateReportingValue(IGrid grid) {
        updatePatternCount(grid);
    }

    private void updatePatternCount(IGrid grid) {
        if (getLevel() == null) {
            return;
        }

        // Only calculate on server side
        if (getLevel().isClientSide()) {
            return;
        }

        try {
            ICraftingService craftingService = grid.getCraftingService();
            if (craftingService == null) {
                cachedPatternCount = 0;
                getHost().markForUpdate();
                return;
            }

            int count = 0;

            // Count only smithing patterns in the network
            var craftableItems = craftingService.getCraftables(AEItemKey.filter());

            for (var craftableItem : craftableItems) {
                var patterns = craftingService.getCraftingFor(craftableItem);
                for (var pattern : patterns) {
                    // Only count AESmithingTablePattern (smithing table recipes)
                    if (pattern instanceof AESmithingTablePattern) {
                        count++;
                    }
                }
            }

            if (cachedPatternCount != count) {
                cachedPatternCount = count;
                getHost().markForUpdate(); // Sync to client
            }
        } catch (Exception e) {
            cachedPatternCount = 0;
            getHost().markForUpdate();
        }
    }

    /**
     * Gets the current pattern count for display.
     */
    public int getPatternCount() {
        return cachedPatternCount;
    }

    @Override
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        // Disable all interactions - this monitor is read-only
        return false;
    }

    @Override
    public boolean onPartShiftActivate(Player player, InteractionHand hand, Vec3 pos) {
        // Disable shift-click locking - this monitor cannot be locked
        return false;
    }

    @Override
    public AEKey getDisplayed() {
        return null;
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isPowered()) {
            return this.isActive() ? MODELS_HAS_CHANNEL : MODELS_ON;
        }
        return MODELS_OFF;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        cachedPatternCount = data.getInt("patternCount");
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("patternCount", cachedPatternCount);
    }

    @Override
    public void writeToStream(net.minecraft.network.FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeInt(cachedPatternCount);
    }

    @Override
    public boolean readFromStream(net.minecraft.network.FriendlyByteBuf data) {
        super.readFromStream(data);
        int newCount = data.readInt();
        boolean changed = cachedPatternCount != newCount;
        cachedPatternCount = newCount;
        return changed; // Return true to trigger visual redraw
    }

    @Override
    public TickingRequest getTickingRequest(appeng.api.networking.IGridNode node) {
        return new TickingRequest(20, 100, false, false); // Tick every 20-100 ticks
    }

    @Override
    public TickRateModulation tickingRequest(appeng.api.networking.IGridNode node, int ticksSinceLastCall) {
        var grid = node.getGrid();
        if (grid != null) {
            updatePatternCount(grid);

            // Force initial sync on first tick
            if (!hasInitialSync) {
                hasInitialSync = true;
                getHost().markForUpdate();
            }
        }

        return TickRateModulation.SLOWER; // Slow down ticking
    }

    @Override
    public boolean requireDynamicRender() {
        return true;
    }

    @Override
    public void renderDynamic(float partialTicks, PoseStack poseStack,
            MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
        poseStack.pushPose();

        // Use AE2's approach: translate to center, rotate, then translate forward
        var orientation = appeng.api.orientation.BlockOrientation.get(this.getSide(), this.getSpin());
        poseStack.translate(0.5, 0.5, 0.5);
        BlockEntityRenderHelper.rotateToFace(poseStack, orientation);
        poseStack.translate(0, 0.05, 0.52);

        // Scale for text rendering (negative Y to flip text)
        float scale = 0.01f;
        poseStack.scale(scale, -scale, scale);

        // Get Minecraft font renderer
        var font = Minecraft.getInstance().font;
        String text;
        int color;

        if (!this.isActive()) {
            // No channel - display "OFFLINE" in red with smooth blinking
            text = "OFFLINE";
            // Smooth blinking using sine wave (0.5 to 1.0 alpha range for smoother effect)
            long time = System.currentTimeMillis();
            float blinkFactor = (float) (Math.sin(time / 500.0) * 0.25 + 0.75); // Range: 0.5 to 1.0
            int alpha = (int) (blinkFactor * 255);
            color = (alpha << 24) | 0xFF0000; // Red with varying alpha
        } else {
            // Has channel - display pattern count in white
            text = String.valueOf(cachedPatternCount);
            color = 0xFFFFFFFF; // White with full alpha
        }

        int textWidth = font.width(text);

        // Center the text
        float x = -textWidth / 2f;
        float y = -font.lineHeight / 2f;

        // Render with full brightness
        font.drawInBatch(text, x, y, color, false,
                poseStack.last().pose(), buffers,
                net.minecraft.client.gui.Font.DisplayMode.NORMAL,
                0, LightTexture.FULL_BRIGHT);

        poseStack.popPose();
    }
}
