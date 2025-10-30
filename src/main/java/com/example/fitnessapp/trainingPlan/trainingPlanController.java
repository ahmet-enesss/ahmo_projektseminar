package com.example.fitnessapp.trainingPlan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class trainingPlanController {

    @Autowired
    trainingPlanService trainingPlanServiceService;

    @RequestMapping("/trainingPlan")
    public List<trainingPlan> getVideolist() {
        return trainingPlanServiceService.getTrainingPlanList();
    }
    @RequestMapping("/trainingPlan/{id}")
    public trainingPlan getTrainingPlan(@PathVariable Long id) {
        return trainingPlanServiceService.getTrainingPlan(id);
    }
    @PostMapping(value="/trainingPlan")
    public void addTrainingPlan(@RequestBody trainingPlan trainingPlan) {
        trainingPlanServiceService.addTrainingPlan(trainingPlan);
    }
    @PutMapping("/trainingPlan/{id}")
    public void updateTrainingPlan(@PathVariable Long id , @RequestBody trainingPlan trainingPlan) {
        trainingPlanServiceService.updateTrainingPlan(id, trainingPlan);
    }

}
