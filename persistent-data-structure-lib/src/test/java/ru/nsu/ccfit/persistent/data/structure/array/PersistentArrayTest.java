package ru.nsu.ccfit.persistent.data.structure.array;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PersistentArrayTest {
    PersistentArray<String> persistentArray;

    private void addABC() {
        persistentArray = new PersistentArray<>(32);
        persistentArray.add("A");
        persistentArray.add("B");
        persistentArray.add("C");
    }

    private void addABC(int depth, int bitPerEdge) {
        persistentArray = new PersistentArray<>(depth, bitPerEdge);
        persistentArray.add("A");
        persistentArray.add("B");
        persistentArray.add("C");
    }

    @Test
    void testPersistentArrayAddAndGet() {
        addABC();
        Assertions.assertEquals("A", persistentArray.get(0));
        Assertions.assertEquals("B", persistentArray.get(1));
        Assertions.assertEquals("C", persistentArray.get(2));
    }

    @Test
    void testPersistentArrayToArray() {
        addABC();
        Assertions.assertEquals("[A, B, C]", Arrays.toString(persistentArray.toArray()));
    }

    @Test
    void testPersistentArraySize() {
        persistentArray = new PersistentArray<>(32);
        Assertions.assertEquals(0, persistentArray.size());
        persistentArray.add("A");
        persistentArray.add("B");
        persistentArray.add("C");
        Assertions.assertEquals(3, persistentArray.size());
    }

    @Test
    void testPersistentAdd() {
        persistentArray = new PersistentArray<>(1, 1);
        Assertions.assertEquals(2, persistentArray.maxSize);

        Assertions.assertTrue(persistentArray.add("A"));
        Assertions.assertTrue(persistentArray.add("B"));
    }

    @Test
    void testPersistentArrayIsEmpty() {
        persistentArray = new PersistentArray<>(32);
        Assertions.assertTrue(persistentArray.isEmpty());
        persistentArray.add("A");
        Assertions.assertFalse(persistentArray.isEmpty());
    }

    @Test
    void testPersistentArrayUndoRedo() {
        addABC();
        Assertions.assertEquals(4, persistentArray.getVersionCount());
        persistentArray.undo();
        Assertions.assertEquals(4, persistentArray.getVersionCount());
        persistentArray.undo();
        Assertions.assertEquals(4, persistentArray.getVersionCount());
        Assertions.assertEquals("[A]", persistentArray.toString());

        persistentArray.redo();
        Assertions.assertEquals(4, persistentArray.getVersionCount());
        Assertions.assertEquals("[A, B]", persistentArray.toString());

        persistentArray.undo();
        Assertions.assertEquals(4, persistentArray.getVersionCount());
        persistentArray.undo();
        Assertions.assertEquals(4, persistentArray.getVersionCount());
        Assertions.assertEquals("[]", persistentArray.toString());

        persistentArray.redo();
        Assertions.assertEquals(4, persistentArray.getVersionCount());
        persistentArray.redo();
        Assertions.assertEquals(4, persistentArray.getVersionCount());
        persistentArray.redo();
        Assertions.assertEquals(4, persistentArray.getVersionCount());
        Assertions.assertEquals("[A, B, C]", persistentArray.toString());
    }

    @Test
    void testPersistentArrayInsertedUndoRedo() {
        PersistentArray<PersistentArray<String>> persistentArrayrent = new PersistentArray<>();
        PersistentArray<String> child1 = new PersistentArray<>();
        PersistentArray<String> child2 = new PersistentArray<>();
        PersistentArray<String> child3 = new PersistentArray<>();
        persistentArrayrent.add(child1);
        persistentArrayrent.add(child2);
        persistentArrayrent.add(child3);

        persistentArrayrent.get(0).add("1");
        persistentArrayrent.get(0).add("2");
        persistentArrayrent.get(0).add("3");

        persistentArrayrent.get(1).add("11");
        persistentArrayrent.get(1).add("22");
        persistentArrayrent.get(1).add("33");

        persistentArrayrent.get(2).add("111");
        persistentArrayrent.get(2).add("222");
        persistentArrayrent.get(2).add("333");

        Assertions.assertEquals("[[1, 2, 3], [11, 22, 33], [111, 222, 333]]", persistentArrayrent.toString());
        persistentArrayrent.undo();
        Assertions.assertEquals("[[1, 2, 3], [11, 22, 33], [111, 222]]", persistentArrayrent.toString());

        PersistentArray<String> child4 = new PersistentArray<>();
        persistentArrayrent.add(1, child4);
        child4.add("Test_str_1");
        Assertions.assertEquals("[[1, 2, 3], [Test_str_1], [11, 22, 33], [111, 222]]", persistentArrayrent.toString());
        persistentArrayrent.undo();
        Assertions.assertEquals("[[1, 2, 3], [], [11, 22, 33], [111, 222]]", persistentArrayrent.toString());

        persistentArrayrent.get(0).set(0, "Test_str_2");
        persistentArrayrent.get(0).set(1, "Test_str_3");
        Assertions.assertEquals("[[Test_str_2, Test_str_3, 3], [], [11, 22, 33], [111, 222]]", persistentArrayrent.toString());
        persistentArrayrent.undo();
        Assertions.assertEquals("[[Test_str_2, 2, 3], [], [11, 22, 33], [111, 222]]", persistentArrayrent.toString());
    }

    @Test
    void testPersistentArrayIterator() {
        addABC();
        Iterator<String> i = persistentArray.iterator();
        Assertions.assertEquals("A", i.next());
        Assertions.assertEquals("B", i.next());
        Assertions.assertEquals("C", i.next());
        Assertions.assertFalse(i.hasNext());
    }

    @Test
    void testPersistentArrayForEach() {
        addABC();
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : persistentArray) {
            stringBuilder.append(s);
        }
        Assertions.assertEquals("ABC", stringBuilder.toString());

        stringBuilder = new StringBuilder();
        persistentArray.forEach(stringBuilder::append);
        Assertions.assertEquals("ABC", stringBuilder.toString());
    }

    @Test
    void testPersistentArrayPop() {
        addABC();
        Assertions.assertEquals("C", persistentArray.pop());
        Assertions.assertEquals("B", persistentArray.pop());
        persistentArray.undo();
        persistentArray.undo();
        Assertions.assertEquals("C", persistentArray.pop());
    }

    @Test
    void testPersistentArraySet() {
        addABC();
        Assertions.assertEquals("[A, B, C]", persistentArray.toString());
        Assertions.assertEquals("A", persistentArray.set(0, "Q"));
        Assertions.assertEquals("B", persistentArray.set(1, "W"));
        Assertions.assertEquals("[Q, W, C]", persistentArray.toString());
        persistentArray.undo();
        persistentArray.undo();
        Assertions.assertEquals("[A, B, C]", persistentArray.toString());
    }

    @Test
    void testPersistentArrayCascade() {
        PersistentArray<String> persistentArray_0 = new PersistentArray<>(32);
        persistentArray_0.add("A");

        PersistentArray<String> persistentArray_1 = persistentArray_0.conj("B");

        Assertions.assertEquals("[A]", persistentArray_0.toString());
        Assertions.assertEquals("[A, B]", persistentArray_1.toString());

        PersistentArray<String> persistentArray_2 = persistentArray_1.assoc(0, "C");

        Assertions.assertEquals("[C, B]", persistentArray_2.toString());
    }

    @Test
    void testPersistentArrayStream() {
        PersistentArray<Integer> persistentArray = new PersistentArray<>();
        persistentArray.add(4);
        persistentArray.add(5);
        persistentArray.add(6);
        persistentArray.add(7);

        Assertions.assertEquals("[12, 14]", Arrays.toString(
                persistentArray.stream().map(i -> i * 2).filter(x -> x > 10).toArray()));

        persistentArray.undo();

        Assertions.assertEquals("[12]", Arrays.toString(
                persistentArray.stream().map(i -> i * 2).filter(x -> x > 10).toArray()));
    }

    @Test
    void testPersistentArrayConstructor() {
        PersistentArray<String> persistentArray_0 = new PersistentArray<>();
        Assertions.assertEquals(1073741824, persistentArray_0.maxSize);
        Assertions.assertEquals(6, persistentArray_0.depth);
        Assertions.assertEquals(32, persistentArray_0.width);

        PersistentArray<String> persistentArray_1 = new PersistentArray<>(27);
        Assertions.assertEquals(32, persistentArray_1.maxSize);
        Assertions.assertEquals(1, persistentArray_1.depth);
        Assertions.assertEquals(32, persistentArray_1.width);

        PersistentArray<String> persistentArray_2 = new PersistentArray<>(32);
        Assertions.assertEquals(32, persistentArray_2.maxSize);
        Assertions.assertEquals(1, persistentArray_2.depth);
        Assertions.assertEquals(32, persistentArray_2.width);

        PersistentArray<String> persistentArray_3 = new PersistentArray<>(33);
        Assertions.assertEquals(1024, persistentArray_3.maxSize);
        Assertions.assertEquals(2, persistentArray_3.depth);
        Assertions.assertEquals(32, persistentArray_3.width);

        PersistentArray<String> persistentArray_4 = new PersistentArray<>(3, 1);
        Assertions.assertEquals(8, persistentArray_4.maxSize);
        Assertions.assertEquals(3, persistentArray_4.depth);
        Assertions.assertEquals(2, persistentArray_4.width);
    }

    @Test
    void testPersistentArrayAddInTheMiddle() {
        persistentArray = new PersistentArray<>(3, 1);
        persistentArray.add("3");
        persistentArray.add("7");
        persistentArray.add("6");
        persistentArray.add("9");
        persistentArray.add("1");
        Assertions.assertEquals("[3, 7, 6, 9, 1]", persistentArray.toString());
        persistentArray.add(3, "8");
        Assertions.assertEquals("[3, 7, 6, 8, 9, 1]", persistentArray.toString());
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> persistentArray.add(-1, "8"));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> persistentArray.add(6, "8"));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> persistentArray.add(9999, "8"));
    }

    @Test
    void testPersistentArrayToString() {
        addABC();
        Assertions.assertEquals("[A, B, C]", persistentArray.toString());
    }

    @Test
    void testPersistentArrayRemove() {
        addABC(3, 1);

        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> persistentArray.remove(-1));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> persistentArray.remove(3));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> persistentArray.remove(999));

        Assertions.assertEquals("B", persistentArray.remove(1));
        Assertions.assertEquals("[A, C]", persistentArray.toString());

        Assertions.assertEquals("C", persistentArray.remove(1));
        Assertions.assertEquals("[A]", persistentArray.toString());

        Assertions.assertEquals("A", persistentArray.remove(0));
        Assertions.assertEquals("[]", persistentArray.toString());
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> persistentArray.remove(0));
    }

    @Test
    void testPersistentArrayClear() {
        addABC();
        persistentArray.clear();
        Assertions.assertEquals("[]", persistentArray.toString());
        persistentArray.undo();
        Assertions.assertEquals("[A, B, C]", persistentArray.toString());
    }
}