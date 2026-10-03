# Changelog

## [1.2.0] - 2026-10-03 (Minecraft 1.20.1)

### New look
- New screen textures: a dark display with a colored icon for each counter type.
- Bigger count, drawn under the icon. Long texts shrink to fit the screen.
- New mod icon.

### Fixed
- Fabric: the mod now loads on dedicated servers.
- Screen colors no longer turn purple on fluix cables.
- The Processing counter now also counts patterns whose main output is a fluid.

### Changed
- Internal cleanup: all counters share one part class. No gameplay change.
- Removed unused files and dependencies (ClothConfig, Jade translations, old textures).
