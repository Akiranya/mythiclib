package io.lumine.mythic.lib.util;

import java.util.Objects;

public class Triplet<L, C, R> {
    private final L left;
    private final C center;
    private final R right;

    private Triplet(L left, C center, R right) {
        this.left = left;
        this.center = center;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public static <L, C, R> Triplet<L, C, R> of(L left, C center, R right) {
        return new Triplet<>(left, center, right);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;
        return Objects.equals(left, triplet.left) && Objects.equals(center, triplet.center) && Objects.equals(right, triplet.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, center, right);
    }
}
