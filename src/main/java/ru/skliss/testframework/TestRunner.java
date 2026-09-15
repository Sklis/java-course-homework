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
 * через {@code priority} аннотации {@link Test} (0..10, больше — раньше,
 * при равенстве — по имени) и отдельно через аннотацию {@link Order}
 * (1..10, меньше — раньше). Чтобы не потерять ни одно из требований, оба
 * признака используются как единый составной ключ сортировки:
 * <ol>
 *     <li>{@link Order#value()} по возрастанию (меньше — раньше);</li>
 *     <li>при равенстве {@link Order} — {@link Test#priority()} по убыванию
 *         (больше — раньше);</li>
 *     <li>при равенстве обоих — имя теста по алфавиту.</li>
 * </ol>
 */
public final class TestRunner {

    /** Утилитный класс — не инстанцируется. */
    private TestRunner() {
    }

    /**
     * Запускает все тесты в указанном классе и возвращает отчёт.
     *
     * <p>Перед запуском проверяет структурную корректность класса
     * и бросает {@link BadTestClassError} при любом нарушении правил
     * (неправильный {@code static}-модификатор, недостижимый конструктор и т.д.).
     *
     * <p>Карта в результате всегда содержит все четыре ключа {@link TestResult},
     * даже если список для какого-либо результата пуст — это позволяет вызывающему
     * коду безопасно обращаться к любому ключу без проверки на {@code null}.
     *
     * @param c класс, содержащий тестовые методы
     * @return карта {@code результат → список детальных записей о тестах}
     * @throws BadTestClassError если класс структурно некорректен
     */
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
        validateSingleHook(beforeSuiteMethods, "@BeforeSuite");
        validateSingleHook(afterSuiteMethods, "@AfterSuite");
        validateDisabledOnlyWithTest(c);
        validatePriorityAndOrder(testMethods);

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

    /**
     * Исполняет один тестовый метод и возвращает его результат.
     *
     * <p>Последовательность действий:
     * <ol>
     *     <li>Если метод помечен {@link Disabled} — немедленно возвращает SKIPPED,
     *         {@link BeforeEach}/{@link AfterEach} не вызываются.</li>
     *     <li>Создаёт свежий экземпляр тестового класса через переданный конструктор.</li>
     *     <li>Вызывает все {@link BeforeEach}-методы. Если один из них бросает
     *         исключение — тест помечается ERROR и тестовый метод не запускается.</li>
     *     <li>Вызывает тестовый метод. {@link TestAssertionError} → FAILED,
     *         любое другое исключение → ERROR.</li>
     *     <li>Вызывает все {@link AfterEach}-методы независимо от исхода теста.
     *         Если {@link AfterEach} падает, а тест до этого прошёл успешно —
     *         результат меняется на ERROR.</li>
     * </ol>
     *
     * @param testMethod        тестовый метод для выполнения
     * @param constructor       конструктор без параметров тестового класса
     * @param beforeEachMethods список методов, помеченных {@link BeforeEach}
     * @param afterEachMethods  список методов, помеченных {@link AfterEach}
     * @return детальная запись о результате теста
     */
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
            throw new BadTestClassError(
                    "Не удалось создать экземпляр класса " + constructor.getDeclaringClass().getName(), e);
        }

        TestResult result = TestResult.SUCCESS;
        Throwable failure = null;

        try {
            invokeInstanceHooks(beforeEachMethods, instance);
        } catch (InvocationTargetException e) {
            // @BeforeEach упал — тестовый метод не запускаем, тест считается ошибочным.
            result = TestResult.ERROR;
            failure = e.getCause();
        } catch (ReflectiveOperationException e) {
            result = TestResult.ERROR;
            failure = e;
        }

        if (result == TestResult.SUCCESS) {
            try {
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
        }

        try {
            invokeInstanceHooks(afterEachMethods, instance);
        } catch (InvocationTargetException e) {
            // Если до этого момента всё было хорошо, но упал @AfterEach — считаем тест ошибочным.
            if (result == TestResult.SUCCESS) {
                result = TestResult.ERROR;
                failure = e.getCause();
            }
        } catch (ReflectiveOperationException e) {
            if (result == TestResult.SUCCESS) {
                result = TestResult.ERROR;
                failure = e;
            }
        }

        return new ru.skliss.testframework.model.Test(result, name, failure);
    }

    /**
     * Возвращает имя теста: значение атрибута {@link Test#value()} если задано,
     * иначе — имя Java-метода.
     *
     * @param testMethod метод, помеченный {@link Test}
     * @return непустая строка с именем теста
     */
    private static String resolveTestName(Method testMethod) {
        Test annotation = testMethod.getAnnotation(Test.class);
        String value = annotation.value();
        return (value == null || value.isBlank()) ? testMethod.getName() : value;
    }

    /**
     * Строит составной компаратор для сортировки тестовых методов перед запуском.
     *
     * <p>Ключи сортировки применяются в следующем порядке:
     * <ol>
     *     <li>{@link Order#value()} по возрастанию;</li>
     *     <li>{@link Test#priority()} по убыванию;</li>
     *     <li>имя теста по алфавиту.</li>
     * </ol>
     *
     * @return компаратор для упорядочивания методов
     */
    private static Comparator<Method> testOrderComparator() {
        Comparator<Method> byOrder = Comparator.comparingInt(m -> orderOf(m));
        Comparator<Method> byPriorityDesc = Comparator.comparingInt((Method m) -> priorityOf(m)).reversed();
        Comparator<Method> byName = Comparator.comparing(TestRunner::resolveTestName);
        return byOrder.thenComparing(byPriorityDesc).thenComparing(byName);
    }

    /**
     * Возвращает значение {@link Order} для метода, или {@code 5} если аннотация отсутствует.
     *
     * @param m тестовый метод
     * @return значение порядка выполнения
     */
    private static int orderOf(Method m) {
        Order order = m.getAnnotation(Order.class);
        return order == null ? 5 : order.value();
    }

    /**
     * Возвращает значение {@link Test#priority()} для метода.
     *
     * @param m метод, помеченный {@link Test}
     * @return приоритет теста
     */
    private static int priorityOf(Method m) {
        return m.getAnnotation(Test.class).priority();
    }

    /**
     * Собирает все методы класса {@code c}, помеченные указанной аннотацией.
     *
     * @param c              класс для поиска
     * @param annotationType тип искомой аннотации
     * @return список найденных методов (может быть пустым)
     */
    private static List<Method> findAnnotated(Class<?> c,
                                               Class<? extends java.lang.annotation.Annotation> annotationType) {
        List<Method> methods = new ArrayList<>();
        for (Method m : c.getDeclaredMethods()) {
            if (m.isAnnotationPresent(annotationType)) {
                methods.add(m);
            }
        }
        return methods;
    }

    /**
     * Проверяет, что ни один из методов не является {@code static}.
     *
     * @param methods        список проверяемых методов
     * @param annotationName имя аннотации для сообщения об ошибке
     * @throws BadTestClassError если хотя бы один метод объявлен {@code static}
     */
    private static void validateNotStatic(List<Method> methods, String annotationName) {
        for (Method m : methods) {
            if (Modifier.isStatic(m.getModifiers())) {
                throw new BadTestClassError(
                        "Метод " + m.getName() + " помечен " + annotationName
                                + ", но объявлен static — так нельзя");
            }
        }
    }

    /**
     * Проверяет, что все методы объявлены {@code static}.
     *
     * @param methods        список проверяемых методов
     * @param annotationName имя аннотации для сообщения об ошибке
     * @throws BadTestClassError если хотя бы один метод не объявлен {@code static}
     */
    private static void validateStatic(List<Method> methods, String annotationName) {
        for (Method m : methods) {
            if (!Modifier.isStatic(m.getModifiers())) {
                throw new BadTestClassError(
                        "Метод " + m.getName() + " помечен " + annotationName
                                + ", но не объявлен static — так нельзя");
            }
        }
    }

    /**
     * Находит конструктор без параметров класса {@code c} и делает его доступным.
     *
     * @param c класс с тестами
     * @return доступный конструктор без параметров
     * @throws BadTestClassError если такого конструктора нет
     */
    private static Constructor<?> resolveConstructor(Class<?> c) {
        try {
            Constructor<?> constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new BadTestClassError(
                    "У класса " + c.getName() + " нет доступного конструктора без параметров", e);
        }
    }

    /**
     * Вызывает статические методы-хуки ({@link BeforeSuite} / {@link AfterSuite}).
     *
     * <p>Если тело хука бросает исключение, оно пробрасывается как есть,
     * не оборачиваясь в {@link BadTestClassError}: это ошибка времени выполнения,
     * а не структурная проблема класса.
     *
     * @param hooks список статических методов для вызова
     * @throws RuntimeException  если тело одного из хуков завершилось с исключением
     * @throws BadTestClassError если метод недоступен для вызова через рефлексию
     */
    private static void invokeStaticHooks(List<Method> hooks) {
        for (Method hook : hooks) {
            try {
                hook.setAccessible(true);
                hook.invoke(null);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException re) throw re;
                if (cause instanceof Error err) throw err;
                throw new RuntimeException(cause);
            } catch (ReflectiveOperationException e) {
                throw new BadTestClassError("Ошибка вызова " + hook.getName(), e);
            }
        }
    }

    /**
     * Проверяет, что хуков указанного типа не более одного.
     *
     * <p>Задание подразумевает единственный {@link BeforeSuite} и единственный
     * {@link AfterSuite} — фраза «выполняются один раз» теряет смысл при нескольких методах.
     *
     * @param methods        найденные методы с данной аннотацией
     * @param annotationName имя аннотации для сообщения об ошибке
     * @throws BadTestClassError если методов больше одного
     */
    private static void validateSingleHook(List<Method> methods, String annotationName) {
        if (methods.size() > 1) {
            throw new BadTestClassError(
                    "Разрешён только один метод " + annotationName + ", найдено: " + methods.size());
        }
    }

    /**
     * Проверяет, что аннотация {@link Disabled} используется только совместно с {@link Test}.
     *
     * <p>По условию задания {@link Disabled} «вешается на метод, где уже есть {@link Test}».
     * Использование {@link Disabled} без {@link Test} — структурная ошибка класса.
     *
     * @param c класс с тестами
     * @throws BadTestClassError если {@link Disabled} обнаружен на методе без {@link Test}
     */
    private static void validateDisabledOnlyWithTest(Class<?> c) {
        for (Method m : c.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Disabled.class) && !m.isAnnotationPresent(Test.class)) {
                throw new BadTestClassError(
                        "Метод " + m.getName() + " помечен @Disabled, но не помечен @Test");
            }
        }
    }

    /**
     * Проверяет, что значения {@link Test#priority()} и {@link Order#value()}
     * лежат в допустимых диапазонах.
     *
     * <p>Допустимые диапазоны согласно заданию:
     * {@code priority} ∈ [0..10], {@code @Order} ∈ [1..10].
     *
     * @param methods тестовые методы для проверки
     * @throws BadTestClassError если хотя бы одно значение выходит за пределы диапазона
     */
    private static void validatePriorityAndOrder(List<Method> methods) {
        for (Method m : methods) {
            int priority = m.getAnnotation(Test.class).priority();
            if (priority < 0 || priority > 10) {
                throw new BadTestClassError(
                        "Метод " + m.getName() + ": priority=" + priority + " вне диапазона [0..10]");
            }
            Order order = m.getAnnotation(Order.class);
            if (order != null && (order.value() < 1 || order.value() > 10)) {
                throw new BadTestClassError(
                        "Метод " + m.getName() + ": @Order=" + order.value() + " вне диапазона [1..10]");
            }
        }
    }

    /**
     * Вызывает объектные хуки ({@link BeforeEach} / {@link AfterEach}) по очереди.
     *
     * <p>Метод намеренно не перехватывает исключения — решение о том,
     * как трактовать падение хука (пропустить тест или пометить его ERROR),
     * принимает вызывающий код {@link #runSingleTest}.
     *
     * @param hooks    список объектных методов для вызова
     * @param instance экземпляр тестового класса
     * @throws ReflectiveOperationException если вызов метода завершился с ошибкой рефлексии
     *                                      или тело метода бросило исключение
     *                                      (в последнем случае — {@link InvocationTargetException})
     */
    private static void invokeInstanceHooks(List<Method> hooks, Object instance)
            throws ReflectiveOperationException {
        for (Method hook : hooks) {
            hook.setAccessible(true);
            hook.invoke(instance);
        }
    }
}
