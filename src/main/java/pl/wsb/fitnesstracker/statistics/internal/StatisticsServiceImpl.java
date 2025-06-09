package pl.wsb.fitnesstracker.statistics.internal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.wsb.fitnesstracker.statistics.api.Statistics;
import pl.wsb.fitnesstracker.statistics.api.StatisticsNotFoundException;
import pl.wsb.fitnesstracker.statistics.api.StatisticsService;
import pl.wsb.fitnesstracker.user.api.User;
import pl.wsb.fitnesstracker.user.api.UserProvider;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final UserProvider userProvider;

    @Override
    public Optional<Statistics> getStatistics(Long statisticsId) {
        return statisticsRepository.findById(statisticsId);
    }

    @Override
    public Optional<Statistics> getStatisticsByUserId(Long userId) {
        return statisticsRepository.findByUserId(userId);
    }

    @Override
    public List<Statistics> findAllStatistics() {
        return statisticsRepository.findAll();
    }

    @Override
    public List<Statistics> findStatisticsWithCaloriesGreaterThan(int calories) {
        return statisticsRepository.findByTotalCaloriesBurnedGreaterThan(calories);
    }

    @Override
    public Statistics createStatistics(Statistics statistics, Long userId) {
        if (statistics.getId() != null) {
            throw new IllegalArgumentException("Statistics has already DB ID, update is not permitted!");
        }

        User user = userProvider.getUser(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id " + userId + " not found!")
        );

        Statistics newStatistics = new Statistics(
                user,
                statistics.getTotalTrainings(),
                statistics.getTotalDistance(),
                statistics.getTotalCaloriesBurned()
        );

        return statisticsRepository.save(newStatistics);
    }

    @Override
    public Statistics updateStatistics(Statistics statistics, Long statisticsId, Long userId) {
        Statistics statisticsToUpdate;

        try {
            statisticsToUpdate = statisticsRepository.getReferenceById(statisticsId);
        } catch (EntityNotFoundException e) {
            throw new StatisticsNotFoundException(statisticsId);
        }

        User user = userProvider.getUser(userId).orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

        statisticsToUpdate.setUser(user);
        statisticsToUpdate.setTotalTrainings(statistics.getTotalTrainings());
        statisticsToUpdate.setTotalDistance(statistics.getTotalDistance());
        statisticsToUpdate.setTotalCaloriesBurned(statistics.getTotalCaloriesBurned());

        return statisticsRepository.save(statisticsToUpdate);
    }

    @Override
    public void deleteStatistics(Long statisticsId) {
        if (!statisticsRepository.existsById(statisticsId)) {
            throw new StatisticsNotFoundException(statisticsId);
        }
        statisticsRepository.deleteById(statisticsId);
    }
}
