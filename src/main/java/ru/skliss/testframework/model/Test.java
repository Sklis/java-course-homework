package ru.skliss.testframework.model;

import ru.skliss.testframework.TestResult;

/**
 * Детальное описание результата исполнения одного теста.
 * <p>
 * Класс называется {@code Test} - как и требует условие задания - и лежит
 * в отдельном пакете от аннотации {@code @Test}
 * ({@link ru.skliss.testframework.annotation.Test}), чтобы оба имени могли
 * сосуществовать без конфликта.
 */
public final class Test {

    private final TestResult result;
    private final String name;
    private final Throwable exception;

    public Test(TestResult result, String name, Throwable exception) {
        this.result = result;
        this.name = name;
        this.exception = exception;
    }

    /** Тип результата. */
    public TestResult getResult() {
        return result;
    }

    /** Название теста (имя метода либо значение аннотации @Test). */
    public String getName() {
        return name;
    }

    /** Упавшее исключение, если тест завершился с FAILED или ERROR; иначе null. */
    public Throwable getException() {
        return exception;
    }

    @Override
    public String toString() {
        if (exception == null) {
            return name + " [" + result + "]";
        }
        return name + " [" + result + "] -> "
                + exception.getClass().getSimpleName() + ": " + exception.getMessage();
    }
}
