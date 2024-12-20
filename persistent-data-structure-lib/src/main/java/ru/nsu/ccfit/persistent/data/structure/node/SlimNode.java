package ru.nsu.ccfit.persistent.data.structure.node;

public class SlimNode<T, V extends Comparable<V>> {

    private final T value;

    private final FatNode<T, V> left;

    private final FatNode<T, V> right;

    public SlimNode(
            T value,
            FatNode<T, V> left,
            FatNode<T, V> right) {
        this.value = value;
        this.left = left;
        this.right = right;
    }

    public T getValue() {
        return value;
    }

    public FatNode<T, V> getLeft() {
        return left;
    }

    public FatNode<T, V> getRight() {
        return right;
    }

}
