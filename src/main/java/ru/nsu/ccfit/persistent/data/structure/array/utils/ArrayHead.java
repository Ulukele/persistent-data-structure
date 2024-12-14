package ru.nsu.ccfit.persistent.data.structure.array.utils;

public class ArrayHead<E> {

    /**
     * Корневой узел массива
     */
    private final ArrayNode<E> root;

    /**
     * Текущий размер массива
     */
    private int size = 0;

    public ArrayHead() {
        this.root = new ArrayNode<>();
    }

    public ArrayHead(ArrayHead<E> other) {
        this.root = new ArrayNode<>(other.root);
        this.size = other.size;
    }

    public ArrayHead(ArrayHead<E> other, Integer sizeDelta) {
        this.root = new ArrayNode<>(other.root);
        this.size = other.size + sizeDelta;
    }

    public ArrayHead(ArrayHead<E> other, Integer newSize, Integer maxIndex) {
        this.root = new ArrayNode<>(other.root, maxIndex);
        this.size = newSize;
    }

    public ArrayNode<E> getRoot() {
        return root;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return String.format("%09x %d", root.hashCode(), size);
    }
}
