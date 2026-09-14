package ru.skliss.testframework.demo;

import ru.skliss.testframework.TestResult;
import ru.skliss.testframework.TestRunner;
import ru.skliss.testframework.model.Test;

import java.util.List;
import java.util.Map;

/**
 * Точка входа для ручного запуска демонстрации: {@code mvn compile exec:java}.
 */
public class Main {

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
