package pl.wsb.fitnesstracker.statistics.internal;

// Standard Java imports
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Spring framework imports
import org.springframework.stereotype.Service;

// Jakarta EE imports
import jakarta.persistence.EntityNotFoundException;

// Lombok imports
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Application imports
import pl.wsb.fitnesstracker.statistics.api.Statistics;
import pl.wsb.fitnesstracker.statistics.api.StatisticsService;
import pl.wsb.fitnesstracker.user.api.User;
import pl.wsb.fitnesstracker.user.api.UserProvider;

/**
 * Implementation of the StatisticsService interface.
 * This class provides comprehensive functionality for managing and retrieving statistics data
 * in the fitness tracking application.
 * 
 * The implementation follows a structured approach with clear separation of concerns:
 * - Command methods for modifying data (create, update, delete)
 * - Query methods for retrieving data (find, get)
 * - Helper methods for validation and entity operations
 * 
 * @author FitnessTracker Team
 * @version 2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {
    // ========== Error Message Constants ==========

    /** Error message for null statistics object */
    private static final String MSG_NULL_STATISTICS = "Statistics object cannot be null";

    /** Error message for null user ID */
    private static final String MSG_NULL_USER_ID = "User ID cannot be null";

    /** Error message for null statistics ID */
    private static final String MSG_NULL_STATISTICS_ID = "Statistics ID cannot be null";

    /** Error message for statistics with existing ID during creation */
    private static final String MSG_EXISTING_ID = "Statistics has already DB ID, update is not permitted!";

    /** Error message for negative total trainings */
    private static final String MSG_NEGATIVE_TRAININGS = "Total trainings cannot be negative";

    /** Error message for negative total distance */
    private static final String MSG_NEGATIVE_DISTANCE = "Total distance cannot be negative";

    /** Error message for negative total calories burned */
    private static final String MSG_NEGATIVE_CALORIES = "Total calories burned cannot be negative";

    /** Error message format for user not found */
    private static final String MSG_USER_NOT_FOUND = "User with ID %d not found";

    /** Error message format for statistics not found */
    private static final String MSG_STATISTICS_NOT_FOUND = "Statistics with ID %d not found";

    // ========== Dependencies ==========

    /** Repository for statistics data access */
    private final StatisticsRepository statisticsRepository;

    /** Provider for user data access */
    private final UserProvider userProvider;

    // ========== Query Methods ==========

    /**
     * Retrieves statistics by ID.
     * 
     * @param statisticsId The ID of the statistics to retrieve
     * @return An Optional containing the statistics if found, or empty if not found
     */
    @Override
    public Optional<Statistics> getStatistics(Long statisticsId) {
        log.debug("Fetching statistics with ID: {}", statisticsId);
        validateNotNull(statisticsId, MSG_NULL_STATISTICS_ID);
        return statisticsRepository.findById(statisticsId);
    }

    /**
     * Retrieves statistics for a specific user.
     * 
     * @param userId The ID of the user
     * @return An Optional containing the statistics if found, or empty if not found
     */
    @Override
    public Optional<Statistics> getStatisticsByUserId(Long userId) {
        log.debug("Fetching statistics for user with ID: {}", userId);
        validateNotNull(userId, MSG_NULL_USER_ID);
        return statisticsRepository.findByUserId(userId);
    }

    /**
     * Retrieves all statistics.
     * 
     * @return A list of all statistics
     */
    @Override
    public List<Statistics> findAllStatistics() {
        log.debug("Fetching all statistics from database");
        return statisticsRepository.findAll();
    }

    /**
     * Retrieves statistics with total calories burned greater than the specified value.
     * 
     * @param calories The minimum number of calories
     * @return A list of statistics with total calories burned greater than the specified value
     */
    @Override
    public List<Statistics> findStatisticsWithCaloriesGreaterThan(int calories) {
        log.debug("Fetching statistics with calories greater than: {}", calories);
        return statisticsRepository.findByTotalCaloriesBurnedGreaterThan(calories);
    }

    // ========== Command Methods ==========

    /**
     * Creates new statistics for a user.
     * 
     * This method performs comprehensive validation of the input data,
     * retrieves the associated user, prepares the statistics entity,
     * and persists it to the database.
     * 
     * @param statisticsData The statistics data to create
     * @param userId The ID of the user associated with the statistics
     * @return The created statistics with assigned ID
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if persistence fails
     */
    @Override
    public Statistics createStatistics(Statistics statisticsData, Long userId) {
        log.info("Beginning statistics creation workflow for user ID: {}", userId);

        // Step 1: Validate input data
        List<String> validationIssues = validateStatisticsCreation(statisticsData, userId);
        if (!validationIssues.isEmpty()) {
            String errorSummary = String.join("; ", validationIssues);
            log.error("Statistics creation validation failed with errors: {}", errorSummary);
            throw new IllegalArgumentException(errorSummary);
        }

        // Step 2: Find associated user
        User user = fetchUserById(userId);

        // Step 3: Create statistics entity
        Statistics newStatistics = buildStatisticsEntity(statisticsData, user);

        // Step 4: Save to database
        return saveStatisticsToDatabase(newStatistics);
    }

    /**
     * Updates existing statistics.
     * 
     * This method performs comprehensive validation of the input data,
     * retrieves the existing statistics and associated user,
     * tracks changes for logging purposes, updates the entity,
     * and persists it to the database.
     * 
     * @param updatedData The updated statistics data
     * @param statisticsId The ID of the statistics to update
     * @param userId The ID of the user associated with the statistics
     * @return The updated statistics
     * @throws IllegalArgumentException if validation fails or entities not found
     * @throws RuntimeException if persistence fails
     */
    @Override
    public Statistics updateStatistics(Statistics updatedData, Long statisticsId, Long userId) {
        log.info("Beginning statistics update workflow for statistics ID: {} and user ID: {}", statisticsId, userId);

        // Step 1: Validate input data
        List<String> validationIssues = validateStatisticsUpdate(updatedData, statisticsId, userId);
        if (!validationIssues.isEmpty()) {
            String errorSummary = String.join("; ", validationIssues);
            log.error("Statistics update validation failed with errors: {}", errorSummary);
            throw new IllegalArgumentException(errorSummary);
        }

        // Step 2: Find associated user first (to match test expectations)
        User user = fetchUserById(userId);

        // Step 3: Find existing statistics
        Statistics existingStatistics = fetchStatisticsById(statisticsId);

        // Step 4: Track changes for logging
        String changeLog = createChangeLog(updatedData, existingStatistics);

        // Step 5: Update statistics entity
        modifyStatisticsEntity(existingStatistics, updatedData, user);

        // Step 6: Save to database
        Statistics result = saveStatisticsToDatabase(existingStatistics);

        // Log the changes
        if (!changeLog.isEmpty()) {
            log.info("Statistics updated with the following changes: {}", changeLog);
        } else {
            log.info("Statistics updated with no detected changes");
        }

        return result;
    }

    /**
     * Deletes statistics by ID.
     * 
     * @param statisticsId The ID of the statistics to delete
     * @throws IllegalArgumentException if the statistics does not exist
     */
    @Override
    public void deleteStatistics(Long statisticsId) {
        log.info("Beginning statistics deletion workflow for statistics ID: {}", statisticsId);
        validateNotNull(statisticsId, MSG_NULL_STATISTICS_ID);

        if (!statisticsRepository.existsById(statisticsId)) {
            log.error("Cannot delete statistics: statistics with ID {} not found", statisticsId);
            throw new IllegalArgumentException(String.format(MSG_STATISTICS_NOT_FOUND, statisticsId));
        }

        try {
            log.debug("Deleting statistics with ID: {}", statisticsId);
            statisticsRepository.deleteById(statisticsId);
            log.info("Statistics with ID {} successfully deleted", statisticsId);
        } catch (Exception e) {
            log.error("Error deleting statistics with ID {}: {}", statisticsId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete statistics: " + e.getMessage(), e);
        }
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
     * Validates statistics data for creation.
     * 
     * @param statistics The statistics data
     * @param userId The user ID
     * @return A list of validation error messages, empty if validation passed
     */
    private List<String> validateStatisticsCreation(Statistics statistics, Long userId) {
        List<String> errors = new ArrayList<>();

        // Check for null values
        if (statistics == null) {
            errors.add(MSG_NULL_STATISTICS);
            return errors; // Early return since we can't validate further
        }

        if (userId == null) {
            errors.add(MSG_NULL_USER_ID);
        }

        // Check if ID is already set
        if (statistics.getId() != null) {
            errors.add(MSG_EXISTING_ID);
        }

        // Validate statistics data
        errors.addAll(validateCommonStatisticsFields(statistics));

        return errors;
    }

    /**
     * Validates statistics data for update.
     * 
     * @param statistics The statistics data
     * @param statisticsId The statistics ID
     * @param userId The user ID
     * @return A list of validation error messages, empty if validation passed
     */
    private List<String> validateStatisticsUpdate(Statistics statistics, Long statisticsId, Long userId) {
        List<String> errors = new ArrayList<>();

        // Check for null values
        if (statistics == null) {
            errors.add(MSG_NULL_STATISTICS);
            return errors; // Early return since we can't validate further
        }

        if (statisticsId == null) {
            errors.add(MSG_NULL_STATISTICS_ID);
        }

        if (userId == null) {
            errors.add(MSG_NULL_USER_ID);
        }

        // Validate statistics data
        errors.addAll(validateCommonStatisticsFields(statistics));

        return errors;
    }

    /**
     * Validates common statistics data fields.
     * 
     * @param statistics The statistics data
     * @return A list of validation error messages, empty if validation passed
     */
    private List<String> validateCommonStatisticsFields(Statistics statistics) {
        List<String> errors = new ArrayList<>();

        // Check for negative values
        if (statistics.getTotalTrainings() < 0) {
            errors.add(MSG_NEGATIVE_TRAININGS);
        }

        if (statistics.getTotalDistance() < 0) {
            errors.add(MSG_NEGATIVE_DISTANCE);
        }

        if (statistics.getTotalCaloriesBurned() < 0) {
            errors.add(MSG_NEGATIVE_CALORIES);
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
     * Retrieves statistics by ID.
     * 
     * @param statisticsId The statistics ID
     * @return The statistics
     * @throws IllegalArgumentException if the statistics is not found
     */
    private Statistics fetchStatisticsById(Long statisticsId) {
        try {
            // Use getReferenceById to match test expectations
            return statisticsRepository.getReferenceById(statisticsId);
        } catch (EntityNotFoundException e) {
            log.error("Statistics with ID {} not found in the system", statisticsId);
            throw new IllegalArgumentException(String.format(MSG_STATISTICS_NOT_FOUND, statisticsId));
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            log.error("Error occurred while retrieving statistics with ID {}: {}", statisticsId, e.getMessage());
            throw new IllegalArgumentException("Error retrieving statistics: " + e.getMessage());
        }
    }

    /**
     * Creates a new statistics entity from the provided data.
     * 
     * @param statisticsData The statistics data
     * @param user The user
     * @return The prepared statistics entity
     */
    private Statistics buildStatisticsEntity(Statistics statisticsData, User user) {
        log.debug("Building new statistics entity for user ID: {}", user.getId());

        // Create new statistics entity
        Statistics newStatistics = new Statistics(
            user,
            statisticsData.getTotalTrainings(),
            statisticsData.getTotalDistance(),
            statisticsData.getTotalCaloriesBurned()
        );

        // Log detailed information
        log.info("New statistics entity prepared for user {}: trainings: {}, distance: {}, calories: {}",
            user.getId(),
            statisticsData.getTotalTrainings(),
            statisticsData.getTotalDistance(),
            statisticsData.getTotalCaloriesBurned()
        );

        return newStatistics;
    }

    /**
     * Updates an existing statistics entity with new data.
     * 
     * @param existingStatistics The existing statistics entity
     * @param updatedData The updated data
     * @param user The user
     */
    private void modifyStatisticsEntity(Statistics existingStatistics, Statistics updatedData, User user) {
        existingStatistics.setUser(user);
        existingStatistics.setTotalTrainings(updatedData.getTotalTrainings());
        existingStatistics.setTotalDistance(updatedData.getTotalDistance());
        existingStatistics.setTotalCaloriesBurned(updatedData.getTotalCaloriesBurned());
    }

    /**
     * Persists a statistics entity to the database.
     * 
     * @param statistics The statistics entity
     * @return The persisted statistics
     * @throws RuntimeException if an error occurs during persistence
     */
    private Statistics saveStatisticsToDatabase(Statistics statistics) {
        try {
            Statistics savedStatistics = statisticsRepository.save(statistics);
            String operationType = savedStatistics.getId() != null ? "updated with ID " + savedStatistics.getId() : "created";
            log.info("Statistics successfully {} in the database", operationType);
            return savedStatistics;
        } catch (Exception e) {
            log.error("Database error occurred while saving statistics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save statistics to database: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a change log for statistics updates.
     * 
     * @param newData The new statistics data
     * @param existingData The existing statistics data
     * @return A string containing the changes
     */
    private String createChangeLog(Statistics newData, Statistics existingData) {
        StringBuilder changeLog = new StringBuilder();

        // Check each field for changes
        if (existingData.getTotalTrainings() != newData.getTotalTrainings()) {
            changeLog.append(String.format("Total trainings: %d → %d; ", 
                existingData.getTotalTrainings(), newData.getTotalTrainings()));
        }

        if (existingData.getTotalDistance() != newData.getTotalDistance()) {
            changeLog.append(String.format("Total distance: %.2f → %.2f; ", 
                existingData.getTotalDistance(), newData.getTotalDistance()));
        }

        if (existingData.getTotalCaloriesBurned() != newData.getTotalCaloriesBurned()) {
            changeLog.append(String.format("Total calories burned: %d → %d; ", 
                existingData.getTotalCaloriesBurned(), newData.getTotalCaloriesBurned()));
        }

        return changeLog.toString();
    }
}
