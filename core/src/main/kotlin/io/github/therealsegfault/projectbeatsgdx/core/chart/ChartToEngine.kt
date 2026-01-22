package io.github.therealsegfault.projectbeatsgdx.core.chart

import io.github.therealsegfault.projectbeatsgdx.core.NoteEvent

/**
 * Converts a [Chart] into engine [NoteEvent]s.
 *
 * Important: we assign a stable seq by sorting notes by (timeSeconds, lane).
 */
object ChartToEngine {
    fun toNoteEvents(chart: Chart): List<NoteEvent> {
        val sorted = chart.notes
            .sortedWith(compareBy<ChartNote>({ it.timeSeconds }, { it.lane }))

        return sorted.mapIndexed { idx, n ->
            NoteEvent(
                hitTimeSeconds = n.timeSeconds.toDouble(),
                lane = n.lane,
                seq = idx
            )
        }
    }
}
