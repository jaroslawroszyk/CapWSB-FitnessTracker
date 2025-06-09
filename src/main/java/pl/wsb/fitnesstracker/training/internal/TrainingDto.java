package pl.wsb.fitnesstracker.training.internal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import pl.wsb.fitnesstracker.user.api.UserDto;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Data Transfer Object (DTO) for training information.
 * 
 * This class represents training data in a format suitable for API responses.
 * It contains all the necessary information about a training session including
 * user details, timing, activity type, and performance metrics.
 * 
 * @author Fitness Tracker Team
 * @version 2.0
 */
@Getter
@ToString
@EqualsAndHashCode
public class TrainingDto {
    /** Unique identifier for the training */
    private final Long id;

    /** User who performed the training */
    private final UserDto user;

    /** When the training started */
    private final Date startTime;

    /** When the training ended */
    private final Date endTime;

    /** Type of activity performed (e.g., RUNNING, CYCLING) */
    private final ActivityType activityType;

    /** Distance covered during the training in kilometers */
    private final double distance;

    /** Average speed maintained during the training in km/h */
    private final double averageSpeed;

    /**
     * Creates a new TrainingDto with the specified parameters.
     * 
     * @param id Unique identifier for the training
     * @param user User who performed the training
     * @param startTime When the training started
     * @param endTime When the training ended
     * @param activityType Type of activity performed
     * @param distance Distance covered during the training in kilometers
     * @param averageSpeed Average speed maintained during the training in km/h
     */
    public TrainingDto(Long id, UserDto user, Date startTime, Date endTime, 
                      ActivityType activityType, double distance, double averageSpeed) {
        this.id = id;
        this.user = user;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activityType = activityType;
        this.distance = distance;
        this.averageSpeed = averageSpeed;
    }

    /**
     * Calculates the duration of the training in minutes.
     * 
     * @return The duration of the training in minutes, or 0 if start or end time is null
     */
    public long getDurationMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }

        long durationMillis = endTime.getTime() - startTime.getTime();
        return TimeUnit.MILLISECONDS.toMinutes(durationMillis);
    }

    /**
     * Calculates the estimated calories burned during the training.
     * This is a simplified calculation based on activity type, duration, and user weight.
     * 
     * @param weightKg The user's weight in kilograms (default: 70)
     * @return Estimated calories burned
     */
    public int getEstimatedCaloriesBurned(double weightKg) {
        // Default weight if not provided
        double weight = weightKg > 0 ? weightKg : 70.0;

        // MET values (Metabolic Equivalent of Task) for different activities
        double metValue;
        switch (activityType) {
            case RUNNING:
                metValue = 10.0;
                break;
            case CYCLING:
                metValue = 8.0;
                break;
            case SWIMMING:
                metValue = 7.0;
                break;
            case WALKING:
                metValue = 3.5;
                break;
            default:
                metValue = 5.0;
        }

        // Calories = MET * weight (kg) * duration (hours)
        double durationHours = getDurationMinutes() / 60.0;
        return (int) (metValue * weight * durationHours);
    }
}
