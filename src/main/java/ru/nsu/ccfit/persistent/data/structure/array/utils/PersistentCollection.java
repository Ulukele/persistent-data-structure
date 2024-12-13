package ru.nsu.ccfit.persistent.data.structure.array.utils;

import ru.nsu.ccfit.persistent.data.structure.PersistentStructure;

/**
 * Базовый класс коллекции с поддержкой возврата к предыдущему состоянию.
 */
public abstract class PersistentCollection implements PersistentStructure {

    public final int depth;
    public final int mask;
    public final int maxSize;
    public final int bitPerEdge;
    public final int width;

    protected PersistentCollection(int depth, int bitPerEdge) {
        this.depth = depth;
        this.bitPerEdge = bitPerEdge;

        mask = (int) Math.pow(2, bitPerEdge) - 1;
        maxSize = (int) Math.pow(2, bitPerEdge * depth);

        width = (int) Math.pow(2, bitPerEdge);
    }

    protected static double log(int n, int newBase) {
        return (Math.log(n) / Math.log(newBase));
    }
}
