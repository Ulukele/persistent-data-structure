package ru.nsu.ccfit.persistent.data.structure.map;

import ru.nsu.ccfit.persistent.data.structure.PersistentStructure;
import ru.nsu.ccfit.persistent.data.structure.node.ModificationBox;
import ru.nsu.ccfit.persistent.data.structure.node.ModificationBoxNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ассоциативный массив поддерживающий операции возврата к предыдущему состоянию.
 *
 * @param <K> Тип ключа ассоциативного массива.
 * @param <V> Тип значения ассоциативного массива.
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
                roots.put(currentVersion, roots.get(currentVersion - 1));
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

    @Override
    public V remove(Object key) {
        var entryWithParent = getEntryWithParent(key);
        if (entryWithParent == null || entryWithParent.isEmpty()) {
            return null;
        }
        doBeforeModifyAction();
        var entry = entryWithParent.get(0);
        var parent = entryWithParent.size() > 1
                ? entryWithParent.get(1)
                : null;
        var left = entry.getLeft(currentVersion - 1);
        var right = entry.getRight(currentVersion - 1);
        final ModificationBoxNode<Map.Entry<K, V>, Long> newRoot;
        if (left == null && right == null) {
            newRoot = removeLeaf(parent, entry);
        } else if (left == null) {
            newRoot = removeByReplace(parent, entry, entry.getRight(currentVersion - 1));
        } else if (right == null) {
            newRoot = removeByReplace(parent, entry, entry.getLeft(currentVersion - 1));
        } else {
            newRoot = removeFullNode(parent, entry);
        }
        roots.put(currentVersion, newRoot);
        return entry.getValue(currentVersion - 1).getValue();
    }

    private ModificationBoxNode<Entry<K, V>, Long> removeLeaf(
            ModificationBoxNode<Entry<K, V>, Long> parent,
            ModificationBoxNode<Entry<K, V>, Long> entry) {
        // leaf is root case
        if (parent == null) {
            return null; // null is new root -- no root
        }
        @SuppressWarnings("unchecked")
        Comparable<? super K> ek = (Comparable<? super K>) entry.getValue(currentVersion - 1).getKey();
        @SuppressWarnings("unchecked")
        Comparable<? super K> pk = (Comparable<? super K>) parent.getValue(currentVersion - 1).getKey();
        var isLeft = ek.compareTo(parent.getValue(currentVersion - 1).getKey()) < 0;
        return modifyInSubtree(
                currentVersion,
                roots.get(currentVersion - 1),
                pk::compareTo,
                isLeft
                        ? ModificationBox.createLeftModification(currentVersion, null)
                        : ModificationBox.createRightModification(currentVersion, null)
        );
    }

    private ModificationBoxNode<Entry<K, V>, Long> removeByReplace(
            ModificationBoxNode<Entry<K, V>, Long> parent,
            ModificationBoxNode<Entry<K, V>, Long> entry,
            ModificationBoxNode<Entry<K, V>, Long> newEntry) {
        // entry is root case
        if (parent == null) {
            return newEntry; // newEntry is new root
        }
        @SuppressWarnings("unchecked")
        Comparable<? super K> ek = (Comparable<? super K>) entry.getValue(currentVersion - 1).getKey();
        @SuppressWarnings("unchecked")
        Comparable<? super K> pk = (Comparable<? super K>) parent.getValue(currentVersion - 1).getKey();
        var isLeft = ek.compareTo(parent.getValue(currentVersion - 1).getKey()) < 0;
        return modifyInSubtree(
                currentVersion,
                roots.get(currentVersion - 1),
                pk::compareTo,
                isLeft
                        ? ModificationBox.createLeftModification(currentVersion, newEntry)
                        : ModificationBox.createRightModification(currentVersion, newEntry)
        );
    }

    private ModificationBoxNode<Entry<K, V>, Long> removeFullNode(
            ModificationBoxNode<Entry<K, V>, Long> parent,
            ModificationBoxNode<Entry<K, V>, Long> toRemove) {
        var entry = toRemove.getLeft(currentVersion);
        ModificationBoxNode<Map.Entry<K, V>, Long> valueToCopyParent = null;
        Map.Entry<K, V> valueToCopy = entry.getValue(currentVersion);
        while (entry != null) {
            if (entry.getRight(currentVersion) != null) {
                valueToCopyParent = entry;
            }
            valueToCopy = entry.getValue(currentVersion);
            entry = entry.getRight(currentVersion);
        }
        final ModificationBoxNode<Entry<K, V>, Long> leftSubtree;
        if (valueToCopyParent == null) {
            leftSubtree = null;
        } else {
            @SuppressWarnings("unchecked")
            Comparable<? super K> pk = (Comparable<? super K>) valueToCopyParent.getValue(currentVersion - 1).getKey();
            leftSubtree = modifyInSubtree(
                    currentVersion,
                    toRemove.getLeft(currentVersion),
                    pk::compareTo,
                    ModificationBox.createRightModification(currentVersion, null)
            );
        }
        var replaceNode = new ModificationBoxNode<>(
                leftSubtree,
                toRemove.getRight(currentVersion),
                valueToCopy
        );
        return removeByReplace(
                parent,
                toRemove,
                replaceNode
        );
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

    private ModificationBoxNode<Entry<K, V>, Long> modifyInSubtree(
            Long version,
            ModificationBoxNode<Entry<K, V>, Long> subRoot,
            Function<K, Integer> moveFunction,
            ModificationBox<Entry<K, V>, Long> modification) {
        if (subRoot == null) {
            return null;
        }
        // Search
        var entry = subRoot;
        ArrayList<ModificationBoxNode<Entry<K, V>, Long>> path = new ArrayList<>();
        while (entry != null) {
            path.add(entry);
            var move = moveFunction.apply(entry.getValue(version).getKey());
            if (move == 0) {
                break;
            } else if (move > 0) {
                entry = entry.getRight(version);
            } else {
                entry = entry.getLeft(version);
            }
        }
        // Copy until reach unmodified
        var reversePath = path.reversed();
        var pathIterator = reversePath.iterator();
        // Node searched for modify
        var target = pathIterator.next();
        var lastCopy = target.modify(modification);
        if (lastCopy == target) {
            return null;
        }
        while (pathIterator.hasNext()) {
            var copyCandidate = pathIterator.next();
            lastCopy = copyCandidate.modify(
                    moveFunction.apply(copyCandidate.getValue(version).getKey()) > 0
                            ? ModificationBox.createRightModification(version, lastCopy)
                            : ModificationBox.createLeftModification(version, lastCopy)
            );
            if (lastCopy == copyCandidate) {
                return null;
            }
        }
        return lastCopy;
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
        var entryWithParent = getEntryWithParent(key);
        if (entryWithParent == null || entryWithParent.isEmpty()) {
            return null;
        }
        return entryWithParent.get(0);
    }

    private List<ModificationBoxNode<Map.Entry<K, V>, Long>> getEntryWithParent(Object key) {
        Objects.requireNonNull(key);
        @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
        var entry = getCurrentRoot();
        ModificationBoxNode<Map.Entry<K, V>, Long> previous = null;
        while (entry != null) {
            var internalEntry = entry.getValue(currentVersion);
            if (internalEntry == null) {
                return null;
            }
            var entryKey = internalEntry.getKey();
            var compareResult = k.compareTo(entryKey);
            if (compareResult == 0) {
                return previous == null
                        ? Collections.singletonList(entry)
                        : List.of(entry, previous);
            }
            previous = entry;
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
        if (currentVersion != 1 && lastVersion >= currentVersion) {
            roots.get(currentVersion - 1).cleanFromVersion(currentVersion);
        }
        lastVersion = currentVersion;
        roots.remove(currentVersion);
    }

    private void deleteMemoized() {
        if (useMemoize) {
            memoizedEntrySet = null;
        }
    }

}
