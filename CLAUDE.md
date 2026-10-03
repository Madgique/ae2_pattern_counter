# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

"AE2 Pattern Counter" for **Minecraft 1.12.2 / Forge only** (branch `1.12.2`). The 1.20.1 multi-loader version lives on branch `1.20.1`.

The mod adds AE2 cable parts that display how many patterns of one type (Crafting, Processing, Fluid) the network knows.

## Build System

- Template: CleanroomMC TemplateDevEnv (RetroFuturaGradle). Gradle runs on Java 25; code targets Java 8.
- `use_modern_java_syntax = true`: Jabel allows `var`, switch expressions, etc. Java 9+ **APIs** are still unavailable.
- Mappings: MCP `stable_39`.
- Dependencies go in `gradle/scripts/dependencies.gradle` (CurseMaven: `rfg.deobf('curse.maven:<slug>-<projectId>:<fileId>')`).
- Mod id, name and version are in `gradle.properties`; the generated `Tags` class exposes them to code.

### Commands
- `./gradlew build` - Build the mod jar (`build/libs/`)
- `./gradlew runClient` / `./gradlew runServer` - Development instances (`run/`)

## Architecture

- `AE2PatternCounterMod` - `@Mod` class: creative tab, item registration, item models (client), AE2 part model registration (pre-init).
- `items/PatternCounterItem` - one item per counter type; implements AE2 `IPartItem` and places the part with `placeBus`.
- `parts/PatternCounterPart` - the only part class. `PatternCounterPart.Type` holds each counter's models and pattern filter.
  - Counting: craftable entries of the item storage list -> `ICraftingGrid.getCraftingFor` -> `HashSet` (AE2 1.12 lists a pattern under each output).
  - Fluid patterns are detected by the `ae2fc:fluid_drop` item (AE2 Fluid Crafting Rework), with no compile dependency.

To add a counter: add a `Type` value, its `models/part/<id>_on|off.json`, item model, texture, lang key and recipe.

## Development Notes

- Always use imports instead of fully-qualified class names.
- Use the Edit tool to modify files; do not use `sed`/`awk` on code files.
- AE2 reference: https://github.com/AE2-UEL/Applied-Energistics-2
- Part models use AE2's `ae2_uvl_marker` / `uvlightmap` for emissive faces (not `forge_data`).
- Recipes use AE2's `appliedenergistics2:part` ingredient type (`part.storage_monitor`, `material.blank_pattern`).
- `CHANGELOG.md` contains only the current version's changes.
