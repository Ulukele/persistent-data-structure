package ru.nsu.ccfit.persistent.data.structure.node;

/**
 * Информация об обновлении узла.
 */
public class ModificationBox<T, V extends Comparable<V>> {

    /**
     * Тип обновляемого поля.
     */
    private final ModificationType modificationType;

    /**
     * Версия обновления.
     */
    private final V modificationVersion;

    /**
     * Новое значение узлового типа.
     */
    private final ModificationBoxNode<T, V> nodeModification;

    /**
     * Новое значение в узле.
     */
    private final T valueModification;

    /**
     * Создает обновление левого узла.
     *
     * @param modificationVersion Версия.
     * @param nodeModification    Новое значение левого узла.
     * @param <T>                 Тип значения в узле.
     * @param <V>                 Тип значения версии.
     * @return Обновление левого узла.
     */
    public static <T, V extends Comparable<V>> ModificationBox<T, V> createLeftModification(
            V modificationVersion,
            ModificationBoxNode<T, V> nodeModification) {
        return new ModificationBox<>(
                ModificationType.LEFT,
                modificationVersion,
                nodeModification,
                null
        );
    }

    /**
     * Создает обновление правого узла.
     *
     * @param modificationVersion Версия.
     * @param nodeModification    Новое значение правого узла.
     * @param <T>                 Тип значения в узле.
     * @param <V>                 Тип значения версии.
     * @return Обновление правого узла.
     */
    public static <T, V extends Comparable<V>> ModificationBox<T, V> createRightModification(
            V modificationVersion,
            ModificationBoxNode<T, V> nodeModification) {
        return new ModificationBox<>(
                ModificationType.RIGHT,
                modificationVersion,
                nodeModification,
                null
        );
    }

    /**
     * Создает обновление значения в узле.
     *
     * @param modificationVersion Версия.
     * @param valueModification   Новое значение.
     * @param <T>                 Тип значения в узле.
     * @param <V>                 Тип значения версии.
     * @return Обновление правого узла.
     */
    public static <T, V extends Comparable<V>> ModificationBox<T, V> createValueModification(
            V modificationVersion,
            T valueModification) {
        return new ModificationBox<>(
                ModificationType.VALUE,
                modificationVersion,
                null,
                valueModification
        );
    }

    private ModificationBox(
            ModificationType modificationType,
            V modificationVersion,
            ModificationBoxNode<T, V> nodeModification,
            T valueModification) {
        this.modificationType = modificationType;
        this.modificationVersion = modificationVersion;
        this.nodeModification = nodeModification;
        this.valueModification = valueModification;
    }

    public ModificationType getModificationType() {
        return modificationType;
    }

    public V getModificationVersion() {
        return modificationVersion;
    }

    public ModificationBoxNode<T, V> getNodeModification() {
        return nodeModification;
    }

    public T getValueModification() {
        return valueModification;
    }

}
