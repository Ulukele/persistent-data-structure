package ru.nsu.ccfit.persistent.data.structure.array;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import ru.nsu.ccfit.persistent.data.structure.array.utils.ArrayHead;
import ru.nsu.ccfit.persistent.data.structure.array.utils.ArrayNode;
import ru.nsu.ccfit.persistent.data.structure.array.utils.PersistentCollection;

/**
 * Персистентный массив поддерживающий операции возврата к предыдущему
 * состоянию.
 */
public class PersistentArray<E> extends PersistentCollection implements List<E> {

    private PersistentArray<PersistentArray<?>> parent;
    private final Stack<PersistentArray<?>> insertedUndo = new Stack<>();
    private final Stack<PersistentArray<?>> insertedRedo = new Stack<>();
    protected final Stack<ArrayHead<E>> redo = new Stack<>();
    protected final Stack<ArrayHead<E>> undo = new Stack<>();

    public PersistentArray() {
        this(6, 5);
    }

    public PersistentArray(int maxSize) {
        this((int) Math.ceil(log(maxSize, (int) Math.pow(2, 5))), 5);
    }

    public PersistentArray(int depth, int bitPerEdge) {
        super(depth, bitPerEdge);
        ArrayHead<E> head = new ArrayHead<>();
        undo.push(head);
        redo.clear();
    }

    public PersistentArray(PersistentArray<E> other) {
        super(other.depth, other.bitPerEdge);
        this.undo.addAll(other.undo);
        this.redo.addAll(other.redo);
    }

    @Override
    public void undo() {
        if (!insertedUndo.empty()) {
            insertedUndo.peek().undo();
            insertedRedo.push(insertedUndo.pop());
        } else {
            if (!undo.empty()) {
                redo.push(undo.pop());
            }
        }
    }

    @Override
    public void redo() {
        if (!insertedRedo.empty()) {
            insertedRedo.peek().redo();
            insertedUndo.push(insertedRedo.pop());
        } else {
            if (!redo.empty()) {
                undo.push(redo.pop());
            }
        }
    }

    private void tryParentUndo(E value) {
        if (value instanceof PersistentArray) {
            ((PersistentArray) value).parent = this;
        }

        if (parent != null) {
            parent.insertedUndo.push(this);
        }
    }

    @Override
    public int size() {
        return size(getCurrentHead());
    }

    private int size(ArrayHead<E> head) {
        return head.getSize();
    }

    protected ArrayHead<E> getCurrentHead() {
        return this.undo.peek();
    }

    private void checkIndex(int index) {
        checkIndex(getCurrentHead(), index);
    }

    private void checkIndex(ArrayHead<E> head, int index) {
        if ((index < 0) || (index >= head.getSize())) {
            throw new IndexOutOfBoundsException("Invalid index");
        }
    }

    public boolean isFull() {
        return isFull(getCurrentHead());
    }

    private boolean isFull(ArrayHead<E> head) {
        return head.getSize() >= maxSize;
    }

    @Override
    public boolean isEmpty() {
        return getCurrentHead().getSize() <= 0;
    }

    public int getVersionCount() {
        return undo.size() + redo.size();
    }

    @Override
    public E set(int index, E element) {
        checkIndex(index);

        E result = get(index);

        AbstractMap.SimpleEntry<ArrayNode<E>, Integer> copedNodeP = copyLeafToChange(getCurrentHead(), index);
        int leafIndex = copedNodeP.getValue();
        ArrayNode<E> copedNode = copedNodeP.getKey();
        copedNode.getValue().set(leafIndex, element);

        tryParentUndo(element);

        return result;
    }

    public PersistentArray<E> assoc(int index, E element) {
        PersistentArray<E> result = new PersistentArray<>(this);
        result.set(index, element);
        return result;
    }

    @Override
    public boolean add(E element) {
        if (isFull()) {
            throw new IllegalStateException("Array is full");
        }

        ArrayHead<E> newHead = new ArrayHead<>(getCurrentHead(), 0);
        undo.push(newHead);
        redo.clear();
        tryParentUndo(element);

        return add(newHead, element);
    }

    public PersistentArray<E> conj(E element) {
        PersistentArray<E> result = new PersistentArray<>(this);
        result.add(element);
        return result;
    }

