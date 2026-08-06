# Create: Dragons Plus 1.11.4 Preview 3

## Fixed

- Fixed the production-only Fabric startup crash reported in [#104](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus/issues/104), caused by Preview 2 mixin targets not surviving namespace remapping.
- Kept the startup fixes from [#103](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus/issues/103) while replacing the affected mixin selectors with production-safe targets.
- Restored Fabric behaviour-provider lookup for block entities that do not extend Create's `SmartBlockEntity`.
- Preserved water and lava contained by `WaterAndLavaLoggedBlock` implementations when contraptions assemble and disassemble.

## Compatibility

- Targets Java 17, Minecraft 1.20.1, Fabric Loader 0.17.2 or newer, Fabric API 0.92.6 or newer, and Create Fabric `>=6.0.8.1 <6.1.0`.
- This preview supersedes `1.11.4-p.1` and `1.11.4-p.2`; published preview artifacts must not be overwritten.
