package pl.wsb.fitnesstracker.statistics.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.wsb.fitnesstracker.statistics.api.Statistics;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Statistics} entities.
 */
public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    
    /**
     * Finds statistics for a specific user.
     *
     * @param userId The ID of the user
     * @return An optional containing the statistics for the user, or empty if not found
     */
    Optional<Statistics> findByUserId(Long userId);
    
    /**
     * Finds all statistics where total calories burned is greater than the specified value.
     *
     * @param calories The minimum number of calories
     * @return A list of statistics where total calories burned is greater than the specified value
     */
    default List<Statistics> findByTotalCaloriesBurnedGreaterThan(int calories) {
        return findAll().stream()
                .filter(statistics -> statistics.getTotalCaloriesBurned() > calories)
                .toList();
    }
}