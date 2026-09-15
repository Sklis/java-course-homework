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
├── Assertions.java        assertTrue/assertEquals/assertNotNull/fail
├── TestRunner.java        точка входа: runTests(Class<?>)
└── demo/
    ├── SampleTestClass.java   пример тестов (по одному на каждый результат)
    └── Main.java              запуск демо и печать отчёта

src/test/java/ru/skliss/testframework/TestRunnerTest.java
    юнит-тесты самого фреймворка (на настоящем JUnit 5)
```

Класс `Test` (результат) и аннотация `Test` (`@Test`) намеренно лежат
в разных пакетах (`model` и `annotation`), как и указано в задании — оба
называются `Test`, конфликт снят через пакеты и полные имена в импортax.

## Как это устроено

- `TestRunner.runTests(Class<?> c)` — единственная точка входа. Сначала
  проверяет структуру класса (пункт 3 задания) и **до** запуска любого теста
  бросает `BadTestClassError`, если:
  - у класса нет доступного конструктора без параметров / его нельзя создать;
  - `@Test`, `@BeforeEach` или `@AfterEach` висят на `static` методе;
  - `@BeforeSuite` или `@AfterSuite` висят на **не** `static` методе.
- Для каждого теста создаётся **новый экземпляр класса** (как в JUnit),
  затем выполняются `@BeforeEach` → тест → `@AfterEach` (даже если тест упал).
- `@BeforeSuite`/`@AfterSuite` выполняются один раз вокруг всех тестов.
- `@Disabled` тест попадает в отчёт со статусом `SKIPPED` и не выполняется
  (соответственно `@BeforeEach`/`@AfterEach` для него тоже не вызываются).
- Если тест бросил `TestAssertionError` → результат `FAILED`.
  Если тест бросил любое другое исключение → результат `ERROR`.
- Если падает `@BeforeEach` — сам тестовый метод не запускается вовсе, тест
  помечается `ERROR`, а `@AfterEach` всё равно пытается выполниться (для
  очистки ресурсов). Если падает `@AfterEach` после успешного теста — тест
  тоже помечается `ERROR`. Важно: падение хука никогда не прерывает
  выполнение всего класса — оно превращается в `ERROR` только у текущего
  теста, остальные тесты продолжают выполняться.

### Порядок выполнения тестов

В тексте задания порядок описан дважды: через `priority` в `@Test` (0–10,
больше — раньше, при равенстве — по имени) и отдельно через `@Order`
(1–10, меньше — раньше). Чтобы учесть оба требования, решение использует
составной компаратор:

1. `@Order` по возрастанию (меньше — раньше);
2. при равенстве `@Order` — `priority` по убыванию (выше приоритет — раньше);
3. при равенстве обоих — по имени теста.

Если в вашей версии задания это два независимых/взаимоисключающих
требования — компаратор в `TestRunner.testOrderComparator()` меняется
в одном месте.

## Сборка и запуск

Стандартный Maven-проект (Java 17+):

```bash
mvn test              # прогнать юнит-тесты фреймворка
mvn compile exec:java # собрать и запустить демо (SampleTestClass + Main)
```

Демо печатает пошаговый лог хуков и итоговый отчёт по каждому `TestResult`.

## Что проверено

- Логика (`TestRunner`, сортировка, хуки, классификация результатов)
  проверена вручную через `javac`/`java` и юнит-тестами в
  `TestRunnerTest` — все 4 результата (`SUCCESS`, `FAILED`, `ERROR`,
  `SKIPPED`), порядок исполнения по `@Order`/`priority`, разовый вызов
  `@BeforeSuite`/`@AfterSuite`, а также все три сценария `BadTestClassError`
  (static `@Test`, не-static `@BeforeSuite`, отсутствие конструктора).
- Отдельно есть регрессионный тест на падение `@BeforeEach`
  (`failingBeforeEachIsReportedAsErrorInsteadOfCrashingTheRun`): раньше
  исключение из `@BeforeEach` вылетало из `TestRunner.runTests()`
  необработанным и обрушивало прогон всего класса вместо того, чтобы
  аккуратно пометить один тест как `ERROR`. Исправлено.
