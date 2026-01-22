package io.github.therealsegfault.projectbeatsgdx.core.chart

/**
 * Minimal chart format that keeps the "engine" decoupled from parsing.
 * Parsing (JSON, osu!, etc.) should live in a platform module.
 */
data class Chart(
    val id: String,
    val title: String,
    val artist: String,
    /** Relative path under assets/, e.g. "music/test.ogg" */
    val audio: String,
    /** Seconds a note is visible before its hitTime. */
    val approachTimeSeconds: Float,
    val lanes: Int,
    val notes: List<ChartNote>
)

data class ChartNote(
    /** Absolute hit time in seconds. */
    val timeSeconds: Float,
    /** 0-based lane index. */
    val lane: Int,
    val type: ChartNoteType = ChartNoteType.TAP
)

enum class ChartNoteType {
    TAP,
    // Reserved for later (flick, hold, etc.)
}
