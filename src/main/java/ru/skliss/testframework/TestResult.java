package ru.skliss.testframework;

/**
 * Тип результата, с которым завершился тест.
 */
public enum TestResult {
    /** тест выполнен успешно */
    SUCCESS,
    /** условие теста провалено (тест бросил TestAssertionError) */
    FAILED,
    /** тест упал с произвольным исключением */
    ERROR,
    /** тест не исполнялся (помечен @Disabled) */
    SKIPPED
}
