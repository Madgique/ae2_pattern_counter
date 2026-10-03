# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

"AE2 Pattern Counter" for **Minecraft 1.21.1 / NeoForge only** (branch `1.21.1`).
Other branches: `1.20.1` (Forge + Fabric, Architectury), `1.12.2` (Forge, AE2 Unofficial Extended Life).

The mod adds AE2 cable parts that display how many patterns of one type (Crafting, Processing, Smithing, Stonecutting, Fluid) the network knows.
AE2 has no Fabric or Forge release for 1.21.1, so this branch is a single NeoForge module.

## Build System

- NeoForge MDK with ModDevGradle (`net.neoforged.moddev`), Java 21, Parchment mappings.
- AE2 comes from Maven Central: `org.appliedenergistics:appliedenergistics2:${ae2_version}` (GuideME is pulled at runtime).
- Mod metadata lives in `gradle.properties`; `src/main/templates/META-INF/neoforge.mods.toml` is expanded at build time.

### Commands
- `./gradlew build` - Build the mod jar (`build/libs/`)
- `./gradlew runClient` / `./gradlew runServer` - Development instances (`run/`)

## Architecture

- `AE2PatternCounterMod` - `@Mod` class: item and creative tab `DeferredRegister`s, AE2 part model registration.
- `parts/PatternCounterPart` - the only part class. `PatternCounterPart.Type` holds each counter's id, models and pattern filter.
  - Counting: `ICraftingService.getCraftables` -> `getCraftingFor`. AE2 indexes each pattern once, under its primary output.
  - The count is synced with `writeToStream`/`readFromStream` and drawn in `renderDynamic`, under the type icon of the texture.

To add a counter: add a `Type` value, its `models/part/<id>_on|off.json`, item model, textures, lang key and recipe.

## Resources

- Part models use `neoforge_data` for emissive faces. No `tintindex`: it would tint the screen with the cable color.
- Recipes are in `data/ae2patterncounter/recipe/` (1.21 format: `"result": {"id": ..., "count": 1}`).
- Textures: 16x16; the placed part shows only pixels 2..13. The outer ring is the item icon frame.

## Development Notes

- Always use imports instead of fully-qualified class names.
- Use the Edit tool to modify code files; do not use `sed`/`awk` on code files.
- Use `Locale.ROOT` for `toLowerCase` on identifiers.
- AE2 reference: https://github.com/AppliedEnergistics/Applied-Energistics-2/tree/1.21.1
- `CHANGELOG.md` contains only the current version's changes (it is uploaded as the release notes).

## Releasing

Push a tag `v<mod_version>-1.21.1`. `.github/workflows/publish.yml` builds and uploads to Modrinth and CurseForge
(secrets: `MODRINTH_TOKEN`, `MODRINTH_PROJECT_ID`, `CURSEFORGE_TOKEN`, `CURSEFORGE_PROJECT_ID`).
