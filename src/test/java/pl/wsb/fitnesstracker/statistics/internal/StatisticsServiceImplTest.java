package pl.wsb.fitnesstracker.statistics.internal;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.wsb.fitnesstracker.statistics.api.Statistics;
import pl.wsb.fitnesstracker.statistics.api.StatisticsNotFoundException;
import pl.wsb.fitnesstracker.user.api.User;
import pl.wsb.fitnesstracker.user.api.UserNotFoundException;
import pl.wsb.fitnesstracker.user.api.UserProvider;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StatisticsServiceImplTest {

    private StatisticsRepository statisticsRepository;
    private UserProvider userProvider;
    private StatisticsServiceImpl statisticsService;

    @BeforeEach
    void setUp() {
        statisticsRepository = mock(StatisticsRepository.class);
        userProvider = mock(UserProvider.class);
        statisticsService = new StatisticsServiceImpl(statisticsRepository, userProvider);
    }

    @Test
    void getStatistics_shouldReturnStatistics() {
        // Given
        Statistics statistics = createStatistics(1L);
        when(statisticsRepository.findById(1L)).thenReturn(Optional.of(statistics));

        // When
        Optional<Statistics> result = statisticsService.getStatistics(1L);

        // Then
        assertThat(result).isPresent().contains(statistics);
    }

    @Test
    void getStatisticsByUserId_shouldReturnStatistics() {
        // Given
        Statistics statistics = createStatistics(1L);
        when(statisticsRepository.findByUserId(1L)).thenReturn(Optional.of(statistics));

        // When
        Optional<Statistics> result = statisticsService.getStatisticsByUserId(1L);

        // Then
        assertThat(result).isPresent().contains(statistics);
    }

    @Test
    void findAllStatistics_shouldReturnAllStatistics() {
        // Given
        List<Statistics> statisticsList = List.of(
                createStatistics(1L),
                createStatistics(2L)
        );
        when(statisticsRepository.findAll()).thenReturn(statisticsList);

        // When
        List<Statistics> result = statisticsService.findAllStatistics();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(statisticsList);
    }

    @Test
    void findStatisticsWithCaloriesGreaterThan_shouldReturnFilteredStatistics() {
        // Given
        List<Statistics> filteredStatistics = List.of(createStatistics(1L));
        when(statisticsRepository.findByTotalCaloriesBurnedGreaterThan(5000)).thenReturn(filteredStatistics);

        // When
        List<Statistics> result = statisticsService.findStatisticsWithCaloriesGreaterThan(5000);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactlyElementsOf(filteredStatistics);
    }

    @Test
    void createStatistics_shouldThrowWhenIdIsSet() {
        // Given
        Statistics statistics = createStatistics(1L);

        // When/Then
        assertThatThrownBy(() -> statisticsService.createStatistics(statistics, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already DB ID");
    }

    @Test
    void createStatistics_shouldSaveWhenUserExists() {
        // Given
        Statistics statistics = new Statistics(null, 10, 100.0, 8000);
        User user = createUser(1L);
        when(userProvider.getUser(1L)).thenReturn(Optional.of(user));
        when(statisticsRepository.save(any(Statistics.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Statistics result = statisticsService.createStatistics(statistics, 1L);

        // Then
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getTotalTrainings()).isEqualTo(10);
        assertThat(result.getTotalDistance()).isEqualTo(100.0);
        assertThat(result.getTotalCaloriesBurned()).isEqualTo(8000);
    }

    @Test
    void createStatistics_shouldThrowWhenUserNotFound() {
        // Given
        Statistics statistics = new Statistics(null, 10, 100.0, 8000);
        when(userProvider.getUser(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> statisticsService.createStatistics(statistics, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateStatistics_shouldUpdateStatisticsSuccessfully() {
        // Given
        Statistics existing = createStatistics(1L);
        Statistics update = new Statistics(null, 15, 150.0, 12000);
        User user = createUser(2L);

        when(statisticsRepository.getReferenceById(1L)).thenReturn(existing);
        when(userProvider.getUser(2L)).thenReturn(Optional.of(user));
        when(statisticsRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        Statistics result = statisticsService.updateStatistics(update, 1L, 2L);

        // Then
        assertThat(result.getTotalTrainings()).isEqualTo(15);
        assertThat(result.getTotalDistance()).isEqualTo(150.0);
        assertThat(result.getTotalCaloriesBurned()).isEqualTo(12000);
        assertThat(result.getUser()).isEqualTo(user);
    }

    @Test
    void updateStatistics_shouldThrowWhenStatisticsNotFound() {
        // Given
        when(statisticsRepository.getReferenceById(1L)).thenThrow(EntityNotFoundException.class);

        // When/Then
        assertThatThrownBy(() -> statisticsService.updateStatistics(mock(Statistics.class), 1L, 2L))
                .isInstanceOf(StatisticsNotFoundException.class);
    }

    @Test
    void updateStatistics_shouldThrowWhenUserNotFound() {
        // Given
        Statistics statistics = createStatistics(1L);
        when(statisticsRepository.getReferenceById(1L)).thenReturn(statistics);
        when(userProvider.getUser(2L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> statisticsService.updateStatistics(statistics, 1L, 2L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteStatistics_shouldDeleteWhenExists() {
        // Given
        when(statisticsRepository.existsById(1L)).thenReturn(true);

        // When
        statisticsService.deleteStatistics(1L);

        // Then
        verify(statisticsRepository).deleteById(1L);
    }

    @Test
    void deleteStatistics_shouldThrowWhenNotExists() {
        // Given
        when(statisticsRepository.existsById(1L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> statisticsService.deleteStatistics(1L))
                .isInstanceOf(StatisticsNotFoundException.class);
    }

    private Statistics createStatistics(Long id) {
        Statistics statistics = new Statistics(createUser(id), 10, 100.0, 8000);
        setId(statistics, id);
        return statistics;
    }

    private User createUser(Long id) {
        User user = new User("John", "Doe", LocalDate.now().minusYears(30), "john.doe@example.com");
        setId(user, id);
        return user;
    }

    private void setId(Statistics statistics, Long id) {
        try {
            java.lang.reflect.Field field = Statistics.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(statistics, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID on Statistics", e);
        }
    }

    private void setId(User user, Long id) {
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID on User", e);
        }
    }
}