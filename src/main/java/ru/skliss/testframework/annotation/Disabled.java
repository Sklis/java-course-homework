package ru.skliss.testframework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Вешается на метод, уже помеченный {@link Test}.
 * Такой тест попадёт в отчёт со статусом SKIPPED, но выполняться не будет.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Disabled {
}
