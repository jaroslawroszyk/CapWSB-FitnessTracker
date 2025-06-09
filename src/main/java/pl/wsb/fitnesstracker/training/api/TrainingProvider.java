package pl.wsb.fitnesstracker.training.api;

import pl.wsb.fitnesstracker.training.internal.ActivityType;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TrainingProvider {

    /**
     * Retrieves a training based on their ID.
     * If the user with given ID is not found, then {@link Optional#empty()} will be returned.
     *
     * @param trainingId id of the training to be searched
     * @return An {@link Optional} containing the located Training, or {@link Optional#empty()} if not found
     */
    Optional<Training> getTraining(Long trainingId);

    /**
     * Finds all trainings for a specific user.
     *
     * @param userId The ID of the user
     * @return A list of trainings for the specified user
     */
    List<Training> findTrainingsByUserId(Long userId);

    /**
     * Finds all trainings with a specific activity type.
     *
     * @param activityType The activity type to search for
     * @return A list of trainings with the specified activity type
     */
    List<Training> findTrainingsByActivityType(ActivityType activityType);

    /**
     * Finds all trainings that ended after a specific date.
     *
     * @param date The date to compare against
     * @return A list of trainings that ended after the specified date
     */
    List<Training> findTrainingsWithEndDateAfter(Date date);

    /**
     * Finds all trainings in the system.
     *
     * @return A list of all trainings
     */
    List<Training> findAllTrainings();

    /**
     * Finds all trainings that occurred within a specific date range.
     * This is useful for generating reports or analyzing training patterns over time.
     *
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return A list of trainings that occurred within the specified date range
     */
    List<Training> findTrainingsByDateRange(Date startDate, Date endDate);
}

/*
- [ ] wyszukiwanie wszystkich treningów findAllTrainings
- [ ] wyszukiwanie treningów dla określonego Użytkownika: po ID ? findTrainingsByUserId
- [ ] wyszukiwanie wszystkich treningów zakończonych (po konkretnej zdefiniowanej dacie) findTrainingsWithEndDateAfter
- [ ] wyszukiwanie wszystkich treningów dla konkretnej aktywności (np. wszystkie treningi biegowe) findTrainingsByActivityType
- [ ] utworzenie nowego treningu ??
- [ ] aktualizacja treningu (dowolnie wybrane pole np. dystans) ??
 */