    @Override
    public void add(int index, E element) {
        checkIndex(index);
        if (isFull()) {
            throw new IllegalStateException("Array is full");
        }

        ArrayHead<E> oldHead = getCurrentHead();

        AbstractMap.SimpleEntry<ArrayNode<E>, Integer> copedNodeP = copyLeafToMove(oldHead, index);
        int leafIndex = copedNodeP.getValue();
        ArrayNode<E> copedNode = copedNodeP.getKey();
        copedNode.getValue().set(leafIndex, element);

        ArrayHead<E> newHead = getCurrentHead();
        for (int i = index; i < oldHead.getSize(); i++) {
            add(newHead, get(oldHead, i));
        }
        tryParentUndo(element);
    }

    private boolean add(ArrayHead<E> head, E newElement) {
        add(head).getValue().add(newElement);

        return true;
    }

    private ArrayNode<E> add(ArrayHead<E> head) {
        if (isFull(head)) {
            throw new IllegalStateException("Array is full");
        }

        head.setSize(head.getSize() + 1);
        ArrayNode<E> currentNode = head.getRoot();
        for (int level = bitPerEdge * (depth - 1); level > 0; level -= bitPerEdge) {
            int widthIndex = ((head.getSize() - 1) >> level) & mask;
            ArrayNode<E> tmp;
            ArrayNode<E> newNode;

            if (currentNode.getChild() == null) {
                currentNode.setChild(new LinkedList<>());
                newNode = new ArrayNode<>();
                currentNode.getChild().add(newNode);
            } else {
                if (widthIndex == currentNode.getChild().size()) {
                    newNode = new ArrayNode<>();
                    currentNode.getChild().add(newNode);
                } else {
                    tmp = currentNode.getChild().get(widthIndex);
                    newNode = new ArrayNode<>(tmp);
                    currentNode.getChild().set(widthIndex, newNode);
                }
            }
            currentNode = newNode;
        }

        if (currentNode.getValue() == null) {
            currentNode.setValue(new ArrayList<>());
        }
        return currentNode;
    }

