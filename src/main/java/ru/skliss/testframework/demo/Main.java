package ru.skliss.testframework.demo;

import ru.skliss.testframework.TestResult;
import ru.skliss.testframework.TestRunner;
import ru.skliss.testframework.model.Test;

import java.util.List;
import java.util.Map;

/**
 * Точка входа для ручного запуска демонстрации.
 *
 * <p>Запуск: {@code mvn compile exec:java}
 */
public class Main {

    /**
     * Запускает тесты из {@link SampleTestClass} и печатает сводный отчёт.
     *
     * <p>Для каждого возможного результата ({@link TestResult}) выводится
     * количество тестов и детальная строка по каждому из них.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        Map<TestResult, List<Test>> report = TestRunner.runTests(SampleTestClass.class);

        System.out.println();
        System.out.println("===== Отчёт о прохождении тестов =====");
        for (TestResult result : TestResult.values()) {
            List<Test> tests = report.get(result);
            System.out.println(result + " (" + tests.size() + "):");
            for (Test t : tests) {
                System.out.println("  - " + t);
            }
        }
    }
}
