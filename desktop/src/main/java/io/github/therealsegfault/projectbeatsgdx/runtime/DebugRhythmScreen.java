package io.github.therealsegfault.projectbeatsgdx.runtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.therealsegfault.projectbeatsgdx.core.EngineConfig;
import io.github.therealsegfault.projectbeatsgdx.core.EngineCore;
import io.github.therealsegfault.projectbeatsgdx.core.HitWindows;
import io.github.therealsegfault.projectbeatsgdx.core.LiveNoteView;
import io.github.therealsegfault.projectbeatsgdx.runtime.chart.ChartLoader;
import io.github.therealsegfault.projectbeatsgdx.core.chart.Chart;
import io.github.therealsegfault.projectbeatsgdx.core.chart.ChartNote;

public class DebugRhythmScreen implements Screen {
    private final ProjectBeatsGame game;
    private SpriteBatch batch;
    private BitmapFont font;
    private EngineCore engine;

    public DebugRhythmScreen(ProjectBeatsGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();

        Chart loadedChart = ChartLoader.loadInternal("charts/demo.json");

        EngineConfig cfg = new EngineConfig(
                loadedChart.getLanes(),
                loadedChart.getApproachTimeSeconds(),
                1.5,
                0.04,
                128,
                64,
                new HitWindows(0.04, 0.08, 0.12, 0.16, 0.20)
        );

        engine = new EngineCore(cfg, System.currentTimeMillis());

        for (ChartNote note : loadedChart.getNotes()) {
            engine.spawnNote(note.getLane(), note.getTimeSeconds());
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        double tNow = System.currentTimeMillis() / 1000.0;
        engine.updateAutoMiss(tNow);
        engine.cleanupJudged(tNow, 2.0);

        batch.begin();
        for (LiveNoteView note : engine.notesSnapshot()) {
            if (note.getJudged()) continue;
            font.draw(batch, "Note " + note.getLane(), 100, 100 + note.getLane() * 20);
        }

        font.draw(batch, "Score: " + engine.score().getScore() + " Combo: " + engine.score().getCombo(), 10, 460);
        batch.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        batch.dispose();
        font.dispose();
    }
}