# ProjectBeats (LibGDX + Maven, Java 21)

This is a **desktop-first** LibGDX scaffold using **Maven multi-module** layout.

## Modules
- `core/` : engine-only code (no LibGDX types). Put timing, judgement, charting here.
- `desktop/` : LWJGL3 launcher + LibGDX runtime shell (render, input, audio).
- `teavm/` : placeholder module for a future TeaVM web target (not wired yet).

## Run (desktop)
From the repo root:

```bash
mvn -q -pl desktop -am exec:java
```

Or build a runnable fat jar:

```bash
mvn -q -pl desktop -am package
java -jar desktop/target/projectbeats-desktop-0.1.0-SNAPSHOT-all.jar
```

## Assets
Put shared assets in `/assets`. At runtime they will be accessible via LibGDX's `internal` file handle:

```java
Gdx.files.internal("assets/yourfile.ext")
```

(We copy `/assets` into the desktop jar under `assets/`.)

## Next steps
- Replace the placeholder `DebugRhythmScreen` with your real engine hook.
- Keep core deterministic and pure; treat LibGDX as an adapter.
