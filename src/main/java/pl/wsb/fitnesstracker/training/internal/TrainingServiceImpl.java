package pl.wsb.fitnesstracker.training.internal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.wsb.fitnesstracker.training.api.Training;
import pl.wsb.fitnesstracker.training.api.TrainingNotFoundException;
import pl.wsb.fitnesstracker.training.api.TrainingProvider;
import pl.wsb.fitnesstracker.training.api.TrainingService;
import pl.wsb.fitnesstracker.user.api.User;
import pl.wsb.fitnesstracker.user.api.UserProvider;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the TrainingService and TrainingProvider interfaces.
 * This class provides methods for managing and retrieving training data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingServiceImpl implements TrainingProvider, TrainingService {
    private final TrainingRepository trainingRepository;
    private final UserProvider userProvider;

    @Override
    public Optional<Training> getTraining(final Long trainingId) {
        log.debug("Getting training with ID: {}", trainingId);
        return trainingRepository.findById(trainingId);
    }

    @Override
    public List<Training> findTrainingsByUserId(Long userId) {
        log.debug("Finding trainings for user with ID: {}", userId);
        return trainingRepository.findByUserId(userId);
    }

    @Override
    public List<Training> findTrainingsByActivityType(ActivityType activityType) {
        log.debug("Finding trainings with activity type: {}", activityType);
        return trainingRepository.findByActivityType(activityType);
    }

    @Override
    public List<Training> findTrainingsWithEndDateAfter(Date date) {
        log.debug("Finding trainings ending after date: {}", date);
        return trainingRepository.findByEndDateAfter(date);
    }

    @Override
    public List<Training> findAllTrainings() {
        log.debug("Finding all trainings");
        return trainingRepository.findAll();
    }

    @Override
    public List<Training> findTrainingsByDateRange(Date startDate, Date endDate) {
        log.debug("Finding trainings between dates: {} and {}", startDate, endDate);
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must not be null");
        }
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        return trainingRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public Training createTraining(Training training, Long userId) {
        log.info("Creating training for user with ID: {}", userId);

        if (training == null) {
            log.error("Training object is null");
            throw new IllegalArgumentException("Training object cannot be null");
        }

        if (userId == null) {
            log.error("User ID is null");
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (training.getId() != null) {
            log.error("Training already has an ID: {}", training.getId());
            throw new IllegalArgumentException("Training has already DB ID, update is not permitted!");
        }

        User user = userProvider.getUser(userId).orElseThrow(() -> {
            log.error("User with ID {} not found", userId);
            return new IllegalArgumentException("User with id " + userId + " not found!");
        });

        try {
            Training newTraining = new Training(
                    user,
                    training.getStartTime(),
                    training.getEndTime(),
                    training.getActivityType(),
                    training.getDistance(),
                    training.getAverageSpeed()
            );

            log.info("Saving new training for user: {}", user.getId());
            return trainingRepository.save(newTraining);
        } catch (Exception e) {
            log.error("Error creating training: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating training", e);
        }
    }


    @Override
    public Training updateTraining(Training training, Long trainingId, Long userId) {
        log.info("Updating training with ID: {} for user with ID: {}", trainingId, userId);

        if (training == null) {
            log.error("Training object is null");
            throw new IllegalArgumentException("Training object cannot be null");
        }

        if (trainingId == null) {
            log.error("Training ID is null");
            throw new IllegalArgumentException("Training ID cannot be null");
        }

        if (userId == null) {
            log.error("User ID is null");
            throw new IllegalArgumentException("User ID cannot be null");
        }

        Training trainingToUpdate;

        try {
            trainingToUpdate = trainingRepository.getReferenceById(trainingId);
            log.debug("Found training to update: {}", trainingToUpdate);
        } catch (EntityNotFoundException e) {
            log.error("Training with ID {} not found", trainingId);
            throw new TrainingNotFoundException(trainingId);
        }

        User user;
        try {
            user = userProvider.getUser(userId).orElseThrow(() -> {
                log.error("User with ID {} not found", userId);
                return new IllegalArgumentException("User with ID " + userId + " not found");
            });
            log.debug("Found user: {}", user);
        } catch (Exception e) {
            log.error("Error finding user with ID {}: {}", userId, e.getMessage(), e);
            throw e;
        }

        try {
            trainingToUpdate.setUser(user);
            trainingToUpdate.setDistance(training.getDistance());
            trainingToUpdate.setActivityType(training.getActivityType());
            trainingToUpdate.setEndTime(training.getEndTime());
            trainingToUpdate.setStartTime(training.getStartTime());
            trainingToUpdate.setAverageSpeed(training.getAverageSpeed());

            log.info("Saving updated training for user: {}", user.getId());
            return trainingRepository.save(trainingToUpdate);
        } catch (Exception e) {
            log.error("Error updating training: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating training", e);
        }
    }
}
