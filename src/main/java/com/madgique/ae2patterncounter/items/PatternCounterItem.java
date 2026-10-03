package com.madgique.ae2patterncounter.items;

import com.madgique.ae2patterncounter.Tags;
import com.madgique.ae2patterncounter.parts.PatternCounterPart;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Item form of a {@link PatternCounterPart}; places the part on an AE2 cable.
 */
public class PatternCounterItem extends Item implements IPartItem<PatternCounterPart> {

    private final PatternCounterPart.Type type;

    public PatternCounterItem(PatternCounterPart.Type type, CreativeTabs tab) {
        this.type = type;
        setRegistryName(Tags.MOD_ID, type.id);
        setTranslationKey(Tags.MOD_ID + "." + type.id);
        setCreativeTab(tab);
    }

    @Override
    public PatternCounterPart createPartFromItemStack(ItemStack is) {
        return new PatternCounterPart(is, type);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
            EnumFacing side, float hitX, float hitY, float hitZ) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, side, player, hand, world);
    }
}
