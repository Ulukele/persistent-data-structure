package ru.nsu.ccfit.persistent.data.structure.map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

class PersistentMapTest {

    private static Stream<Consumer<Map<String, Integer>>> provideModifiersForRemove() {
        return Stream.of(
                (m) -> {
                    m.put("a", 1);
                    m.remove("a");
                },
                (m) -> {
                    m.put("a", 1);
                    m.put("b", 2);
                    m.remove("b");
                },
                (m) -> {
                    m.put("a", 1);
                    m.put("b", 2);
                    m.remove("a");
                },
                (m) -> {
                    m.put("a", 1);
                    m.put("b", 2);
                    m.put("c", 3);
                    m.remove("b");
                },
                (m) -> {
                    m.put("a", 1);
                    m.put("b", 2);
                    m.put("c", 3);
                    m.remove("c");
                },
                (m) -> {
                    m.put("a", 1);
                    m.put("b", 2);
                    m.put("c", 3);
                    m.remove("a");
                }
        );
    }

    @Test
    void undo_once_success() {
        PersistentMap<String, Integer> map = new PersistentMap<>();
        map.put("a", 1);

        map.undo();
        Assertions.assertTrue(map.isEmpty());
        map.redo();
        Assertions.assertFalse(map.isEmpty());
        Assertions.assertEquals(1, map.get("a"));
    }

    @Test
    void undo_twice_success() {
        PersistentMap<String, Integer> map = new PersistentMap<>();
        map.put("a", 1);
        map.put("b", 2);

        map.undo();
        map.undo();
        Assertions.assertTrue(map.isEmpty());
        map.redo();
        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(1, map.get("a"));
        map.redo();
        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals(1, map.get("a"));
        Assertions.assertEquals(2, map.get("b"));
    }

    @Test
    void redo_afterModification_doNotChangeState() {
        PersistentMap<String, Integer> map = new PersistentMap<>();
        map.put("b", 2); // v1
        map.put("a", 1); // v2
        map.undo(); // to v1
        map.put("c", 3); // v2
        map.redo(); // should still be v2

        Assertions.assertEquals(2, map.size());
        Assertions.assertNull(map.get("a"));
        Assertions.assertEquals(3, map.get("c"));
        Assertions.assertEquals(2, map.get("b"));
    }

