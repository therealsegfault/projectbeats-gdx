package io.github.therealsegfault.projectbeatsgdx.core

/**
 * Mutable runtime score.
 *
 * Added for Rhythm-RPG direction:
 * - drift: accumulated instability caused by misses
 * - driftDebt: when > 0, miss penalties are multiplied ("drift debt doubles drift penalty")
 */
class ScoreState {
  var score: Int = 0
  var combo: Int = 0

  var drift: Double = 0.0
  var driftDebt: Double = 0.0
}



  fun getScore(): Int = score
  fun getCombo(): Int = combo
  fun getDrift(): Double = drift
  fun getDriftDebt(): Double = driftDebt

}