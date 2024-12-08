package ru.nsu.ccfit.persistent.data.structure.map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PersistentMapTest {

    @Test
    void put_one_success() {
        PersistentMap<String, Integer> map = new PersistentMap<>();
        map.put("a", 1);

        map.undo();
        Assertions.assertTrue(map.isEmpty());
        map.redo();
        Assertions.assertFalse(map.isEmpty());
        Assertions.assertEquals(1, map.get("a"));
    }

    @Test
    void put_two_success() {
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
        map.put("a", 1); // v1
        map.put("b", 2); // v2
        map.undo(); // to v1
        map.undo(); // to v0
        map.put("c", 3); // v1
        map.redo(); // should still be v1

        Assertions.assertEquals(1, map.size());
        Assertions.assertNull(map.get("a"));
        Assertions.assertNull(map.get("b"));
        Assertions.assertEquals(3, map.get("c"));
    }

}