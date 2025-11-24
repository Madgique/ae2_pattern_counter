package com.madgique.ae2patterncounter.fabric;

import com.madgique.ae2patterncounter.AE2PatternCounterMod;
import net.fabricmc.api.ModInitializer;

public class AE2PatternCounterModFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        AE2PatternCounterMod.init();
    }
}