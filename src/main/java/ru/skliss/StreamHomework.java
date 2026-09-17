package ru.skliss;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Stream API — Задание 2.
 * Java 17. Один класс: тестовые данные, все решения и main для демонстрации.
 * Все методы решений — static.
 */
public class StreamHomework {

    /** Модель "Сотрудник" для заданий 3 и 4. Java 17 record — сразу immutable + equals/hashCode/toString. */
    record Employee(String name, int age, String position) {
    }

    /** ---------------------------------------------------------------------
     1. 3-е наибольшее число в списке (дубликаты считаются отдельно)
        5 2 10 9 4 3 10 1 13 => 10
     --------------------------------------------------------------------- */
    static Optional<Integer> thirdLargest(List<Integer> numbers) {
        return numbers.stream()
                .sorted(Comparator.reverseOrder())
                .skip(2)
                .findFirst();
    }

    /** ---------------------------------------------------------------------
     2. 3-е наибольшее "уникальное" число (повторяющиеся значения считаются одним числом)
        5 2 10 9 4 3 10 1 13 => 9
     --------------------------------------------------------------------- */
    static Optional<Integer> thirdLargestUnique(List<Integer> numbers) {
        return numbers.stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .skip(2)
                .findFirst();
    }

    /** ---------------------------------------------------------------------
     3. Имена 3 самых старших "Инженер", по убыванию возраста
     --------------------------------------------------------------------- */
    static List<String> topThreeOldestEngineerNames(List<Employee> employees, String s) {
        return employees.stream()
                .filter(e -> e.position().equals(s))
                .sorted(Comparator.comparingInt(Employee::age).reversed())
                .limit(3)
                .map(Employee::name)
                .collect(Collectors.toList());
    }

    /** ---------------------------------------------------------------------
     4. Средний возраст сотрудников с должностью "Инженер"
     --------------------------------------------------------------------- */
    static double averageAgeOfEngineers(List<Employee> employees, String s) {
        return employees.stream()
                .filter(e -> e.position().equals(s))
                .mapToInt(Employee::age)
                .average()
                .orElse(0.0);
    }

    /** ---------------------------------------------------------------------
     5. Самое длинное слово в списке слов
     --------------------------------------------------------------------- */
    static Optional<String> longestWord(List<String> words) {
        return words.stream()
                .max(Comparator.comparingInt(String::length));
    }

    /** ---------------------------------------------------------------------
     6. Строка слов в нижнем регистре через пробел -> HashMap "слово -> кол-во вхождений"
     --------------------------------------------------------------------- */
    static Map<String, Long> wordFrequency(String text) {
        return Arrays.stream(text.trim().split("\\s+"))
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        HashMap::new,
                        Collectors.counting()));
    }

    /** ---------------------------------------------------------------------
     7. Печать строк по возрастанию длины; при равной длине — по алфавиту
     --------------------------------------------------------------------- */
    static void printSortedByLengthThenAlphabetically(List<String> words) {
        words.stream()
                .sorted(Comparator.comparingInt(String::length)
                        .thenComparing(Comparator.naturalOrder()))
                .forEach(System.out::println);
    }

    /** ---------------------------------------------------------------------
     8. Массив строк по 5 слов через пробел -> самое длинное слово среди всех
        (если таких несколько — вернуть любое)
     --------------------------------------------------------------------- */
    static Optional<String> longestWordInArrayOfPhrases(String[] phrases) {
        return Arrays.stream(phrases)
                .flatMap(phrase -> Arrays.stream(phrase.trim().split("\\s+")))
                .max(Comparator.comparingInt(String::length));
    }

    /** ---------------------------------------------------------------------
     main — тестовые данные + демонстрация всех решений
     --------------------------------------------------------------------- */
    public static void main(String[] args) {

        // 1- е задание
        System.out.println("=== 1. 3-е наибольшее число (с повторами) ===");
        List<Integer> numbers = List.of(5, 2, 10, 9, 4, 3, 10, 1, 13);
        System.out.println("Вход: " + numbers);
        System.out.println("Результат: " + thirdLargest(numbers).orElse(null)); // 10

        //2-е задание
        System.out.println();
        System.out.println("=== 2. 3-е наибольшее уникальное число ===");
        System.out.println("Вход: " + numbers);
        System.out.println("Результат: " + thirdLargestUnique(numbers).orElse(null)); // 9

        // 3-е задание
        System.out.println();
        System.out.println("=== 3. Топ-3 самых старших 'Инженер' (имена, по убыванию возраста) ===");
        List<Employee> employees = List.of(
                new Employee("Иванов", 45, "Инженер"),
                new Employee("Петров", 38, "Инженер"),
                new Employee("Сидоров", 52, "Инженер"),
                new Employee("Кузнецова", 29, "Инженер"),
                new Employee("Смирнов", 60, "Менеджер"),
                new Employee("Волков", 41, "Инженер"),
                new Employee("Морозова", 33, "Бухгалтер")
        );
        System.out.println("Вход: ");
        employees.forEach(System.out::println);
        System.out.println("Результат: " + topThreeOldestEngineerNames(employees, "Инженер"));

        // 4-е задание
        System.out.println();
        System.out.println("=== 4. Средний возраст 'Инженер' ===");
        System.out.println("Вход: ");
        employees.forEach(System.out::println);
        System.out.println("Результат: " + averageAgeOfEngineers(employees, "Инженер"));

        // 5-е задание
        System.out.println();
        System.out.println("=== 5. Самое длинное слово в списке ===");
        List<String> words = List.of("кот", "программирование", "стрим", "java", "функциональный");
        System.out.println("Вход: " + words);
        System.out.println("Результат: " + longestWord(words).orElse(null));
        System.out.println("Количество символов: " + longestWord(words).orElse(null).length());

        // 6-е задание
        System.out.println();
        System.out.println("=== 6. Частота слов в строке (HashMap) ===");
        String text = "java stream java collector map stream java";
        System.out.println("Вход: \"" + text + "\"");
        System.out.println("Результат: " + wordFrequency(text));

        // 7-е задание
        System.out.println();
        System.out.println("=== 7. Строки по возрастанию длины, при равенстве — по алфавиту ===");
        List<String> mixedWords = List.of("бб", "яблоко", "а", "гусь", "аа", "кот", "дом");
        System.out.println("Вход: " + mixedWords);
        printSortedByLengthThenAlphabetically(mixedWords);

        // 8-е задание
        System.out.println();
        System.out.println("=== 8. Самое длинное слово среди массива фраз по 5 слов ===");
        String[] phrases = {
                "быстрая лиса прыгает через забор",
                "программирование это очень интересное занятие",
                "маленький кот спит на окне"
        };
        System.out.println("Вход: " + Arrays.toString(phrases));
        System.out.println("Результат: " + longestWordInArrayOfPhrases(phrases).orElse(null));
        System.out.printf("Количество символов: " + longestWordInArrayOfPhrases(phrases).orElse(null).length());

        // Финиш
    }
}
