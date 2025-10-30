package com.example.fitnessapp.Excercise;

import com.example.fitnessapp.trainingPlan.trainingPlan;
import org.springframework.data.repository.CrudRepository;

public interface trainingPlanRepository extends CrudRepository<trainingPlan,Long> {

}