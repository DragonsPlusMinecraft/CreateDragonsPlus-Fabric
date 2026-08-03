## Welcome to **Create: Dragons Plus**

A Fabric 1.20.1 library and feature mod for DragonsPlusMinecraft Create add-ons.

## Add Dependency

```groovy
repositories {
    maven { url "https://maven.dragons.plus/releases" } // DragonsPlusMinecraft Maven
}

dependencies {
    modImplementation("plus.dragons.createdragonsplus:create-dragons-plus-fabric:${create_dragons_plus_version}")
}
```

The first Fabric release targets Java 17, Minecraft 1.20.1, Fabric Loader 0.17.2 or newer,
Fabric API 0.92.6 or newer, and Create Fabric `>=6.0.8.1 <6.1.0`. JEI is optional.

## Contribute

Feel free to open a PR to either provide localization or to add another feature! All help is appreciated!

### If you want to help us to translate...

Please use `src/generated/resources/assets/create_dragons_plus/lang/en_us.json` as the key reference
and submit completed translations under `src/main/resources/assets/create_dragons_plus/lang`.
