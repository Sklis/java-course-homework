package ru.skliss.testframework.exception;

/**
 * Бросается, когда класс с тестами структурно некорректен, например:
 * <ul>
 *     <li>от класса невозможно создать объект (нет доступного конструктора
 *         без параметров, конструктор бросает исключение и т.п.);</li>
 *     <li>методы @Test, @BeforeEach или @AfterEach объявлены static;</li>
 *     <li>методы @BeforeSuite или @AfterSuite объявлены НЕ static.</li>
 * </ul>
 * Это ошибка конфигурации тестового класса, а не результат исполнения теста,
 * поэтому она прерывает запуск {@code TestRunner.runTests(...)} целиком.
 */
public class BadTestClassError extends RuntimeException {

    public BadTestClassError(String message) {
        super(message);
    }

    public BadTestClassError(String message, Throwable cause) {
        super(message, cause);
    }
}
