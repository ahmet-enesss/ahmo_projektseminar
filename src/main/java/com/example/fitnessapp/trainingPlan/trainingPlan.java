package com.example.fitnessapp.trainingPlan;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class trainingPlan{
    public trainingPlan() {
        super();

    }
    public trainingPlan(Long id, String name, String description, String sessions) {
        super();
        this.id= id;
        this.name= name;
        this.description = description;
        this.sessions= sessions;
    }


    @Id
    private long id ;
    private String name;
    private String description;
    private String sessions;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id= id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {this.name= name;}
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getSessions() {return sessions;}
    public void setSessions(String sessions) {
        this.sessions = sessions;
    }

}
