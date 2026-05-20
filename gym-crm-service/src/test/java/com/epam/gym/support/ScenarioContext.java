package com.epam.gym.support;

import io.cucumber.spring.ScenarioScope;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope(proxyMode = ScopedProxyMode.NO)
@Scope("cucumber-glue")
public class ScenarioContext {

    private String traineeUsername;
    private String traineePassword;
    private String traineeToken;

    private String trainerUsername;
    private String trainerPassword;

    private String anotherTraineeUsername;

    private int responseStatus;
    private String responseBody;

    public String getTraineeUsername() { return traineeUsername; }
    public void setTraineeUsername(String v) { this.traineeUsername = v; }

    public String getTraineePassword() { return traineePassword; }
    public void setTraineePassword(String v) { this.traineePassword = v; }

    public String getTraineeToken() { return traineeToken; }
    public void setTraineeToken(String v) { this.traineeToken = v; }

    public String getTrainerUsername() { return trainerUsername; }
    public void setTrainerUsername(String v) { this.trainerUsername = v; }

    public String getTrainerPassword() { return trainerPassword; }
    public void setTrainerPassword(String v) { this.trainerPassword = v; }

    public String getAnotherTraineeUsername() { return anotherTraineeUsername; }
    public void setAnotherTraineeUsername(String v) { this.anotherTraineeUsername = v; }

    public int getResponseStatus() { return responseStatus; }
    public void setResponseStatus(int v) { this.responseStatus = v; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String v) { this.responseBody = v; }
}