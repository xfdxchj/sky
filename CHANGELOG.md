# Changelog

## P3.1 - Combat interception and data correction
- Replaced the client-side `AttackEntityCallback` approach with a server-side Mixin at `PlayerEntity.attack(Entity)`.
- Added the Mixin configuration to `fabric.mod.json` and the Fabric Loader annotation processor to the Gradle dependencies.
- Corrected the simplified melee formula to include Hypixel's +5 base damage and floor the final result.
- Added community-wiki baseline Crit Chance (30%) and Crit Damage (50%) to new profiles.
- Added finite-value and upper-bound protection before passing damage to Minecraft.
- Removed generic kill rewards for unregistered entities; only explicitly mapped starter test mobs receive rewards.
- Added `DATA_SOURCES.md` to distinguish official Hypixel API resources, community wiki data and project placeholders.
- Updated the P3 test plan and README.

## P3 - Initial combat prototype
- Added Damage stat, training sword, damage formula, mob metadata and combat command.
- Initial implementation used a client-side attack callback incorrectly; superseded by P3.1.


## P5.1
- Added manifest-driven native structure NBT import and placement.
- Added rotation/mirror transforms, bounds checks and all-air paste safety.
- Added structure commands and a project-authored test fixture.


## P5.2
- Added JSON-driven bridge and portal connection definitions linked to existing warp nodes.
- Added a route adjacency query and operator-only bridge deck generation with full preflight safety checks.
- Added proximity-gated portal travel through the existing warp manager.
- Added starter connection fixtures and P5.2 design/test documentation.
