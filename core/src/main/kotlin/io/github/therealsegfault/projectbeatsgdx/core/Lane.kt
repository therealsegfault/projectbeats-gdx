package io.github.therealsegfault.projectbeatsgdx.core

enum class Lane(val id: Int) {
  UP(0),
  LEFT(1),
  DOWN(2),
  RIGHT(3);

  companion object {
    fun fromId(id: Int): Lane {
      return when (id) {
        0 -> UP
        1 -> LEFT
        2 -> DOWN
        3 -> RIGHT
        else -> RIGHT
      }
    }
  }
}
