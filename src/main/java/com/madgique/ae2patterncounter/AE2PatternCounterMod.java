package com.madgique.ae2patterncounter;

import java.util.EnumMap;
import java.util.Map;

import com.madgique.ae2patterncounter.items.PatternCounterItem;
import com.madgique.ae2patterncounter.parts.PatternCounterPart;

import appeng.api.AEApi;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:appliedenergistics2")
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class AE2PatternCounterMod {

    public static final Map<PatternCounterPart.Type, PatternCounterItem> ITEMS = new EnumMap<>(PatternCounterPart.Type.class);

    public static final CreativeTabs TAB = new CreativeTabs(Tags.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ITEMS.get(PatternCounterPart.Type.CRAFTING));
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        for (PatternCounterPart.Type type : PatternCounterPart.Type.values()) {
            AEApi.instance().registries().partModels().registerModels(type.modelOff, type.modelOn);
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (PatternCounterPart.Type type : PatternCounterPart.Type.values()) {
            PatternCounterItem item = new PatternCounterItem(type, TAB);
            ITEMS.put(type, item);
            event.getRegistry().register(item);
        }
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
    public static class Client {
        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event) {
            for (PatternCounterItem item : ITEMS.values()) {
                ModelLoader.setCustomModelResourceLocation(item, 0,
                        new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        }
    }
}
