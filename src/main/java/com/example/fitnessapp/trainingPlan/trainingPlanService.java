package com.example.fitnessapp.trainingPlan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class trainingPlanService {

    @Autowired
    private trainingPlanRepository trainingPlanRepository;

    public trainingPlanService(trainingPlanRepository trainingPlanRepository) {
        this.trainingPlanRepository = trainingPlanRepository;
    }

    public List<trainingPlan> getTrainingPlanList() {
        ArrayList<trainingPlan> mylist = new ArrayList<>();
        Iterator<trainingPlan> it = trainingPlanRepository.findAll().iterator();
        while (it.hasNext())
            mylist.add(it.next());
        return mylist;
    }

    public trainingPlan getTrainingPlan(Long id) {
        return trainingPlanRepository.findById(id).orElse(null);
    }

    public void addTrainingPlan(trainingPlan trainingPlan) {
        trainingPlanRepository.save(trainingPlan);
    }

    public void updateTrainingPlan(Long id, trainingPlan trainingPlan) {
        if (trainingPlanRepository.existsById(id)) {
            trainingPlanRepository.save(trainingPlan);
        }
    }
}
