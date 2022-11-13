package ru.chameleon.topskillsfromhhru;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.yaal.project.hhapi.HhApi;
import ru.yaal.project.hhapi.dictionary.Constants;
import ru.yaal.project.hhapi.search.SearchException;
import ru.yaal.project.hhapi.search.parameter.Text;
import ru.yaal.project.hhapi.vacancy.Vacancy;
import ru.yaal.project.hhapi.vacancy.VacancyList;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Skills {

    public static void main(String[] args) throws SearchException, IOException {
        Scanner stringScanner = new Scanner(System.in);
        System.out.println("Введите название профессии для поиска ключевых навыков. /n Для выхода из программы введите \"end\"");
        String request = stringScanner.nextLine();

        if (request.equals("end")) {
            return;
        }
        System.out.println(getVacancies(request));
        main(args);
    }

    public static Map<String, Integer> getVacancies(String request) throws SearchException, IOException {
        // get vacancies by name (limit = 100)
        VacancyList vacancies = HhApi.search(100, new Text(request, Constants.VacancySearchFields.VACANCY_NAME));
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
