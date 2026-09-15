package ru.skliss.testframework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Задаёт порядок исполнения тестового метода в диапазоне [1..10].
 *
 * <p>Тесты с меньшим значением выполняются раньше тестов с большим значением.
 * Если аннотация не задана, порядок по умолчанию равен {@code 5}.
 *
 * <p>При равных значениях {@code @Order} тесты дополнительно сортируются
 * по убыванию {@link Test#priority()}, а затем по имени.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Order {

    /**
     * Порядок исполнения теста. Меньшее значение — раньше выполняется.
     * Допустимый диапазон: [1..10]. По умолчанию {@code 5}.
     *
     * @return порядковый номер теста
     */
    int value() default 5;
}
