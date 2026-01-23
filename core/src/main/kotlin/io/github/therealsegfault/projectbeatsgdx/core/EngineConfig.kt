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

  // Engine-tuned parameters
  val approachTimeSeconds: Double = 2.0
  val driftDebtMultiplier: Double = 1.0
  val driftPenaltyPerMiss: Double = 0.05
}