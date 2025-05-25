package pl.wsb.fitnesstracker.training.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.wsb.fitnesstracker.training.api.Training;
import pl.wsb.fitnesstracker.user.api.User;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

interface TrainingRepository extends JpaRepository<Training, Long> {
    List<Training> findByUserId(Long userId);
    List<Training> findByActivityType(ActivityType activityType);

    default List<Training> findByEndDateAfter(Date date) {
        return findAll().stream()
                .filter(training -> training.getEndTime().after(date))
                .toList();
    }

    List<Training> findByUserAndStartTimeBetween(User user, LocalDateTime start, LocalDateTime end);
}
