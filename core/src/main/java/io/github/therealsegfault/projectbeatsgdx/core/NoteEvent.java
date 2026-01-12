package io.github.therealsegfault.projectbeatsgdx.core;

/** Immutable note descriptor: lane + hit time + stable seq ordering. */
public final class NoteEvent {
    public final int lane;          // 0..3
    public final double timeSeconds;
    public final int seq;

    public NoteEvent(int lane, double timeSeconds, int seq) {
        this.lane = lane;
        this.timeSeconds = timeSeconds;
        this.seq = seq;
    }
}
