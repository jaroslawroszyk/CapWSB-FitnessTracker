package pl.wsb.fitnesstracker.training.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wsb.fitnesstracker.training.api.Training;
import pl.wsb.fitnesstracker.training.api.TrainingProvider;
import pl.wsb.fitnesstracker.training.api.TrainingService;
import pl.wsb.fitnesstracker.user.api.User;
import pl.wsb.fitnesstracker.user.api.UserProvider;

/**
 * Implementation of the TrainingService and TrainingProvider interfaces.
 * This class provides methods for managing and retrieving training data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingServiceImpl implements TrainingProvider, TrainingService {
    // Constants for validation error messages
    private static final String ERROR_NULL_TRAINING = "Training object cannot be null";
    private static final String ERROR_NULL_USER_ID = "User ID cannot be null";
    private static final String ERROR_NULL_TRAINING_ID = "Training ID cannot be null";
    private static final String ERROR_EXISTING_ID = "Training has already DB ID, update is not permitted!";
    private static final String ERROR_NULL_START_TIME = "Start time cannot be null";
    private static final String ERROR_NULL_END_TIME = "End time cannot be null";
    private static final String ERROR_NULL_ACTIVITY_TYPE = "Activity type cannot be null";
    private static final String ERROR_INVALID_TIME_RANGE = "End time must be after start time";
    private static final String ERROR_NEGATIVE_DISTANCE = "Distance cannot be negative";
    private static final String ERROR_NEGATIVE_SPEED = "Average speed cannot be negative";
    private static final String ERROR_USER_NOT_FOUND = "User with ID %d not found";
    private static final String ERROR_TRAINING_NOT_FOUND = "Training with ID %d not found";

    // Repository and provider dependencies
    private final TrainingRepository trainingRepository;
    private final UserProvider userProvider;

    // ========== Query Methods ==========

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Training> getTraining(final Long trainingId) {
        log.debug("Retrieving training with ID: {}", trainingId);
        return trainingRepository.findById(trainingId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Training> findAllTrainings() {
        log.debug("Retrieving all trainings from database");
        return trainingRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Training> findTrainingsByUserId(Long userId) {
        log.debug("Retrieving trainings for user with ID: {}", userId);
        validateNotNull(userId, ERROR_NULL_USER_ID);
        return trainingRepository.findByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Training> findTrainingsByActivityType(ActivityType activityType) {
        log.debug("Retrieving trainings with activity type: {}", activityType);
        validateNotNull(activityType, ERROR_NULL_ACTIVITY_TYPE);
        return trainingRepository.findByActivityType(activityType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Training> findTrainingsWithEndDateAfter(Date date) {
        log.debug("Retrieving trainings ending after date: {}", date);
        validateNotNull(date, "Date cannot be null");
        return trainingRepository.findByEndDateAfter(date);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Training> findTrainingsByDateRange(Date startDate, Date endDate) {
        log.debug("Retrieving trainings between dates: {} and {}", startDate, endDate);
        validateDateRange(startDate, endDate);
        return trainingRepository.findByDateRange(startDate, endDate);
    }

    // ========== Command Methods ==========

    /**
     * {@inheritDoc}
     */
    @Override
    public Training createTraining(Training trainingData, Long userId) {
        log.info("Initiating training creation process for user ID: {}", userId);

        // Step 1: Perform validation
        List<String> validationErrors = validateTrainingCreation(trainingData, userId);
        if (!validationErrors.isEmpty()) {
            String errorMessage = String.join("; ", validationErrors);
            log.error("Training creation validation failed: {}", errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        // Step 2: Retrieve user
        User user = retrieveUserById(userId);

        // Step 3: Prepare training entity
        Training newTraining = prepareTrainingEntity(trainingData, user);

        // Step 4: Persist and return
        return persistTraining(newTraining);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Training updateTraining(Training updatedData, Long trainingId, Long userId) {
        log.info("Initiating training update process for training ID: {} and user ID: {}", trainingId, userId);

        // Step 1: Perform validation
        List<String> validationErrors = validateTrainingUpdate(updatedData, trainingId, userId);
        if (!validationErrors.isEmpty()) {
            String errorMessage = String.join("; ", validationErrors);
            log.error("Training update validation failed: {}", errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        // Step 2: Retrieve existing training
        Training existingTraining = retrieveTrainingById(trainingId);

        // Step 3: Retrieve user
        User user = retrieveUserById(userId);

        // Step 4: Track changes for logging
        String changeLog = generateChangeLog(updatedData, existingTraining);

        // Step 5: Update training entity
        updateTrainingEntity(existingTraining, updatedData, user);

        // Step 6: Persist and return
        Training result = persistTraining(existingTraining);

        // Log the changes
        if (!changeLog.isEmpty()) {
            log.info("Training updated with changes: {}", changeLog);
        } else {
            log.info("Training updated with no changes detected");
        }

        return result;
    }

    // ========== Helper Methods for Validation ==========

    /**
     * Validates that a value is not null.
     * 
     * @param value The value to check
     * @param errorMessage The error message to use if the value is null
     * @throws IllegalArgumentException if the value is null
     */
    private void validateNotNull(Object value, String errorMessage) {
        if (value == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Validates a date range.
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @throws IllegalArgumentException if either date is null or if the start date is after the end date
     */
    private void validateDateRange(Date startDate, Date endDate) {
        validateNotNull(startDate, "Start date cannot be null");
        validateNotNull(endDate, "End date cannot be null");

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
    private List<String> validateTrainingCreation(Training training, Long userId) {
        List<String> errors = new ArrayList<>();

        // Check for null values
        if (training == null) {
            errors.add(ERROR_NULL_TRAINING);
            return errors; // Early return since we can't validate further
        }

        if (userId == null) {
            errors.add(ERROR_NULL_USER_ID);
        }

        // Check if ID is already set
        if (training.getId() != null) {
            errors.add(ERROR_EXISTING_ID);
        }

        // Validate training data
        errors.addAll(validateTrainingData(training));

        return errors;
    }

    /**
     * Validates training data for update.
     * 
     * @param training The training data
     * @param trainingId The training ID
     * @param userId The user ID
     * @return A list of validation error messages, empty if validation passed
     */
    private List<String> validateTrainingUpdate(Training training, Long trainingId, Long userId) {
        List<String> errors = new ArrayList<>();

        // Check for null values
        if (training == null) {
            errors.add(ERROR_NULL_TRAINING);
            return errors; // Early return since we can't validate further
        }

        if (trainingId == null) {
            errors.add(ERROR_NULL_TRAINING_ID);
        }

        if (userId == null) {
            errors.add(ERROR_NULL_USER_ID);
        }

        // Validate training data
        errors.addAll(validateTrainingData(training));

        return errors;
    }

    /**
     * Validates common training data fields.
     * 
     * @param training The training data
     * @return A list of validation error messages, empty if validation passed
     */
    private List<String> validateTrainingData(Training training) {
        List<String> errors = new ArrayList<>();

        // Check for null values in required fields
        if (training.getStartTime() == null) {
            errors.add(ERROR_NULL_START_TIME);
        }

        if (training.getEndTime() == null) {
            errors.add(ERROR_NULL_END_TIME);
        }

        if (training.getActivityType() == null) {
            errors.add(ERROR_NULL_ACTIVITY_TYPE);
        }

        // Skip time range validation if either time is null
        if (training.getStartTime() != null && training.getEndTime() != null) {
            // Check if end time is after start time
            if (training.getEndTime().before(training.getStartTime())) {
                errors.add(ERROR_INVALID_TIME_RANGE);
            }
        }

        // Check if distance is valid
        if (training.getDistance() < 0) {
            errors.add(ERROR_NEGATIVE_DISTANCE);
        }

        // Check if average speed is valid
        if (training.getAverageSpeed() < 0) {
            errors.add(ERROR_NEGATIVE_SPEED);
        }

        return errors;
    }

    // ========== Helper Methods for Entity Operations ==========

    /**
     * Retrieves a user by ID.
     * 
     * @param userId The user ID
     * @return The user
     * @throws IllegalArgumentException if the user is not found
     */
    private User retrieveUserById(Long userId) {
        try {
            return userProvider.getUser(userId)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", userId);
                    return new IllegalArgumentException(String.format(ERROR_USER_NOT_FOUND, userId));
                });
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            log.error("Error retrieving user with ID {}: {}", userId, e.getMessage());
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
    private Training retrieveTrainingById(Long trainingId) {
        return trainingRepository.findById(trainingId)
            .orElseThrow(() -> {
                log.error("Training with ID {} not found", trainingId);
                return new IllegalArgumentException(String.format(ERROR_TRAINING_NOT_FOUND, trainingId));
            });
    }

    /**
     * Prepares a new training entity from the provided data.
     * 
     * @param trainingData The training data
     * @param user The user
     * @return The prepared training entity
     */
    private Training prepareTrainingEntity(Training trainingData, User user) {
        // Calculate training duration
        long durationMillis = trainingData.getEndTime().getTime() - trainingData.getStartTime().getTime();
        long durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);

        log.debug("Preparing new training entity with duration: {} minutes", durationMinutes);

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
        log.info("New training prepared for user {}: {} (duration: {} min, distance: {}, avg speed: {})",
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
    private void updateTrainingEntity(Training existingTraining, Training updatedData, User user) {
        existingTraining.setUser(user);
        existingTraining.setStartTime(updatedData.getStartTime());
        existingTraining.setEndTime(updatedData.getEndTime());
        existingTraining.setActivityType(updatedData.getActivityType());
        existingTraining.setDistance(updatedData.getDistance());
        existingTraining.setAverageSpeed(updatedData.getAverageSpeed());
    }

    /**
     * Persists a training entity.
     * 
     * @param training The training entity
     * @return The persisted training
     * @throws RuntimeException if an error occurs during persistence
     */
    private Training persistTraining(Training training) {
        try {
            Training savedTraining = trainingRepository.save(training);
            log.info("Training {} successfully persisted", savedTraining.getId() != null ? "updated with ID " + savedTraining.getId() : "created");
            return savedTraining;
        } catch (Exception e) {
            log.error("Error persisting training: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to persist training: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a change log for training updates.
     * 
     * @param newData The new training data
     * @param existingData The existing training data
     * @return A string containing the changes
     */
    private String generateChangeLog(Training newData, Training existingData) {
        StringBuilder changes = new StringBuilder();

        // Check each field for changes
        if (existingData.getDistance() != newData.getDistance()) {
            changes.append(String.format("Distance: %.2f → %.2f; ", existingData.getDistance(), newData.getDistance()));
        }

        if (!existingData.getActivityType().equals(newData.getActivityType())) {
            changes.append(String.format("Activity: %s → %s; ", existingData.getActivityType(), newData.getActivityType()));
        }

        if (!existingData.getStartTime().equals(newData.getStartTime())) {
            changes.append(String.format("Start: %s → %s; ", existingData.getStartTime(), newData.getStartTime()));
        }

        if (!existingData.getEndTime().equals(newData.getEndTime())) {
            changes.append(String.format("End: %s → %s; ", existingData.getEndTime(), newData.getEndTime()));
        }

        if (existingData.getAverageSpeed() != newData.getAverageSpeed()) {
            changes.append(String.format("Speed: %.2f → %.2f; ", existingData.getAverageSpeed(), newData.getAverageSpeed()));
        }

        return changes.toString();
    }
}
