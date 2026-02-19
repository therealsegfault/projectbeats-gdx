package io.github.therealsegfault.projectbeatsgdx.core
import io.github.therealsegfault.projectbeatsgdx.core.HitWindows
class EngineConfig(
val lanes: Int,
val approachSeconds: Double,
val spawnLookaheadSeconds: Double,
val minNoteGapSeconds: Double,
val maxAlive: Int,
val maxAlivePerLane: Int,
val windows: HitWindows
) {
val chartId: String? = null
val audioPath: String? = null
// Configured from chart (invariants: approach = difficulty/chart, NOT hardcoded)
val approachTimeSeconds: Double = approachSeconds
val driftDebtMultiplier: Double = 1.0
val driftPenaltyPerMiss: Double = 0.05
}
</DOCUMENT>
<DOCUMENT filename="EngineCore.kt">
package io.github.therealsegfault.projectbeatsgdx.core
import kotlin.math.abs
data class LiveNoteView(
val lane: Int,
val timeSeconds: Double,
val spawnTimeSeconds: Double,
val approachSeconds: Double,
val judged: Boolean,
val judgement: Judgement?
)
class EngineCore(
private val cfg: EngineConfig,
seed: Long
) {
data class LiveNote(
val lane: Int,
val timeSeconds: Double,
val seq: Long,
val spawnTimeSeconds: Double,
val approachSeconds: Double
) {
var judged: Boolean = false
var judgement: Judgement? = null
var judgedAtSeconds: Double = 0.0
}
private var nextSeq: Long = seed
private val notes: MutableList<LiveNote> = ArrayList()
private val score: ScoreState = ScoreState()
fun reset() {
notes.clear()
score.score = 0
score.combo = 0
score.drift = 0.0
score.driftDebt = 0.0
}
fun notes(): List<LiveNote> = notes
fun score(): ScoreState = score
fun aliveCountAll(): Int = notes.count { !it.judged }
fun aliveCountInLane(lane: Int): Int = notes.count { !it.judged && it.lane == lane }
fun spawnNote(lane: Int, timeSeconds: Double) {
val spawn = timeSeconds - cfg.approachTimeSeconds
val n = LiveNote(
lane = lane,
timeSeconds = timeSeconds,
seq = nextSeq++,
spawnTimeSeconds = spawn,
approachSeconds = cfg.approachTimeSeconds
)
notes.add(n)
}
fun findEarliestLaneNote(lane: Int): LiveNote? {
var best: LiveNote? = null
for (n in notes) {
if (n.judged) continue
if (n.lane != lane) continue
if (best == null || n.timeSeconds < best!!.timeSeconds ||
(n.timeSeconds == best!!.timeSeconds && n.seq < best!!.seq)
) {
best = n
}
}
return best
}
fun laneIsHittable(n: LiveNote?, tNow: Double): Boolean {
if (n == null) return false
return abs(tNow - n.timeSeconds) <= cfg.windows.miss
}
fun judge(n: LiveNote?, tNow: Double): Judgement {
if (n == null) return Judgement.MISS
if (n.judged) return n.judgement ?: Judgement.MISS
val dt = abs(tNow - n.timeSeconds)
val j = when {
dt <= cfg.windows.perfect -> Judgement.PERFECT
dt <= cfg.windows.good -> Judgement.COOL
dt <= cfg.windows.safe -> Judgement.FINE
dt <= cfg.windows.sad -> Judgement.SAD
else -> Judgement.MISS
}
n.judged = true
n.judgement = j
n.judgedAtSeconds = tNow
applyScore(j)
return j
}
fun updateAutoMiss(tNow: Double) {
for (n in notes) {
if (n.judged) continue
if (tNow > n.timeSeconds + cfg.windows.miss) {
judge(n, n.timeSeconds + cfg.windows.miss + 0.0001)
}
}
}
fun cleanupJudged(tNow: Double, keepSeconds: Double) {
var i = 0
while (i < notes.size) {
val n = notes[i]
if (n.judged && tNow - n.judgedAtSeconds > keepSeconds) {
notes.removeAt(i)
} else {
i++
}
}
}
/** Input-safe: only judge if hittable (prevents early-press MISS consuming future note) */
fun tryJudgeLane(lane: Int, tNow: Double): Judgement {
val n = findEarliestLaneNote(lane)
if (n == null || !laneIsHittable(n, tNow)) return Judgement.MISS
return judge(n, tNow)
}
private fun applyScore(j: Judgement) {
when (j) {
Judgement.PERFECT -> {
score.score += 300
score.combo += 1
}
Judgement.COOL -> {
score.score += 200
score.combo += 1
}
Judgement.FINE -> {
score.score += 100
score.combo += 1
}
Judgement.SAD -> {
score.score += 50
score.combo = 0
}
Judgement.MISS -> {
score.combo = 0
val mult = if (score.driftDebt > 0.0) cfg.driftDebtMultiplier else 1.0
score.drift += cfg.driftPenaltyPerMiss * mult
}
}
}
fun notesSnapshot(): List<LiveNoteView> = notes.map {
LiveNoteView(
lane = it.lane,
timeSeconds = it.timeSeconds,
spawnTimeSeconds = it.spawnTimeSeconds,
approachSeconds = it.approachSeconds,
judged = it.judged,
judgement = it.judgement
)
}
}
