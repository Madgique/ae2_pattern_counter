package com.madgique.ae2patterncounter;

import com.madgique.ae2patterncounter.init.ModCreativeTabs;
import com.madgique.ae2patterncounter.init.ModParts;

public class AE2PatternCounterMod {

    public static final String MOD_ID = "ae2patterncounter";

    public static void init() {

        // Register creative tabs first (parts need to reference them)
        ModCreativeTabs.register();

        // Register parts
        ModParts.register();
    }
}