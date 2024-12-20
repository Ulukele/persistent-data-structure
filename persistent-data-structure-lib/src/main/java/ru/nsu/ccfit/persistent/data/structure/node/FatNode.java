package ru.nsu.ccfit.persistent.data.structure.node;

import java.util.TreeMap;

public class FatNode<T, V extends Comparable<V>> {

    private TreeMap<V, SlimNode<T, V>> versions;

    private FatNode() {
        this.versions = new TreeMap<>();
    }

    public FatNode(V version, SlimNode<T, V> node) {
        this();
        versions.put(version, node);
    }

    public FatNode<T, V> getLeft(V version) {
        var versionState = versions.floorEntry(version);
        return versionState == null ? null : versionState.getValue().getLeft();
    }

    public FatNode<T, V> getRight(V version) {
        var versionState = versions.floorEntry(version);
        return versionState == null ? null : versionState.getValue().getRight();
    }

    public T getValue(V version) {
        var versionState = versions.floorEntry(version);
        return versionState == null ? null : versionState.getValue().getValue();
    }

    public void updateLeft(V version, FatNode<T, V> left) {
        var versionState = versions.floorEntry(version).getValue();
        var newState = new SlimNode<>(
                versionState.getValue(),
                left,
                versionState.getRight()
        );
        versions.put(version, newState);
    }

    public void updateRight(V version, FatNode<T, V> right) {
        var versionState = versions.floorEntry(version).getValue();
        var newState = new SlimNode<>(
                versionState.getValue(),
                versionState.getLeft(),
                right
        );
        versions.put(version, newState);
    }

    public void updateValue(V version, T value) {
        var versionState = versions.floorEntry(version).getValue();
        var newState = new SlimNode<>(
                value,
                versionState.getLeft(),
                versionState.getRight()
        );
        versions.put(version, newState);
    }

}