    /**
     * Удаляет последний элемент массива.
     *
     * @return последний элемент массива
     */
    public E pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Array is empty");
        }

        ArrayHead<E> newHead = new ArrayHead<>(getCurrentHead(), -1);
        undo.push(newHead);
        redo.clear();
        LinkedList<AbstractMap.SimpleEntry<ArrayNode<E>, Integer>> path = new LinkedList<>();
        path.add(new AbstractMap.SimpleEntry<>(newHead.getRoot(), 0));
        for (int level = bitPerEdge * (depth - 1); level > 0; level -= bitPerEdge) {
            int index = (newHead.getSize() >> level) & mask;
            ArrayNode<E> tmp;
            ArrayNode<E> newNode;
            tmp = path.getLast().getKey().getChild().get(index);
            newNode = new ArrayNode<>(tmp);
            path.getLast().getKey().getChild().set(index, newNode);
            path.add(new AbstractMap.SimpleEntry<>(newNode, index));
        }

        int index = newHead.getSize() & mask;
        E result = path.getLast().getKey().getValue().remove(index);

        for (int i = path.size() - 1; i >= 1; i--) {
            AbstractMap.SimpleEntry<ArrayNode<E>, Integer> elem = path.get(i);
            if (elem.getKey().isEmpty()) {
                path.get(i - 1).getKey().getChild().remove((int) elem.getValue());
            } else {
                break;
            }
        }

        return result;
    }

    /**
     * Удаляет элемент по указанному индексу.
     * <p>
     * Удаляет элемент в указанной позиции в этом массиве. Сдвигает любые
     * последующие элементы влево (вычитает единицу из их индексов). Возвращает
     * элемент, который был удален из массива.
     * </p>
     *
     * @param index индекс удаляемого элемента
     * @return удаленный элемент
     */
    @Override
    public E remove(int index) {
        checkIndex(index);

        E result = get(index);

        ArrayHead<E> oldHead = getCurrentHead();
        ArrayHead<E> newHead;

        if (index == 0) {
            newHead = new ArrayHead<>();
            undo.push(newHead);
            redo.clear();
        } else {
            AbstractMap.SimpleEntry<ArrayNode<E>, Integer> copedNodeP = copyLeafToMove(oldHead, index);
            int leafIndex = copedNodeP.getValue();
            ArrayNode<E> copedNode = copedNodeP.getKey();
            copedNode.getValue().remove(leafIndex);

            newHead = getCurrentHead();
            newHead.setSize(newHead.getSize() - 1);
        }

        for (int i = index + 1; i < oldHead.getSize(); i++) {
            add(newHead, get(oldHead, i));
        }

        return result;
    }

    /**
     * Удаляет все элементы из этого массива. Массив будет пуст после возврата
     * этого вызова.
     */
    @Override
    public void clear() {
        ArrayHead<E> head = new ArrayHead<>();
        undo.push(head);
        redo.clear();
    }

    private AbstractMap.SimpleEntry<ArrayNode<E>, Integer> copyLeafToChange(ArrayHead<E> head, int index) {
        ArrayHead<E> newHead = new ArrayHead<>(head, 0);
        undo.push(newHead);
        redo.clear();

        ArrayNode<E> currentNode = newHead.getRoot();
        for (int level = bitPerEdge * (depth - 1); level > 0; level -= bitPerEdge) {
            int widthIndex = (index >> level) & mask;
            ArrayNode<E> tmp;
            ArrayNode<E> newNode;
            tmp = currentNode.getChild().get(widthIndex);
            newNode = new ArrayNode<>(tmp);
            currentNode.getChild().set(widthIndex, newNode);
            currentNode = newNode;
        }

        return new AbstractMap.SimpleEntry<>(currentNode, index & mask);
    }

    private AbstractMap.SimpleEntry<ArrayNode<E>, Integer> copyLeafToMove(ArrayHead<E> oldHead, int index) {
        int level = bitPerEdge * (depth - 1);
        ArrayHead<E> newHead = new ArrayHead<>(oldHead, index + 1, (index >> level) & mask);
        undo.push(newHead);
        redo.clear();
        ArrayNode<E> currentNode = newHead.getRoot();
        for (; level > 0; level -= bitPerEdge) {
            int widthIndex = (index >> level) & mask;
            int widthIndexNext = (index >> (level - bitPerEdge)) & mask;
            ArrayNode<E> tmp;
            ArrayNode<E> newNode;
            tmp = currentNode.getChild().get(widthIndex);
            newNode = new ArrayNode<>(tmp, widthIndexNext);
            currentNode.getChild().set(widthIndex, newNode);
            currentNode = newNode;
        }

        return new AbstractMap.SimpleEntry<>(currentNode, index & mask);
    }

    /**
     * Возвращает элемент в указанной позиции в массиве.
     *
     * @param index индекс возвращаемого элемента
     * @return элемент в указанной позиции в массиве
     */
    @Override
    public E get(int index) {
        return get(getCurrentHead(), index);
    }

    private E get(ArrayHead<E> head, int index) {
        checkIndex(head, index);
        return getLeaf(head, index).getValue().get(index & mask);
    }

    private ArrayNode<E> getLeaf(ArrayHead<E> head, int index) {
        checkIndex(head, index);

        ArrayNode<E> node = head.getRoot();
        for (int level = bitPerEdge * (depth - 1); level > 0; level -= bitPerEdge) {
            int widthIndex = (index >> level) & mask;
            node = node.getChild().get(widthIndex);
        }

        return node;
    }

    public String drawGraph() {
        return getCurrentHead().getRoot().drawGraph();
    }

    /**
     * Возвращает строковое представление содержимого массива.
     * <p>
     * Строковое представление состоит из списка элементов этого персистентного
     * массива, заключенного в квадратные скобки («[]»). Смежные элементы
     * разделяются символами «, » (запятая с последующим пробелом).
     *
     * @return строковое представление массива
     */
    @Override
    public String toString() {
        return toString(getCurrentHead());
    }

    private String toString(ArrayHead<E> head) {
        return Arrays.toString(toArray(head));
    }

    /**
     * Возвращает массив, содержащий все элементы этого персистентного массива в
     * правильной последовательности (от первого до последнего элемента).
     *
     * @return массив, содержащий все элементы этого персистентного массива в
     * правильной последовательности
     */
    @Override
    public Object[] toArray() {
        return toArray(getCurrentHead());
    }

    private Object[] toArray(ArrayHead<E> head) {
        Object[] objects = new Object[head.getSize()];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = this.get(head, i);
        }
        return objects;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<E> listIterator() {
        return null;
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return null;
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return null;
    }

    @Override
    public Iterator<E> iterator() {
        return new PersistentArrayIterator<>();
    }

    /**
     * Итератор над ассоциативным массивом.
     */
    public class PersistentArrayIterator<T> implements Iterator<T> {

        int index = 0;

        /**
         * Возвращает true, если итерация содержит больше элементов.
         *
         * @return true, если итерация имеет больше элементов.
         */
        @Override
        public boolean hasNext() {
            return index < size();
        }

        /**
         * Возвращает следующий элемент в итерации.
         *
         * @return следующий элемент в итерации.
         */
        @Override
        public T next() {
            return (T) get(index++);
        }

        @Override
        public void remove() {
        }
    }
}
