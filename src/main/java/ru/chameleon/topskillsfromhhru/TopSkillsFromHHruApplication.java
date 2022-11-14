package ru.chameleon.topskillsfromhhru;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.yaal.project.hhapi.search.SearchException;

import java.io.IOException;

@SpringBootApplication
public class TopSkillsFromHHruApplication {



    public static void main(String[] args) throws SearchException, IOException {
        SpringApplication.run(TopSkillsFromHHruApplication.class, args);
        Skills skills = new Skills();
        skills.findSkill();
    }
}