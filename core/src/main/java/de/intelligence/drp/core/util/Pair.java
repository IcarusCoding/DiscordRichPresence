package de.intelligence.drp.core.util;

public final class Pair<L, R> {

    private L l;
    private R r;

    private Pair(L l, R r) {
        this.l = l;
        this.r = r;
    }

    public static <L, R> Pair<L, R> of(L l, R r) {
        return new Pair<>(l, r);
    }

    public L getLeft() {
        return this.l;
    }

    public void setLeft(L l) {
        this.l = l;
    }

    public R getRight() {
        return this.r;
    }

    public void setRight(R r) {
        this.r = r;
    }

}
