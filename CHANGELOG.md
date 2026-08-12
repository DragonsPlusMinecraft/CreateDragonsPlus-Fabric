# Create: Dragons Plus 1.1.7b

## Fixed

- Fixed the production-only Fabric startup crash in `AirFlowParticle` with Create Fabric 6.0.8.1.
- Corrected the related `AirCurrent` mixin failure that appeared after the particle mixin loaded successfully.
- Made the affected fan processing, potion mixing, and open-ended pipe mixin selectors safe for the intermediary namespace used by released Fabric jars.

## Compatibility

- Targets Java 17, Minecraft 1.20.1, Fabric Loader 0.17.2 or newer, Fabric API 0.92.6 or newer, and Create Fabric `>=6.0.8.1 <6.1.0`.
- Supersedes 1.1.7 without registry, configuration, or save-format changes.
