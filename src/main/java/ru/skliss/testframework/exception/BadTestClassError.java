package ru.skliss.testframework.exception;

/**
 * Бросается, когда класс с тестами структурно некорректен, например:
 * <ul>
 *     <li>от класса невозможно создать объект (нет доступного конструктора
 *         без параметров, конструктор бросает исключение и т.п.);</li>
 *     <li>методы {@code @Test}, {@code @BeforeEach} или {@code @AfterEach}
 *         объявлены {@code static};</li>
 *     <li>методы {@code @BeforeSuite} или {@code @AfterSuite} объявлены
 *         НЕ {@code static};</li>
 *     <li>аннотация {@code @Disabled} используется без {@code @Test};</li>
 *     <li>значение {@code priority} или {@code @Order} выходит за допустимый диапазон.</li>
 * </ul>
 * Это ошибка конфигурации тестового класса, а не результат исполнения теста,
 * поэтому она прерывает запуск {@code TestRunner.runTests(...)} целиком.
 */
public class BadTestClassError extends RuntimeException {

    /**
     * Создаёт исключение с описанием структурной проблемы.
     *
     * @param message описание проблемы
     */
    public BadTestClassError(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с описанием структурной проблемы и первопричиной.
     *
     * @param message описание проблемы
     * @param cause   исходное исключение (например, {@link NoSuchMethodException})
     */
    public BadTestClassError(String message, Throwable cause) {
        super(message, cause);
    }
}
