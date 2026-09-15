package ru.skliss.testframework;

import ru.skliss.testframework.exception.TestAssertionError;

import java.util.Objects;

/**
 * Набор статических методов-проверок для написания тестов на нашем фреймворке.
 *
 * <p>Любая непройденная проверка бросает {@link TestAssertionError}, что
 * {@code TestRunner} интерпретирует как результат {@link TestResult#FAILED}
 * (в отличие от любого другого исключения, которое даёт {@link TestResult#ERROR}).
 */
public final class Assertions {

    /** Утилитный класс — не инстанцируется. */
    private Assertions() {
    }

    /**
     * Проверяет, что условие истинно.
     *
     * @param condition проверяемое условие
     * @param message   сообщение об ошибке, если условие ложно
     * @throws TestAssertionError если {@code condition} равно {@code false}
     */
    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new TestAssertionError(message);
        }
    }

    /**
     * Проверяет, что условие ложно.
     *
     * @param condition проверяемое условие
     * @param message   сообщение об ошибке, если условие истинно
     * @throws TestAssertionError если {@code condition} равно {@code true}
     */
    public static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    /**
     * Проверяет, что два объекта равны по {@link Objects#equals}.
     *
     * <p>В сообщение об ошибке автоматически добавляются ожидаемое
     * и фактическое значения.
     *
     * @param expected ожидаемое значение
     * @param actual   фактическое значение
     * @param message  префикс сообщения об ошибке
     * @throws TestAssertionError если объекты не равны
     */
    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new TestAssertionError(
                    message + " (ожидалось: " + expected + ", получено: " + actual + ")");
        }
    }

    /**
     * Проверяет, что объект не {@code null}.
     *
     * @param actual  проверяемый объект
     * @param message сообщение об ошибке, если объект {@code null}
     * @throws TestAssertionError если {@code actual} равно {@code null}
     */
    public static void assertNotNull(Object actual, String message) {
        if (actual == null) {
            throw new TestAssertionError(message);
        }
    }

    /**
     * Безусловно проваливает тест с заданным сообщением.
     *
     * @param message сообщение об ошибке
     * @throws TestAssertionError всегда
     */
    public static void fail(String message) {
        throw new TestAssertionError(message);
    }
}
