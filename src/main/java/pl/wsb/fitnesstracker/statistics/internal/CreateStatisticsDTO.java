package pl.wsb.fitnesstracker.statistics.internal;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for creating or updating Statistics.
 */
@Getter
@Setter
public class CreateStatisticsDTO {
    private final Long id;
    private final Long userId;
    private final int totalTrainings;
    private final double totalDistance;
    private final int totalCaloriesBurned;
    
    /**
     * Creates a new CreateStatisticsDTO with the specified values.
     *
     * @param id The ID of the statistics (null for new statistics)
     * @param userId The ID of the user associated with these statistics
     * @param totalTrainings The total number of trainings
     * @param totalDistance The total distance covered in trainings
     * @param totalCaloriesBurned The total calories burned in trainings
     */
    CreateStatisticsDTO(Long id, Long userId, int totalTrainings, double totalDistance, int totalCaloriesBurned) {
        this.id = id;
        this.userId = userId;
        this.totalTrainings = totalTrainings;
        this.totalDistance = totalDistance;
        this.totalCaloriesBurned = totalCaloriesBurned;
    }
}