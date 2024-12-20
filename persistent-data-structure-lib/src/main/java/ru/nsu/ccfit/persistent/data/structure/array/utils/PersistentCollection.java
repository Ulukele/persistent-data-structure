package ru.nsu.ccfit.persistent.data.structure.array.utils;

import ru.nsu.ccfit.persistent.data.structure.PersistentStructure;

/**
 * Базовый класс коллекции с поддержкой возврата к предыдущему состоянию.
 */
public abstract class PersistentCollection implements PersistentStructure {

    /**
     * Глубина структуры данных.
     */
    public final int depth;

    /**
     * Маска, используемая для вычисления индексов в узлах.
     */
    public final int mask;

    /**
     * Максимальный размер коллекции.
     */
    public final int maxSize;

    /**
     * Количество бит, используемых для представления каждого уровня в структуре
     * данных.
     */
    public final int bitPerEdge;

    /**
     * Ширина структуры данных, равна 2^bitPerEdge
     */
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
