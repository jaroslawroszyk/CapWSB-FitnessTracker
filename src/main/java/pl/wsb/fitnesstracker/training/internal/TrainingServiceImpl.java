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
import pl.wsb.fitnesstracker.user.api.UserNotFoundException;
import pl.wsb.fitnesstracker.user.api.UserProvider;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
class TrainingServiceImpl implements TrainingProvider, TrainingService {
    private final TrainingRepository trainingRepository;

    private final UserProvider userProvider;


    @Override
    public Optional<Training> getTraining(final Long trainingId) {
        return trainingRepository.findById(trainingId);
    }

    @Override
    public List<Training> findTrainingsByUserId(Long userId) {
        return trainingRepository.findByUserId(userId);
    }

    @Override
    public List<Training> findTrainingsByActivityType(ActivityType activityType) {
        return trainingRepository.findByActivityType(activityType);
    }

    @Override
    public List<Training> findTrainingsWithEndDateAfter(Date date) {
        return trainingRepository.findByEndDateAfter(date);
    }

    @Override
    public List<Training> findAllTrainings() {
        return trainingRepository.findAll();
    }

    @Override
    public Training createTraining(Training training, Long userId) {
        if (training.getId() != null) {
            throw new IllegalArgumentException("Training has already DB ID, update is not permitted!");
        }
        User user = userProvider.getUser(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id " + userId + " not found!")
        );
        Training newTraining = new Training(
                user,
                training.getStartTime(),
                training.getEndTime(),
                training.getActivityType(),
                training.getDistance(),
                training.getAverageSpeed()
        );
        return trainingRepository.save(newTraining);
    }


    @Override
    public Training updateTraining(Training training, Long trainingId, Long userId) {
        Training trainingToUpdate;

        try {
            trainingToUpdate = trainingRepository.getReferenceById(trainingId);
        } catch (EntityNotFoundException e) {
            throw new TrainingNotFoundException(trainingId);
        }
        User user = userProvider.getUser(userId).orElseThrow(() -> new UserNotFoundException(userId));
        trainingToUpdate.setUser(user);
        trainingToUpdate.setDistance(training.getDistance());
        trainingToUpdate.setActivityType(training.getActivityType());
        trainingToUpdate.setEndTime(training.getEndTime());
        trainingToUpdate.setStartTime(training.getStartTime());
        trainingToUpdate.setAverageSpeed(training.getAverageSpeed());
        return trainingRepository.save(trainingToUpdate);
    }
}
