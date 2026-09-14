package ru.skliss.testframework.demo;

import ru.skliss.testframework.annotation.AfterEach;
import ru.skliss.testframework.annotation.AfterSuite;
import ru.skliss.testframework.annotation.BeforeEach;
import ru.skliss.testframework.annotation.BeforeSuite;
import ru.skliss.testframework.annotation.Disabled;
import ru.skliss.testframework.annotation.Order;
import ru.skliss.testframework.annotation.Test;

import static ru.skliss.testframework.Assertions.assertEquals;
import static ru.skliss.testframework.Assertions.assertTrue;

/**
 * Демонстрационный класс с тестами: показывает, как пользоваться
 * фреймворком, и по одному разу задействует каждый результат
 * (SUCCESS, FAILED, ERROR, SKIPPED).
 */
public class SampleTestClass {

    private static int suiteTestsSeen;
    private int perTestCounter;

    @BeforeSuite
    public static void initSuite() {
        suiteTestsSeen = 0;
        System.out.println(">> BeforeSuite: инициализация набора тестов");
    }

    @AfterSuite
    public static void tearDownSuite() {
        System.out.println(">> AfterSuite: обработано тестов = " + suiteTestsSeen);
    }

    @BeforeEach
    public void setUp() {
        perTestCounter++;
        System.out.println("  BeforeEach перед тестом #" + perTestCounter);
    }

    @AfterEach
    public void tearDown() {
        suiteTestsSeen++;
        System.out.println("  AfterEach после теста #" + perTestCounter);
    }

    @Test(value = "Сложение работает корректно", priority = 8)
    @Order(1)
    public void additionTest() {
        assertEquals(4, 2 + 2, "2 + 2 должно быть равно 4");
    }

    @Test(value = "Проверка, которая обязана провалиться", priority = 3)
    @Order(3)
    public void failingTest() {
        assertTrue(1 > 2, "1 не может быть больше 2");
    }

    @Test("Тест с непредвиденным исключением")
    @Order(2)
    public void erroringTest() {
        throw new IllegalStateException("Что-то пошло не так внутри теста");
    }

    @Test("Отключенный тест")
    @Disabled
    public void disabledTest() {
        assertTrue(false, "этот код не должен выполниться");
    }
}
