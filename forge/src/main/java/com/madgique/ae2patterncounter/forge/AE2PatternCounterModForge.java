package com.madgique.ae2patterncounter.forge;

import com.madgique.ae2patterncounter.AE2PatternCounterMod;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AE2PatternCounterMod.MOD_ID)
public class AE2PatternCounterModForge {

  public AE2PatternCounterModForge() {

    // Submit our event bus to let architectury register our content on the right
    // time
    EventBuses.registerModEventBus(AE2PatternCounterMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
    FMLJavaModLoadingContext.get().getModEventBus().register(this);
    AE2PatternCounterMod.init();
  }

}