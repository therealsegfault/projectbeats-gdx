package io.github.therealsegfault.projectbeatsgdx.runtime.chart;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import io.github.therealsegfault.projectbeatsgdx.core.chart.Chart;
import io.github.therealsegfault.projectbeatsgdx.core.chart.ChartNote;
import io.github.therealsegfault.projectbeatsgdx.core.chart.ChartNoteType;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads the minimal chart JSON format from assets.
 *
 * This stays in the desktop/runtime module so the pure engine (core) doesn't depend on libGDX.
 */
public final class ChartLoader {
    private ChartLoader() {}

    public static Chart loadInternal(String internalPath) {
        FileHandle fh = Gdx.files.internal(internalPath);
        JsonValue root = new JsonReader().parse(fh);

        String id = root.getString("id");
        String title = root.getString("title", "");
        String artist = root.getString("artist", "");
        String audio = root.getString("audio", "");
        float approachSeconds = root.getFloat("approachSeconds", 1.6f);

        List<ChartNote> notes = new ArrayList<>();
        JsonValue notesArr = root.get("notes");
        if (notesArr != null) {
            for (JsonValue n = notesArr.child; n != null; n = n.next) {
                float t = n.getFloat("t");
                int lane = n.getInt("lane");
                notes.add(new ChartNote(t, lane));
            }
        }

        return new Chart(id, title, artist, audio, approachSeconds, notes);
    }
}
