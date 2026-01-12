package io.github.therealsegfault.projectbeatsgdx.core;

/**
 * Engine clock source.
 * Desktop runtime can implement with LibGDX music position or a DSP-like clock.
 */
public interface Clock {
    /** Seconds since song start. */
    double nowSeconds();
}
