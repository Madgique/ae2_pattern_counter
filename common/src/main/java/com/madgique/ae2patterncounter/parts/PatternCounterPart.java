package com.madgique.ae2patterncounter.parts;

import java.util.Locale;
import java.util.function.Predicate;

import com.madgique.ae2patterncounter.AE2PatternCounterMod;
import com.mojang.blaze3d.vertex.PoseStack;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.orientation.BlockOrientation;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.client.render.BlockEntityRenderHelper;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.AESmithingTablePattern;
import appeng.crafting.pattern.AEStonecuttingPattern;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractDisplayPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Read-only display part that shows how many patterns of one {@link Type} the network knows.
 */
public class PatternCounterPart extends AbstractDisplayPart implements IGridTickable {

    public enum Type {
        CRAFTING(p -> p instanceof AECraftingPattern),
        PROCESSING(p -> p instanceof AEProcessingPattern),
        SMITHING(p -> p instanceof AESmithingTablePattern),
        STONECUTTING(p -> p instanceof AEStonecuttingPattern),
        FLUID(Type::hasFluid);

        public final String id;
        public final ResourceLocation modelOff;
        public final ResourceLocation modelOn;
        private final IPartModel modelsOff;
        private final IPartModel modelsOn;
        private final IPartModel modelsHasChannel;
        private final Predicate<IPatternDetails> matcher;

        Type(Predicate<IPatternDetails> matcher) {
            this.id = name().toLowerCase(Locale.ROOT) + "_pattern_counter";
            this.modelOff = new ResourceLocation(AE2PatternCounterMod.MOD_ID, "part/" + id + "_off");
            this.modelOn = new ResourceLocation(AE2PatternCounterMod.MOD_ID, "part/" + id + "_on");
            this.modelsOff = new PartModel(MODEL_BASE, modelOff, MODEL_STATUS_OFF);
            this.modelsOn = new PartModel(MODEL_BASE, modelOn, MODEL_STATUS_ON);
            this.modelsHasChannel = new PartModel(MODEL_BASE, modelOn, MODEL_STATUS_HAS_CHANNEL);
            this.matcher = matcher;
        }

        private static boolean hasFluid(IPatternDetails pattern) {
            for (GenericStack output : pattern.getOutputs()) {
                if (output != null && output.what() instanceof AEFluidKey) {
                    return true;
                }
            }
            for (IPatternDetails.IInput input : pattern.getInputs()) {
                for (GenericStack possible : input.getPossibleInputs()) {
                    if (possible != null && possible.what() instanceof AEFluidKey) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private final Type type;
    private int patternCount;

    public PatternCounterPart(IPartItem<?> partItem, Type type) {
        super(partItem, true);
        this.type = type;
        getMainNode().addService(IGridTickable.class, this);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(20, 100, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        var craftingService = node.getGrid().getCraftingService();
        int count = 0;
        // AE2 indexes each pattern once, under its primary output, so no pattern is counted twice.
        for (var key : craftingService.getCraftables(what -> true)) {
            for (var pattern : craftingService.getCraftingFor(key)) {
                if (type.matcher.test(pattern)) {
                    count++;
                }
            }
        }

        if (count != patternCount) {
            patternCount = count;
            getHost().markForUpdate();
            return TickRateModulation.FASTER;
        }
        return TickRateModulation.SLOWER;
    }

    @Override
    public IPartModel getStaticModels() {
        return selectModel(type.modelsOff, type.modelsOn, type.modelsHasChannel);
    }

    @Override
    public void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeVarInt(patternCount);
    }

    @Override
    public boolean readFromStream(FriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        // The count is drawn dynamically, so a new value does not need a chunk redraw.
        patternCount = data.readVarInt();
        return changed;
    }

    @Override
    public boolean requireDynamicRender() {
        return true;
    }

    @Override
    public void renderDynamic(float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
            int combinedLight, int combinedOverlay) {
        String text;
        int color;
        if (isActive()) {
            text = String.valueOf(patternCount);
            color = 0xFFFFFFFF;
        } else {
            text = "OFFLINE";
            // Smooth red blink, alpha between 0.5 and 1.0
            float blink = (float) (Math.sin(System.currentTimeMillis() / 500.0) * 0.25 + 0.75);
            color = ((int) (blink * 255) << 24) | 0xFF0000;
        }

        var font = Minecraft.getInstance().font;
        // Text fits the 10 px wide number area of the texture; long texts ("OFFLINE") shrink.
        float scale = Math.min(0.018f, (10 / 16f) / font.width(text));

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        BlockEntityRenderHelper.rotateToFace(poseStack, BlockOrientation.get(getSide(), getSpin()));
        // Number area: under the type icon, 3 px below the face center
        poseStack.translate(0, -3 / 16f, 0.52);
        poseStack.scale(scale, -scale, scale);

        font.drawInBatch(text, -font.width(text) / 2f, -font.lineHeight / 2f, color, false,
                poseStack.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

        poseStack.popPose();
    }
}
