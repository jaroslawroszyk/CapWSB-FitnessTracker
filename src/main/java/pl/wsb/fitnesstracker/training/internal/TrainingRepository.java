package pl.wsb.fitnesstracker.training.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.wsb.fitnesstracker.training.api.Training;
import pl.wsb.fitnesstracker.user.api.User;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {
    /**
     * Finds all trainings for a specific user.
     *
     * @param userId The ID of the user
     * @return A list of trainings for the specified user
     */
    List<Training> findByUserId(Long userId);

    /**
     * Finds all trainings with a specific activity type.
     *
     * @param activityType The activity type to search for
     * @return A list of trainings with the specified activity type
     */
    List<Training> findByActivityType(ActivityType activityType);

    /**
     * Finds all trainings that ended after a specific date.
     *
     * @param date The date to compare against
     * @return A list of trainings that ended after the specified date
     */
    default List<Training> findByEndDateAfter(Date date) {
        return findAll().stream()
                .filter(training -> training.getEndTime().after(date))
                .toList();
    }

    /**
     * Finds all trainings for a specific user that started between two dates.
     *
     * @param user The user
     * @param start The start date (inclusive)
     * @param end The end date (inclusive)
     * @return A list of trainings that match the criteria
     */
    List<Training> findByUserAndStartTimeBetween(User user, LocalDateTime start, LocalDateTime end);

    /**
     * Finds all trainings that occurred within a specific date range.
     * This is useful for generating reports or analyzing training patterns over time.
     *
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return A list of trainings that occurred within the specified date range
     */
    default List<Training> findByDateRange(Date startDate, Date endDate) {
        return findAll().stream()
                .filter(training -> !training.getStartTime().before(startDate) && !training.getEndTime().after(endDate))
                .toList();
    }
}
