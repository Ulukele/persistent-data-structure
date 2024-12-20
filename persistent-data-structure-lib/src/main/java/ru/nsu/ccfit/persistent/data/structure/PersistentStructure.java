package ru.nsu.ccfit.persistent.data.structure;

/**
 * Структура данных поддерживающая операции возврата к предыдущему состоянию.
 */
public interface PersistentStructure {

    /**
     * Выполняет возврат к предыдущей версии.
     */
    void undo();


    /**
     * Отменяет возврат к предыдущей версии.
     */
    void redo();

}
