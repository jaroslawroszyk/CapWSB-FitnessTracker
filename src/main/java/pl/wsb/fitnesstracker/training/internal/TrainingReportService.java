package pl.wsb.fitnesstracker.training.internal;

import org.springframework.stereotype.Service;
import pl.wsb.fitnesstracker.training.api.MonthlyTrainingReport;
import pl.wsb.fitnesstracker.training.api.Training;
import pl.wsb.fitnesstracker.user.internal.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class TrainingReportService {
    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;

    public TrainingReportService(TrainingRepository trainingRepository, UserRepository userRepository) {
        this.trainingRepository = trainingRepository;
        this.userRepository = userRepository;
    }

    public List<MonthlyTrainingReport> generateReports() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusMonths(1).withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(1);

        return userRepository.findAll().stream()
                .map(user -> {
                    List<Training> trainings = trainingRepository.findByUserAndStartTimeBetween(
                            user, start.atStartOfDay(), end.atStartOfDay()
                    );

                    if (trainings.isEmpty()) return null;

                    double totalDistance = trainings.stream().mapToDouble(Training::getDistance).sum();
                    double avgSpeed = trainings.stream().mapToDouble(Training::getAverageSpeed).average().orElse(0);

                    return new MonthlyTrainingReport(
                            user.getEmail(), trainings.size(), totalDistance, avgSpeed
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
