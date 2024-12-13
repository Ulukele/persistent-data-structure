package ru.nsu.ccfit.persistent.data.structure.list;

import ru.nsu.ccfit.persistent.data.structure.PersistentStructure;
import ru.nsu.ccfit.persistent.data.structure.node.ModificationBox;
import ru.nsu.ccfit.persistent.data.structure.node.ModificationBoxNode;

import java.util.AbstractList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * Двусвязный список поддерживающий операции возврата к предыдущему состоянию.
 *
 * @param <V> Тип хранимых значений.
 */
public class PersistentDoubleLinkedList<V> extends AbstractList<V> implements List<V>, PersistentStructure {

    /**
     * Ассоциативный массив: версия -> корень списка.
     */
    private final HashMap<Long, ModificationBoxNode<V, Long>> heads;

    /**
     * Текущая версия структуры.
     */
    private Long currentVersion;

    /**
     * Последняя доступная версия структуры.
     */
    private Long lastVersion;

    public PersistentDoubleLinkedList() {
        this.heads = new HashMap<>();
        this.currentVersion = 0L;
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

    @Override
    public int size() {
        int result = 0;
        var node = getCurrentHead();
        while (node != null) {
            node = node.getRight(currentVersion);
            result++;
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override
    public boolean add(V v) {
        var last = getNode(size() - 1);
        doBeforeModifyAction();
        var newHead = addAfter(
                currentVersion,
                last,
                new ModificationBoxNode<>(last, null, v)
        );
        heads.put(currentVersion, newHead);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        var index = indexOf(o);
        if (index == -1) {
            return false;
        }
        remove(index);
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        c.forEach(this::add);
        return !c.isEmpty();
    }

    @Override
    public void clear() {
        doBeforeModifyAction();
        heads.put(currentVersion, null);
    }

    @Override
    public V get(int index) {
        var node = getNode(index);
        if (node == null) {
            throw new IndexOutOfBoundsException();
        }
        return node.getValue(currentVersion);
    }

    @Override
    public V set(int index, V element) {
        var node = getNode(index);
        if (node == null) {
            throw new IndexOutOfBoundsException();
        }
        doBeforeModifyAction();
        var previousValue = node.getValue(currentVersion);
        var newNode = node.modify(
                ModificationBox.createValueModification(currentVersion, element)
        );
        final ModificationBoxNode<V, Long> newHead;
        if (newNode == node) {
            newHead = getCurrentHead();
        } else {
            newHead = propagateModification(currentVersion, newNode);
        }
        heads.put(currentVersion, newHead);
        return previousValue;
    }

    @Override
    public void add(int index, V element) {
        var node = getNode(index);
        if (node == null) {
            throw new IndexOutOfBoundsException();
        }
        doBeforeModifyAction();
        addAfter(
                currentVersion,
                node,
                new ModificationBoxNode<>(
                        node,
                        node.getRight(currentVersion),
                        element
                )
        );
    }

    @Override
    public V remove(int index) {
        var node = getNode(index);
        if (node == null) {
            throw new IndexOutOfBoundsException();
        }
        var previousValue = node.getValue(currentVersion);
        doBeforeModifyAction();
        var left = node.getLeft(currentVersion);
        var right = node.getRight(currentVersion);
        final ModificationBoxNode<V, Long> newRoot;
        if (left == null) {
            newRoot = right;
        } else {
            newRoot = addAfter(currentVersion, left, right);
        }
        heads.put(currentVersion, newRoot);
        return previousValue;
    }

    @Override
    public int indexOf(Object o) {
        int i = 0;
        var node = getCurrentHead();
        while (node != null) {
            if (o.equals(node.getValue(currentVersion))) {
                break;
            }
            i++;
            node = node.getRight(currentVersion);
        }
        if (node == null) {
            return -1;
        }
        return i;
    }

    @Override
    public int lastIndexOf(Object o) {
        int i = 0;
        int result = -1;
        var node = getCurrentHead();
        while (node != null) {
            if (o.equals(node.getValue(currentVersion))) {
                result = i;
            }
            i++;
            node = node.getRight(currentVersion);
        }
        return result;
    }

    private ModificationBoxNode<V, Long> addAfter(
            Long version,
            ModificationBoxNode<V, Long> node,
            ModificationBoxNode<V, Long> newNode) {
        if (node == null) {
            return newNode;
        }
        var modifiedNode = node.modify(
                ModificationBox.createRightModification(version, newNode)
        );
        if (modifiedNode == node) {
            return getCurrentHead();
        }
        return propagateModification(version, modifiedNode);
    }

    private ModificationBoxNode<V, Long> propagateModification(
            Long version,
            ModificationBoxNode<V, Long> modifiedNode) {
        var lastCreated = modifiedNode;
        var node = lastCreated;
        while (node != null) {
            var prev = node.getLeft(version);
            if (prev == null) {
                break;
            }
            lastCreated = prev.modify(
                    ModificationBox.createRightModification(version, lastCreated)
            );
            if (lastCreated == prev) {
                return getCurrentHead();
            }
            node = prev;
        }
        return lastCreated;
    }

    private ModificationBoxNode<V, Long> getNode(int index) {
        var condition = new Predicate<ModificationBoxNode<V, Long>>() {

            int currentIndex = 0;

            @Override
            public boolean test(ModificationBoxNode<V, Long> node) {
                return currentIndex++ == index;
            }
        };
        return iterateUntilCondition(condition);
    }

    private ModificationBoxNode<V, Long> iterateUntilCondition(Predicate<ModificationBoxNode<V, Long>> condition) {
        var node = getCurrentHead();
        while (node != null) {
            if (condition.test(node)) {
                break;
            }
            node = node.getRight(currentVersion);
        }
        return node;
    }

    private ModificationBoxNode<V, Long> getCurrentHead() {
        return heads.get(currentVersion);
    }

    private void doBeforeModifyAction() {
        currentVersion++;
        if (currentVersion != 1 && lastVersion >= currentVersion) {
            heads.get(currentVersion - 1).cleanFromVersion(currentVersion);
        }
        lastVersion = currentVersion;
        heads.put(currentVersion, heads.get(currentVersion - 1));
    }

}
