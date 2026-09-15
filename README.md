# Задание 1. Разработка аналога JUnit (обработчик аннотаций)

Решение домашнего задания «Разработка обработчика аннотаций»: минимальный
аналог JUnit, размечающий тестовые методы аннотациями, запускающий их
в заданном порядке и возвращающий сгруппированный по результату отчёт.

## Структура проекта

```
src/main/java/ru/skliss/testframework/
├── annotation/
│   ├── Test.java          @Test(value, priority)
│   ├── Disabled.java      @Disabled
│   ├── BeforeEach.java    @BeforeEach
│   ├── AfterEach.java     @AfterEach
│   ├── BeforeSuite.java   @BeforeSuite (только static)
│   ├── AfterSuite.java    @AfterSuite  (только static)
│   └── Order.java         @Order(value)
├── model/Test.java        результат одного теста: result, name, exception
├── exception/
│   ├── BadTestClassError.java   ошибка структуры тестового класса
│   └── TestAssertionError.java  ошибка проваленной проверки в тесте
├── TestResult.java        enum: SUCCESS, FAILED, ERROR, SKIPPED
├── Assertions.java        assertTrue / assertFalse / assertEquals / assertNotNull / fail
├── TestRunner.java        точка входа: runTests(Class<?>)
└── demo/
    ├── SampleTestClass.java   примеры тестов: по одному на каждый результат
    │                          + @Test без атрибутов, assertFalse, assertNotNull
    └── Main.java              запуск демо и печать отчёта

src/test/java/ru/skliss/testframework/TestRunnerTest.java
    юнит-тесты самого фреймворка (на настоящем JUnit 5) — 12 тестов
```

Класс `Test` (результат) и аннотация `Test` (`@Test`) намеренно лежат
в разных пакетах (`model` и `annotation`), как и указано в задании — оба
называются `Test`, конфликт снят через пакеты и полные имена в импортах.

## Как это устроено

- `TestRunner.runTests(Class<?> c)` — единственная точка входа. Сначала
  проверяет структуру класса и **до** запуска любого теста
  бросает `BadTestClassError`, если:
  - у класса нет доступного конструктора без параметров / его нельзя создать;
  - `@Test`, `@BeforeEach` или `@AfterEach` висят на `static` методе;
  - `@BeforeSuite` или `@AfterSuite` висят на **не** `static` методе;
  - `@BeforeSuite` или `@AfterSuite` объявлены более одного раза;
  - `@Disabled` используется на методе без `@Test`;
  - значение `priority` в `@Test` выходит за пределы [0..10];
  - значение `@Order` выходит за пределы [1..10].
- Для каждого теста создаётся **новый экземпляр класса** (как в JUnit),
  затем выполняются `@BeforeEach` → тест → `@AfterEach` (даже если тест упал).
- `@BeforeSuite`/`@AfterSuite` выполняются ровно один раз вокруг всех тестов;
  `@AfterSuite` гарантированно вызывается через `try/finally`.
- `@Disabled` тест попадает в отчёт со статусом `SKIPPED` и не выполняется
  (`@BeforeEach`/`@AfterEach` для него тоже не вызываются).
- Если тест бросил `TestAssertionError` → результат `FAILED`.
  Если тест бросил любое другое исключение → результат `ERROR`.
- Если падает `@BeforeEach` — сам тестовый метод не запускается вовсе, тест
  помечается `ERROR`, а `@AfterEach` всё равно пытается выполниться.
  Если падает `@AfterEach` после успешного теста — тест помечается `ERROR`.
  Падение хука никогда не прерывает выполнение всего класса.
- Если `@BeforeSuite`/`@AfterSuite` сами бросают исключение — оно
  пробрасывается как есть (не оборачивается в `BadTestClassError`).

### Порядок выполнения тестов

В задании порядок описан дважды: через `priority` в `@Test` (0–10,
больше — раньше, при равенстве — по имени) и отдельно через `@Order`
(1–10, меньше — раньше). Чтобы учесть оба требования, используется
составной компаратор:

1. `@Order` по возрастанию (меньше — раньше);
2. при равенстве `@Order` — `priority` по убыванию (выше приоритет — раньше);
3. при равенстве обоих — по имени теста.

## Сборка и запуск

Стандартный Maven-проект (Java 17+):

```bash
mvn test              # прогнать юнит-тесты фреймворка
mvn compile exec:java # собрать и запустить демо (SampleTestClass + Main)
```

Демо печатает пошаговый лог хуков и итоговый отчёт по каждому `TestResult`.

## Что проверено

Всё проверено 12 юнит-тестами в `TestRunnerTest` (JUnit 5):

| Тест | Что проверяет |
|---|---|
| `classifiesEachOutcomeCorrectly` | все 4 результата: SUCCESS / FAILED / ERROR / SKIPPED |
| `respectsExecutionOrder` | сортировка по `@Order` |
| `suiteHooksRunOnce` | `@BeforeSuite`/`@AfterSuite` вызываются ровно по одному разу |
| `staticTestMethodIsRejected` | `static @Test` → `BadTestClassError` |
| `instanceBeforeSuiteIsRejected` | не-`static @BeforeSuite` → `BadTestClassError` |
| `nonInstantiableClassIsRejected` | нет конструктора без параметров → `BadTestClassError` |
| `failingBeforeEachIsReportedAsErrorInsteadOfCrashingTheRun` | падение `@BeforeEach` → ERROR, остальные тесты продолжают выполняться |
| `multipleBeforeSuiteIsRejected` | два `@BeforeSuite` → `BadTestClassError` |
| `multipleAfterSuiteIsRejected` | два `@AfterSuite` → `BadTestClassError` |
| `disabledWithoutTestIsRejected` | `@Disabled` без `@Test` → `BadTestClassError` |
| `outOfRangePriorityIsRejected` | `priority=11` → `BadTestClassError` |
| `outOfRangeOrderIsRejected` | `@Order(0)` → `BadTestClassError` |
