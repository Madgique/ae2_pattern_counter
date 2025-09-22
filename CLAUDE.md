# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a clean Minecraft mod template called "Add Pattern to Network Tool" built using the Architectury framework to support multiple mod loaders (Forge and Fabric) for Minecraft 1.20.1.

## Build System & Commands

This project uses Gradle with the Architectury plugin for multi-platform mod development.

### Essential Commands
- `./gradlew build` - Build all modules (common + forge + fabric)
- `./gradlew :forge:build` - Build only the Forge version
- `./gradlew :fabric:build` - Build only the Fabric version
- `./gradlew :common:build` - Build only the common module
- `./gradlew :forge:runClient` - Run Minecraft client with the mod for testing
- `./gradlew clean` - Clean build artifacts

### Development Setup
- Java 17 required (configured via toolchain)
- Uses official Mojang mappings
- Mod version: 1.0 (defined in gradle.properties)
- Target Minecraft: 1.20.1
- Forge version: 47.3.0
- Architectury version: 9.2.14

## Architecture

### Multi-Platform Structure
The project follows Architectury's multi-platform pattern:

- **`common/`** - Platform-agnostic code shared between mod loaders
  - Contains core mod logic and configuration
  - Main class: `AddPatternToNetworkToolMod`
  
- **`forge/`** - Forge-specific implementation
  - Platform-specific mod initialization and event handling
  - Contains `AddPatternToNetworkToolModForge` which bootstraps the common code
  - Includes `mods.toml` for Forge mod metadata

- **`fabric/`** - Fabric-specific implementation
  - Platform-specific mod initialization and event handling
  - Contains `AddPatternToNetworkToolModFabric` which bootstraps the common code  
  - Includes `fabric.mod.json` for Fabric mod metadata

### Dependencies
- Architectury API for cross-platform compatibility
- ClothConfig for configuration GUI (includes AutoConfig)
- Multiple mod loaders supported (Forge and Fabric)

## Development Notes

### Adding New Features
- Add cross-platform code to `common/src/main/java/com/madgique/addpatterntonetworktool/`
- **Forge** platform-specific implementations go in `forge/src/main/java/com/madgique/addpatterntonetworktool/forge/`
- **Fabric** platform-specific implementations go in `fabric/src/main/java/com/madgique/addpatterntonetworktool/fabric/`
- **CRITICAL**: Never delete Fabric or Forge modules - both are required for multi-platform support
- **IMPORTANT**: Prefer accesswidener for accessing private classes/methods, but reflection can be used when needed

### Access Widener Usage
- Access wideners modify access levels of classes, methods, and fields in Minecraft's code
- File format: `.accesswidener` extension, start with `accessWidener v2 named`
- Syntax:
  - Classes: `<access> class <className>`
  - Methods: `<access> method <className> <methodName> <methodDesc>`
  - Fields: `<access> field <className> <fieldName> <fieldDesc>`
- Access types: `accessible` (public), `extendable` (subclassing), `mutable` (removes final)
- Configuration: Set `accessWidenerPath` in build.gradle and `accessWidener` in fabric.mod.json
- Documentation: https://wiki.fabricmc.net/tutorial:accesswideners

### Testing
- Use `./gradlew :forge:runClient` to launch a development instance
- Test configuration changes by modifying the generated config file
- The `forge/run/` directory contains the development environment with logs, saves, and config

### Resource Management
- Forge metadata: `forge/src/resources/META-INF/mods.toml`
- Fabric metadata: `fabric/src/resources/fabric.mod.json`
- Both contain mod display information and dependencies

### AE2 Reference
- AE2 Source Code: https://github.com/AppliedEnergistics/Applied-Energistics-2
- Use this repository to understand AE2's internal classes and structure
- Essential for working with private classes like MachineGroupKey and MachineGroup