package com.madgique.ae2patterncounter;

import net.minecraft.network.chat.Component;

public class NetworkToolPatternCounter {

    public static Component createPatternCountComponent(int patternCount) {
        if (patternCount < 0) {
            return Component.translatable("addpatterntonetworktool.patterns", "N/A");
        }
        return Component.translatable("addpatterntonetworktool.patterns", patternCount);
    }
}