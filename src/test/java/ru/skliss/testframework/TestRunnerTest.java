package ru.skliss.testframework;

import org.junit.jupiter.api.DisplayName;
import ru.skliss.testframework.annotation.AfterSuite;
import ru.skliss.testframework.annotation.BeforeSuite;
import ru.skliss.testframework.annotation.Disabled;
import ru.skliss.testframework.annotation.Order;
import ru.skliss.testframework.annotation.Test;
import ru.skliss.testframework.exception.BadTestClassError;
import ru.skliss.testframework.exception.TestAssertionError;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты для собственного мини-фреймворка (написаны на настоящем JUnit 5,
 * чтобы проверить, что TestRunner работает правильно).
 */
class TestRunnerTest {

    @org.junit.jupiter.api.Test
    @DisplayName("Успешный, проваленный, ошибочный и отключенный тесты корректно распределяются по результату")
    void classifiesEachOutcomeCorrectly() {
        Map<TestResult, List<ru.skliss.testframework.model.Test>> report = TestRunner.runTests(FixtureAllOutcomes.class);

        assertEquals(1, report.get(TestResult.SUCCESS).size());
        assertEquals(1, report.get(TestResult.FAILED).size());
        assertEquals(1, report.get(TestResult.ERROR).size());
        assertEquals(1, report.get(TestResult.SKIPPED).size());

        assertEquals("successTest", report.get(TestResult.SUCCESS).get(0).getName());
        assertTrue(report.get(TestResult.FAILED).get(0).getException() instanceof TestAssertionError);
        assertTrue(report.get(TestResult.ERROR).get(0).getException() instanceof IllegalStateException);
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Тесты выполняются в порядке @Order, при равенстве - по убыванию priority")
    void respectsExecutionOrder() {
        FixtureOrdering.executionLog.clear();
        TestRunner.runTests(FixtureOrdering.class);
        assertEquals(List.of("first", "second", "third"), FixtureOrdering.executionLog);
    }

    @org.junit.jupiter.api.Test
    @DisplayName("BeforeSuite/AfterSuite выполняются по одному разу вокруг всех тестов")
    void suiteHooksRunOnce() {
        FixtureSuiteHooks.beforeSuiteCalls = 0;
        FixtureSuiteHooks.afterSuiteCalls = 0;
        TestRunner.runTests(FixtureSuiteHooks.class);
        assertEquals(1, FixtureSuiteHooks.beforeSuiteCalls);
        assertEquals(1, FixtureSuiteHooks.afterSuiteCalls);
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Static @Test метод -> BadTestClassError")
    void staticTestMethodIsRejected() {
        assertThrows(BadTestClassError.class, () -> TestRunner.runTests(FixtureStaticTest.class));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Не static @BeforeSuite метод -> BadTestClassError")
    void instanceBeforeSuiteIsRejected() {
        assertThrows(BadTestClassError.class, () -> TestRunner.runTests(FixtureInstanceBeforeSuite.class));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Класс без конструктора без параметров -> BadTestClassError")
    void nonInstantiableClassIsRejected() {
        assertThrows(BadTestClassError.class, () -> TestRunner.runTests(FixtureNoDefaultConstructor.class));
    }

    // ---- Тестовые классы-фикстуры ----

    static class FixtureAllOutcomes {
        @Test
        void successTest() {
            ru.skliss.testframework.Assertions.assertEquals(4, 2 + 2, "2+2=4");
        }

        @Test
        void failedTest() {
            ru.skliss.testframework.Assertions.fail("специально проваленный тест");
        }

        @Test
        void erroringTest() {
            throw new IllegalStateException("бум");
        }

        @Test
        @Disabled
        void disabledTest() {
            throw new AssertionError("не должно выполниться");
        }
    }

    static class FixtureOrdering {
        static final List<String> executionLog = new java.util.ArrayList<>();

        @Test
        @Order(2)
        void second() {
            executionLog.add("second");
        }

        @Test
        @Order(1)
        void first() {
            executionLog.add("first");
        }

        @Test
        @Order(3)
        void third() {
            executionLog.add("third");
        }
    }

    static class FixtureSuiteHooks {
        static int beforeSuiteCalls;
        static int afterSuiteCalls;

        @BeforeSuite
        static void before() {
            beforeSuiteCalls++;
        }

        @AfterSuite
        static void after() {
            afterSuiteCalls++;
        }

        @Test
        void t1() {
        }

        @Test
        void t2() {
        }
    }

    static class FixtureStaticTest {
        @Test
        static void staticTest() {
        }
    }

    static class FixtureInstanceBeforeSuite {
        @BeforeSuite
        void notStatic() {
        }

        @Test
        void t() {
        }
    }

    static class FixtureNoDefaultConstructor {
        FixtureNoDefaultConstructor(int required) {
        }

        @Test
        void t() {
        }
    }
}
