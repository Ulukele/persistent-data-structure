package ru.nsu.ccfit.persistent.data.structure.array.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Узел B-дерева для персистентной коллекции
 */
public class ArrayNode<E> {

    /**
     * Массив дочерних узлов, который хранит ссылки на дочерние узлы текущего
     * узла.
     */
    private List<ArrayNode<E>> child;

    /**
     * Значения элементов узла
     */
    private List<E> value;

    public ArrayNode() {
    }

    public ArrayNode(ArrayNode<E> other) {
        if (other != null) {
            if (other.child != null) {
                child = new ArrayList<>();
                child.addAll(other.child);
            }

            if (other.value != null) {
                value = new ArrayList<>();
                value.addAll(other.value);
            }
        }
    }

    public ArrayNode(ArrayNode<E> other, int maxIndex) {
        if (other.child != null) {
            child = new ArrayList<>();
            for (int i = 0; i <= maxIndex; i++) {
                child.add(other.child.get(i));
            }
        }

        if (other.value != null) {
            value = new ArrayList<>();
            for (int i = 0; i <= maxIndex; i++) {
                value.add(other.value.get(i));
            }
        }
    }

    /**
     * Возвращает список потомков этого узла.
     *
     * @return список потомков этого узла
     */
    public List<ArrayNode<E>> getChild() {
        return child;
    }

    /**
     * Устанавливает список потомков этому узлу.
     *
     * @param child список потомков
     */
    public void setChild(List<ArrayNode<E>> child) {
        this.child = child;
    }

    /**
     * Возвращает список значений этого узла.
     *
     * @return список значений этого узла.
     */
    public List<E> getValue() {
        return value;
    }

    /**
     * Устанавливает список значений этому узлу.
     *
     * @param value список значений
     */
    public void setValue(List<E> value) {
        this.value = value;
    }

    /**
     * Возвращает true, если узел не имеет потомков и не содержит значений.
     *
     * @return true, если узел не имеет потомков и не содержит значений
     */
    public boolean isEmpty() {
        if ((child == null) && (value == null)) {
            return true;
        }

        if ((value != null) && (!value.isEmpty())) {
            return false;
        }

        return (child == null) || (child.isEmpty());
    }

    /**
     * Возвращает строковое представление содержимого узла.
     *
     * @return строковое представление содержимого узла
     */
    @Override
    public String toString() {
        String childNodes = child == null ? "[child null]" : Arrays.toString(child.toArray());
        String values = value == null ? "[value null]" : Arrays.toString(value.toArray());
        return String.format("%09x %s %s", hashCode(), childNodes, values);
    }
}
