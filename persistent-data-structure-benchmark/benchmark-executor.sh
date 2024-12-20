#!/bin/sh

> "fnm_in.txt"
for i in $(seq 10 35); do
    arg_n=$((i * 1000))
    ~/.jdks/openjdk-21.0.1/bin/java -jar target/persistent-data-structure-benchmark-0.0.1-SNAPSHOT.jar FatNodePersistentMap 1000 FatNodePersistentMap "$arg_n" >> "fnm_in.txt"
done

> "m_in.txt"
for i in $(seq 10 35); do
    arg_n=$((i * 1000))
    ~/.jdks/openjdk-21.0.1/bin/java -jar target/persistent-data-structure-benchmark-0.0.1-SNAPSHOT.jar PersistentMap 1000 PersistentMap "$arg_n" >> "m_in.txt"
done