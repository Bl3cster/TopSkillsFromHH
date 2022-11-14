package ru.chameleon.topskillsfromhhru;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.yaal.project.hhapi.dictionary.Constants;
import ru.yaal.project.hhapi.search.ISearch;
import ru.yaal.project.hhapi.search.ISearchParameter;
import ru.yaal.project.hhapi.search.SearchException;
import ru.yaal.project.hhapi.search.SearchParamNames;
import ru.yaal.project.hhapi.search.SearchParameterBox;
import ru.yaal.project.hhapi.search.parameter.Text;
import ru.yaal.project.hhapi.vacancy.Salary;
import ru.yaal.project.hhapi.vacancy.Vacancy;
import ru.yaal.project.hhapi.vacancy.VacancyList;
import ru.yaal.project.hhapi.vacancy.VacancySearch;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.yaal.project.hhapi.dictionary.Constants.Currency.CURRENCIES;
import static ru.yaal.project.hhapi.dictionary.Constants.Experience.EXPERIENCES;

public class Skills {
    static String currency;
    static String experience;

    public static void main(String[] args) throws SearchException, IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите название профессии для поиска ключевых навыков. \n Для выхода из программы введите \"end\" \n");
        String request = scanner.nextLine();
        if (request.equals("end")) {
            return;
        }
        System.out.println("""
                Введите цифру 0, если хотите узнать подборку навыков по вакансии без учёта зарплаты и прочих параметров;\s
                Введите 1, если желаете задать параметры поиска.
                """);
        int dreamJob = scanner.nextInt();
        if (dreamJob == 0) {
            Map<String, Integer> map = getVacancies(request);

            for (Map.Entry<String, Integer> s : map.entrySet()) {
                System.out.println(s.getKey() + " -> " + s.getValue());
            }
        } else {
            System.out.println("""
                    Введите цифру 0, если хотите получать зарплату в рублях;\s
                    Введите 1, если хотите получать зарплату в долларах;\s
                    Введите 2, если хотите получать зарплату в евро;\s
                    Введите 3, если хотите получать зарплату в манатах\s
                    Введите 4, если хотите получать зарплату в белорусских рублях\s
                    Введите 5, если хотите получать зарплату в тенге\s
                    Введите 6, если хотите получать зарплату в гривнах
                    """);
            int currencyFind = scanner.nextInt();
            switch (currencyFind) {
                case 0 -> currency = "RUR";
                case 1 -> currency = "USD";
                case 2 -> currency = "EUR";
                case 4 -> currency = "AZN";
                case 5 -> currency = "BYR";
                case 6 -> currency = "KZT";
                case 7 -> currency = "UAH";

            }
            System.out.println("Введите минимальную желаемую зарплату: \n");
            int minSalary = scanner.nextInt();
            System.out.println("""
                    Введите цифру 0, если у Вас нет опыта;\s
                    Введите 1, если Ваш опыт от 1 года до 3 лет;\s
                    Введите 2, если Ваш опыт от 3 до 6 лет;\s
                    Введите 3, если Ваш опыт более 6 лет.
                    """);
            int experienceFind = scanner.nextInt();
            switch (experienceFind) {
                case 0 -> experience = "noExperience";
                case 1 -> experience = "between1And3";
                case 2 -> experience = "between3And6";
                case 3 -> experience = "moreThan6";
            }
            System.out.println(getVacancies(request, minSalary, experience, currency));
        }
        main(args);
    }

    public static Map<String, Integer> getVacancies(String request, int minSalary, String experiences, String currency) throws SearchException, IOException {
        // get vacancies by name (limit = 100)
        ISearchParameter text = new Text(request, Constants.VacancySearchFields.VACANCY_NAME);
        ISearchParameter salary = new Salary(minSalary, null, CURRENCIES.getById(currency));
        ISearchParameter experience = EXPERIENCES.getById(experiences);
        ISearchParameter onlyWithSalary = Constants.OnlyWithSalary.ON;
        Map<String, Integer> mapOfSkills;

        List<Vacancy> vacancies = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ISearch<VacancyList> search = new VacancySearch(100)
                    .addParameter(text)
                    .addParameter(salary)
                    .addParameter(experience)
                    .addParameter(onlyWithSalary)
                    .addParameter(new SearchParameterBox(SearchParamNames.PAGE, String.valueOf(i)));
            vacancies.addAll(search.search());
        }
        mapOfSkills = countSkills(vacancies);
        // return sorted map of skills
        return mapOfSkills.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static Map<String, Integer> getVacancies(String request) throws SearchException, IOException {
        // get vacancies by name (limit = 100)
        ISearchParameter text = new Text(request, Constants.VacancySearchFields.VACANCY_NAME);
        Map<String, Integer> mapOfSkills;
        List<Vacancy> vacancies = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ISearch<VacancyList> search = new VacancySearch(100)
                    .addParameter(text)
                    .addParameter(new SearchParameterBox(SearchParamNames.PAGE, String.valueOf(i)));
            vacancies.addAll(search.search());
        }
        mapOfSkills = countSkills(vacancies);
        // return sorted map of skills
        return mapOfSkills.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private static Map<String, Integer> countSkills(List<Vacancy> vacancies) throws IOException {
        Map<String, Integer> mapOfSkills = new HashMap<>();
        // iterate by vacancies
        for (Vacancy vacancy : vacancies) {
            ObjectNode objectNode = new ObjectMapper().readValue(vacancy.getUrl(), ObjectNode.class);
            for (JsonNode node : objectNode.findValues("key_skills")) {
                for (String skill : node.findValuesAsText("name")) {
                    mapOfSkills.put(skill, mapOfSkills.getOrDefault(skill, 0) + 1);
                }
            }
        }
        return mapOfSkills;
    }
}