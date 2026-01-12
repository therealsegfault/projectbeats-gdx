package io.github.therealsegfault.projectbeatsgdx.core;

public enum Lane {
    W(0), A(1), S(2), D(3);

    public final int id;
    Lane(int id) { this.id = id; }

    public static Lane fromId(int id) {
        return switch (id) {
            case 0 -> W;
            case 1 -> A;
            case 2 -> S;
            case 3 -> D;
            default -> throw new IllegalArgumentException("Invalid lane id: " + id);
        };
    }
}
