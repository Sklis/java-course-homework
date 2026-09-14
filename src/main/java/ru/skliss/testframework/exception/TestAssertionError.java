package ru.skliss.testframework.exception;

/**
 * Бросается из тестового метода (например, из методов {@code Assertions}),
 * когда проверяемое условие теста не выполнено. Такой тест помечается
 * результатом FAILED (в отличие от любого другого исключения, которое
 * помечает тест как ERROR).
 */
public class TestAssertionError extends RuntimeException {

    public TestAssertionError(String message) {
        super(message);
    }
}
