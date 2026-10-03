# AE2 Pattern Counter (1.12.2)

**Monitor your Applied Energistics 2 pattern inventory in real-time!**

AE2 Pattern Counter adds monitor parts that display how many patterns of a given type are available in your AE2 network.

## Features

### Three Specialized Counters
- **Crafting Pattern Counter** - Counts crafting (workbench) patterns
- **Processing Pattern Counter** - Counts processing patterns
- **Fluid Pattern Counter** - Counts patterns that use fluids (requires [AE2 Fluid Crafting Rework](https://www.curseforge.com/minecraft/mc-mods/ae2-fluid-crafting-rework); shows 0 without it)

Smithing and stonecutting counters exist only in the 1.20.1 version: those blocks do not exist in Minecraft 1.12.2.

### Visual Feedback
- **Real-time Count Display** - The pattern count is drawn on the monitor face
- **Power Status Indicator** - Bright texture when powered, dark when off
- **Offline Detection** - Blinking "OFFLINE" message when the part has no channel
- **Color-Coded** - Each pattern type has its own color

### Easy to Use
- Place on any AE2 cable like other AE2 parts
- Rotate with a wrench when placed on the top or bottom of a block
- No configuration needed

## Requirements
- **Minecraft**: 1.12.2
- **Forge**: 14.23.5.2847+
- **Applied Energistics 2**: [AE2 Unofficial Extended Life](https://www.curseforge.com/minecraft/mc-mods/ae2-extended-life) (tested with v0.56.6; official rv6 should work but is untested)
- **AE2 Fluid Crafting Rework**: Optional, needed for the Fluid Pattern Counter

## Building
Java 25 runs Gradle; the mod itself targets Java 8 (modern syntax via Jabel).

```bash
./gradlew build
```

The jar is in `build/libs/`. Use `./gradlew runClient` to start a development client.

## License

This mod is licensed under LGPLv2.1

## Credits

Created by Madgique
