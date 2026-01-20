package io.github.therealsegfault.projectbeatsgdx.core

/**
 * Hit windows are in seconds (absolute time distance from hitTime).
 *
 * perfect <= good <= safe <= sad <= miss
 */
data class HitWindows(
  var perfect: Double,
  var good: Double,
  var safe: Double,
  var sad: Double,
  var miss: Double
)
