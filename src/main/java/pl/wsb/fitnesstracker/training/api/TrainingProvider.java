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

    List<Training> findTrainingsByUserId(Long userId);

    List<Training> findTrainingsByActivityType(ActivityType activityType);

    List<Training> findTrainingsWithEndDateAfter(Date date);

    List<Training> findAllTrainings();
}

/*
- [ ] wyszukiwanie wszystkich treningów findAllTrainings
- [ ] wyszukiwanie treningów dla określonego Użytkownika: po ID ? findTrainingsByUserId
- [ ] wyszukiwanie wszystkich treningów zakończonych (po konkretnej zdefiniowanej dacie) findTrainingsWithEndDateAfter
- [ ] wyszukiwanie wszystkich treningów dla konkretnej aktywności (np. wszystkie treningi biegowe) findTrainingsByActivityType
- [ ] utworzenie nowego treningu ??
- [ ] aktualizacja treningu (dowolnie wybrane pole np. dystans) ??
 */
