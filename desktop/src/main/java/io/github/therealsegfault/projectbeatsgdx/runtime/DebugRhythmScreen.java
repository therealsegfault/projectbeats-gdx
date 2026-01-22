package io.github.therealsegfault.projectbeatsgdx.runtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.therealsegfault.projectbeatsgdx.core.EngineConfig;
import io.github.therealsegfault.projectbeatsgdx.core.EngineCore;
import io.github.therealsegfault.projectbeatsgdx.core.Judgement;

import java.util.Locale;
import io.github.therealsegfault.projectbeatsgdx.core.chart.Chart;
import io.github.therealsegfault.projectbeatsgdx.core.chart.ChartToEngine;
import io.github.therealsegfault.projectbeatsgdx.runtime.chart.ChartLoader;

import java.util.Random;

/**
 * Minimal playable debug scene:
 * - Fixed receptors (no moving cluster yet)
 * - Notes fly from offscreen toward lane targets with smoothstep interpolation
 * - WASD + touch to hit
 *
 * This mirrors your JS math (simplified):
 *   prog = (t - spawnTime) / approachTime
 *   eprog = smoothstep(prog)
 *   pos = lerp(spawnPos, targetPos, eprog)
 */
public final class DebugRhythmScreen extends ScreenAdapter {

    private static final float UI_SCALE = 0.85f;

    // Lane colors (JS uses #2a7bff for all lanes)
    private static final Color LANE_BLUE = new Color(0.1647f, 0.4823f, 1.0f, 1.0f);
    private static final Color WHITE = new Color(1f, 1f, 1f, 1f);

    private final ProjectBeatsGame game;

    private final OrthographicCamera cam = new OrthographicCamera();
    private final BitmapFont font = new BitmapFont();

    private ShapeRenderer shapes;

    private Music music;
    private boolean started = false;

    // Engine core (portable)
    private final EngineConfig cfg = new EngineConfig();
    private final EngineCore engine = new EngineCore(cfg, 1337L);

    // Placeholder song config (manual math)
    private final double bpm = 120.0;
    private final double beat = 60.0 / bpm;

    // Simple deterministic spawner (avoids re-spawning the whole grid every frame)
    private final Random rng = new Random(1337L);

    //
    // Debug toggles
    //
    private boolean useDemoChart = true;
    private boolean useCopLaneLayout = true;

    private Chart loadedChart = null;
    private double nextBeatTime = 0.0;
    private double lastSpawnedTime = -1e9;

    // "lightshow" state per lane (like your JS)
    private final float[] lanePulse = new float[] {0f, 0f, 0f, 0f};
    private final float[] laneGlow  = new float[] {0f, 0f, 0f, 0f};

    public DebugRhythmScreen(ProjectBeatsGame game) {
        this.game = game;

        // Match your JS-ish defaults
        cfg.spawnLookaheadSeconds = 8.0;
        cfg.minNoteGapSeconds = 0.22;
        cfg.maxAlive = 8;
        cfg.maxAlivePerLane = 1;
        cfg.approachTimeSeconds = 2.8;
    }

