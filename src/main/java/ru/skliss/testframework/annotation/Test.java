package ru.skliss.testframework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Помечает метод как тестовый. Метод должен быть исполняемым (не static)
 * методом без параметров.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {

    /**
     * Имя теста. Если не задано - используется имя метода.
     */
    String value() default "";

    /**
     * Приоритет теста в диапазоне [0..10], где 10 - самый высокий приоритет.
     * По умолчанию 5.
     */
    int priority() default 5;
}
