package com.madgique.addpatterntonetworktool;

public class AddPatternToNetworkToolMod {

    public static final String MOD_ID = "add_pattern_to_network_tool";

    public static void init() {
        System.err.println("[AddPatternToNetworkTool] Initialization started.");
        // The mod now uses mixins to extend AE2's NetworkStatus
        // No additional initialization needed
    }
}