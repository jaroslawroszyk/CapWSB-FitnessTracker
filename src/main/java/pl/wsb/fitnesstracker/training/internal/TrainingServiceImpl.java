package pl.wsb.fitnesstracker.training.internal;

// Standard Java imports
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

// Spring framework imports
import org.springframework.stereotype.Service;

// Jakarta EE imports
import jakarta.persistence.EntityNotFoundException;

// Lombok imports
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Application imports
import pl.wsb.fitnesstracker.training.api.Training;
import pl.wsb.fitnesstracker.training.api.TrainingProvider;
import pl.wsb.fitnesstracker.training.api.TrainingService;
import pl.wsb.fitnesstracker.user.api.User;
import pl.wsb.fitnesstracker.user.api.UserProvider;

/**
 * Implementation of the TrainingService and TrainingProvider interfaces.
 * This class provides comprehensive functionality for managing and retrieving training data
 * in the fitness tracking application.
 * 
 * The implementation follows a structured approach with clear separation of concerns:
 * - Command methods for modifying data (create, update)
 * - Query methods for retrieving data (find, get)
 * - Helper methods for validation and entity operations
 * 
 * @author FitnessTracker Team
 * @version 2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingServiceImpl implements TrainingProvider, TrainingService {
    // ========== Error Message Constants ==========

    /** Error message for null training object */
    private static final String MSG_NULL_TRAINING = "Training object cannot be null";

    /** Error message for null user ID */
    private static final String MSG_NULL_USER_ID = "User ID cannot be null";

    /** Error message for null training ID */
    private static final String MSG_NULL_TRAINING_ID = "Training ID cannot be null";

    /** Error message for training with existing ID during creation */
    private static final String MSG_EXISTING_ID = "Training has already DB ID, update is not permitted!";

    /** Error message for null start time */
    private static final String MSG_NULL_START_TIME = "Start time cannot be null";

    /** Error message for null end time */
    private static final String MSG_NULL_END_TIME = "End time cannot be null";

    /** Error message for null activity type */
    private static final String MSG_NULL_ACTIVITY_TYPE = "Activity type cannot be null";

    /** Error message for invalid time range */
    private static final String MSG_INVALID_TIME_RANGE = "End time must be after start time";

    /** Error message for negative distance */
    private static final String MSG_NEGATIVE_DISTANCE = "Distance cannot be negative";

    /** Error message for negative speed */
    private static final String MSG_NEGATIVE_SPEED = "Average speed cannot be negative";

    /** Error message format for user not found */
    private static final String MSG_USER_NOT_FOUND = "User with ID %d not found";

    /** Error message format for training not found */
    private static final String MSG_TRAINING_NOT_FOUND = "Training with ID %d not found";

    // ========== Dependencies ==========

    /** Repository for training data access */
    private final TrainingRepository trainingRepository;

    /** Provider for user data access */
    private final UserProvider userProvider;

    // ========== Command Methods ==========

    /**
     * Creates a new training record in the system.
     * 
     * This method performs comprehensive validation of the input data,
     * retrieves the associated user, prepares the training entity,
     * and persists it to the database.
     *
     * @param trainingData The training data to create
     * @param userId The ID of the user associated with the training
     * @return The created training with assigned ID
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if persistence fails
     */
    @Override
    public Training createTraining(Training trainingData, Long userId) {
        log.info("Beginning training creation workflow for user ID: {}", userId);

        // Step 1: Validate input data
        List<String> validationIssues = checkTrainingCreationData(trainingData, userId);
        if (!validationIssues.isEmpty()) {
            String errorSummary = String.join("; ", validationIssues);
            log.error("Training creation validation failed with errors: {}", errorSummary);
            throw new IllegalArgumentException(errorSummary);
        }

        // Step 2: Find associated user
        User user = fetchUserById(userId);

        // Step 3: Create training entity
        Training newTraining = buildTrainingEntity(trainingData, user);

        // Step 4: Save to database
        return saveTrainingToDatabase(newTraining);
    }

    /**
     * Updates an existing training record in the system.
     * 
     * This method performs comprehensive validation of the input data,
     * retrieves the existing training and associated user,
     * tracks changes for logging purposes, updates the entity,
     * and persists it to the database.
     *
     * @param updatedData The updated training data
     * @param trainingId The ID of the training to update
     * @param userId The ID of the user associated with the training
     * @return The updated training
     * @throws IllegalArgumentException if validation fails or entities not found
     * @throws RuntimeException if persistence fails
     */
    @Override
    public Training updateTraining(Training updatedData, Long trainingId, Long userId) {
        log.info("Beginning training update workflow for training ID: {} and user ID: {}", trainingId, userId);

        // Step 1: Validate input data
        List<String> validationIssues = checkTrainingUpdateData(updatedData, trainingId, userId);
        if (!validationIssues.isEmpty()) {
            String errorSummary = String.join("; ", validationIssues);
            log.error("Training update validation failed with errors: {}", errorSummary);
            throw new IllegalArgumentException(errorSummary);
        }

        // Step 2: Find existing training
        Training existingTraining = fetchTrainingById(trainingId);

        // Step 3: Find associated user
        User user = fetchUserById(userId);

        // Step 4: Track changes for logging
        String changesSummary = createChangeLog(updatedData, existingTraining);

        // Step 5: Update training entity
        modifyTrainingEntity(existingTraining, updatedData, user);

        // Step 6: Save to database
        Training result = saveTrainingToDatabase(existingTraining);

        // Log the changes
        if (!changesSummary.isEmpty()) {
            log.info("Training updated with the following changes: {}", changesSummary);
        } else {
            log.info("Training updated with no detected changes");
        }

        return result;
    }

    // ========== Query Methods ==========

    /**
     * Retrieves a specific training by its ID.
     * 
     * @param trainingId The ID of the training to retrieve
     * @return An Optional containing the training if found, or empty if not found
     */
    @Override
    public Optional<Training> getTraining(final Long trainingId) {
        log.debug("Fetching training with ID: {}", trainingId);
        return trainingRepository.findById(trainingId);
    }

    /**
     * Retrieves all trainings in the system.
     * 
     * @return A list of all trainings
     */
    @Override
    public List<Training> findAllTrainings() {
        log.debug("Fetching all trainings from database");
        return trainingRepository.findAll();
    }

    /**
     * Retrieves all trainings for a specific user.
     * 
     * @param userId The ID of the user
     * @return A list of trainings for the specified user
     * @throws IllegalArgumentException if userId is null
     */
    @Override
    public List<Training> findTrainingsByUserId(Long userId) {
        log.debug("Fetching trainings for user with ID: {}", userId);
        ensureNotNull(userId, MSG_NULL_USER_ID);
        return trainingRepository.findByUserId(userId);
    }

    /**
     * Retrieves all trainings with a specific activity type.
     * 
     * @param activityType The activity type to search for
     * @return A list of trainings with the specified activity type
     * @throws IllegalArgumentException if activityType is null
     */
    @Override
    public List<Training> findTrainingsByActivityType(ActivityType activityType) {
        log.debug("Fetching trainings with activity type: {}", activityType);
        ensureNotNull(activityType, MSG_NULL_ACTIVITY_TYPE);
        return trainingRepository.findByActivityType(activityType);
    }

    /**
     * Retrieves all trainings that ended after a specific date.
     * 
     * @param date The date to compare against
     * @return A list of trainings that ended after the specified date
     * @throws IllegalArgumentException if date is null
     */
    @Override
    public List<Training> findTrainingsWithEndDateAfter(Date date) {
        log.debug("Fetching trainings ending after date: {}", date);
        ensureNotNull(date, "Date cannot be null");
        return trainingRepository.findByEndDateAfter(date);
    }

    /**
     * Retrieves all trainings that occurred within a specific date range.
     * 
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return A list of trainings that occurred within the specified date range
     * @throws IllegalArgumentException if either date is null or if startDate is after endDate
     */
    @Override
    public List<Training> findTrainingsByDateRange(Date startDate, Date endDate) {
        log.debug("Fetching trainings between dates: {} and {}", startDate, endDate);
        validateDateRangeParameters(startDate, endDate);
        return trainingRepository.findByDateRange(startDate, endDate);
    }

    // ========== Validation Helper Methods ==========

    /**
     * Ensures that a value is not null.
     * 
     * @param value The value to check
     * @param errorMessage The error message to use if the value is null
     * @throws IllegalArgumentException if the value is null
     */
    private void ensureNotNull(Object value, String errorMessage) {
        if (value == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Validates date range parameters.
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @throws IllegalArgumentException if either date is null or if startDate is after endDate
     */
    private void validateDateRangeParameters(Date startDate, Date endDate) {
        ensureNotNull(startDate, "Start date cannot be null");
        ensureNotNull(endDate, "End date cannot be null");

        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }

    /**
     * Validates training data for creation.
     * 
     * @param training The training data
     * @param userId The user ID
     * @return A list of validation error messages, empty if validation passed
     */
    private List<String> checkTrainingCreationData(Training training, Long userId) {
        List<String> issues = new ArrayList<>();

        // Check for null values
        if (training == null) {
            issues.add(MSG_NULL_TRAINING);
            return issues; // Early return since we can't validate further
        }

        if (userId == null) {
            issues.add(MSG_NULL_USER_ID);
        }

        // Check if ID is already set
        if (training.getId() != null) {
            issues.add(MSG_EXISTING_ID);
        }

        // Validate common training data
        issues.addAll(validateCommonTrainingFields(training));

        return issues;
    }

    /**
     * Validates training data for update.
     * 
     * @param training The training data
     * @param trainingId The training ID
     * @param userId The user ID
     * @return A list of validation error messages, empty if validation passed
     */
    private List<String> checkTrainingUpdateData(Training training, Long trainingId, Long userId) {
        List<String> issues = new ArrayList<>();

        // Check for null values
        if (training == null) {
            issues.add(MSG_NULL_TRAINING);
            return issues; // Early return since we can't validate further
        }

        if (trainingId == null) {
            issues.add(MSG_NULL_TRAINING_ID);
        }

        if (userId == null) {
            issues.add(MSG_NULL_USER_ID);
        }

        // Validate common training data
        issues.addAll(validateCommonTrainingFields(training));

        return issues;
    }

    /**
     * Validates common training data fields.
     * 
     * @param training The training data
     * @return A list of validation error messages, empty if validation passed
     */
    private List<String> validateCommonTrainingFields(Training training) {
        List<String> issues = new ArrayList<>();

        // Check for null values in required fields
        if (training.getStartTime() == null) {
            issues.add(MSG_NULL_START_TIME);
        }

        if (training.getEndTime() == null) {
            issues.add(MSG_NULL_END_TIME);
        }

        if (training.getActivityType() == null) {
            issues.add(MSG_NULL_ACTIVITY_TYPE);
        }

        // Skip time range validation if either time is null
        if (training.getStartTime() != null && training.getEndTime() != null) {
            // Check if end time is after start time
            if (training.getEndTime().before(training.getStartTime())) {
                issues.add(MSG_INVALID_TIME_RANGE);
            }
        }

        // Check if distance is valid
        if (training.getDistance() < 0) {
            issues.add(MSG_NEGATIVE_DISTANCE);
        }

        // Check if average speed is valid
        if (training.getAverageSpeed() < 0) {
            issues.add(MSG_NEGATIVE_SPEED);
        }

        return issues;
    }

    // ========== Entity Operation Helper Methods ==========

    /**
     * Retrieves a user by ID.
     * 
     * @param userId The user ID
     * @return The user
     * @throws IllegalArgumentException if the user is not found
     */
    private User fetchUserById(Long userId) {
        try {
            return userProvider.getUser(userId)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found in the system", userId);
                    return new IllegalArgumentException(String.format(MSG_USER_NOT_FOUND, userId));
                });
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            log.error("Error occurred while retrieving user with ID {}: {}", userId, e.getMessage());
            throw new IllegalArgumentException("Error retrieving user: " + e.getMessage());
        }
    }

    /**
     * Retrieves a training by ID.
     * 
     * @param trainingId The training ID
     * @return The training
     * @throws IllegalArgumentException if the training is not found
     */
    private Training fetchTrainingById(Long trainingId) {
        return trainingRepository.findById(trainingId)
            .orElseThrow(() -> {
                log.error("Training with ID {} not found in the system", trainingId);
                return new IllegalArgumentException(String.format(MSG_TRAINING_NOT_FOUND, trainingId));
            });
    }

    /**
     * Creates a new training entity from the provided data.
     * 
     * @param trainingData The training data
     * @param user The user
     * @return The prepared training entity
     */
    private Training buildTrainingEntity(Training trainingData, User user) {
        // Calculate training duration
        long durationMillis = trainingData.getEndTime().getTime() - trainingData.getStartTime().getTime();
        long durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);

        log.debug("Building new training entity with calculated duration: {} minutes", durationMinutes);

        // Create new training entity
        Training newTraining = new Training(
            user,
            trainingData.getStartTime(),
            trainingData.getEndTime(),
            trainingData.getActivityType(),
            trainingData.getDistance(),
            trainingData.getAverageSpeed()
        );

        // Log detailed information
        log.info("New training entity prepared for user {}: {} (duration: {} min, distance: {}, avg speed: {})",
            user.getId(),
            trainingData.getActivityType(),
            durationMinutes,
            trainingData.getDistance(),
            trainingData.getAverageSpeed()
        );

        return newTraining;
    }

    /**
     * Updates an existing training entity with new data.
     * 
     * @param existingTraining The existing training entity
     * @param updatedData The updated data
     * @param user The user
     */
    private void modifyTrainingEntity(Training existingTraining, Training updatedData, User user) {
        existingTraining.setUser(user);
        existingTraining.setStartTime(updatedData.getStartTime());
        existingTraining.setEndTime(updatedData.getEndTime());
        existingTraining.setActivityType(updatedData.getActivityType());
        existingTraining.setDistance(updatedData.getDistance());
        existingTraining.setAverageSpeed(updatedData.getAverageSpeed());
    }

    /**
     * Persists a training entity to the database.
     * 
     * @param training The training entity
     * @return The persisted training
     * @throws RuntimeException if an error occurs during persistence
     */
    private Training saveTrainingToDatabase(Training training) {
        try {
            Training savedTraining = trainingRepository.save(training);
            String operationType = savedTraining.getId() != null ? "updated with ID " + savedTraining.getId() : "created";
            log.info("Training successfully {} in the database", operationType);
            return savedTraining;
        } catch (Exception e) {
            log.error("Database error occurred while saving training: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save training to database: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a change log for training updates.
     * 
     * @param newData The new training data
     * @param existingData The existing training data
     * @return A string containing the changes
     */
    private String createChangeLog(Training newData, Training existingData) {
        StringBuilder changeLog = new StringBuilder();

        // Check each field for changes
        if (existingData.getDistance() != newData.getDistance()) {
            changeLog.append(String.format("Distance: %.2f → %.2f; ", existingData.getDistance(), newData.getDistance()));
        }

        if (!existingData.getActivityType().equals(newData.getActivityType())) {
            changeLog.append(String.format("Activity: %s → %s; ", existingData.getActivityType(), newData.getActivityType()));
        }

        if (!existingData.getStartTime().equals(newData.getStartTime())) {
            changeLog.append(String.format("Start time: %s → %s; ", existingData.getStartTime(), newData.getStartTime()));
        }

        if (!existingData.getEndTime().equals(newData.getEndTime())) {
            changeLog.append(String.format("End time: %s → %s; ", existingData.getEndTime(), newData.getEndTime()));
        }

        if (existingData.getAverageSpeed() != newData.getAverageSpeed()) {
            changeLog.append(String.format("Average speed: %.2f → %.2f; ", existingData.getAverageSpeed(), newData.getAverageSpeed()));
        }

        return changeLog.toString();
    }
}
