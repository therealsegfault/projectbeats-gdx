package io.github.therealsegfault.projectbeatsgdx.core

/**
 * A chart note (immutable). Runtime notes are EngineCore.LiveNote.
 */
data class NoteEvent(
  val timeSeconds: Double,
  val lane: Int,
  val seq: Long
)
