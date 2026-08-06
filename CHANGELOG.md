# Create: Dragons Plus 1.11.4 Preview 2

## Fixed

- Fixed the startup crash reported in [#103](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus/issues/103) when Create add-ons load contraption or block-entity behaviour classes.
- Restored Fabric behaviour-provider lookup for block entities that do not extend Create's `SmartBlockEntity`.
- Preserved water and lava contained by `WaterAndLavaLoggedBlock` implementations when contraptions assemble and disassemble.

## Compatibility

- Targets Java 17, Minecraft 1.20.1, Fabric Loader 0.17.2 or newer, Fabric API 0.92.6 or newer, and Create Fabric `>=6.0.8.1 <6.1.0`.
- This preview supersedes `1.11.4-p.1`; published Preview 1 artifacts must not be overwritten.
