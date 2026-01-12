package io.github.therealsegfault.projectbeatsgdx.core;

public final class EngineConfig {
    public Difficulty difficulty = Difficulty.NORMAL;
    public double approachTimeSeconds = 2.8;
    public HitWindows windows = new HitWindows(0.08, 0.18, 0.26, 0.33, 0.40);

    public int maxAlive = 8;
    public int maxAlivePerLane = 1;

    public double spawnLookaheadSeconds = 8.0;
    public double minNoteGapSeconds = 0.22;

    public int lanes = 4;
}
