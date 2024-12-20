package ru.nsu.ccfit.persistent.data.structure;

import ru.nsu.ccfit.persistent.data.structure.map.FatNodePersistentMap;
import ru.nsu.ccfit.persistent.data.structure.map.PersistentMap;

import java.util.Arrays;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        try {
            executeUnsafe(args);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
        }
    }

    private static void executeUnsafe(String[] args) {
        if (args.length != 4) {
            throw new IllegalArgumentException("Specify preheat structure, preheat n, structure, n");
        }
        // preheat
        execute(Arrays.copyOfRange(args, 0, 2), false);

        // benchmark
        execute(Arrays.copyOfRange(args, 2, 4), true);
    }

    private static void execute(String[] args, boolean log) {
        var structureName = args[0];
        var n = Long.parseLong(args[1]);
        if (structureName.endsWith("Map")) {
            executeMap(getMapFromType(structureName), n, log);
        } else {
            throw new IllegalArgumentException("Unknown structure");
        }
    }

    private static void executeMap(Map<Long, Long> map, long n, boolean log) {
        // insert
        var startTime = System.currentTimeMillis();
        for (long i = 0L; i < n; ++i) {
            map.put(i, i);
        }
        var endTime = System.currentTimeMillis();
        var insertTime = endTime - startTime;

        // read
        startTime = System.currentTimeMillis();
        for (long i = 0L; i < n; ++i) {
            map.get(i);
        }
        endTime = System.currentTimeMillis();
        var readTime = endTime - startTime;

        // update
        startTime = System.currentTimeMillis();
        for (long i = 0L; i < n; ++i) {
            map.put(i, -i);
        }
        endTime = System.currentTimeMillis();
        var updateTime = endTime - startTime;
        if (log) {
            System.out.printf("%d %d %d%n", insertTime, readTime, updateTime);
        }
    }

    private static Map<Long, Long> getMapFromType(String type) {
        if (type.equals("PersistentMap")) {
            return new PersistentMap<>();
        } else if (type.equals("FatNodePersistentMap")) {
            return new FatNodePersistentMap<>();
        } else {
            throw new IllegalArgumentException("Unknown map type");
        }
    }

}
