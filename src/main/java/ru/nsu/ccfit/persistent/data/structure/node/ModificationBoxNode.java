package ru.nsu.ccfit.persistent.data.structure.node;

/**
 * Обновляемый узел.
 *
 * @param <T> Тип значения в узле.
 * @param <V> Тип значения версии.
 */
public class ModificationBoxNode<T, V extends Comparable<V>> {

    /**
     * Левый узел.
     */
    private final ModificationBoxNode<T, V> left;

    /**
     * Правый узел.
     */
    private final ModificationBoxNode<T, V> right;

    /**
     * Значение в узле.
     */
    private final T value;

    /**
     * Обновление узла.
     */
    private ModificationBox<T, V> modificationBox;

    private ModificationBoxNode(
            ModificationBoxNode<T, V> left,
            ModificationBoxNode<T, V> right,
            T value,
            ModificationBox<T, V> modificationBox) {
        this.left = left;
        this.right = right;
        this.value = value;
        this.modificationBox = modificationBox;
    }

    public ModificationBoxNode(
            ModificationBoxNode<T, V> left,
            ModificationBoxNode<T, V> right,
            T value) {
        this(
                left,
                right,
                value,
                null
        );
    }

    /**
     * Возвращает значение левого узла в запрашиваемой версии.
     *
     * @param version Версия.
     * @return Значение левого узла в запрашиваемой версии.
     */
    public ModificationBoxNode<T, V> getLeft(V version) {
        if (modificationBox == null) {
            return left;
        } else if (modificationBox.getModificationType() != ModificationType.LEFT) {
            return left;
        } else if (modificationBox.getModificationVersion().compareTo(version) > 0) {
            return left;
        } else {
            return modificationBox.getNodeModification();
        }
    }

    /**
     * Возвращает значение правого узла в запрашиваемой версии.
     *
     * @param version Версия.
     * @return Значение правого узла в запрашиваемой версии.
     */
    public ModificationBoxNode<T, V> getRight(V version) {
        if (modificationBox == null) {
            return right;
        } else if (modificationBox.getModificationType() != ModificationType.RIGHT) {
            return right;
        } else if (modificationBox.getModificationVersion().compareTo(version) > 0) {
            return right;
        } else {
            return modificationBox.getNodeModification();
        }
    }

    /**
     * Возвращает значение в запрашиваемой версии.
     *
     * @param version Версия.
     * @return Значение в запрашиваемой версии.
     */
    public T getValue(V version) {
        if (modificationBox == null) {
            return value;
        } else if (modificationBox.getModificationType() != ModificationType.VALUE) {
            return value;
        } else if (modificationBox.getModificationVersion().compareTo(version) > 0) {
            return value;
        } else {
            return modificationBox.getValueModification();
        }
    }

    /**
     * Возвращает текущую модификацию узла.
     *
     * @return Текущую модификацию узла.
     */
    public ModificationBox<T, V> getModificationBox() {
        return modificationBox;
    }

    /**
     * Возвращает обновленный узел.
     *
     * @param modification обновление.
     * @return Обновленный узел.
     */
    public ModificationBoxNode<T, V> modify(ModificationBox<T, V> modification) {
        if (modification == null) {
            throw new IllegalArgumentException("Modification can not be null");
        }
        if (modificationBox == null) {
            modificationBox = modification;
            return this;
        }
        var version = modification.getModificationVersion();
        var modifiedCopy = new ModificationBoxNode<>(
                getLeft(version),
                getRight(version),
                getValue(version)
        ).modify(modification);
        return new ModificationBoxNode<>(
                modifiedCopy.getLeft(version),
                modifiedCopy.getRight(version),
                modifiedCopy.getValue(version),
                null
        );
    }

}
