package ru.nsu.ccfit.persistent.data.structure.map;

import ru.nsu.ccfit.persistent.data.structure.PersistentStructure;
import ru.nsu.ccfit.persistent.data.structure.node.FatNode;
import ru.nsu.ccfit.persistent.data.structure.node.SlimNode;

import java.beans.BeanProperty;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FatNodePersistentMap<K, V> implements Map<K, V>, PersistentStructure {

    /**
     * Ассоциативный массив: версия -> корень двоичного дерева.
     */
    private final HashMap<Long, FatNode<Map.Entry<K, V>, Long>> roots;

    /**
     * Текущая версия структуры.
     */
    private Long currentVersion;

    /**
     * Последняя доступная версия структуры.
     */
    private Long lastVersion;

    public FatNodePersistentMap() {
        this.roots = new HashMap<>();
        this.currentVersion = 0L;
    }

    @Override
    public int size() {
        return entrySet().size();
    }

    @BeanProperty
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return entrySet().stream()
                .anyMatch(e -> e.getValue().equals(value));
    }

    @Override
    public V get(Object key) {
        var entry = getEntry(key);
        return entry == null ? null : entry.getValue(currentVersion).getValue();
    }

    @Override
    public V put(K key, V value) {
        Objects.requireNonNull(key);
        @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
        var previousRoot = getCurrentRoot();
        var entryWithParent = getEntryWithParent(key, false);
        doBeforeModifyAction();
        var entry = entryWithParent.get(0);
        var parent = entryWithParent.get(1);
        final FatNode<Map.Entry<K, V>, Long> newRoot;

        V oldValue = null;
        if (entry != null) {
            oldValue = entry.getValue(currentVersion).getValue();
            newRoot = previousRoot;
            entry.updateValue(currentVersion, Map.entry(key, value));
        } else if (parent == null) {
            newRoot = new FatNode<>(
                    currentVersion,
                    new SlimNode<>(
                            Map.entry(key, value),
                            null,
                            null)
            );
        } else {
            newRoot = previousRoot;
            var compareResult = k.compareTo(
                    parent.getValue(currentVersion).getKey()
            );
            var newNode = new FatNode<>(
                    currentVersion,
                    new SlimNode<>(Map.entry(key, value), null, null)
            );
            if (compareResult > 0) {
                parent.updateRight(currentVersion, newNode);
            } else {
                parent.updateLeft(currentVersion, newNode);
            }
        }
        roots.put(currentVersion, newRoot);
        return oldValue;
    }

    @Override
    public V remove(Object key) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (final var entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        doBeforeModifyAction();
        roots.put(currentVersion, null);
    }

    @Override
    public Set<K> keySet() {
        return entrySet().stream()
                .map(Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<V> values() {
        return entrySet().stream()
                .map(Entry::getValue)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return getRootEntrySet(getCurrentRoot(), new HashSet<>());
    }

    @Override
    public void undo() {
        if (currentVersion > 0) {
            this.currentVersion--;
        }
    }

    @Override
    public void redo() {
        if (currentVersion < lastVersion) {
            currentVersion++;
        }
    }

    private FatNode<Map.Entry<K, V>, Long> getEntry(Object key) {
        var entryWithParent = getEntryWithParent(key, true);
        if (entryWithParent == null || entryWithParent.isEmpty()) {
            return null;
        }
        return entryWithParent.get(0);
    }

    private List<FatNode<Entry<K, V>, Long>> getEntryWithParent(
            Object key,
            boolean exact) {
        Objects.requireNonNull(key);
        @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
        var entry = getCurrentRoot();
        FatNode<Map.Entry<K, V>, Long> previous = null;
        while (entry != null) {
            var internalEntry = entry.getValue(currentVersion);
            if (internalEntry == null) {
                return null;
            }
            var entryKey = internalEntry.getKey();
            var compareResult = k.compareTo(entryKey);
            if (compareResult == 0) {
                return Stream.of(entry, previous).toList();
            }
            previous = entry;
            if (compareResult > 0) {
                entry = entry.getRight(currentVersion);
            } else {
                entry = entry.getLeft(currentVersion);
            }
        }
        if (exact) {
            return null;
        } else {
            return Stream.of(entry, previous).toList();
        }
    }

    private Set<Entry<K, V>> getRootEntrySet(
            FatNode<Entry<K, V>, Long> root,
            Set<Entry<K, V>> internalEntrySet) {
        if (root == null) {
            return internalEntrySet;
        }
        getRootEntrySet(root.getLeft(currentVersion), internalEntrySet);
        getRootEntrySet(root.getRight(currentVersion), internalEntrySet);
        internalEntrySet.add(root.getValue(currentVersion));
        return internalEntrySet;
    }

    private FatNode<Map.Entry<K, V>, Long> getCurrentRoot() {
        return roots.get(currentVersion);
    }

    private void doBeforeModifyAction() {
        currentVersion++;
        lastVersion = currentVersion;
        roots.remove(currentVersion);
    }

}
