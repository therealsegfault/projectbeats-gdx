package io.github.therealsegfault.projectbeatsgdx.runtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.therealsegfault.projectbeatsgdx.core.EngineConfig;
import io.github.therealsegfault.projectbeatsgdx.core.EngineCore;
import io.github.therealsegfault.projectbeatsgdx.core.HitWindows;
import io.github.therealsegfault.projectbeatsgdx.core.chart.ChartNoteType;
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
                loadedChart.getLaneCount(),
                loadedChart.getApproachSeconds(),
                1.5,
                0.04,
                128,
                64,
                HitWindows.Default()
        );

        engine = new EngineCore(cfg, System.currentTimeMillis());

        for (ChartNote note : loadedChart.getNotes()) {
            engine.spawnNote(note.getTime(), note.getLane(), ChartNoteType.TAP);
        }

        engine.start();
        engine.end();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        engine.update(System.currentTimeMillis() / 1000.0, delta);

        batch.begin();
        for (EngineCore.LiveNote note : engine.notesSnapshot()) {
            if (note.isJudged()) continue;
            font.draw(batch, "Note " + note.lane, 100, 100 + note.lane * 20);
        }

        font.draw(batch, "Score: " + engine.getScore().score + " Combo: " + engine.getScore().combo, 10, 460);
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