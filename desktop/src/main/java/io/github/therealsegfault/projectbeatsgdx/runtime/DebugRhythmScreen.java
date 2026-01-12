package io.github.therealsegfault.projectbeatsgdx.runtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.therealsegfault.projectbeatsgdx.core.EngineConfig;
import io.github.therealsegfault.projectbeatsgdx.core.EngineCore;

import java.util.Locale;

public final class DebugRhythmScreen extends ScreenAdapter {

    private final ProjectBeatsGame game;

    private final OrthographicCamera cam = new OrthographicCamera();
    private final BitmapFont font = new BitmapFont(); // default, replace later
    private final GlyphLayout layout = new GlyphLayout();

    private Music music;
    private boolean started = false;

    // Engine core (portable)
    private final EngineConfig cfg = new EngineConfig();
    private final EngineCore engine = new EngineCore(cfg, 1337L);

    // Placeholder song config (manual math)
    private final double bpm = 120.0;

    public DebugRhythmScreen(ProjectBeatsGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        cam.setToOrtho(false, 1280, 720);

        // NOTE: Put an actual audio file in /assets/music/test.ogg (or change path).
        // LibGDX loads "internal" from classpath; in this scaffold we copy /assets into the jar under /assets/.
        // So the path is "assets/music/test.ogg".
        //
        // If the file doesn't exist, you'll see a log error and the screen will still run without audio.
        try {
            music = Gdx.audio.newMusic(Gdx.files.internal("assets/music/test.ogg"));
            music.setLooping(false);
            music.setVolume(0.9f);
        } catch (Exception e) {
            Gdx.app.error("ProjectBeats", "Failed to load test song. Put a file at assets/music/test.ogg", e);
            music = null;
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        double t = (music != null && started) ? music.getPosition() : 0.0;

        // Spawn a simple beat grid a bit ahead (demonstrates generator, not final design)
        if (started) {
            engine.generateGrid(bpm, t, t + cfg.spawnLookaheadSeconds);
        }

        // Input: WASD -> hit lane 0..3
        if (started) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.W)) hitLane(0, t);
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) hitLane(1, t);
            if (Gdx.input.isKeyJustPressed(Input.Keys.S)) hitLane(2, t);
            if (Gdx.input.isKeyJustPressed(Input.Keys.D)) hitLane(3, t);
        }

        // Start/stop
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            toggleStart();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restart();
        }

        // Cleanup judged notes to keep list sane (placeholder keep window)
        if (started) {
            engine.cleanupJudged(t, 1.0);
        }

        // Draw debug overlay
        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();

        String header = "ProjectBeats (LibGDX Maven)  |  SPACE=start/stop  R=restart  WASD=hit";
        font.draw(game.batch, header, 24, 700);

        String status = String.format(Locale.US,
                "t=%.3f  bpm=%.1f  activeNotes=%d  score=%d  combo=%d",
                t, bpm, engine.aliveCountAll(), engine.score().score, engine.score().combo);
        font.draw(game.batch, status, 24, 670);

        if (!started) {
            font.draw(game.batch, "Press SPACE to start.", 24, 635);
        }

        game.batch.end();
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
        started = false;
        if (music != null) {
            music.stop();
        }
    }

    private void hitLane(int lane, double t) {
        EngineCore.LiveNote n = engine.findEarliestLaneNote(lane);
        if (!engine.laneIsHittable(n, t)) return;
        engine.judge(n, t);
    }

    @Override
    public void resize(int width, int height) {
        cam.setToOrtho(false, width, height);
        cam.update();
    }

    @Override
    public void dispose() {
        if (music != null) music.dispose();
        font.dispose();
    }
}
