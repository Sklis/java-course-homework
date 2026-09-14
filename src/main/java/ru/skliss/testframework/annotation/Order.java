package ru.skliss.testframework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Вешается на тестовые методы. Задаёт порядок исполнения в диапазоне [1..10].
 * Тесты с меньшим значением выполняются раньше тестов с большим значением.
 * По умолчанию 5.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Order {

    int value() default 5;
}
