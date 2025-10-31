package com.example.fitnessapp;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RequestMapping("/")
    public String sayHello() {
        return "Hey folks!";
    }
    @RequestMapping("/bye")
    public String sayBye() {
        return "Adios!";
    }
    @RequestMapping("/goodnight")
    public String sayGoodNight() {
        return "Schlaf gut!";
    }

}