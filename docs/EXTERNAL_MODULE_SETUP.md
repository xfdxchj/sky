# External Extension Mod Setup

This guide is for a separate Fabric mod that extends SkyBlock RPG Core without copying Core source.

## 1. Obtain the Core compile-time API

Build Core once with Java 21:

```bash
./gradlew build
```

Copy the resulting remapped mod jar from Core's `build/libs/` into the extension project's `libs/` directory. Use the same Core version that will be installed in the test instance.

The extension uses the Core jar as a compile-only dependency. Do not shade, embed, or redistribute Core classes inside the extension jar.

Example:

```groovy
dependencies {
    modCompileOnly files("libs/skyblock-rpg-0.1.0-alpha.3.jar")
}
```

If the artifact filename changes, update the path. The player must install both Core and the extension at runtime.

## 2. Declare the hard dependency

In the extension's `fabric.mod.json`:

```json
{
  "schemaVersion": 1,
  "id": "skyblock_life",
  "version": "0.1.0",
  "name": "SkyBlock RPG Life Skills",
  "environment": "*",
  "entrypoints": {
    "main": ["com.example.skyblocklife.LifeSkillsMod"]
  },
  "depends": {
    "fabricloader": ">=0.16.0",
    "minecraft": "~1.21.1",
    "java": ">=21",
    "fabric-api": "*",
    "skyblockrpg": "*"
  }
}
```

The dependency ensures Core initializes before the extension. Do not add a reverse dependency from Core to the extension.

## 3. Public API packages

Use `com.skyrpg.skyblock.api.*` as the supported Core integration surface. The API-owned `SkillDefinition` and `ItemView` types keep extensions away from Core registry implementation classes. `StatType` is the only current shared data type outside the API package.

Avoid Core implementation packages such as `profile`, `economy`, `inventory`, `skill.SkillManager`, and `item.ItemRegistry`. They are not extension contracts.

## 4. Resource locations

Put extension item definitions at:

```text
src/main/resources/data/skyblock_life/skyblock/items/<item_path>.json
```

Use the same namespace in each definition ID, e.g. `skyblock_life:refined_mithril`.

Core loads item definitions during server startup resource loading. Do not query them in the extension's `onInitialize`; query after a world/server has started.

## 5. API version guard

At startup, check `CoreApi.API_VERSION` before using methods introduced by that API version. Core API v2 is the current contract. A hard dependency on the Core mod alone does not guarantee that an older Core jar has the required methods.

## 6. Server authority

All profile, inventory, currency, reward and skill-XP writes must run on the logical server. Never award rewards from client callbacks. Check every boolean return value.

## 7. Compatibility rule

Do not rely on internal classes merely because they are visible on the compile classpath. If an extension needs an operation not covered by the public API, request a Core API addition rather than reaching into Core profile or manager internals.
