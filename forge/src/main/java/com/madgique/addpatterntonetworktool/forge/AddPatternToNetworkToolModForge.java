package com.madgique.addpatterntonetworktool.forge;

import com.madgique.addpatterntonetworktool.AddPatternToNetworkToolMod;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AddPatternToNetworkToolMod.MOD_ID)
public class AddPatternToNetworkToolModForge {

  public AddPatternToNetworkToolModForge() {
    // Submit our event bus to let architectury register our content on the right time
    EventBuses.registerModEventBus(AddPatternToNetworkToolMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
    FMLJavaModLoadingContext.get().getModEventBus().register(this);
    AddPatternToNetworkToolMod.init();
  }

}