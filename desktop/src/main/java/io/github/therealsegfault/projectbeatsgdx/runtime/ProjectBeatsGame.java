package io.github.therealsegfault.projectbeatsgdx.runtime;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public final class ProjectBeatsGame extends Game {

    public AssetManager assets;
    public SpriteBatch batch;

    @Override
    public void create() {
        assets = new AssetManager();
        batch = new SpriteBatch();

        // Minimal screen that proves the loop + input is working.
        setScreen(new DebugRhythmScreen(this));
        Gdx.app.log("ProjectBeats", "create() OK");
    }

    @Override
    public void dispose() {
        super.dispose();
        if (batch != null) batch.dispose();
        if (assets != null) assets.dispose();
    }
}
