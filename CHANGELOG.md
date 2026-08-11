# Create: Dragons Plus 1.1.7

## Fixed

- Fixed consuming open pipe effects calculating their remainder after Create could place the fluid in the world and clear the pipe's internal tank.
- Preserved the exact unconsumed fluid remainder, including NBT data, while keeping transaction rollback and simulation paths side-effect-free.
- Prevented consuming open pipe effects from falling back to Create's fluid-block placement path; non-consuming handlers retain Create's original behavior.
- Fixed the production-only Fabric startup crash reported in [#104](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus/issues/104), caused by Preview 2 mixin targets not surviving namespace remapping.
- Kept the startup fixes from [#103](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus/issues/103) while replacing the affected mixin selectors with production-safe targets.
- Restored Fabric behaviour-provider lookup for block entities that do not extend Create's `SmartBlockEntity`.
- Preserved water and lava contained by `WaterAndLavaLoggedBlock` implementations when contraptions assemble and disassemble.

## Compatibility

- Targets Java 17, Minecraft 1.20.1, Fabric Loader 0.17.2 or newer, Fabric API 0.92.6 or newer, and Create Fabric `>=6.0.8.1 <6.1.0`.
