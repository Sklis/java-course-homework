package ru.skliss;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class StreamHomework {

    record Employee(String name, int age, String position) {}

    public static void main(String[] args) {
        List<Integer> numbers = List.of(5, 2, 10, 9, 4, 3, 10, 1, 13);
        List<Employee> employees = List.of(
                new Employee("Алексей",  45, "Инженер"),
                new Employee("Мария",    38, "Менеджер"),
                new Employee("Дмитрий", 52, "Инженер"),
                new Employee("Ольга",   29, "Инженер"),
                new Employee("Петр",   41, "Инженер"),
                new Employee("Анна",    33, "Аналитик")
        );
        List<String> words = List.of("apple", "banana", "kiwi", "strawberry", "fig");
        String sentence = "кот пёс кот рыба пёс кот дом";
        List<String> lines = List.of("banana", "apple", "kiwi", "fig", "cherry", "avocado");
        String[] matrix = {
                "one two three four five",
                "six seven eight nine ten",
                "alpha beta gamma delta epsilon"
        };

        // Задача 1: 3-е наибольшее число (с повторами)
        Integer third = numbers.stream()
                .sorted(Comparator.reverseOrder())
                .skip(2)
                .findFirst()
                .orElseThrow();
        System.out.println("1. 3-е наибольшее (с повторами): " + third);

        // Задача 2: 3-е наибольшее уникальное число
        Integer thirdUnique = numbers.stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .skip(2)
                .findFirst()
                .orElseThrow();
        System.out.println("2. 3-е наибольшее уникальное: " + thirdUnique);

        // Задача 3: имена 3 старших инженеров в порядке убывания возраста
        List<String> topEngineers = employees.stream()
                .filter(e -> "Инженер".equals(e.position()))
                .sorted(Comparator.comparingInt(Employee::age).reversed())
                .limit(3)
                .map(Employee::name)
                .collect(Collectors.toList());
        System.out.println("3. Топ-3 старших инженера: " + topEngineers);

        // Задача 4: средний возраст инженеров
        OptionalDouble avgAge = employees.stream()
                .filter(e -> "Инженер".equals(e.position()))
                .mapToInt(Employee::age)
                .average();
        System.out.printf("4. Средний возраст инженеров: %.1f%n", avgAge.orElse(0));

        // Задача 5: самое длинное слово
        String longest = words.stream()
                .max(Comparator.comparingInt(String::length))
                .orElseThrow();
        System.out.println("5. Самое длинное слово: " + longest);

        // Задача 6: частота слов в строке
        Map<String, Long> frequency = Arrays.stream(sentence.split(" "))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        System.out.println("6. Частота слов: " + frequency);

        // Задача 7: слова отсортированы по длине, при равной — алфавитный порядок
        List<String> sorted = lines.stream()
                .sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()))
                .collect(Collectors.toList());
        System.out.println("7. По длине (алфавит при равных): " + sorted);

        // Задача 8: самое длинное слово среди всех строк массива
        String longestInMatrix = Arrays.stream(matrix)
                .flatMap(line -> Arrays.stream(line.split(" ")))
                .max(Comparator.comparingInt(String::length))
                .orElseThrow();
        System.out.println("8. Самое длинное в массиве строк: " + longestInMatrix);
    }
}