    @Override
    public void show() {
        cam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.update();
        shapes = new ShapeRenderer();

        // Put an actual audio file in /assets/music/test.ogg (or change path).
        try {
            music = Gdx.audio.newMusic(Gdx.files.internal("music/test.ogg"));
            music.setLooping(false);
            music.setVolume(0.9f);
        } catch (Exception e) {
            Gdx.app.error("ProjectBeats", "Failed to load test song. Put a file at assets/music/test.ogg", e);
            music = null;
        }

        // Best-effort: load a demo chart if present.
        loadDemoChart();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        decayLaneFx();

        double t = (music != null && started) ? music.getPosition() : 0.0;

        // Spawn notes ahead of time (one per beat, lane caps)
        // If we're using a chart, notes are pre-spawned in loadDemoChart().
        if (started && !useDemoChart) {
            generateNotes(t);
        }

        // Input: WASD -> hit lane 0..3
        if (started) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.W)) hitLane(0, t);
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) hitLane(1, t);
            if (Gdx.input.isKeyJustPressed(Input.Keys.S)) hitLane(2, t);
            if (Gdx.input.isKeyJustPressed(Input.Keys.D)) hitLane(3, t);

            // Touch/click: pick closest visible note
            if (Gdx.input.justTouched()) {
                float sx = Gdx.input.getX();
                float sy = Gdx.graphics.getHeight() - Gdx.input.getY(); // screen Y -> world Y (ortho)
                hitAt(sx, sy, t);
            }
        }

        // Start/stop
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            toggleStart();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restart();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            useCopLaneLayout = !useCopLaneLayout;
        }

        // Remove judged notes after a short delay (keeps list sane for debug)
        if (started) {
            engine.cleanupJudged(t, 1.0);
        }

        // Draw gameplay (receptors + notes)
        drawGameplay(t);

        // Draw debug overlay text
        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();

        String header = "ProjectBeats (LibGDX Maven) | SPACE=start/stop  R=restart  L=toggle layout  WASD=hit  click/tap=hit";
        font.draw(game.batch, header, 24, Gdx.graphics.getHeight() - 24);

        String status = String.format(Locale.US,
                "t=%.3f  bpm=%.1f  chart=%s  activeNotes=%d  score=%d  combo=%d",
                t, bpm, (loadedChart != null ? loadedChart.getId() : "(rng)"), engine.aliveCountAll(), engine.score().score, engine.score().combo);
        font.draw(game.batch, status, 24, Gdx.graphics.getHeight() - 52);

        if (!started) {
            font.draw(game.batch, "Press SPACE to start.", 24, Gdx.graphics.getHeight() - 84);
        }

        game.batch.end();
    }


    private void loadDemoChart() {
        if (!useDemoChart) return;

        try {
            loadedChart = ChartLoader.load("charts/demo.json");

            // Keep the engine "approach" difficulty-driven (not BPM-driven).
            cfg.approachTimeSeconds = loadedChart.getApproachSeconds();
            engine.reset();

            // Pre-spawn all notes. For big charts later, we can stream-spawn.
            for (var ev : ChartToEngine.toNoteEvents(loadedChart)) {
                engine.spawnNote(ev.timeSeconds, ev.lane, cfg.approachTimeSeconds);
            }

            // Optional: if the chart points to a different audio file, prefer that.
            if (music != null && loadedChart.getAudio() != null && !loadedChart.getAudio().isBlank()) {
                // Only swap if it's different from what we already loaded.
                if (!loadedChart.getAudio().equals("music/test.ogg")) {
                    music.dispose();
                    music = Gdx.audio.newMusic(Gdx.files.internal(loadedChart.getAudio()));
                    music.setLooping(false);
                    music.setVolume(0.9f);
                }
            }
        } catch (Exception e) {
            // Fall back to the RNG spawner if the chart is missing or malformed.
            Gdx.app.error("ProjectBeats", "Failed to load charts/demo.json; falling back to RNG spawner.", e);
            loadedChart = null;
            useDemoChart = false;
        }
    }

    private void drawCopLaneGameplay(double t) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        // Layout tuned to feel like UNBEATABLE cop fights: stacked horizontal lanes + a judge line.
        int lanes = cfg.laneCount;
        float laneSpacing = (h * 0.55f) / (lanes - 1);
        float baseY = h * 0.22f;

        float judgeX = w * 0.82f;
        float spawnX = w * -0.10f;

        float noteSize = Math.min(w, h) * 0.040f * UI_SCALE;

        shapes.setProjectionMatrix(cam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Lanes
        shapes.setColor(1f, 1f, 1f, 0.16f);
        for (int lane = 0; lane < lanes; lane++) {
            float y = baseY + lane * laneSpacing;
            shapes.rect(0f, y - 2f, w, 4f);
        }

        // Judge line
        shapes.setColor(1f, 1f, 1f, 0.30f);
        shapes.rect(judgeX - 2f, baseY - 40f, 4f, (lanes - 1) * laneSpacing + 80f);

        // Notes
        for (var n : engine.notesSnapshot()) {
            if (n.judged) continue;

            double p = (t - n.spawnTimeSeconds) / n.approachSeconds;
            if (p < 0.0) continue;
            if (p > 1.25) continue;

            float y = baseY + (float) n.lane * laneSpacing;
            float x = (float) (spawnX + (judgeX - spawnX) * p);

            // Fade based on how "hot" the lane is (hit flash).
            float a = 0.70f + 0.30f * laneFx[n.lane];
            shapes.setColor(1f, 1f, 1f, a);
            shapes.rect(x - noteSize / 2f, y - noteSize / 2f, noteSize, noteSize);
        }

        shapes.end();

        // Receptors (simple crosshair / circles)
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(1f, 1f, 1f, 0.65f);
        for (int lane = 0; lane < lanes; lane++) {
            float y = baseY + lane * laneSpacing;
            shapes.circle(judgeX, y, noteSize * 0.65f);
            shapes.line(judgeX - noteSize, y, judgeX + noteSize, y);
            shapes.line(judgeX, y - noteSize, judgeX, y + noteSize);
        }
        shapes.end();
    }

    private void drawGameplay(double t) {
        if (useCopLaneLayout) {
            drawCopLaneGameplay(t);
            return;
        }

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        float minDim = Math.min(w, h);
        float spread = minDim * 0.115f * UI_SCALE;

        // Fixed receptor cluster center (matches your defaultCenter() feel)
        float cx = w * 0.5f;
        float cy = h * 0.44f;

        // Radius (JS: minDim * 0.045 * UI_SCALE on desktop)
        float r = minDim * 0.045f * UI_SCALE;

        // Beat pulse (JS: 0.5 + 0.5*sin((t%BEAT)/BEAT * 2pi))
        float bp = (float)((t % beat) / beat);
        float beatPulse = 0.5f + 0.5f * (float)Math.sin(bp * Math.PI * 2.0);

        // Offsets: W, A, S, D
        float[] ox = new float[] {0f, -spread, 0f, spread};
        float[] oy = new float[] {-spread, 0f, spread, 0f};

        // Receptors
        shapes.setProjectionMatrix(cam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);

        for (int lane = 0; lane < 4; lane++) {
            float tx = cx + ox[lane];
            float ty = cy + oy[lane];

            float pulse = lanePulse[lane];
            float glow = laneGlow[lane];

            // White ring
            shapes.setColor(1f, 1f, 1f, 0.65f + beatPulse * 0.20f);
            shapes.circle(tx, ty, r * (1.12f + pulse * 0.10f), 48);

            // Blue ring (slightly larger)
            shapes.setColor(LANE_BLUE.r, LANE_BLUE.g, LANE_BLUE.b, 0.30f + glow * 0.45f);
            shapes.circle(tx, ty, r * (1.38f + beatPulse * 0.20f + pulse * 0.22f), 48);
        }

        shapes.end();

        // Notes (filled)
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Notes fly in from above (fixed ang = -pi/2) for now
        float ang = (float)(-Math.PI / 2.0);
        float nx = (float)Math.cos(ang);
        float ny = (float)Math.sin(ang);
        float spawnDist = Math.max(w, h) * 0.70f;

        for (EngineCore.LiveNote n : engine.notes()) {
            if (n.judged) continue;
            if (t < n.spawnTimeSeconds) continue;

            int lane = n.lane;

            float tx = cx + ox[lane];
            float ty = cy + oy[lane];

            float sx = tx - nx * spawnDist;
            float sy = ty - ny * spawnDist;

            float prog = clamp01((float)((t - n.spawnTimeSeconds) / n.approachSeconds));
            float eprog = smoothstep(prog);

            float x = lerp(sx, tx, eprog);
            float y = lerp(sy, ty, eprog);

            float approachScale = 1.35f - 0.35f * eprog;
            float noteR = r * 0.95f * approachScale;

            // Outer glow-ish fill
            float glow = 0.35f + beatPulse * 0.25f + laneGlow[lane] * 0.25f;
            shapes.setColor(LANE_BLUE.r, LANE_BLUE.g, LANE_BLUE.b, 0.15f + glow);
            shapes.circle(x, y, noteR * 1.25f, 28);

            // Core fill
            shapes.setColor(LANE_BLUE.r, LANE_BLUE.g, LANE_BLUE.b, 0.85f);
            shapes.circle(x, y, noteR, 28);

            // White center dot (helps readability)
            shapes.setColor(1f, 1f, 1f, 0.80f);
            shapes.circle(x, y, noteR * 0.35f, 20);
        }

        shapes.end();
    }

    private void generateNotes(double tNow) {
        // ensure nextBeatTime catches up if song seeks / lags
        if (nextBeatTime < tNow - 1.0) {
            nextBeatTime = Math.floor(tNow / beat) * beat;
        }

        while (nextBeatTime < tNow + cfg.spawnLookaheadSeconds) {
            if (engine.aliveCountAll() >= cfg.maxAlive) return;

            double bt = nextBeatTime;
            nextBeatTime += beat;

            if (bt - lastSpawnedTime < cfg.minNoteGapSeconds) continue;

            int lane = rng.nextInt(4);
            if (engine.aliveCountInLane(lane) >= cfg.maxAlivePerLane) {
                lastSpawnedTime = bt;
                continue;
            }

            engine.spawnNote(lane, bt);
            lastSpawnedTime = bt;
        }
    }

    private void hitLane(int lane, double t) {
        EngineCore.LiveNote n = engine.findEarliestLaneNote(lane);
        if (!engine.laneIsHittable(n, t)) return;

        Judgement j = engine.judge(n, t);
        onJudged(lane, j);
    }

    private void hitAt(float x, float y, double t) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        float minDim = Math.min(w, h);
        float spread = minDim * 0.115f * UI_SCALE;

        float cx = w * 0.5f;
        float cy = h * 0.44f;

        float r = minDim * 0.045f * UI_SCALE;
        float pickR = r * 1.5f;

        float[] ox = new float[] {0f, -spread, 0f, spread};
        float[] oy = new float[] {-spread, 0f, spread, 0f};

        float ang = (float)(-Math.PI / 2.0);
        float nx = (float)Math.cos(ang);
        float ny = (float)Math.sin(ang);
        float spawnDist = Math.max(w, h) * 0.70f;

        EngineCore.LiveNote best = null;
        float bestDist = Float.POSITIVE_INFINITY;

        for (EngineCore.LiveNote n : engine.notes()) {
            if (n.judged) continue;
            if (t < n.spawnTimeSeconds) continue;
            if (t > n.timeSeconds + cfg.windows.miss) continue;

            int lane = n.lane;

            float tx = cx + ox[lane];
            float ty = cy + oy[lane];

            float sx = tx - nx * spawnDist;
            float sy = ty - ny * spawnDist;

            float prog = clamp01((float)((t - n.spawnTimeSeconds) / n.approachSeconds));
            float eprog = smoothstep(prog);

            float nxp = lerp(sx, tx, eprog);
            float nyp = lerp(sy, ty, eprog);

            float dx = x - nxp;
            float dy = y - nyp;
            float d = (float)Math.sqrt(dx * dx + dy * dy);
            if (d < bestDist) {
                bestDist = d;
                best = n;
            }
        }

        if (best == null) return;
        if (bestDist > pickR) return;
        if (!engine.laneIsHittable(best, t)) return;

        Judgement j = engine.judge(best, t);
        onJudged(best.lane, j);
    }

    private void onJudged(int lane, Judgement j) {
        if (j == Judgement.MISS) {
            laneGlow[lane] = Math.min(1.2f, laneGlow[lane] + 0.40f);
            lanePulse[lane] = Math.max(0f, lanePulse[lane] - 0.15f);
            return;
        }

        lanePulse[lane] = Math.min(1.8f, lanePulse[lane] + 1.0f);
        laneGlow[lane] = Math.min(1.2f, laneGlow[lane] + 0.9f);
    }

    private void decayLaneFx() {
        for (int i = 0; i < 4; i++) {
            lanePulse[i] = Math.max(0f, lanePulse[i] - 0.085f);
            laneGlow[i] = Math.max(0f, laneGlow[i] - 0.050f);
        }
    }

    private void toggleStart() {
        started = !started;
        if (music != null) {
            if (started) music.play();
            else music.pause();
        }
    }

    private void restart() {
        engine.reset();
        rng.setSeed(1337L);
        started = false;
        loadDemoChartIfPresent();
        nextBeatTime = 0.0;
        lastSpawnedTime = -1e9;

        for (int i = 0; i < 4; i++) {
            lanePulse[i] = 0f;
            laneGlow[i] = 0f;
        }

        if (music != null) {
            music.stop();
        }
    }

    @Override
    public void resize(int width, int height) {
        cam.setToOrtho(false, width, height);
        cam.update();
    }

    @Override
    public void dispose() {
        if (music != null) music.dispose();
        if (shapes != null) shapes.dispose();
        font.dispose();
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }

    private static float smoothstep(float t) { return t * t * (3f - 2f * t); }
}
