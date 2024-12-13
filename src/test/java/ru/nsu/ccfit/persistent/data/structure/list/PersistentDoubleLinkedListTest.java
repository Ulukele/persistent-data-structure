package ru.nsu.ccfit.persistent.data.structure.list;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

class PersistentDoubleLinkedListTest {

    @ParameterizedTest
    @ValueSource(ints = {-1, 1000})
    void set_wrongIndex_sameAsArrayList(int idx) {
        Consumer<List<Integer>> modifier = (l) -> l.addAll(List.of(1, 2, 3));
        Function<List<Integer>, Integer> effectFeature = (l) -> l.set(idx, 0);

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void set_correctIndex_sameAsArrayList(int idx) {
        Consumer<List<Integer>> modifier = (l) -> l.addAll(List.of(1, 2, 3));
        Function<List<Integer>, Integer> effectFeature = (l) -> l.set(idx, 0);

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void add_toEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {};
        Function<List<Integer>, Boolean> effectFeature = (l) -> l.add(0);

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void add_toNotEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {l.add(0);};
        Function<List<Integer>, Boolean> effectFeature = (l) -> l.add(0);

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void remove_fromEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {};
        Function<List<Integer>, Integer> effectFeature = (l) -> l.remove(0);

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void remove_existedIndex_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {l.add(0);};
        Function<List<Integer>, Integer> effectFeature = (l) -> l.remove(0);

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void remove_notExistedIndex_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {l.add(0);};
        Function<List<Integer>, Integer> effectFeature = (l) -> l.remove(1);
        Function<List<Integer>, List<Integer>> equalsFeature = (l) -> l.stream().toList();

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void indexOf_toEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {};
        Function<List<Integer>, Integer> effectFeature = (l) -> l.indexOf(1);

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void indexOf_notExisted_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {l.add(0);};
        Function<List<Integer>, Integer> effectFeature = (l) -> l.indexOf(1);

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void indexOf_existed_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {l.add(0);};
        Function<List<Integer>, Integer> effectFeature = (l) -> l.indexOf(0);

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void lastIndexOf_toEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {};
        Function<List<Integer>, Integer> effectFeature = (l) -> l.lastIndexOf(1);

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void lastIndexOf_notExisted_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {l.add(0);};
        Function<List<Integer>, Integer> effectFeature = (l) -> l.lastIndexOf(1);

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void lastIndexOf_existed_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {l.add(0);};
        Function<List<Integer>, Integer> effectFeature = (l) -> l.lastIndexOf(0);

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void addAll_toEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {};
        Function<List<Integer>, Boolean> effectFeature = (l) -> l.addAll(List.of(1, 2, 3));

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void addAll_toNotEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> l.addAll(List.of(1, 2, 3));
        Function<List<Integer>, Boolean> effectFeature = (l) -> l.addAll(List.of(1, 2, 3));

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void iterator_fromEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {};
        Function<List<Integer>, List<Integer>> effectFeature = (l) -> {
            ArrayList<Integer> res = new ArrayList<>();
            var i = l.iterator();
            while (i.hasNext()) {
                res.add(i.next());
            }
            return res;
        };

        assertSameModifyEffect(modifier, effectFeature, false);
    }

    @Test
    void iterator_fromNotEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> l.addAll(List.of(1, 2, 3));
        Function<List<Integer>, List<Integer>> effectFeature = (l) -> {
            ArrayList<Integer> res = new ArrayList<>();
            var i = l.iterator();
            while (i.hasNext()) {
                res.add(i.next());
            }
            return res;
        };

        assertSameModifyEffect(modifier, effectFeature, false);
    }

    @Test
    void isEmpty_toEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {};
        Function<List<Integer>, Boolean> effectFeature = List::isEmpty;

