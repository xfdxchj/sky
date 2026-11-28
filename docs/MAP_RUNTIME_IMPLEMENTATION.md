# In-mod map resource pipeline

## Goal

Player-facing map delivery is an in-mod resource path, not a manually installed datapack.
The runtime structure loader reads templates from the mod resource namespace and the
map manager will generate them into the target world while recording generation state.

## Current implementation stage

The Gradle task prepareBundledMapResources creates the resource tree consumed by the
normal Minecraft resource manager:

- Hub: slices the_hub_incl_carnival.nbt into bounded vanilla structure NBT resources.
- Gold Mine Modern: copies the audited structure NBT as one resource.
- Both: create Core structure manifests under
  data/skyblockrpg/skyblock/structures/ and templates under
  data/skyblockrpg/structures/.

The resulting JAR resource paths include:

~~~text
data/skyblockrpg/structures/hub_x00_y00_z00.nbt
data/skyblockrpg/skyblock/structures/hub_x00_y00_z00.json
data/skyblockrpg/structures/gold_mine_modern_version.nbt
data/skyblockrpg/skyblock/structures/gold_mine_modern.json
~~~

## Build

Normal build (no community maps embedded):

~~~powershell
.\gradlew.bat build
~~~

Local/private map-enabled build (requires Python 3.10+):

~~~powershell
.\gradlew.bat build -PincludeCommunityMaps=true
~~~

If Python is not on PATH, supply its executable:

~~~powershell
.\gradlew.bat build -PincludeCommunityMaps=true -PpythonExecutable=py
~~~

On Linux CI, use -PpythonExecutable=python3.

## Important boundary

The map assets are community downloads. Their redistribution permission has not been
verified. The opt-in switch is deliberately off by default and intended for local/private
builds while permission is unresolved. Do not publish a map-enabled JAR until the relevant
asset license/permission is confirmed.

## Not yet complete

Bundling is only the resource-delivery foundation. It does not itself place the maps
into a world. The next runtime stage must provide:

1. a logical map registry (one map may consist of many structure slices);
2. a safe, isolated destination and preflight of the complete map footprint;
3. bounded server-thread generation with progress tracking;
4. persistent per-world generation records and recovery after restart;
5. automatic map POI/warp activation only after successful generation.

The current runtime registry exposes diagnostics through /skyblock map list and /skyblock map info <mapId>. Map definitions are loaded after structure manifests at server startup. The registry rejects missing structure references, out-of-bounds slices, overlapping slices, and incomplete map coverage.\n\nThe legacy datapack staging task remains only as a development/export utility.
