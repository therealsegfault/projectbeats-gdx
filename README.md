# ProjectBeats (libGDX) — Maven build

This repo is a Maven multi-module project:
- `core` (Kotlin): timing + lane-judging engine (no libGDX dependency)
- `desktop` (Java): libGDX LWJGL3 runner + debug screens

## Build

```bash
mvn -DskipTests package
```

## Run (desktop)

```bash
mvn -pl desktop -am exec:java
```

### macOS note
LWJGL/GLFW requires the first thread. Run with:

```bash
MAVEN_OPTS='-XstartOnFirstThread' mvn -pl desktop -am exec:java
```

## Demo chart

A tiny JSON chart lives at `assets/charts/demo.json`.
The debug screen will try to load it and spawn notes from it.
