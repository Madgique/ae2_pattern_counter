package com.madgique.addpatterntonetworktool.fabric;

import com.madgique.addpatterntonetworktool.AddPatternToNetworkToolMod;
import net.fabricmc.api.ModInitializer;

public class AddPatternToNetworkToolModFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        AddPatternToNetworkToolMod.init();
    }
}