package pl.wsb.fitnesstracker.statistics.api;

import java.util.List;
import java.util.Optional;

public interface StatisticsProvider {

    /**
     * Retrieves a statistics based on their ID.
     * If the statistics with given ID is not found, then {@link Optional#empty()} will be returned.
     *
     * @param statisticsId id of the statistics to be searched
     * @return An {@link Optional} containing the located Statistics, or {@link Optional#empty()} if not found
     */
    Optional<Statistics> getStatistics(Long statisticsId);

    /**
     * Retrieves statistics for a specific user.
     *
     * @param userId id of the user
     * @return An {@link Optional} containing the located Statistics, or {@link Optional#empty()} if not found
     */
    Optional<Statistics> getStatisticsByUserId(Long userId);

    /**
     * Retrieves all statistics.
     *
     * @return A list of all statistics
     */
    List<Statistics> findAllStatistics();

    /**
     * Retrieves all statistics where total calories burned is greater than the specified value.
     *
     * @param calories the minimum number of calories
     * @return A list of statistics where total calories burned is greater than the specified value
     */
    List<Statistics> findStatisticsWithCaloriesGreaterThan(int calories);
}
