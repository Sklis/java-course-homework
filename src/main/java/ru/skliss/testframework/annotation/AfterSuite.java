package ru.skliss.testframework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Метод, помеченный этой аннотацией, выполняется один раз после всех тестов
 * класса. Метод обязательно должен быть static.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterSuite {
}
