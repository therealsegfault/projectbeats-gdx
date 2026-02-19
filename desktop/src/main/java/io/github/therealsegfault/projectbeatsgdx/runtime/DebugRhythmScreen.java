io.github.therealsegfault.projectbeatsgdx.runtime;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.therealsegfault.projectbeatsgdx.core.EngineConfig;
import io.github.therealsegfault.projectbeatsgdx.core.EngineCore;
import io.github.therealsegfault.projectbeatsgdx.core.HitWindows;
import io.github.therealsegfault.projectbeatsgdx.core.LiveNoteView;
import io.github.therealsegfault.projectbeatsgdx.core.NoteEvent;
import io.github.therealsegfault.projectbeatsgdx.core.chart.ChartToEngine;
import io.github.therealsegfault.projectbeatsgdx.runtime.chart.ChartLoader;
import io.github.therealsegfault.projectbeatsgdx.core.chart.Chart;
import java.util.List;
public class DebugRhythmScreen implements Screen {
private final ProjectBeatsGame game;
private SpriteBatch batch;
private BitmapFont font;
private EngineCore engine;
private ShapeRenderer shapeRenderer;
private double startTime;
public DebugRhythmScreen(ProjectBeatsGame game) {
this.game = game;
}
@Override
public void show() {
batch = new SpriteBatch();
font = new BitmapFont();
shapeRenderer = new ShapeRenderer();
Chart loadedChart = ChartLoader.loadInternal("charts/demo.json");
EngineConfig cfg = new EngineConfig(
loadedChart.getLanes(),
(double) loadedChart.getApproachTimeSeconds(),
1.5,
0.04,
128,
64,
new HitWindows(0.04, 0.08, 0.12, 0.16, 0.20)
);
engine = new EngineCore(cfg, System.currentTimeMillis());
List<NoteEvent> noteEvents = ChartToEngine.toNoteEvents(loadedChart);
for (NoteEvent ne : noteEvents) {
engine.spawnNote(ne.getLane(), ne.getTimeSeconds());
}
startTime = System.currentTimeMillis() / 1000.0;
}
private void attemptJudge(int lane) {
double tNow = System.currentTimeMillis() / 1000.0 - startTime;
engine.tryJudgeLane(lane, tNow);
}
@Override
public void render(float delta) {
Gdx.gl.glClearColor(0, 0, 0, 1);
Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
double tNow = System.currentTimeMillis() / 1000.0 - startTime;
engine.updateAutoMiss(tNow);
engine.cleanupJudged(tNow, 2.0);
if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) attemptJudge(0);
if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) attemptJudge(1);
if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) attemptJudge(2);
if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) attemptJudge(3);
shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
// hit bars
shapeRenderer.setColor(Color.YELLOW);
for (int l = 0; l < 4; l++) {
float x = 300 + l * 80;
shapeRenderer.rect(x - 25, 95, 50, 10);
}
// notes (moving)
shapeRenderer.setColor(Color.WHITE);
for (LiveNoteView note : engine.notesSnapshot()) {
if (note.getJudged()) continue;
double progress = (tNow - note.getSpawnTimeSeconds()) / note.getApproachSeconds();
progress = Math.max(0.0, Math.min(1.0, progress));
float x = 300 + note.getLane() * 80;
float y = 500 - (float) progress * 400f;
shapeRenderer.rect(x - 15, y - 15, 30, 30);
}
shapeRenderer.end();
batch.begin();
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
shapeRenderer.dispose();
}
}
