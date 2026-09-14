package ru.skliss.testframework;

import ru.skliss.testframework.annotation.AfterEach;
import ru.skliss.testframework.annotation.AfterSuite;
import ru.skliss.testframework.annotation.BeforeEach;
import ru.skliss.testframework.annotation.BeforeSuite;
import ru.skliss.testframework.annotation.Disabled;
import ru.skliss.testframework.annotation.Order;
import ru.skliss.testframework.annotation.Test;
import ru.skliss.testframework.exception.BadTestClassError;
import ru.skliss.testframework.exception.TestAssertionError;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Точка входа фреймворка. Находит тестовые методы в классе, проверяет его
 * структуру, исполняет тесты (с учётом хуков и порядка) и возвращает
 * сгруппированный по результату отчёт.
 *
 * <p><b>Порядок исполнения тестов.</b> В задании порядок описан дважды:
 * через {@code priority} аннотации {@code @Test} (0..10, больше - раньше,
 * при равенстве - по имени) и отдельно через аннотацию {@code @Order}
 * (1..10, меньше - раньше). Чтобы не терять ни одно из требований, оба
 * признака используются как единый составной ключ сортировки:
 * <ol>
 *     <li>{@code @Order} по возрастанию (меньше - раньше);</li>
 *     <li>при равенстве {@code @Order} - {@code priority} по убыванию
 *         (больше - раньше);</li>
 *     <li>при равенстве обоих - по имени теста.</li>
 * </ol>
 */
public final class TestRunner {

    private TestRunner() {
    }

    public static Map<TestResult, List<ru.skliss.testframework.model.Test>> runTests(Class<?> c) {
        List<Method> testMethods = findAnnotated(c, Test.class);
        List<Method> beforeEachMethods = findAnnotated(c, BeforeEach.class);
        List<Method> afterEachMethods = findAnnotated(c, AfterEach.class);
        List<Method> beforeSuiteMethods = findAnnotated(c, BeforeSuite.class);
        List<Method> afterSuiteMethods = findAnnotated(c, AfterSuite.class);

        validateNotStatic(testMethods, "@Test");
        validateNotStatic(beforeEachMethods, "@BeforeEach");
        validateNotStatic(afterEachMethods, "@AfterEach");
        validateStatic(beforeSuiteMethods, "@BeforeSuite");
        validateStatic(afterSuiteMethods, "@AfterSuite");

        Constructor<?> constructor = resolveConstructor(c);

        testMethods.sort(testOrderComparator());

        Map<TestResult, List<ru.skliss.testframework.model.Test>> report = new EnumMap<>(TestResult.class);
        for (TestResult r : TestResult.values()) {
            report.put(r, new ArrayList<>());
        }

        invokeStaticHooks(beforeSuiteMethods);
        try {
            for (Method testMethod : testMethods) {
                ru.skliss.testframework.model.Test outcome =
                        runSingleTest(testMethod, constructor, beforeEachMethods, afterEachMethods);
                report.get(outcome.getResult()).add(outcome);
            }
        } finally {
            invokeStaticHooks(afterSuiteMethods);
        }

        return report;
    }

    private static ru.skliss.testframework.model.Test runSingleTest(Method testMethod,
                                                                      Constructor<?> constructor,
                                                                      List<Method> beforeEachMethods,
                                                                      List<Method> afterEachMethods) {
        String name = resolveTestName(testMethod);

        if (testMethod.isAnnotationPresent(Disabled.class)) {
            return new ru.skliss.testframework.model.Test(TestResult.SKIPPED, name, null);
        }

        Object instance;
        try {
            instance = constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            // Инстанцирование конкретного теста не удалось - это тоже
            // делает класс "плохим тестовым классом".
            throw new BadTestClassError("Не удалось создать экземпляр класса " + constructor.getDeclaringClass().getName(), e);
        }

        TestResult result = TestResult.SUCCESS;
        Throwable failure = null;

        try {
            invokeInstanceHooks(beforeEachMethods, instance);
            testMethod.setAccessible(true);
            testMethod.invoke(instance);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            result = (cause instanceof TestAssertionError) ? TestResult.FAILED : TestResult.ERROR;
            failure = cause;
        } catch (ReflectiveOperationException e) {
            result = TestResult.ERROR;
            failure = e;
        }

        try {
            invokeInstanceHooks(afterEachMethods, instance);
        } catch (RuntimeException e) {
            // Если сам тест прошёл, но упал @AfterEach - считаем тест ошибочным.
            if (result == TestResult.SUCCESS) {
                result = TestResult.ERROR;
                failure = e;
            }
        }

        return new ru.skliss.testframework.model.Test(result, name, failure);
    }

    private static String resolveTestName(Method testMethod) {
        Test annotation = testMethod.getAnnotation(Test.class);
        String value = annotation.value();
        return (value == null || value.isBlank()) ? testMethod.getName() : value;
    }

    private static Comparator<Method> testOrderComparator() {
        Comparator<Method> byOrder = Comparator.comparingInt(m -> orderOf(m));
        Comparator<Method> byPriorityDesc = Comparator.comparingInt((Method m) -> priorityOf(m)).reversed();
        Comparator<Method> byName = Comparator.comparing(TestRunner::resolveTestName);
        return byOrder.thenComparing(byPriorityDesc).thenComparing(byName);
    }

    private static int orderOf(Method m) {
        Order order = m.getAnnotation(Order.class);
        return order == null ? 5 : order.value();
    }

    private static int priorityOf(Method m) {
        Test test = m.getAnnotation(Test.class);
        return test.priority();
    }

    private static List<Method> findAnnotated(Class<?> c, Class<? extends java.lang.annotation.Annotation> annotationType) {
        List<Method> methods = new ArrayList<>();
        for (Method m : c.getDeclaredMethods()) {
            if (m.isAnnotationPresent(annotationType)) {
                methods.add(m);
            }
        }
        return methods;
    }

    private static void validateNotStatic(List<Method> methods, String annotationName) {
        for (Method m : methods) {
            if (Modifier.isStatic(m.getModifiers())) {
                throw new BadTestClassError(
                        "Метод " + m.getName() + " помечен " + annotationName + ", но объявлен static - так нельзя");
            }
        }
    }

    private static void validateStatic(List<Method> methods, String annotationName) {
        for (Method m : methods) {
            if (!Modifier.isStatic(m.getModifiers())) {
                throw new BadTestClassError(
                        "Метод " + m.getName() + " помечен " + annotationName + ", но не объявлен static - так нельзя");
            }
        }
    }

    private static Constructor<?> resolveConstructor(Class<?> c) {
        try {
            Constructor<?> constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new BadTestClassError("У класса " + c.getName() + " нет доступного конструктора без параметров", e);
        }
    }

    private static void invokeStaticHooks(List<Method> hooks) {
        for (Method hook : hooks) {
            try {
                hook.setAccessible(true);
                hook.invoke(null);
            } catch (ReflectiveOperationException e) {
                throw new BadTestClassError("Ошибка при выполнении " + hook.getName(), e);
            }
        }
    }

    private static void invokeInstanceHooks(List<Method> hooks, Object instance) {
        for (Method hook : hooks) {
            try {
                hook.setAccessible(true);
                hook.invoke(instance);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Ошибка при выполнении " + hook.getName(), e.getCause());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Ошибка при выполнении " + hook.getName(), e);
            }
        }
    }
}
