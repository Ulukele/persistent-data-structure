package ru.nsu.ccfit.persistent.data.structure.map;

import ru.nsu.ccfit.persistent.data.structure.PersistentStructure;
import ru.nsu.ccfit.persistent.data.structure.node.ModificationBox;
import ru.nsu.ccfit.persistent.data.structure.node.ModificationBoxNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * Ассоциативный массив поддерживающий операции возврата к предыдущему состоянию.
 *
 * @param <K>
 *     Тип ключа ассоциативного массива.
 * @param <V>
 *     Тип значения ассоциативного массива.
 */
public class PersistentMap<K, V> implements Map<K, V>, PersistentStructure {

    /**
     * Ассоциативный массив: версия -> корень двоичного дерева.
     */
    private final HashMap<Long, ModificationBoxNode<Map.Entry<K, V>, Long>> roots;

    /**
     * Текущая версия структуры.
     */
    private Long currentVersion;

    /**
     * Последняя доступная версия структуры.
     */
    private Long lastVersion;

    /**
     * Флаг использования мемоизации (по умолчанию false).
     */
    private final boolean useMemoize;

    /**
     * Мемоизированный набор пар ключ - значение (используется при useMemoize=true).
     */
    private Set<Entry<K, V>> memoizedEntrySet;

    public PersistentMap() {
        this(false);
    }

    public PersistentMap(boolean useMemoize) {
        this.useMemoize = useMemoize;
        this.roots = new HashMap<>();
        this.currentVersion = 0L;
        this.memoizedEntrySet = null;
    }

    @Override
    public void undo() {
        if (currentVersion > 0) {
            this.currentVersion--;
            deleteMemoized();
        }
    }

    @Override
    public void redo() {
        if (currentVersion < lastVersion) {
            currentVersion++;
            deleteMemoized();
        }
    }

    @Override
    public int size() {
        return entrySet().size();
    }

    @Override
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
        var root = getCurrentRoot();
        doBeforeModifyAction();
        if (root == null) {
            roots.put(
                    currentVersion,
                    new ModificationBoxNode<>(
                            null,
                            null,
                            Map.entry(key, value)
                    )
            );
            return null;
        }
        List<ModificationBoxNode<Entry<K, V>, Long>> path = new ArrayList<>();
        List<Boolean> isLeftMove = new ArrayList<>();
        boolean found = false;
        var entry = root;
        while (entry != null) {
            path.add(entry);
            var internalEntry = entry.getValue(currentVersion - 1);
            if (internalEntry == null) {
                throw new IllegalArgumentException("entry value can not be null");
            }
            var entryKey = internalEntry.getKey();
            var compareResult = k.compareTo(entryKey);
            if (compareResult == 0) {
                found = true;
                break;
            }
            if (compareResult > 0) {
                isLeftMove.add(Boolean.FALSE);
                entry = entry.getRight(currentVersion - 1);
            } else {
                isLeftMove.add(Boolean.TRUE);
                entry = entry.getLeft(currentVersion - 1);
            }
        }
        var last = path.getLast();
        final ModificationBoxNode<Entry<K, V>, Long> newNode;
        if (found) {
            newNode = last.modify(
                    ModificationBox.createValueModification(
                            currentVersion,
                            Map.entry(key, value)
                    )
            );
            if (newNode == last) {
                return last.getValue(currentVersion - 1).getValue();
            }
            path.remove(path.size() - 1);
        } else {
            newNode = new ModificationBoxNode<>(
                    null,
                    null,
                    Map.entry(key, value)
            );
        }
        var newRoot = upperLink(
                currentVersion,
                path,
                isLeftMove,
                newNode
        );
        if (newRoot == null) {
            roots.put(currentVersion, roots.get(currentVersion - 1));
        } else {
            roots.put(currentVersion, newRoot);
        }
        return null;
    }

    private ModificationBoxNode<Entry<K, V>, Long> upperLink(
            Long version,
            List<ModificationBoxNode<Entry<K, V>, Long>> path,
            List<Boolean> isLeftMove,
            ModificationBoxNode<Entry<K, V>, Long> newNode) {
        ModificationBoxNode<Entry<K, V>, Long> lastCreated = newNode;
        for (int i = path.size() - 1; i >= 0; --i) {
            var isLeft = isLeftMove.get(i);
            var current = path.get(i);
            var modification = current.getModificationBox();
            if (modification == null) {
                current.modify(
                        isLeft
                                ? ModificationBox.createLeftModification(version, lastCreated)
                                : ModificationBox.createRightModification(version, lastCreated)
                );
                return null;
            } else {
                var rawNewNode = new ModificationBoxNode<>(
                        current.getLeft(version),
                        current.getRight(version),
                        current.getValue(version)
                ).modify(
                        isLeft
                                ? ModificationBox.createLeftModification(version, lastCreated)
                                : ModificationBox.createRightModification(version, lastCreated)
                );
                lastCreated = new ModificationBoxNode<>(
                        rawNewNode.getLeft(version),
                        rawNewNode.getRight(version),
                        rawNewNode.getValue(version)
                );
            }
        }
        return lastCreated;
    }

    @Override
    public V remove(Object key) {
        var entry = getEntry(key);
        if (entry == null) {
            return null;
        }
        // not implemented
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
        if (useMemoize && memoizedEntrySet != null) {
            return memoizedEntrySet;
        }
        var result = getRootEntrySet(getCurrentRoot(), new HashSet<>());
        if (useMemoize) {
            memoizedEntrySet = result;
        }
        return result;
    }

    private Set<Entry<K, V>> getRootEntrySet(
            ModificationBoxNode<Map.Entry<K, V>, Long> root,
            Set<Entry<K, V>> internalEntrySet) {
        if (root == null) {
            return internalEntrySet;
        }
        getRootEntrySet(root.getLeft(currentVersion), internalEntrySet);
        getRootEntrySet(root.getRight(currentVersion), internalEntrySet);
        internalEntrySet.add(root.getValue(currentVersion));
        return internalEntrySet;
    }

    private ModificationBoxNode<Map.Entry<K, V>, Long> getEntry(Object key) {
        Objects.requireNonNull(key);
        @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
        var entry = getCurrentRoot();
        while (entry != null) {
            var internalEntry = entry.getValue(currentVersion);
            if (internalEntry == null) {
                return null;
            }
            var entryKey = internalEntry.getKey();
            var compareResult = k.compareTo(entryKey);
            if (compareResult == 0) {
                return entry;
            }
            if (compareResult > 0) {
                entry = entry.getRight(currentVersion);
            } else {
                entry = entry.getLeft(currentVersion);
            }
        }
        return null;
    }

    private ModificationBoxNode<Map.Entry<K, V>, Long> getCurrentRoot() {
        return roots.get(currentVersion);
    }

    private void doBeforeModifyAction() {
        deleteMemoized();
        currentVersion++;
        lastVersion = currentVersion;
        roots.remove(currentVersion);
    }

    private void deleteMemoized() {
        if (useMemoize) {
            memoizedEntrySet = null;
        }
    }

}
