package io.github.therealsegfault.projectbeatsgdx.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Rhythm engine core (portable, deterministic).
 * This is the Java analogue of your JS engine decisions:
 * - time-based hitTime (seconds)
 * - earliest hittable note per lane
 * - stable seq ordering
 */
public final class EngineCore {

    public static final class LiveNote {
        public int seq;
        public int lane;
        public double timeSeconds;
        public double spawnTimeSeconds;
        public double approachSeconds;
        public boolean judged;
        public Judgement judgement = Judgement.MISS;
    }

    private final EngineConfig cfg;
    private final ScoreState score = new ScoreState();
    private final List<LiveNote> notes = new ArrayList<>();
    private int nextSeq = 0;

    // deterministic lane selection placeholder
    private final Random rng;

    public EngineCore(EngineConfig cfg, long seed) {
        this.cfg = cfg;
        this.rng = new Random(seed);
    }

    public ScoreState score() { return score; }
    public List<LiveNote> notes() { return notes; }

    public void reset() {
        notes.clear();
        nextSeq = 0;
        score.score = 0;
        score.combo = 0;
    }

    public void spawnNote(int lane, double hitTimeSeconds) {
        LiveNote n = new LiveNote();
        n.seq = nextSeq++;
        n.lane = lane;
        n.timeSeconds = hitTimeSeconds;
        n.approachSeconds = cfg.approachTimeSeconds;
        n.spawnTimeSeconds = hitTimeSeconds - n.approachSeconds;
        notes.add(n);
    }

    public int aliveCountInLane(int lane) {
        int c = 0;
        for (LiveNote n : notes) {
            if (!n.judged && n.lane == lane) c++;
        }
        return c;
    }

    public int aliveCountAll() {
        int c = 0;
        for (LiveNote n : notes) if (!n.judged) c++;
        return c;
    }

    /** Find earliest by (time, seq) for a lane (PDiva-style). */
    public LiveNote findEarliestLaneNote(int lane) {
        LiveNote best = null;
        for (LiveNote n : notes) {
            if (n.judged) continue;
            if (n.lane != lane) continue;
            if (best == null) best = n;
            else if (n.timeSeconds < best.timeSeconds) best = n;
            else if (n.timeSeconds == best.timeSeconds && n.seq < best.seq) best = n;
        }
        return best;
    }

    public boolean laneIsHittable(LiveNote n, double t) {
        if (n == null) return false;
        HitWindows w = cfg.windows;
        if (t < n.timeSeconds - w.sad) return false;
        if (t > n.timeSeconds + w.miss) return false;
        return true;
    }

    public Judgement judge(LiveNote n, double t) {
        if (n == null || n.judged) return Judgement.MISS;

        double d = Math.abs(n.timeSeconds - t);
        HitWindows w = cfg.windows;

        if (d <= w.perfect) return registerHit(n, Judgement.PERFECT, 500);
        if (d <= w.good)    return registerHit(n, Judgement.COOL,    300);
        if (d <= w.safe)    return registerHit(n, Judgement.FINE,    150);
        if (d <= w.sad)     return registerHit(n, Judgement.SAD,      50);
        return registerMiss(n);
    }

    private Judgement registerHit(LiveNote n, Judgement j, int baseScore) {
        n.judged = true;
        n.judgement = j;

        score.combo++;
        score.score += (int)Math.floor(baseScore * (1.0 + score.combo * 0.03));
        return j;
    }

    private Judgement registerMiss(LiveNote n) {
        n.judged = true;
        n.judgement = Judgement.MISS;
        score.combo = 0;
        return Judgement.MISS;
    }

    /**
     * Very simple generator: one note per beat from tStart..tEnd with lane caps.
     * Replace this with your real generator once you provide your new JS bases.
     */
    public void generateGrid(double bpm, double tStart, double tEnd) {
        double beat = 60.0 / bpm;
        double last = -1e9;

        for (double t = tStart; t <= tEnd; t += beat) {
            if (aliveCountAll() >= cfg.maxAlive) break;
            if (t - last < cfg.minNoteGapSeconds) continue;

            int lane = rng.nextInt(cfg.lanes);
            if (aliveCountInLane(lane) >= cfg.maxAlivePerLane) {
                last = t; // advance timing even if skipped
                continue;
            }

            spawnNote(lane, t);
            last = t;
        }
    }

    /** Remove judged notes older than a fade window. Runtime can call this per frame. */
    public void cleanupJudged(double nowSeconds, double keepSeconds) {
        notes.removeIf(n -> n.judged && (nowSeconds - n.timeSeconds) > keepSeconds);
    }
}
