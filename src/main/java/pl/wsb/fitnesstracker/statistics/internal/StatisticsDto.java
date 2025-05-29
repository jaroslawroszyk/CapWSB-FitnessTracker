package pl.wsb.fitnesstracker.statistics.internal;

import lombok.Getter;
import lombok.Setter;
import pl.wsb.fitnesstracker.user.api.UserDto;

/**
 * Data Transfer Object for Statistics entity.
 */
@Getter
@Setter
public class StatisticsDto {
    private final Long id;
    private final UserDto user;
    private final int totalTrainings;
    private final double totalDistance;
    private final int totalCaloriesBurned;
    
    /**
     * Creates a new StatisticsDto with the specified values.
     *
     * @param id The ID of the statistics
     * @param user The user associated with these statistics
     * @param totalTrainings The total number of trainings
     * @param totalDistance The total distance covered in trainings
     * @param totalCaloriesBurned The total calories burned in trainings
     */
    StatisticsDto(Long id, UserDto user, int totalTrainings, double totalDistance, int totalCaloriesBurned) {
        this.id = id;
        this.user = user;
        this.totalTrainings = totalTrainings;
        this.totalDistance = totalDistance;
        this.totalCaloriesBurned = totalCaloriesBurned;
    }
}