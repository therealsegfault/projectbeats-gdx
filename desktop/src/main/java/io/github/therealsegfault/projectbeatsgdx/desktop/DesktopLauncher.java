package io.github.therealsegfault.projectbeatsgdx.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.therealsegfault.projectbeatsgdx.runtime.ProjectBeatsGame;

public final class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("ProjectBeats (LibGDX)");
        cfg.setWindowedMode(1280, 720);
        cfg.useVsync(true);
        cfg.setForegroundFPS(240);

        new Lwjgl3Application(new ProjectBeatsGame(), cfg);
    }
}
