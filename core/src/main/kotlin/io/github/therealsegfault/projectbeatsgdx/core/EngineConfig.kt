package io.github.therealsegfault.projectbeatsgdx.core

class EngineConfig {
  var difficulty: Difficulty = Difficulty.NORMAL
  var approachTimeSeconds: Double = 2.8
  var windows: HitWindows = HitWindows(0.08, 0.18, 0.26, 0.33, 0.40)

  var maxAlive: Int = 8
  var maxAlivePerLane: Int = 1

  var spawnLookaheadSeconds: Double = 8.0
  var minNoteGapSeconds: Double = 0.22

  var lanes: Int = 4

  // Rhythm-RPG knobs
  var driftPenaltyPerMiss: Double = 1.0
  var driftDebtMultiplier: Double = 2.0
}