    @Test
    void size_emptyMap_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {};
        Function<Map<String, Integer>, Integer> effectFeature = Map::size;

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void size_notEmptyMap_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.put("b", 2);
            m.put("a", 1);
        };
        Function<Map<String, Integer>, Integer> effectFeature = Map::size;

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void isEmpty_emptyMap_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {};
        Function<Map<String, Integer>, Boolean> effectFeature = Map::isEmpty;

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void isEmpty_notEmptyMap_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.put("b", 2);
            m.put("a", 1);
        };
        Function<Map<String, Integer>, Boolean> effectFeature = Map::isEmpty;

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void containsKey_noKey_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.put("b", 2);
            m.put("a", 1);
        };
        Function<Map<String, Integer>, Boolean> effectFeature = (m) -> m.containsKey("c");

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void containsKey_withKey_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.put("b", 2);
            m.put("a", 1);
        };
        Function<Map<String, Integer>, Boolean> effectFeature = (m) -> m.containsKey("a");

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void containsValue_noValue_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.put("b", 2);
            m.put("a", 1);
        };
        Function<Map<String, Integer>, Boolean> effectFeature = (m) -> m.containsValue(3);

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void containsValue_withValue_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.put("b", 2);
            m.put("a", 1);
        };
        Function<Map<String, Integer>, Boolean> effectFeature = (m) -> m.containsValue(1);

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void get_noEntry_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.put("b", 2);
            m.put("a", 1);
        };
        Function<Map<String, Integer>, Integer> effectFeature = (m) -> m.get("c");

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void containsValue_withEntry_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.put("b", 2);
            m.put("a", 1);
        };
        Function<Map<String, Integer>, Integer> effectFeature = (m) -> m.get("a");

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void put_newKey_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.put("b", 2);
            m.put("a", 1);
        };
        Function<Map<String, Integer>, Integer> effectFeature = (m) -> m.put("c", 3);

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void put_existedKey_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.put("b", 2);
            m.put("a", 1);
        };
        Function<Map<String, Integer>, Integer> effectFeature = (m) -> m.put("a", 3);

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void putAll_toEmpty_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.putAll(
                    Map.of(
                            "a", 1,
                            "b", 2
                    )
            );
        };
        Function<Map<String, Integer>, Set<Map.Entry<String, Integer>>> effectFeature = Map::entrySet;

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void putAll_toNotEmpty_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.putAll(
                    Map.of(
                            "a", 1,
                            "b", 2
                    )
            );
            m.putAll(
                    Map.of(
                            "a", 10,
                            "b", 20
                    )
            );
        };
        Function<Map<String, Integer>, Set<Map.Entry<String, Integer>>> effectFeature = Map::entrySet;

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void clear_toEmpty_success() {
        Consumer<Map<String, Integer>> modifier = Map::clear;
        Function<Map<String, Integer>, Set<Map.Entry<String, Integer>>> effectFeature = Map::entrySet;

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void clear_toNotEmpty_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.putAll(
                    Map.of(
                            "a", 1,
                            "b", 2
                    )
            );
        };
        Function<Map<String, Integer>, Set<Map.Entry<String, Integer>>> effectFeature = Map::entrySet;

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void keySet_toEmpty_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {};
        Function<Map<String, Integer>, Set<String>> effectFeature = Map::keySet;

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void keySet_toNotEmpty_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.putAll(
                    Map.of(
                            "a", 1,
                            "b", 2
                    )
            );
        };
        Function<Map<String, Integer>, Set<String>> effectFeature = Map::keySet;

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void values_toEmpty_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {};
        Function<Map<String, Integer>, Collection<Integer>> effectFeature = (m) -> new ArrayList<>(m.values());

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void values_toNotEmpty_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.putAll(
                    Map.of(
                            "a", 1,
                            "b", 2
                    )
            );
        };
        Function<Map<String, Integer>, Collection<Integer>> effectFeature = (m) -> new ArrayList<>(m.values());

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void entrySet_toEmpty_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {};
        Function<Map<String, Integer>, Set<Map.Entry<String, Integer>>> effectFeature = Map::entrySet;

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void entrySet_toNotEmpty_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.putAll(
                    Map.of(
                            "a", 1,
                            "b", 2
                    )
            );
        };
        Function<Map<String, Integer>, Set<Map.Entry<String, Integer>>> effectFeature = Map::entrySet;

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void remove_toEmpty_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.remove("a");
        };
        Function<Map<String, Integer>, Set<Map.Entry<String, Integer>>> effectFeature = Map::entrySet;

        assertSameModifyEffect(modifier, effectFeature);
    }

    @Test
    void remove_toSingleElement_success() {
        Consumer<Map<String, Integer>> modifier = (m) -> {
            m.put("a", 1);
            m.remove("a");
        };
        Function<Map<String, Integer>, Set<Map.Entry<String, Integer>>> effectFeature = Map::entrySet;

        assertSameModifyEffect(modifier, effectFeature);
    }

    @ParameterizedTest
    @MethodSource("provideModifiersForRemove")
    void remove_toNotEmpty_success(Consumer<Map<String, Integer>> modifier) {
        Function<Map<String, Integer>, Set<Map.Entry<String, Integer>>> effectFeature = Map::entrySet;

        assertSameModifyEffect(modifier, effectFeature);
    }

    private <T> void assertSameModifyEffect(
            Consumer<Map<String, Integer>> modifier,
            Function<Map<String, Integer>, T> effectFeature) {
        PersistentMap<String, Integer> map = new PersistentMap<>();
        HashMap<String, Integer> expectedMap = new HashMap<>();
        modifier.accept(map);
        modifier.accept(expectedMap);

        var actualEffect = effectFeature.apply(map);
        var expectedEffect = effectFeature.apply(expectedMap);

        Assertions.assertEquals(expectedEffect, actualEffect);
    }


}