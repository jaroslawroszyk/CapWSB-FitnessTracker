package pl.wsb.fitnesstracker.statistics.api;

/**
 * Interface (API) for modifying operations on {@link Statistics} entities through the API.
 * Implementing classes are responsible for executing changes within a database transaction,
 * whether by continuing an existing transaction or creating a new one if required.
 */
public interface StatisticsService extends StatisticsProvider {
    
    /**
     * Creates new statistics for a user.
     *
     * @param statistics The statistics entity to be created
     * @param userId The ID of the user to associate with the statistics
     * @return The persisted statistics entity with assigned ID
     */
    Statistics createStatistics(Statistics statistics, Long userId);
    
    /**
     * Updates existing statistics.
     *
     * @param statistics The statistics entity with updated values
     * @param statisticsId The ID of the statistics to update
     * @param userId The ID of the user to associate with the statistics
     * @return The updated statistics entity
     */
    Statistics updateStatistics(Statistics statistics, Long statisticsId, Long userId);
    
    /**
     * Deletes statistics by ID.
     *
     * @param statisticsId The ID of the statistics to delete
     */
    void deleteStatistics(Long statisticsId);
}