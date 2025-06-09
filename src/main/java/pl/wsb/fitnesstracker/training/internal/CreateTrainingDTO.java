package pl.wsb.fitnesstracker.training.internal;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * Data Transfer Object (DTO) for creating or updating a training.
 * 
 * This class represents the data needed to create or update a training record.
 * Unlike the TrainingDto, this class uses IDs instead of entity references
 * for related objects like User, making it suitable for API requests.
 * 
 * @author Fitness Tracker Team
 * @version 2.0
 */
@Getter
@ToString
@EqualsAndHashCode
public class CreateTrainingDTO {
    /** Unique identifier for the training (null for new trainings) */
    private final Long id;

    /** ID of the user who performed the training */
    private final Long userId;

    /** When the training started */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private final Date startTime;

    /** When the training ended */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private final Date endTime;

    /** Type of activity performed (e.g., RUNNING, CYCLING) */
    private final ActivityType activityType;

    /** Distance covered during the training in kilometers */
    private final double distance;

    /** Average speed maintained during the training in km/h */
    private final double averageSpeed;

    /**
     * Creates a new CreateTrainingDTO with the specified parameters.
     * 
     * @param id Unique identifier for the training (null for new trainings)
     * @param userId ID of the user who performed the training
     * @param startTime When the training started
     * @param endTime When the training ended
     * @param activityType Type of activity performed
     * @param distance Distance covered during the training in kilometers
     * @param averageSpeed Average speed maintained during the training in km/h
     */
    public CreateTrainingDTO(Long id, Long userId, Date startTime, Date endTime, 
                           ActivityType activityType, double distance, double averageSpeed) {
        this.id = id;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activityType = activityType;
        this.distance = distance;
        this.averageSpeed = averageSpeed;
    }

    /**
     * Validates if this training data is valid.
     * 
     * @return true if the training data is valid, false otherwise
     */
    public boolean isValid() {
        // Check for required fields
        if (userId == null || startTime == null || endTime == null || activityType == null) {
            return false;
        }

        // Check if end time is after start time
        if (endTime.before(startTime)) {
            return false;
        }

        // Check if distance and average speed are non-negative
        return distance >= 0 && averageSpeed >= 0;
    }
}
