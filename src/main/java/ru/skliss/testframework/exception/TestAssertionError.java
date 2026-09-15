package ru.skliss.testframework.exception;

/**
 * Бросается из тестового метода (например, из методов {@code Assertions}),
 * когда проверяемое условие теста не выполнено.
 *
 * <p>Такой тест помечается результатом {@code FAILED} — в отличие от любого
 * другого исключения, которое помечает тест как {@code ERROR}.
 */
public class TestAssertionError extends RuntimeException {

    /**
     * Создаёт исключение с описанием провала проверки.
     *
     * @param message описание провала (что именно ожидалось и что получено)
     */
    public TestAssertionError(String message) {
        super(message);
    }
}
