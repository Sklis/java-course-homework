package ru.skliss.testframework;

import ru.skliss.testframework.exception.TestAssertionError;

import java.util.Objects;

/**
 * Небольшой набор проверок для написания тестов на нашем фреймворке.
 * Любая непройденная проверка бросает {@link TestAssertionError}, что
 * TestRunner интерпретирует как результат FAILED (в отличие от любого
 * другого исключения, которое даёт ERROR).
 */
public final class Assertions {

    private Assertions() {
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new TestAssertionError(message);
        }
    }

    public static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new TestAssertionError(message + " (ожидалось: " + expected + ", получено: " + actual + ")");
        }
    }

    public static void assertNotNull(Object actual, String message) {
        if (actual == null) {
            throw new TestAssertionError(message);
        }
    }

    public static void fail(String message) {
        throw new TestAssertionError(message);
    }
}
