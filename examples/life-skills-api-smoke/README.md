# Life Skills API Smoke Test

This is a tiny independent Fabric extension used to verify that another mod can compile against and initialize with the Core public API.

1. Build Core with Java 21.
2. Copy Core's remapped jar into this project's `libs/` directory using the filename referenced in `build.gradle`.
3. Run `./gradlew build`.
4. Put both Core and the smoke-test jar into a Minecraft 1.21.1 Fabric test instance.
5. Launch a world and check the log for four successful namespaced skill registrations.

The XP curve is test-only placeholder data. This project intentionally does not implement mining, fishing, farming or foraging mechanics.

The smoke test validates the compile-time API boundary and skill registration. Player-data persistence, item datapack loading, inventory, currency and reward behavior still require the detailed checks in `docs/CORE_API_v2_TEST_PLAN.md`.
