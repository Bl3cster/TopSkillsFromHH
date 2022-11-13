package ru.chameleon.topskillsfromhhru;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.yaal.project.hhapi.HhApi;
import ru.yaal.project.hhapi.dictionary.Constants;
import ru.yaal.project.hhapi.search.SearchException;
import ru.yaal.project.hhapi.search.parameter.Text;
import ru.yaal.project.hhapi.vacancy.Salary;
import ru.yaal.project.hhapi.vacancy.Vacancy;
import ru.yaal.project.hhapi.vacancy.VacancyList;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import static ru.yaal.project.hhapi.dictionary.Constants.Experience.EXPERIENCES;

public class Skills {

    public static void main(String[] args) throws SearchException, IOException {
        Scanner stringScanner = new Scanner(System.in);
        System.out.println("Введите название профессии для поиска ключевых навыков. \n Для выхода из программы введите \"end\"");
        String request = stringScanner.nextLine();
        if (request.equals("end")) {
            return;
        }
        System.out.println("Введите минимальную желаемую зарплату:");
        int minSalary = stringScanner.nextInt();
        System.out.println("""
                Введите цифру 0, если у Вас нет опыта;\s
                Введите 1, если Ваш опыт от 1 года до 3 лет;\s
                Введите 2, если Ваш опыт от 3 до 6 лет;\s
                Введите 3, если Ваш опыт более 6 лет.\s
                """);
        int experience = stringScanner.nextInt();
        switch (experience) {
            case 0 -> System.out.println(getVacancies(request, minSalary, "noExperience"));
            case 1 -> System.out.println(getVacancies(request, minSalary, "between1And3"));
            case 2 -> System.out.println(getVacancies(request, minSalary, "between3And6"));
            case 3 -> System.out.println(getVacancies(request, minSalary, "moreThan6"));
        }
        main(args);
    }

    public static Map<String, Integer> getVacancies(String request, int minSalary, String experiences) throws SearchException, IOException {
        // get vacancies by name (limit = 100)
        VacancyList vacancies = HhApi.search(100,
                new Text(request, Constants.VacancySearchFields.VACANCY_NAME),
                new Salary(minSalary, null, Constants.Currency.RUR),
                EXPERIENCES.getById(experiences));

        Map<String, Integer> mapOfSkills = new HashMap<>();
        // iterate by vacancies
        for (Vacancy vacancy : vacancies) {
            ObjectNode objectNode = new ObjectMapper().readValue(vacancy.getUrl(), ObjectNode.class);
            // iterate by skills
            for (JsonNode skill : objectNode.findValues("key_skills")) {
                // iterate by skill names
                for (String name : skill.findValuesAsText("name")) {
                    mapOfSkills.put(name, mapOfSkills.getOrDefault(name, 0) + 1);
                }
            }
        }
        // return sorted map of skills
        return mapOfSkills.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}