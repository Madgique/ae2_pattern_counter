package com.madgique.ae2patterncounter.parts;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

import com.madgique.ae2patterncounter.Tags;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.render.TesrRenderHelper;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractPartDisplay;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Read-only display part that shows how many patterns of one {@link Type} the network knows.
 */
public class PatternCounterPart extends AbstractPartDisplay implements IGridTickable {

    /** Item that AE2 Fluid Crafting (ae2fc) puts in patterns in place of a fluid. */
    private static final ResourceLocation AE2FC_FLUID_DROP = new ResourceLocation("ae2fc", "fluid_drop");

    public enum Type {
        CRAFTING(ICraftingPatternDetails::isCraftable),
        PROCESSING(p -> !p.isCraftable()),
        FLUID(Type::hasFluid);

        public final String id = name().toLowerCase(Locale.ROOT) + "_pattern_counter";
        public final ResourceLocation modelOff = new ResourceLocation(Tags.MOD_ID, "part/" + id + "_off");
        public final ResourceLocation modelOn = new ResourceLocation(Tags.MOD_ID, "part/" + id + "_on");
        private final IPartModel modelsOff = new PartModel(MODEL_BASE, modelOff, MODEL_STATUS_OFF);
        private final IPartModel modelsOn = new PartModel(MODEL_BASE, modelOn, MODEL_STATUS_ON);
        private final IPartModel modelsHasChannel = new PartModel(MODEL_BASE, modelOn, MODEL_STATUS_HAS_CHANNEL);
        private final Predicate<ICraftingPatternDetails> matcher;

        Type(Predicate<ICraftingPatternDetails> matcher) {
            this.matcher = matcher;
        }

        private static boolean hasFluid(ICraftingPatternDetails pattern) {
            return containsFluidDrop(pattern.getInputs()) || containsFluidDrop(pattern.getOutputs());
        }

        private static boolean containsFluidDrop(IAEItemStack[] stacks) {
            for (IAEItemStack stack : stacks) {
                if (stack != null && AE2FC_FLUID_DROP.equals(stack.getItem().getRegistryName())) {
                    return true;
                }
            }
            return false;
        }
    }

    private final Type type;
    private int patternCount;

    public PatternCounterPart(ItemStack is, Type type) {
        super(is);
        this.type = type;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(20, 100, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        IGrid grid = node.getGrid();
        ICraftingGrid craftingGrid = grid.getCache(ICraftingGrid.class);
        IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
        var channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);

        // AE2 lists a pattern under each of its outputs, so collect them in a set to count each pattern once.
        Set<ICraftingPatternDetails> patterns = new HashSet<>();
        for (IAEItemStack stack : storageGrid.getInventory(channel).getStorageList()) {
            if (stack.isCraftable()) {
                patterns.addAll(craftingGrid.getCraftingFor(stack, null, 0, getTile().getWorld()));
            }
        }

        int count = 0;
        for (ICraftingPatternDetails pattern : patterns) {
            if (type.matcher.test(pattern)) {
                count++;
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
    public void writeToStream(ByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeInt(patternCount);
    }

    @Override
    public boolean readFromStream(ByteBuf data) throws IOException {
        boolean changed = super.readFromStream(data);
        // The count is drawn dynamically, so a new value does not need a chunk redraw.
        patternCount = data.readInt();
        return changed;
    }

    @Override
    public boolean requireDynamicRender() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(double x, double y, double z, float partialTicks, int destroyStage) {
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

        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        // Text fits the 10 px wide number area of the texture; long texts ("OFFLINE") shrink.
        float scale = Math.min(0.018f, (10 / 16f) / font.getStringWidth(text));

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        EnumFacing facing = getSide().getFacing();
        TesrRenderHelper.moveToFace(facing);
        TesrRenderHelper.rotateToFace(facing, getSpin());
        // Number area: under the type icon, 3 px below the face center
        GlStateManager.translate(0, 3 / 16f, 0.01);
        GlStateManager.scale(scale, scale, scale);

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        font.drawString(text, -font.getStringWidth(text) / 2f, -font.FONT_HEIGHT / 2f, color, false);

        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
