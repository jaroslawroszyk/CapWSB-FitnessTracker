package pl.wsb.fitnesstracker.training.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlyTrainingReport {
    private String userEmail;
    private int trainingCount;
    private double totalDistance;
    private double averageSpeed;

    public MonthlyTrainingReport(String userEmail, int trainingCount, double totalDistance, double averageSpeed) {
        this.userEmail = userEmail;
        this.trainingCount = trainingCount;
        this.totalDistance = totalDistance;
        this.averageSpeed = averageSpeed;
    }
}