        assertSameModifyEffect(modifier, effectFeature, false);
    }

    @Test
    void isEmpty_toNotEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> l.addAll(List.of(1, 2, 3));
        Function<List<Integer>, Boolean> effectFeature = List::isEmpty;

        assertSameModifyEffect(modifier, effectFeature, false);
    }

    @Test
    void contains_toEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {};
        Function<List<Integer>, Boolean> effectFeature = (l) -> l.contains(0);

        assertSameModifyEffect(modifier, effectFeature, false);
    }

    @Test
    void contains_toNotEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> l.addAll(List.of(1, 2, 3));
        Function<List<Integer>, Boolean> effectFeature = (l) -> l.contains(0);

        assertSameModifyEffect(modifier, effectFeature, false);
    }

    @Test
    void contains_notContained_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> l.addAll(List.of(1, 2, 3));
        Function<List<Integer>, Boolean> effectFeature = (l) -> l.contains(4);

        assertSameModifyEffect(modifier, effectFeature, false);
    }

    @Test
    void toArray_toNotEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> l.addAll(List.of(1, 2, 3));
        Function<List<Integer>, List<Integer>> effectFeature = l
                -> (Arrays.stream(l.toArray()).map(Integer.class::cast).toList());

        assertSameModifyEffect(modifier, effectFeature, false);
    }

    @Test
    void removeAll_toEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {};
        Function<List<Integer>, Boolean> effectFeature = (l) -> l.removeAll(List.of(1, 2, 3));

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void removeAll_toNotEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {l.addAll(List.of(1, 2, 3));};
        Function<List<Integer>, Boolean> effectFeature = (l) -> l.removeAll(List.of(2, 3));

        assertSameModifyEffect(modifier, effectFeature, true);
    }


    @Test
    void clear_notEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {l.addAll(List.of(1, 2, 3));};
        Function<List<Integer>, Integer> effectFeature = (l) -> {
            l.clear();
            return 0;
        };

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void reversed_notEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {l.addAll(List.of(1, 2, 3));};
        Function<List<Integer>, List<Integer>> effectFeature = List::reversed;

        assertSameModifyEffect(modifier, effectFeature, true);
    }

    @Test
    void size_toEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {};
        Function<List<Integer>, Integer> effectFeature = List::size;

        assertSameModifyEffect(modifier, effectFeature, false);
    }

    @Test
    void size_toNotEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {l.addAll(List.of(1, 2, 3));};
        Function<List<Integer>, Integer> effectFeature = List::size;

        assertSameModifyEffect(modifier, effectFeature, false);
    }

    @Test
    void get_toEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {};
        Function<List<Integer>, Integer> effectFeature = (l) -> l.get(0);

        assertSameModifyEffect(modifier, effectFeature, false);
    }

    @Test
    void get_toNotEmpty_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {l.addAll(List.of(1, 2, 3));};
        Function<List<Integer>, Integer> effectFeature = (l) -> l.get(0);

        assertSameModifyEffect(modifier, effectFeature, false);
    }

    @Test
    void get_wrongIndex_sameAsArrayList() {
        Consumer<List<Integer>> modifier = (l) -> {l.addAll(List.of(1, 2, 3));};
        Function<List<Integer>, Integer> effectFeature = (l) -> l.get(1000);

        assertSameModifyEffect(modifier, effectFeature, false);
    }

    @Test
    void undo_once_success() {
        PersistentDoubleLinkedList<Integer> list = new PersistentDoubleLinkedList<>();
        list.add(1);

        list.undo();
        Assertions.assertTrue(list.isEmpty());
        list.redo();
        Assertions.assertFalse(list.isEmpty());
        Assertions.assertEquals(1, list.get(0));
    }

    @Test
    void undo_twice_success() {
        PersistentDoubleLinkedList<Integer> list = new PersistentDoubleLinkedList<>();
        list.add(1);
        list.add(2);

        list.undo();
        list.undo();
        Assertions.assertTrue(list.isEmpty());
        list.redo();
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(1, list.get(0));
        list.redo();
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(1, list.get(0));
        Assertions.assertEquals(2, list.get(1));
    }


    @Test
    void redo_afterModification_doNotChangeState() {
        PersistentDoubleLinkedList<Integer> list = new PersistentDoubleLinkedList<>();
        list.add(2); // v1
        list.add(1); // v2
        list.undo(); // to v1
        list.add(3); // v2
        list.redo(); // should still be v2

        Assertions.assertEquals(2, list.size());
        Assertions.assertFalse(list.contains(1));
        Assertions.assertEquals(3, list.get(1));
        Assertions.assertEquals(2, list.get(0));
    }

    private <T> void assertSameModifyEffect(
            Consumer<List<Integer>> modifier,
            Function<List<Integer>, T> effectFeature,
            boolean checkEquals) {
        PersistentDoubleLinkedList<Integer> list = new PersistentDoubleLinkedList<>();
        ArrayList<Integer> expectedList = new ArrayList<>();
        modifier.accept(list);
        modifier.accept(expectedList);

        T actualEffect = null;
        T expectedEffect = null;
        Exception actualException = null;
        Exception expectedException = null;
        try {
            actualEffect = effectFeature.apply(list);
        } catch (Exception exception) {
            actualException = exception;
            exception.printStackTrace();
        }
        try {
            expectedEffect = effectFeature.apply(expectedList);
        } catch (Exception exception) {
            expectedException = exception;
        }
        Assertions.assertEquals(expectedException == null, actualException == null);
        Assertions.assertEquals(expectedEffect, actualEffect);
        if (expectedException != null && actualException != null) {
            Assertions.assertEquals(expectedException.getClass(), actualException.getClass());
        }
        if (checkEquals) {
            Assertions.assertEquals(expectedList, new ArrayList<>(list));
        }
    }

}