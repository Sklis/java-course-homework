package ru.skliss.testframework.demo;

import ru.skliss.testframework.annotation.AfterEach;
import ru.skliss.testframework.annotation.AfterSuite;
import ru.skliss.testframework.annotation.BeforeEach;
import ru.skliss.testframework.annotation.BeforeSuite;
import ru.skliss.testframework.annotation.Disabled;
import ru.skliss.testframework.annotation.Order;
import ru.skliss.testframework.annotation.Test;

import static ru.skliss.testframework.Assertions.assertFalse;
import static ru.skliss.testframework.Assertions.assertEquals;
import static ru.skliss.testframework.Assertions.assertNotNull;
import static ru.skliss.testframework.Assertions.assertTrue;

/**
 * Демонстрационный класс с тестами: показывает, как пользоваться
 * фреймворком, и по одному разу задействует каждый возможный результат
 * ({@code SUCCESS}, {@code FAILED}, {@code ERROR}, {@code SKIPPED}).
 */
public class SampleTestClass {

    /** Счётчик исполненных тестов за весь набор — инкрементируется в {@link #tearDown()}. */
    private static int suiteTestsSeen;

    /** Порядковый номер текущего теста внутри экземпляра класса. */
    private int perTestCounter;

    /**
     * Инициализирует состояние перед запуском всего набора тестов.
     * Сбрасывает счётчик {@link #suiteTestsSeen}.
     */
    @BeforeSuite
    public static void initSuite() {
        suiteTestsSeen = 0;
        System.out.println(">> BeforeSuite: инициализация набора тестов");
    }

    /**
     * Выводит итоговую статистику после завершения всего набора тестов.
     */
    @AfterSuite
    public static void tearDownSuite() {
        System.out.println(">> AfterSuite: обработано тестов = " + suiteTestsSeen);
    }

    /**
     * Выполняется перед каждым тестом: увеличивает счётчик и печатает номер теста.
     */
    @BeforeEach
    public void setUp() {
        perTestCounter++;
        System.out.println("  BeforeEach перед тестом #" + perTestCounter);
    }

    /**
     * Выполняется после каждого теста: увеличивает глобальный счётчик
     * и печатает номер завершённого теста.
     */
    @AfterEach
    public void tearDown() {
        suiteTestsSeen++;
        System.out.println("  AfterEach после теста #" + perTestCounter);
    }

    /**
     * Демонстрирует результат {@code SUCCESS}: проверяет, что 2 + 2 = 4.
     * Запускается первым ({@code @Order(1)}) с высоким приоритетом ({@code priority = 8}).
     */
    @Test(value = "Сложение работает корректно", priority = 8)
    @Order(1)
    public void additionTest() {
        assertEquals(4, 2 + 2, "2 + 2 должно быть равно 4");
    }

    /**
     * Демонстрирует результат {@code FAILED}: намеренно проваливает проверку
     * (1 не может быть больше 2), что бросает {@code TestAssertionError}.
     */
    @Test(value = "Проверка, которая обязана провалиться", priority = 3)
    @Order(3)
    public void failingTest() {
        assertTrue(1 > 2, "1 не может быть больше 2");
    }

    /**
     * Демонстрирует результат {@code ERROR}: бросает непредвиденное исключение,
     * не являющееся {@code TestAssertionError}.
     */
    @Test("Тест с непредвиденным исключением")
    @Order(2)
    public void erroringTest() {
        throw new IllegalStateException("Что-то пошло не так внутри теста");
    }

    /**
     * Демонстрирует результат {@code SKIPPED}: тест помечен {@link Disabled}
     * и не будет выполнен, но появится в отчёте.
     */
    @Test("Отключенный тест")
    @Disabled
    public void disabledTest() {
        assertTrue(false, "этот код не должен выполниться");
    }

    /**
     * Демонстрирует {@link Test} без атрибутов: имя теста берётся из имени
     * Java-метода ({@code "noAttributesTest"}), приоритет и порядок — по умолчанию (5).
     */
    @Test
    public void noAttributesTest() {
        assertTrue(2 + 2 == 4, "2 + 2 должно быть равно 4");
    }

    /**
     * Демонстрирует {@code assertFalse}: проверяет, что условие ложно.
     */
    @Test("assertFalse: отрицательная проверка")
    public void assertFalseTest() {
        assertFalse(1 > 2, "1 не должно быть больше 2");
    }

    /**
     * Демонстрирует {@code assertNotNull}: проверяет, что объект не равен {@code null}.
     */
    @Test("assertNotNull: проверка на null")
    public void assertNotNullTest() {
        assertNotNull("строка", "строка не должна быть null");
    }
}
